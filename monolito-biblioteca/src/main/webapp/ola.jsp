<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<!DOCTYPE html>
<html>
<head>
    <title>Biblioteca Legada - Struts 1</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; }
        h1 { color: darkred; }
        .caixa { border: 2px solid #8B0000; padding: 20px; background: #FFF0F0; }
    </style>
</head>
<body>
    <div class="caixa">
        <!-- Pegando os atributos que a Action colocou na requisição -->
        <h1><bean:write name="mensagem" scope="request"/></h1>
        <p><bean:write name="subtitulo" scope="request"/></p>
        <hr/>
        <p>✔ Struts 1.x configurado com sucesso!</p>
        <p>✔ XML pra todo lado, como manda o figurino.</p>
        <p>✔ Agora sim, um legado de respeito.</p>
        
        <!-- Link pra outro .do (que não existe, só pra mostrar) -->
        <html:link action="/outra">Clique aqui pra ver o caos</html:link>
    </div>
</body>
</html>