<%@ page contentType="text/html;charset=UTF-8" import="com.careerverse.util.Html" %>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Forgot Password</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="css/auth.css?v=2">
</head>
<body class="login-page">
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">✦ Career<span>Verse</span> AI</div>
      <h4 class="mt-4">Reset your password</h4>
      <p class="text-muted">Enter your registered email. If it exists, we will send a reset link.</p>

      <% if (request.getParameter("sent") != null) { %>
        <div class="alert alert-success">
          If this email is registered, a reset link is on its way. It works for 15 minutes.
          Check your <b>Spam / Promotions</b> folder if you don't see it.
        </div>
      <% } %>
      <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-danger"><%=Html.esc(request.getParameter("error"))%></div>
      <% } %>

      <form action="forgot-password" method="post">
        <input class="form-control mb-3" type="email" name="email"
               autocomplete="email" required placeholder="you@example.com">
        <button class="btn btn-primary w-100">Send reset link</button>
      </form>

      <div class="text-center mt-3">
        <a href="login.jsp">Back to login</a>
      </div>
    </div>
  </div>
</body>
</html>
