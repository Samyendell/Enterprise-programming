<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${action} Book - Book Library</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root { --accent: #2c5282; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        .navbar-custom { background: var(--accent); }
    </style>
</head>
<body class="bg-white">

<nav class="navbar navbar-custom mb-4">
    <div class="container-fluid">
        <div class="d-flex align-items-center gap-3">
            <span class="navbar-brand text-white fw-bold mb-0">Book Library</span>
            <a href="${pageContext.request.contextPath}/books"
               class="btn btn-outline-light btn-sm">View All</a>
        </div>
    </div>
</nav>

<div class="container" style="max-width:700px;">
    <h5 class="mb-4" style="border-bottom:3px solid var(--accent); padding-bottom:.5rem;">
        ${action} Book
    </h5>

    <c:if test="${not empty errors}">
        <div class="alert alert-danger">
            <strong>Please fix the following:</strong>
            <ul class="mb-0 mt-1">
                <c:forEach items="${errors}" var="e">
                    <li><c:out value="${e}"/></li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/books/${action == 'Edit' ? 'edit' : 'add'}"
          id="bookForm" novalidate>

        <c:if test="${action == 'Edit'}">
            <input type="hidden" name="id" value="${book.id}">
        </c:if>

        <div class="row g-3">
            <div class="col-md-6">
                <label for="title" class="form-label">
                    Title <span class="text-danger">*</span>
                </label>
                <input type="text" id="title" name="title"
                       class="form-control" maxlength="150" required
                       value="<c:out value='${book.title}'/>">
                <div class="invalid-feedback">Title is required (max 150 characters).</div>
            </div>

            <div class="col-md-6">
                <label for="author" class="form-label">
                    Author <span class="text-danger">*</span>
                </label>
                <input type="text" id="author" name="author"
                       class="form-control" maxlength="150" required
                       value="<c:out value='${book.author}'/>">
                <div class="invalid-feedback">Author is required (max 150 characters).</div>
            </div>

            <div class="col-md-4">
                <label for="date" class="form-label">
                    Date <span class="text-danger">*</span>
                </label>
                <input type="text" id="date" name="date"
                       class="form-control" maxlength="20"
                       placeholder="e.g. 2003" required
                       value="<c:out value='${book.date}'/>">
                <div class="invalid-feedback">Date must be a valid 4-digit year (not in the future).</div>
            </div>

            <div class="col-md-8">
                <label for="genres" class="form-label">
                    Genres
                </label>
                <input type="text" id="genres" name="genres"
                       class="form-control" maxlength="200"
                       value="<c:out value='${book.genres}'/>">
                <div class="invalid-feedback">Genres must not exceed 200 characters.</div>
            </div>

            <div class="col-12">
                <label for="characters" class="form-label">
                    Characters
                </label>
                <input type="text" id="characters" name="characters"
                       class="form-control"
                       placeholder="Comma-separated character names"
                       value="<c:out value='${book.characters}'/>">
                
            </div>

            <div class="col-12">
                <label for="synopsis" class="form-label">
                    Synopsis
                </label>
                <textarea id="synopsis" name="synopsis"
                          class="form-control" rows="4"
                          maxlength="8000"><c:out value="${book.synopsis}"/></textarea>
                <div class="invalid-feedback">Synopsis must not exceed 8000 characters.</div>
            </div>
        </div>

        <div class="mt-4 d-flex gap-2 justify-content-end">
            <a href="${pageContext.request.contextPath}/books"
               class="btn btn-secondary">Discard</a>
            <button type="submit" class="btn text-white"
                    style="background:var(--accent);">
                ${action == 'Edit' ? 'Save Changes' : 'Submit'}
            </button>
        </div>
    </form>
</div>

<script>
// NOTE: client-side validation that mirrors server-side Validation.java
// gives the user instant feedback before the form is submitted to the server
document.getElementById('bookForm').addEventListener('submit', function(e) {
    var valid = true;
    // NOTE: check length constraints for each text field
    // NOTE: required fields with max length only
    var requiredFields = [
        { id: 'title',  max: 150 },
        { id: 'author', max: 150 }
    ];
    requiredFields.forEach(function(f) {
        var el  = document.getElementById(f.id);
        var val = el.value.trim();
        if (!val || val.length > f.max) {
            el.classList.add('is-invalid');
            valid = false;
        } else {
            el.classList.remove('is-invalid');
        }
    });
    // NOTE: optional fields with max length
    var optionalFields = [
        { id: 'genres',   max: 200 },
        { id: 'synopsis', max: 8000 }
    ];
    optionalFields.forEach(function(f) {
        var el  = document.getElementById(f.id);
        var val = el.value.trim();
        if (val.length > f.max) {
            el.classList.add('is-invalid');
            valid = false;
        } else {
            el.classList.remove('is-invalid');
        }
    });

    // NOTE: validate the date field — only digits, hyphens, slashes allowed
    var dateEl = document.getElementById('date');
    var dateVal = dateEl.value.trim();
    var dateRegex = /^[0-9/\-]+$/;
    if (!dateVal || !dateRegex.test(dateVal)) {
        dateEl.classList.add('is-invalid');
        valid = false;
    } else {
        dateEl.classList.remove('is-invalid');
    }

    if (!valid) e.preventDefault();
});
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
