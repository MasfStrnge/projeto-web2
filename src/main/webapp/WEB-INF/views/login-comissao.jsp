<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <title>SGHA - Acesso da Comissão</title>
    <style>
      body {
        font-family: sans-serif;
        max-width: 560px;
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
      .demo {
        background: #f7f7f7;
        border: 1px solid #ddd;
        padding: 12px;
        margin-top: 24px;
        font-size: 0.9em;
      }
    </style>
  </head>
  <body>
    <h1>SGHA - Acesso da Comissão / Coordenação</h1>
    <p>
      <a href="${pageContext.request.contextPath}/index.jsp"
        >&larr; Voltar ao Portal do Discente</a
      >
    </p>

    <c:if test="${not empty erro}">
      <div class="alerta"><c:out value="${erro}" /></div>
    </c:if>

    <form
      action="${pageContext.request.contextPath}/comissao/login"
      method="post"
    >
      <div class="campo">
        <label for="username">Usuário</label>
        <input type="text" id="username" name="username" required />
      </div>
      <div class="campo">
        <label for="senha">Senha</label>
        <input type="password" id="senha" name="senha" required />
      </div>
      <button type="submit">Entrar</button>
    </form>

    <div class="demo">
      <strong
        >Contas de demonstração cadastradas no banco (senha: <code>123</code>):</strong
      >
      <ul>
        <li><code>extensao</code> &mdash; Comissão de Extensão</li>
        <li>
          <code>complementar</code> &mdash; Comissão de Atividades
          Complementares
        </li>
        <li><code>estagio</code> &mdash; Comissão de Estágio</li>
        <li><code>coordenador</code> &mdash; Coordenador do Curso</li>
        <li>
          <code>secretario</code> &mdash; Secretário do Curso (somente consulta)
        </li>
        <li>
          <code>exmembro</code> &mdash; Conta inativa (testa bloqueio de login)
        </li>
      </ul>
    </div>
  </body>
</html>
