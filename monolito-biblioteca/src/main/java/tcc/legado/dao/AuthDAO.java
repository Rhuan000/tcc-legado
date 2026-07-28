package tcc.legado.dao;

import tcc.legado.model.UsuarioAuth;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private static final Map<String, UsuarioAuth> USUARIOS = new HashMap<>();

    static {
        carregarUsuarios();
    }

    private static void carregarUsuarios() {
        try (InputStream is = AuthDAO.class.getClassLoader().getResourceAsStream("usuarios.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String linha;
            boolean cabecalho = true;
            while ((linha = br.readLine()) != null) {
                if (cabecalho) {
                    cabecalho = false;
                    continue;
                }
                String[] partes = linha.split(";");
                if (partes.length == 3) {
                    UsuarioAuth u = new UsuarioAuth(partes[0], partes[1], partes[2]);
                    USUARIOS.put(partes[0], u);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UsuarioAuth autenticar(String matricula, String senha) {
        UsuarioAuth user = USUARIOS.get(matricula);
        if (user != null && user.getSenha().equals(senha)) {
            return user;
        }
        return null;
    }
}
