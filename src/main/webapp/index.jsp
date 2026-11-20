<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h1>SGHA funcionando</h1>
<p><c:out value="JSTL ok" /></p>
<a href="ola">Testar servlet</a>

<h2>Enviar comprovante (demonstração)</h2>
<form action="${pageContext.request.contextPath}/comprovantes" method="post" enctype="multipart/form-data">
    <label for="comprovante">Arquivo PDF, PNG ou JPEG (até 10 MB):</label>
    <input type="file" id="comprovante" name="comprovante" accept=".pdf,.png,.jpg,.jpeg" required>
    <button type="submit">Enviar</button>
</form>

<c:if test="${not empty caminhoComprovante}">
    <p>Comprovante salvo em: <c:out value="${caminhoComprovante}" /></p>
</c:if>
