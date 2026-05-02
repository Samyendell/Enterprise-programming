<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Search Books</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-dark bg-dark mb-4">
    <div class="container">
        <span class="navbar-brand fw-bold">📚 Book Library — MVC</span>
        <a href="${pageContext.request.contextPath}/books"
           class="btn btn-outline-light btn-sm">← All Books</a>
    </div>
</nav>

<div class="container" style="max-width:720px">
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <h5 class="card-title mb-3">Search Books</h5>
            <form method="get"
                  action="${pageContext.request.contextPath}/books/search">
                <div class="input-group">
                    <input type="text" class="form-control form-control-lg"
                           name="q" value="<c:out value='${query}'/>"
                           placeholder="Search by title, author or genres…"
                           maxlength="200" required>
                    <button class="btn btn-primary" type="submit">Search</button>
                </div>
            </form>
            <c:if test="${not empty error}">
                <div class="alert alert-warning mt-3">
                    <c:out value="${error}"/>
                </div>
            </c:if>
        </div>
    </div>

    <c:if test="${results != null}">
        <p class="text-muted">
            <strong>${fn:length(results)}</strong> result(s) for
            "<strong><c:out value='${query}'/></strong>"
        </p>
        <c:choose>
            <c:when test="${empty results}">
                <div class="alert alert-info">No books matched your search.</div>
            </c:when>
            <c:otherwise>
                <div class="list-group">
                    <c:forEach items="${results}" var="b">
                        <div class="list-group-item">
                            <div class="d-flex justify-content-between">
                                <div>
                                    <strong><c:out value="${b.title}"/></strong>
                                    <span class="text-muted ms-2">
                                        by <c:out value="${b.author}"/>
                                    </span>
                                    <br>
                                    <small>
                                        <span class="badge bg-info text-dark">
                                            <c:out value="${b.genres}"/>
                                        </span>
                                        <span class="ms-2">
                                            <c:out value="${b.date}"/>
                                        </span>
                                    </small>
                                </div>
                                <a href="${pageContext.request.contextPath}/books/edit?id=${b.id}"
                                   class="btn btn-sm btn-warning align-self-center">Edit</a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>