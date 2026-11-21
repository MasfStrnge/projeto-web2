<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>SGHA - Painel da Comissão</title>
    <style>
        body { font-family: sans-serif; max-width: 1000px; margin: 24px auto; padding: 0 16px; }
        header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ccc; padding-bottom: 12px; }
        table { width: 100%; border-collapse: collapse; margin-top: 16px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; vertical-align: top; }
        th { background: #f4f4f4; }
        .erro { background: #fee; border: 1px solid #c33; padding: 10px; margin: 12px 0; color: #900; }
        input[type="number"], input[type="text"] { width: 100%; margin-bottom: 6px; padding: 4px; box-sizing: border-box; }
    </style>
</head>
<body>
    <header>
        <div>
            <strong>Servidor:</strong> <c:out value="${sessionScope.atuacaoLogada.membro.nomeCompleto}" /> |
            <strong>Perfil Ativo:</strong> ${sessionScope.atuacaoLogada.perfil}
        </div>
        <a href="${pageContext.request.contextPath}/comissao/logout">Sair</a>
    </header>

    <c:if test="${not empty erro}">
        <div class="erro"><c:out value="${erro}" /></div>
    </c:if>

    <h2>Solicitações Recebidas</h2>
    <c:choose>
        <c:when test="${empty solicitacoes}">
            <p>Nenhuma solicitação enviada disponível para o seu perfil.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Discente (Matrícula)</th>
                        <th>Documento</th>
                        <th>Categoria</th>
                        <th>Horas Solicitadas</th>
                        <th>Status</th>
                        <th>Parecer / Ação</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${solicitacoes}" var="s">
                        <tr>
                            <td>${s.id}</td>
                            <td><c:out value="${s.discente.nomeCompleto}" /> (<c:out value="${s.discente.matricula}" />)</td>
                            <td>
                                <strong><c:out value="${s.nomeDocumento}" /></strong><br>
                                <small>Obs. aluno: <c:out value="${s.observacaoDiscente}" /></small>
                            </td>
                            <td>${s.tipoHoras}</td>
                            <td>${s.horasSolicitadas}h</td>
                            <td>${s.status}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${s.status.name() == 'ENVIADA' and sessionScope.atuacaoLogada.perfil.name() != 'SECRETARIO'}">
                                        <form action="${pageContext.request.contextPath}/comissao/painel" method="post">
                                            <input type="hidden" name="idSolicitacao" value="${s.id}">
                                            <input type="number" name="horasConcedidas" min="1" max="${s.horasSolicitadas}" placeholder="Horas concedidas" required>
                                            <input type="text" name="parecerPublico" placeholder="Parecer público (visível ao aluno)" required>
                                            <input type="text" name="observacaoAdministrativa" placeholder="Obs. administrativa (privada)">
                                            <button type="submit">Registrar Parecer</button>
                                        </form>
                                    </c:when>
                                    <c:when test="${s.status.name() == 'AVALIADA'}">
                                        <strong>Concedidas:</strong> ${s.horasConcedidas}h<br>
                                        <strong>Parecer:</strong> <c:out value="${s.observacaoParecerPublico}" /><br>
                                        <small><strong>Admin:</strong> <c:out value="${s.observacaoAdministrativa}" /></small>
                                    </c:when>
                                    <c:otherwise>
                                        Somente consulta
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</body>
</html>