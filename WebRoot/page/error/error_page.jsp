<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<div class="alert">
	${empty param.errorMessage ? errorMessage : param.errorMessage}
</div>