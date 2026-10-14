package tcc.legado;

import org.junit.Test;
import tcc.legado.filter.Utf8Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import static org.junit.Assert.*;

public class Utf8FilterTest {
    @Test public void defineUtf8AntesDaPrimeiraLeituraDeParametros() throws Exception {
        String original = "ação, validação, João e Promoção";
        String body = URLEncoder.encode(original, "UTF-8");
        String[] requestEncoding = {"ISO-8859-1"};
        String[] responseEncoding = {null};
        boolean[] parametrosLidos = {false};
        ServletRequest request = (ServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ServletRequest.class}, (p, method, args) -> {
                    if ("setCharacterEncoding".equals(method.getName())) {
                        assertFalse("O corpo já foi decodificado", parametrosLidos[0]);
                        requestEncoding[0] = (String) args[0];
                    }
                    if ("getParameter".equals(method.getName())) {
                        parametrosLidos[0] = true;
                        return URLDecoder.decode(body, requestEncoding[0]);
                    }
                    return null;
                });
        ServletResponse response = (ServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ServletResponse.class}, (p, method, args) -> {
                    if ("setCharacterEncoding".equals(method.getName())) responseEncoding[0] = (String) args[0];
                    return null;
                });
        new Utf8Filter().doFilter(request, response, (req, res) -> {
            assertEquals(original, req.getParameter("descricao"));
            assertEquals("UTF-8", responseEncoding[0]);
        });
        assertTrue(parametrosLidos[0]);
        assertNotEquals(original, URLDecoder.decode(body, "ISO-8859-1"));
    }

    @Test public void filtroExecutaAntesDoAuthFilterQueLeParametros() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("src/main/webapp/WEB-INF/web.xml"));
        NodeList mappings = document.getElementsByTagName("filter-mapping");
        assertTrue(mappings.getLength() >= 2);
        assertTrue(mappings.item(0).getTextContent().contains("Utf8Filter"));
        assertTrue(mappings.item(0).getTextContent().contains("/*"));
        assertTrue(mappings.item(1).getTextContent().contains("AuthFilter"));
    }
}
