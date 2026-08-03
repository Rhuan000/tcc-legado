<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Biblioteca Legada - Struts 1</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container" style="margin-top:30px;">
        <div class="header">
            <h1>Biblioteca Legada - Struts 1</h1>
        </div>
        <div class="content">
            <div class="caixa" style="padding:20px; background:#FFF0F0; border:2px solid #8B0000;">
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
        </div>
    </div>
</body>
</html>