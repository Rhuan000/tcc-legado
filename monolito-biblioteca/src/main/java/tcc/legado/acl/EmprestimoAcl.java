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
import java.time.format.DateTimeParseException;
import java.util.Date;

@ApplicationScoped
public class EmprestimoAcl {

    private static final int TIMEOUT_MILLIS = 3000;
    private static final String URL_PADRAO = "http://localhost:8081";

    private final String urlCalculoPrazo;

    public EmprestimoAcl() {
        this.urlCalculoPrazo = resolverUrlBase() + "/prazos/calcular";
    }

    /**
     * Traduz os tipos e o vocabulário do monólito para o contrato HTTP do
     * serviço de empréstimos, sem compartilhar DTOs entre as aplicações.
     */
    public Date calcularDataPrevista(Date dataEmprestimo, String tipoUsuario) {
        LocalDate dataLocal = dataEmprestimo.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        JSONObject requisicao = new JSONObject()
                .put("dataEmprestimo", dataLocal.toString())
                .put("tipoUsuario", tipoUsuario);

        HttpPost post = new HttpPost(urlCalculoPrazo);
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

            String dataPrevista = new JSONObject(resposta).getString("dataPrevista");
            LocalDate dataPrevistaLocal = LocalDate.parse(dataPrevista);
            return Date.from(dataPrevistaLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (IOException | DateTimeParseException e) {
            throw new IllegalStateException(
                    "Não foi possível calcular o prazo no serviço de empréstimos", e);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Resposta inválida do serviço de empréstimos", e);
        }
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
