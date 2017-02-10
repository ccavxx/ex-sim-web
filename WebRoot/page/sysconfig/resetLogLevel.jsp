<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  
  <body>
  <center>
     <form action="/sim/node/resetLogLevel" method="post">
      <table>
        <tr>
          <td>节 点：</td>
          <td>
          		<select name="nodeId">
          			<c:forEach var="node" items="${nodes}">
          				<option value="${node.nodeId}" ${node.nodeId eq param.nodeId ? "selected" : ""}>
          				${node.type eq "Agent" ? node.alias : node.type}(${node.ip})
          				</option>
          			</c:forEach>
          		</select>
          <td>
        </tr>
        <tr>
          <td>类名：</td>
          <td><input name="className" size="100"><td>
        </tr>
        <tr>
          <td>级 别：</td>
          <td>
          		<select name="level">
                 <option value="INFO">INFO</option>
                 <option value="DEBUG">DEBUG</option>
                 <option value="WARN">WARN</option>
                 <option value="ERROR">ERROR</option>
                 </select>
           <td>
        </tr>
        <tr>
          <td><input value="提交" type="submit" name="btn"></td>
        </tr>
       </table>
     </form>
     </center>
  </body>
</html>
