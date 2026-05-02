<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Book Library</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .synopsis-cell { max-width: 200px; white-space: nowrap;
                         overflow: hidden; text-overflow: ellipsis; }
    </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <span class="navbar-brand fw-bold">📚 Book Library — MVC</span>
        <div>
            <a href="${pageContext.request.contextPath}/books/search"
               class="btn btn-outline-light btn-sm me-2">Search</a>
            <a href="${pageContext.request.contextPath}/books/add"
               class="btn btn-success btn-sm">+ Add Book</a>
        </div>
    </div>
</nav>

<div class="container-fluid px-4">

    <%-- Success banners (POST-Redirect-GET passes success via query param) --%>
    <c:if test="${param.success == 'added'}">
        <div class="alert alert-success alert-dismissible fade show">
            Book added. <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-info alert-dismissible fade show">
            Book updated. <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-warning alert-dismissible fade show">
            Book deleted. <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
                All Books
                <span class="badge bg-secondary ms-1">${fn:length(books)}</span>
            </h5>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>#</th>
                        <th>Title</th>
                        <th>Author</th>
                        <th>Date</th>
                        <th>Genres</th>
                        <th>Synopsis</th>
                        <th class="text-center">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty books}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">
                                    No books found.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${books}" var="b">
                                <tr>
                                    <td>${b.id}</td>
                                    <%-- c:out escapes HTML — prevents XSS from stored data --%>
                                    <td><strong><c:out value="${b.title}"/></strong></td>
                                    <td><c:out value="${b.author}"/></td>
                                    <td><c:out value="${b.date}"/></td>
                                    <td>
                                        <span class="badge bg-info text-dark">
                                            <c:out value="${b.genres}"/>
                                        </span>
                                    </td>
                                    <td class="synopsis-cell text-muted small">
                                        <c:out value="${b.synopsis}"/>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/books/edit?id=${b.id}"
                                           class="btn btn-sm btn-warning me-1">Edit</a>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/books/delete"
                                              style="display:inline"
                                              onsubmit="return confirm('Delete this book?')">
                                            <input type="hidden" name="id" value="${b.id}">
                                            <button type="submit" class="btn btn-sm btn-danger">
                                                Delete
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>