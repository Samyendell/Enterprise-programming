<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5" style="max-width:500px">
    <div class="alert alert-danger">
        <h4 class="alert-heading">Something went wrong</h4>
        <p><c:out value="${error}"/></p>
        <hr>
        <a href="${pageContext.request.contextPath}/books"
           class="btn btn-outline-danger btn-sm">← Return to book list</a>
    </div>
</div>
</body>
</html>