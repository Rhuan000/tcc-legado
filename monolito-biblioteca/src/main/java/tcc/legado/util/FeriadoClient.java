package tcc.legado.util;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FeriadoClient {

    private static final Logger LOG = Logger.getLogger(FeriadoClient.class.getName());
    private static final String API_URL = "https://brasilapi.com.br/api/feriados/v1/{ano}";

    public static List<LocalDate> buscarFeriados(int ano) {
        List<LocalDate> feriados = new ArrayList<>();
        String url = API_URL.replace("{ano}", String.valueOf(ano));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // Timeout curto para simular fragilidade
            request.setConfig(org.apache.http.client.config.RequestConfig.custom()
                    .setConnectTimeout(3000)
                    .setSocketTimeout(3000)
                    .build());

            String resposta = client.execute(request, httpResponse -> {
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("API retornou erro: " + httpResponse.getStatusLine().getStatusCode());
                }
                return EntityUtils.toString(httpResponse.getEntity());
            });

            // Exemplo de resposta: [{"date":"2026-01-01","name":"Confraternização Universal"}, ...]
            JSONArray jsonArray = new JSONArray(resposta);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String dataStr = obj.getString("date");
                LocalDate data = LocalDate.parse(dataStr, formatter);
                feriados.add(data);
            }

            LOG.info("Feriados obtidos para " + ano + ": " + feriados.size() + " registros");
            return feriados;

        } catch (IOException e) {
            LOG.severe("Falha ao obter feriados: " + e.getMessage());
            // VENENO: se a API cair, o sistema lança exceção e trava a transação
            throw new RuntimeException("Não foi possível obter a lista de feriados", e);
        }
    }
}
