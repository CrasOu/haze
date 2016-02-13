<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>单点登录</title>
<link rel="Stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/login.css" />
<script type="text/javascript">
	function doSubmit() {
		document.getElementById("username").value = document
				.getElementById("username").value.toUpperCase();
		return true;
	}
</script>
</head>
<body onload='document.loginForm.username.focus();'>
	<div id="login-box">

		<h3 style="text-align: center">单点登录</h3>

		<c:if test="${not empty error}">
			<div class="error">${error}</div>
		</c:if>
		<c:if test="${not empty msg}">
			<div class="msg">${msg}</div>
		</c:if>

		<form name='loginForm'
			action="<c:url value='/login?returnUrl=${pageContext.request.getParameter("service")}' />" method='POST'
			onsubmit="return doSubmit()">

			<table width="100%" cellpadding="3">
				<tr>
					<td align="right" width="85">用户名:</td>
					<td><input type='text' name='username' id="username"></td>
				</tr>
				<tr>
					<td align="right" width="85">密<span style="color: white">密</span>码:
					</td>
					<td><input type='password' name='password' id="password" /></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><input name="submit" type="submit" value=" 登 录 " /></td>
				</tr>
			</table>

			<input type="hidden" name="${_csrf.parameterName}"
				value="${_csrf.token}" />

		</form>
	</div>

</body>
</html>