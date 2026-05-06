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
        :root { --accent: #2c5282; --accent-light: #ebf2fa; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        .navbar-custom { background: var(--accent); }
        .synopsis-cell { max-width: 180px; white-space: nowrap;
                         overflow: hidden; text-overflow: ellipsis; }
        .genre-tag { background: var(--accent-light); color: var(--accent); border: 1px solid #b0c7e4;
                     border-radius: 3px; padding: 2px 8px; font-size: .8rem; display: inline-block; }
        .tbl-header th { background: #f1f5f9; color: #334155; border-bottom: 2px solid #cbd5e1;
                         font-weight: 600; font-size: .85rem; text-transform: uppercase; letter-spacing: .03em; }
        .btn-edit { background: none; border: 1px solid #94a3b8; color: #334155; font-size: .8rem; }
        .btn-edit:hover { background: #f1f5f9; border-color: #64748b; color: #1e293b; }
        .btn-del { background: none; border: 1px solid #fca5a5; color: #dc2626; font-size: .8rem; }
        .btn-del:hover { background: #fef2f2; }
        .format-pill { font-size: .7rem; padding: 3px 10px; border-radius: 10px;
                       background: #e2e8f0; color: #475569; font-weight: 600; }
    </style>
</head>
<body class="bg-white">

<nav class="navbar navbar-custom mb-4">
    <div class="container-fluid">
        <div class="d-flex align-items-center gap-3">
            <span class="navbar-brand text-white fw-bold mb-0">Book Library</span>
            <a href="${pageContext.request.contextPath}/books/add"
               class="btn btn-light btn-sm">New Entry</a>
        </div>
        <a href="${pageContext.request.contextPath}/books/search"
           class="btn btn-outline-light btn-sm">Find Books</a>
    </div>
</nav>

<div class="container-fluid px-4">

    <c:if test="${param.success == 'added'}">
        <div class="alert alert-success alert-dismissible fade show">
            Book added successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible fade show">
            Book updated successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible fade show">
            Book deleted.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card border-0 shadow-sm">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
            <h6 class="mb-0">
                All Books
                <span class="text-muted fw-normal ms-1">(${fn:length(books)})</span>
            </h6>
            <span class="format-pill">MVC</span>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="tbl-header">
                    <tr>
                        <th style="width:50px">#</th>
                        <th>Title</th>
                        <th>Author</th>
                        <th>Date</th>
                        <th>Genres</th>
                        <th>Characters</th>
                        <th>Synopsis</th>
                        <th class="text-center" style="width:140px">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty books}">
                            <tr>
                                <td colspan="8" class="text-center text-muted py-4">
                                    No books found.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${books}" var="b">
                                <tr id="row-${b.id}">
                                    <td>${b.id}</td>
                                    <td><strong><c:out value="${b.title}"/></strong></td>
                                    <td><c:out value="${b.author}"/></td>
                                    <td><c:out value="${b.date}"/></td>
                                    <td>
                                        <span class="genre-tag">
                                            <c:out value="${b.genres}"/>
                                        </span>
                                    </td>
                                    <td class="text-muted small">
                                        <c:out value="${b.characters}"/>
                                    </td>
                                    <td class="synopsis-cell text-muted small">
                                        <c:out value="${b.synopsis}"/>
                                    </td>
                                    <td class="text-center text-nowrap">
                                        <button type="button" class="btn btn-sm btn-del"
                                                onclick="startDelete(${b.id}, this)">Remove</button>
                                        <a href="${pageContext.request.contextPath}/books/edit?id=${b.id}"
                                           class="btn btn-sm btn-edit">Modify</a>
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

<%-- Inline delete confirmation — replaces the row content temporarily --%>
<script>
function startDelete(id, btn) {
    var row = btn.closest('tr');
    var cells = row.innerHTML;
    row.style.background = '#fef2f2';
    row.innerHTML =
        '<td colspan="8" class="py-2 px-3">' +
            '<div class="d-flex justify-content-between align-items-center">' +
                '<span>Delete this book?</span>' +
                '<div class="d-flex gap-2">' +
                    '<button class="btn btn-sm btn-outline-secondary" ' +
                        'onclick="cancelDelete(this, \'' + escape(cells) + '\')"'>Go Back</button>' +
                    '<form method="post" action="${pageContext.request.contextPath}/books/delete" style="display:inline">' +
                        '<input type="hidden" name="id" value="' + id + '">' +
                        '<button type="submit" class="btn btn-sm btn-danger">Confirm Removal</button>' +
                    '</form>' +
                '</div>' +
            '</div>' +
        '</td>';
}

function cancelDelete(btn, escaped) {
    var row = btn.closest('tr');
    row.style.background = '';
    row.innerHTML = unescape(escaped);
}
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
