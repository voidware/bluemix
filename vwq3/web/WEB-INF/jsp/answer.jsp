<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <link rel="stylesheet" type="text/css" href="/style.css">
  <title>Bluemix Question 3</title>
</head>
<body>

<h1>Question 3 Results</h1>

<%
  String v = (String) request.getAttribute("value");
  int lines = 1;
  for (int i = 0; i < v.length(); i++)
    if (v.charAt(i) == '\n') ++lines;
  if (lines < 10) lines = 10;
  out.println("<textarea cols='80' rows='" + lines + "'>" + v + "</textarea>");
%>

<br/>
<a href="/index.jsp">Back</a>
</body>
</html>
