<%@ page contentType="text/html;charset=UTF-8" import="com.careerverse.util.Html" %>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Reset Password</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="css/auth.css">
</head>
<body class="login-page">
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">✦ Career<span>Verse</span> AI</div>
      <h4 class="mt-4">Create a new password</h4>

      <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-danger"><%=Html.esc(request.getParameter("error"))%></div>
      <% } %>

      <form action="reset-password" method="post">
        <input type="hidden" name="token"
               value="<%=Html.esc(request.getParameter("token"))%>">
        <label>New password</label>
        <div class="input-group mb-3">
          <input id="pw" class="form-control" type="password" name="password"
                 minlength="8" autocomplete="new-password" required>
          <button type="button" class="btn btn-outline-secondary"
                  onclick="toggle('pw', this)">Show</button>
        </div>
        <label>Confirm new password</label>
        <input id="pw2" class="form-control mb-3" type="password" name="confirm"
               minlength="8" autocomplete="new-password" required>
        <button class="btn btn-primary w-100">Reset password</button>
      </form>
    </div>
  </div>

  <script>
    document.querySelector('form').addEventListener('submit', e => {
      const a = document.getElementById('pw'), b = document.getElementById('pw2');
      b.setCustomValidity(a.value === b.value ? '' : 'Passwords do not match');
      if (!b.checkValidity()) { e.preventDefault(); b.reportValidity(); }
    });
    function toggle(id, b) {
      const x = document.getElementById(id);
      x.type = x.type === 'password' ? 'text' : 'password';
      b.textContent = x.type === 'password' ? 'Show' : 'Hide';
    }
  </script>
</body>
</html>
