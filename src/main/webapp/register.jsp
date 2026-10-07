<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>CareerVerse AI - Register</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="css/auth.css">
</head>
<body class="login-page">
  <div class="auth-wrap">
    <div class="auth-card wide">
      <div class="brand">✦ Career<span>Verse</span> AI</div>
      <p class="text-muted">Create your student career profile</p>

      <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-danger"><%=request.getParameter("error")%></div>
      <% } %>

      <form action="register" method="post" autocomplete="on">
        <div class="row g-3">
          <div class="col-md-6">
            <label>Full name</label>
            <input class="form-control" name="name" autocomplete="name" required>
          </div>
          <div class="col-md-6">
            <label>Email</label>
            <input class="form-control" type="email" name="email" autocomplete="email" required>
          </div>
          <div class="col-12">
            <label>Password</label>
            <div class="input-group">
              <input id="pw" class="form-control" type="password" name="password"
                     minlength="8" autocomplete="new-password" required>
              <button type="button" class="btn btn-outline-secondary"
                      onclick="toggle('pw', this)">Show</button>
            </div>
            <small class="text-muted">Your browser can offer to save this password after registration.</small>
          </div>
        </div>
        <button class="btn btn-primary w-100 mt-4">Create account →</button>
      </form>

      <div class="text-center mt-3">
        Already registered? <a href="login.jsp">Log in</a>
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
