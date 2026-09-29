package tcc.legado.acl;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import tcc.legado.model.Emprestimo;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmprestimoAcl {

    private static final int TIMEOUT_MILLIS = 3000;
    private static final String URL_PADRAO = "http://localhost:8081";

    private final String urlBase;

    public EmprestimoAcl() {
        this.urlBase = resolverUrlBase();
    }

    /**
     * Traduz os tipos e o vocabulário do monólito para o contrato HTTP do
     * serviço de empréstimos, sem compartilhar DTOs entre as aplicações.
     */
    public Date calcularDataPrevista(Date dataEmprestimo, String tipoUsuario) {
        JSONObject requisicao = new JSONObject()
                .put("dataEmprestimo", paraLocalDate(dataEmprestimo).toString())
                .put("tipoUsuario", tipoUsuario);

        JSONObject resposta = executarPost(
                "/prazos/calcular", requisicao, "calcular o prazo");

        try {
            LocalDate dataPrevista = LocalDate.parse(resposta.getString("dataPrevista"));
            return Date.from(dataPrevista.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (RuntimeException e) {
            throw respostaInvalida("calcular o prazo", e);
        }
    }

    public double calcularMulta(Date dataPrevista, String tipoUsuario,
                                Date dataReferencia) {
        JSONObject requisicao = new JSONObject()
                .put("dataPrevista", paraLocalDate(dataPrevista).toString())
                .put("tipoUsuario", tipoUsuario)
                .put("dataReferencia", paraLocalDate(dataReferencia).toString());

        JSONObject resposta = executarPost(
                "/multas/calcular", requisicao, "calcular a multa");

        try {
            Object campo = resposta.get("valor");
            if (!(campo instanceof Number)) {
                throw new IllegalArgumentException("valor deve ser um número JSON");
            }
            double valor = ((Number) campo).doubleValue();
            if (!Double.isFinite(valor) || valor < 0) {
                throw new IllegalArgumentException("valor deve ser finito e não negativo");
            }
            return valor;
        } catch (RuntimeException e) {
            throw respostaInvalida("calcular a multa", e);
        }
    }

    public List<Emprestimo> listarTodos() {
        return consultarLista("/emprestimos", "listar empréstimos");
    }

    public List<Emprestimo> buscarAtrasados() {
        return consultarLista("/emprestimos/atrasados", "consultar empréstimos atrasados");
    }

    public List<Long> buscarIdsMaisEmprestadosNoMes(int limite) {
        if (limite < 1 || limite > 100) {
            throw new IllegalArgumentException("Limite deve estar entre 1 e 100");
        }
        String operacao = "consultar contagem mensal de empréstimos";
        String corpo = executar(new HttpGet(urlBase + "/emprestimos/mais-emprestados-mes?limite=" + limite),
                operacao, false);
        try {
            JSONArray resposta = new JSONArray(corpo);
            if (resposta.length() > limite) {
                throw new IllegalArgumentException("Quantidade de resultados excede o limite");
            }
            List<Long> ids = new ArrayList<>();
            long totalAnterior = Long.MAX_VALUE;
            long idAnterior = 0;
            for (int i = 0; i < resposta.length(); i++) {
                JSONObject item = resposta.getJSONObject(i);
                long id = lerId(item, "idLivro");
                long total = lerId(item, "totalEmprestimos");
                if (ids.contains(id) || total > totalAnterior
                        || (total == totalAnterior && id < idAnterior)) {
                    throw new IllegalArgumentException("Contagem mensal duplicada ou fora de ordem");
                }
                ids.add(id);
                totalAnterior = total;
                idAnterior = id;
            }
            return ids;
        } catch (RuntimeException e) {
            throw respostaInvalida(operacao, e);
        }
    }

    public Emprestimo buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do empréstimo deve ser positivo");
        }
        String corpo = executar(new HttpGet(urlBase + "/emprestimos/" + id),
                "consultar empréstimo", true);
        if (corpo == null) {
            return null;
        }
        try {
            Emprestimo emprestimo = converterEmprestimo(new JSONObject(corpo));
            if (!id.equals(emprestimo.getId())) {
                throw new IllegalArgumentException("ID da resposta difere do solicitado");
            }
            return emprestimo;
        } catch (RuntimeException e) {
            throw respostaInvalida("consultar empréstimo", e);
        }
    }

    private List<Emprestimo> consultarLista(String caminho, String operacao) {
        String corpo = executar(new HttpGet(urlBase + caminho), operacao, false);
        try {
            JSONArray resposta = new JSONArray(corpo);
            List<Emprestimo> emprestimos = new ArrayList<>();
            for (int i = 0; i < resposta.length(); i++) {
                emprestimos.add(converterEmprestimo(resposta.getJSONObject(i)));
            }
            return emprestimos;
        } catch (RuntimeException e) {
            throw respostaInvalida(operacao, e);
        }
    }

    private Emprestimo converterEmprestimo(JSONObject json) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(lerId(json, "id"));
        emprestimo.setIdLivro(lerId(json, "idLivro"));
        emprestimo.setIdUsuario(lerId(json, "idUsuario"));
        emprestimo.setDataEmprestimo(lerData(json, "dataEmprestimo"));
        emprestimo.setDataPrevistaDevolucao(lerData(json, "dataPrevistaDevolucao"));
        Object devolucao = json.get("dataDevolucaoReal");
        emprestimo.setDataDevolucaoReal(devolucao == JSONObject.NULL
                ? null : lerData(json, "dataDevolucaoReal"));
        Object multa = json.get("multa");
        if (!(multa instanceof Number) || !Double.isFinite(((Number) multa).doubleValue())) {
            throw new IllegalArgumentException("Multa deve ser um número finito");
        }
        emprestimo.setMulta(((Number) multa).doubleValue());
        return emprestimo;
    }

    private static long lerId(JSONObject json, String campo) {
        Object valor = json.get(campo);
        if (!(valor instanceof Number)) {
            throw new IllegalArgumentException(campo + " deve ser numérico");
        }
        long id = new BigDecimal(valor.toString()).longValueExact();
        if (id <= 0) {
            throw new IllegalArgumentException(campo + " deve ser positivo");
        }
        return id;
    }

    private static Date lerData(JSONObject json, String campo) {
        return java.sql.Date.valueOf(LocalDate.parse(json.getString(campo)));
    }

    private JSONObject executarPost(String caminho, JSONObject requisicao,
                                    String operacao) {
        HttpPost post = new HttpPost(urlBase + caminho);
        post.setEntity(new StringEntity(requisicao.toString(), ContentType.APPLICATION_JSON));
        String corpo = executar(post, operacao, false);
        try {
            return new JSONObject(corpo);
        } catch (RuntimeException e) {
            throw respostaInvalida(operacao, e);
        }
    }

    private String executar(HttpRequestBase requisicao, String operacao, boolean aceitarAusente) {
        requisicao.setConfig(RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLIS)
                .setConnectionRequestTimeout(TIMEOUT_MILLIS)
                .setSocketTimeout(TIMEOUT_MILLIS)
                .build());
        requisicao.setHeader("Accept", "application/json");

        try (CloseableHttpClient cliente = HttpClients.createDefault()) {
            return cliente.execute(requisicao, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (aceitarAusente && status == 404) {
                    return null;
                }
                String corpo = httpResponse.getEntity() == null
                        ? ""
                        : EntityUtils.toString(httpResponse.getEntity());
                if (status != 200) {
                    throw new IOException("Serviço de empréstimos retornou HTTP " + status);
                }
                return corpo;
            });
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível " + operacao + " no serviço de empréstimos", e);
        } catch (RuntimeException e) {
            throw respostaInvalida(operacao, e);
        }
    }

    private static LocalDate paraLocalDate(Date data) {
        // JDBC devolve java.sql.Date, cujo toInstant() não é suportado.
        if (data instanceof java.sql.Date) {
            return ((java.sql.Date) data).toLocalDate();
        }
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static IllegalStateException respostaInvalida(
            String operacao, RuntimeException causa) {
        return new IllegalStateException(
                "Resposta inválida do serviço de empréstimos ao " + operacao, causa);
    }

    private static String resolverUrlBase() {
        String url = System.getProperty("emprestimo.service.url");
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("EMPRESTIMO_SERVICE_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = URL_PADRAO;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
