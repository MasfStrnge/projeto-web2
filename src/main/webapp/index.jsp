<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <title>SGHA - Acesso do Discente</title>
    <style>
      body {
        font-family: sans-serif;
        max-width: 520px;
        margin: 40px auto;
        padding: 0 16px;
      }
      .campo {
        margin-bottom: 12px;
      }
      label {
        display: block;
        font-weight: bold;
        margin-bottom: 4px;
      }
      input {
        width: 100%;
        padding: 8px;
        box-sizing: border-box;
      }
      button {
        padding: 10px 16px;
        cursor: pointer;
      }
      .alerta {
        background: #fee;
        border: 1px solid #c33;
        padding: 10px;
        margin-bottom: 16px;
        color: #900;
      }
      .aviso {
        background: #fff8e1;
        border: 1px solid #f0c36d;
        padding: 10px;
        margin-bottom: 16px;
      }
      footer {
        margin-top: 32px;
        padding-top: 12px;
        border-top: 1px solid #ddd;
        font-size: 0.9em;
        color: #555;
      }
    </style>
  </head>
  <body>
    <h1>Sistema de Gestão de Horas Acadêmicas (SGHA)</h1>
    <p>
      Informe seu nome completo e matrícula para acessar ou iniciar seu
      cadastro.
    </p>

    <c:if test="${param.expirada == '1'}">
      <div class="aviso">
        Sua sessão expirou por inatividade (2 minutos). Identifique-se
        novamente.
      </div>
    </c:if>

    <c:if test="${not empty erro}">
      <div class="alerta"><c:out value="${erro}" /></div>
    </c:if>

    <form
      action="${pageContext.request.contextPath}/discente/acesso"
      method="post"
    >
      <div class="campo">
        <label for="nomeCompleto">Nome Completo</label>
        <input type="text" id="nomeCompleto" name="nomeCompleto" required />
      </div>
      <div class="campo">
        <label for="matricula">Matrícula</label>
        <input type="text" id="matricula" name="matricula" required />
      </div>
      <button type="submit">Entrar no Portal do Discente</button>
    </form>

    <footer>
      <p>
        <strong>Testes técnicos:</strong>
        <a href="${pageContext.request.contextPath}/ola">Testar OlaServlet</a>
      </p>
      <c:if test="${not empty caminhoComprovante}">
        <p>Comprovante salvo em: <c:out value="${caminhoComprovante}" /></p>
      </c:if>
    </footer>
  </body>
</html>
