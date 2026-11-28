<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>SGHA - Painel do Discente</title>
    <style>
        body { font-family: sans-serif; max-width: 900px; margin: 24px auto; padding: 0 16px; }
        header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ccc; padding-bottom: 12px; }
        .card { border: 1px solid #ddd; padding: 16px; margin: 20px 0; border-radius: 4px; }
        .campo { margin-bottom: 12px; }
        label { display: block; font-weight: bold; margin-bottom: 4px; }
        input, select, textarea { width: 100%; padding: 8px; box-sizing: border-box; }
        table { width: 100%; border-collapse: collapse; margin-top: 12px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f4f4f4; }
        .erro { background: #fee; border: 1px solid #c33; padding: 10px; margin: 12px 0; color: #900; }
    </style>
</head>
<body>
    <header>
        <div>
            <strong>Discente:</strong> <c:out value="${requestScope.discente.nomeCompleto}" /> |
            <strong>Matrícula:</strong> <c:out value="${requestScope.discente.matricula}" />
        </div>
        <a href="${pageContext.request.contextPath}/discente/logout">Sair (Logout)</a>
    </header>

    <c:if test="${not empty erro}">
        <div class="erro"><c:out value="${erro}" /></div>
    </c:if>

    <section class="card">
        <h2>Nova Solicitação de Carga Horária</h2>
        <form action="${pageContext.request.contextPath}/discente/solicitacoes" method="post" enctype="multipart/form-data">
            <div class="campo">
                <label for="nomeDocumento">Título / Nome do Documento</label>
                <input type="text" id="nomeDocumento" name="nomeDocumento" required>
            </div>
            <div class="campo">
                <label for="tipoHoras">Comissão Destino</label>
                <select id="tipoHoras" name="tipoHoras" required>
                    <option value="">Selecione...</option>
                    <c:forEach items="${requestScope.tiposHoras}" var="tipo">
                        <option value="${tipo.name()}">${tipo}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="campo">
                <label for="horasSolicitadas">Quantidade de Horas Pretendidas</label>
                <input type="number" id="horasSolicitadas" name="horasSolicitadas" min="1" required>
            </div>
            <div class="campo">
                <label for="arquivoComprovante">Comprovante (PDF, PNG ou JPEG - máx. 5 MB)</label>
                <input type="file" id="arquivoComprovante" name="arquivoComprovante" accept=".pdf,.png,.jpg,.jpeg" required>
            </div>
            <div class="campo">
                <label for="observacaoDiscente">Observação do Discente (opcional)</label>
                <textarea id="observacaoDiscente" name="observacaoDiscente" rows="3"></textarea>
            </div>
            <button type="submit">Salvar Solicitação</button>
        </form>
    </section>

    <section>
        <h2>Minhas Solicitações</h2>
        <c:choose>
            <c:when test="${empty requestScope.solicitacoes}">
                <p>Nenhuma solicitação cadastrada até o momento.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Documento</th>
                            <th>Categoria</th>
                            <th>Horas Solicitadas</th>
                            <th>Status</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${requestScope.solicitacoes}" var="s">
                            <tr>
                                <td>${s.id}</td>
                                <td><c:out value="${s.nomeDocumento}" /></td>
                                <td>${s.tipoHoras}</td>
                                <td>${s.horasSolicitadas}h</td>
                                <td>${s.status}</td>
                                <td>
                                    <c:if test="${s.status.name() == 'CRIADA'}">
                                        <form action="${pageContext.request.contextPath}/discente/solicitacoes" method="post" style="display:inline;">
                                            <input type="hidden" name="acao" value="enviar">
                                            <input type="hidden" name="idSolicitacao" value="${s.id}">
                                            <button type="submit">Enviar para Comissão</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/discente/solicitacoes" method="post" style="display:inline;"
                                        onsubmit="return confirm('Deseja realmente excluir esta solicitação?');">
                                        <input type="hidden" name="acao" value="excluir">
                                        <input type="hidden" name="idSolicitacao" value="${s.id}">
                                        <button type="submit">Excluir</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>
</body>
</html>
