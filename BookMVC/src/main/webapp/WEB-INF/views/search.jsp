<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Search - Book Library</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root { --accent: #2c5282; --accent-light: #ebf2fa; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        .navbar-custom { background: var(--accent); }
        .genre-tag { background: var(--accent-light); color: var(--accent); border: 1px solid #b0c7e4;
                     border-radius: 3px; padding: 2px 8px; font-size: .8rem; display: inline-block; }
        .btn-edit { background: none; border: 1px solid #94a3b8; color: #334155; font-size: .8rem; }
        .btn-edit:hover { background: #f1f5f9; border-color: #64748b; }
    </style>
</head>
<body class="bg-white">

<nav class="navbar navbar-custom mb-4">
    <div class="container">
        <span class="navbar-brand text-white fw-bold">Book Library</span>
        <a href="${pageContext.request.contextPath}/books"
           class="btn btn-outline-light btn-sm">All Books</a>
    </div>
</nav>

<div class="container" style="max-width:700px">
    <h5 class="mb-4" style="border-bottom:3px solid var(--accent); padding-bottom:.5rem;">
        Search Books
    </h5>

    <form method="get"
          action="${pageContext.request.contextPath}/books/search" class="mb-4">
        <div class="input-group">
            <input type="text" class="form-control"
                   name="q" value="<c:out value='${query}'/>"
                   placeholder="Search by title, author or genre..."
                   maxlength="200" required>
            <button class="btn btn-outline-secondary" type="submit">Search</button>
        </div>
    </form>

    <c:if test="${not empty error}">
        <div class="alert alert-warning">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <c:if test="${results != null}">
        <p class="text-muted">
            <strong>${fn:length(results)}</strong> result(s) for
            "<strong><c:out value='${query}'/></strong>"
        </p>
        <c:choose>
            <c:when test="${empty results}">
                <p class="text-muted">No books matched your search.</p>
            </c:when>
            <c:otherwise>
                <div class="list-group">
                    <c:forEach items="${results}" var="b">
                        <div class="list-group-item">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <strong><c:out value="${b.title}"/></strong>
                                    <span class="text-muted ms-2">
                                        by <c:out value="${b.author}"/>
                                    </span>
                                    <br>
                                    <small>
                                        <span class="genre-tag">
                                            <c:out value="${b.genres}"/>
                                        </span>
                                        <span class="text-muted ms-2">
                                            <c:out value="${b.date}"/>
                                        </span>
                                    </small>
                                </div>
                                <a href="${pageContext.request.contextPath}/books/edit?id=${b.id}"
                                   class="btn btn-sm btn-edit">Edit</a>
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
