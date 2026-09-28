package tcc.legado.acl;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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

    private JSONObject executarPost(String caminho, JSONObject requisicao,
                                    String operacao) {
        HttpPost post = new HttpPost(urlBase + caminho);
        post.setConfig(RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MILLIS)
                .setConnectionRequestTimeout(TIMEOUT_MILLIS)
                .setSocketTimeout(TIMEOUT_MILLIS)
                .build());
        post.setEntity(new StringEntity(requisicao.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient cliente = HttpClients.createDefault()) {
            String resposta = cliente.execute(post, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                String corpo = httpResponse.getEntity() == null
                        ? ""
                        : EntityUtils.toString(httpResponse.getEntity());
                if (status != 200) {
                    throw new IOException("Serviço de empréstimos retornou HTTP " + status);
                }
                return corpo;
            });
            return new JSONObject(resposta);
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
