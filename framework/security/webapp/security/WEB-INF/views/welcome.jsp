<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${title}</title>
</head>
<body>
	<sec:authorize access="hasRole('CONNECT')">
		<!-- For login user -->
		<c:url value="/logout" var="logoutUrl" />
		<form action='${logoutUrl}?returnUrl=${pageContext.request.getParameter("returnUrl")}' method="post" id="logoutForm">
			<input type="hidden" name="${_csrf.parameterName}"
				value="${_csrf.token}" />
		</form>
		<script>
			function formSubmit() {
				document.getElementById("logoutForm").submit();
			}
		</script>

		<c:if test="${pageContext.request.userPrincipal.name != null}">
			<h2>
				Welcome , ${pageContext.request.userPrincipal.name} ! &nbsp; <a
					href="javascript:formSubmit()"> Logout</a>
			</h2>
		</c:if>
	</sec:authorize>

	<sec:authorize access="isAnonymous()">
		<br />
		<h2>
			<a href="login">login</a>
		</h2>
	</sec:authorize>

</body>
</html>