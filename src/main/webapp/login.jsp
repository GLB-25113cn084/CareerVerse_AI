<%@ page contentType="text/html;charset=UTF-8" import="com.careerverse.util.Html" %>
<%
  // Never cache this page, and send students who are already logged in
  // (for example after pressing Back) straight to their dashboard.
  response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
  if (session.getAttribute("userId") != null) {
    response.sendRedirect("app/dashboard.jsp");
    return;
  }
%>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>CareerVerse AI - Login</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="css/auth.css?v=2">
</head>
<body class="login-page">
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">✦ Career<span>Verse</span> AI</div>
      <p class="text-muted">AI-powered career guidance & simulation</p>

      <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-danger"><%=Html.esc(request.getParameter("error"))%></div>
      <% } %>
      <% if (request.getParameter("reset") != null) { %>
        <div class="alert alert-success">Password reset successfully.</div>
      <% } %>

      <form action="login" method="post" autocomplete="on">
        <label>Email</label>
        <input class="form-control mb-3" type="email" name="email"
               autocomplete="username" required placeholder="you@example.com">

        <label>Password</label>
        <div class="input-group mb-3">
          <input id="pw" class="form-control" type="password" name="password"
                 autocomplete="current-password" required>
          <button type="button" class="btn btn-outline-secondary"
                  onclick="toggle('pw', this)">Show</button>
        </div>

        <button class="btn btn-primary w-100">Log in →</button>
      </form>

      <div class="text-center mt-3">
        <a href="forgot-password.jsp">Forgot / Reset password?</a><br>
        <span>New here?</span> <a href="register.jsp">Create account</a>
      </div>
    </div>
  </div>

  <script>
    function toggle(id, b) {
      const x = document.getElementById(id);
      x.type = x.type === 'password' ? 'text' : 'password';
      b.textContent = x.type === 'password' ? 'Show' : 'Hide';
    }
  </script>
</body>
</html>