<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Error - Book Library</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
:root {
	--accent: #2c5282;
}

body {
	font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
		sans-serif;
}

.navbar-custom {
	background: var(--accent);
}
</style>
</head>
<body class="bg-white">

	<nav class="navbar navbar-custom mb-4">
		<div class="container-fluid">
			<span class="navbar-brand text-white fw-bold">Book Library</span>
		</div>
	</nav>

	<div class="container mt-4" style="max-width: 500px">
		<div class="alert alert-danger">
			<h5 class="alert-heading mb-2">Something went wrong</h5>
			<p class="mb-3">
				<c:out value="${error}" />
			</p>
			<a href="${pageContext.request.contextPath}/books"
				class="btn btn-outline-danger btn-sm">Return to book list</a>
		</div>
	</div>

</body>
</html>
