package tcc.legado.util;

import tcc.legado.model.Perfil;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "perfis")
public class PerfisWrapper {
    private List<Perfil> perfis;

    @XmlElement(name = "perfil")
    public List<Perfil> getPerfis() { return perfis; }
    public void setPerfis(List<Perfil> perfis) { this.perfis = perfis; }

    private static Map<String, Perfil> cache = null;

    public static Map<String, Perfil> carregarPerfis() {
        if (cache != null) return cache;
        try {
            InputStream is = PerfisWrapper.class.getClassLoader().getResourceAsStream("perfis.xml");
            JAXBContext context = JAXBContext.newInstance(PerfisWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            PerfisWrapper wrapper = (PerfisWrapper) unmarshaller.unmarshal(is);
            Map<String, Perfil> mapa = new HashMap<>();
            for (Perfil p : wrapper.getPerfis()) {
                mapa.put(p.getNome(), p);
            }
            cache = mapa;
            return mapa;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}