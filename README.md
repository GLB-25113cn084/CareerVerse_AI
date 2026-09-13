# CareerVerse AI - Java/JSP/Servlet/MySQL

## Stack
- Frontend: HTML5, CSS3, Bootstrap 5, JavaScript
- Backend: Java 25, JSP, Servlets
- Database: MySQL 8+
- Connectivity: JDBC
- Charts: Chart.js
- Server: Apache Tomcat 10.1+
- Build: Maven
- AI: Gemini API (optional)

## Features implemented
- Registration + email confirmation
- Browser autofill/password-manager support via autocomplete attributes
- Show/hide password
- Forgot/reset password using emailed time-limited token
- Profile picture dropdown and direct profile opening
- Branch/course dropdown
- Multi-select skills/interests
- Current academic year + year-wise CGPA
- Multiple career choices, including smaller/specialized roles
- Weighted skill-gap analysis
- Career-specific languages and project ideas
- Career-specific 6-month roadmap
- Detailed career simulator with stages/checkpoints
- CareerVerse AI chatbot via Gemini API
- Responsive Bootstrap UI

## Setup
1. Install JDK 25, Maven, MySQL 8 and Tomcat 10.1.
2. Create the database using `schema.sql`.
3. Edit `src/main/resources/db.properties`.
4. For Gmail, create a Google App Password and use it as `mail.password`; do not use your normal Gmail password.
5. Add a Gemini API key to `gemini.api.key` if AI chat is required.
6. Run `mvn clean package`.
7. Deploy `target/careerverse.war` to Tomcat's `webapps` folder.
8. Start Tomcat and open `http://localhost:8080/careerverse/login.jsp`.

## Important security note
The website does NOT store a user's password in browser localStorage or in the MySQL database as plaintext. The browser may offer to save the password because the forms use standard autocomplete attributes. The server stores only a BCrypt hash.
