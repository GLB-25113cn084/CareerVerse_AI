# CareerVerse AI – Java/JSP/Servlet/MySQL

AI-based career guidance for engineering students: profile, aptitude test, career matches,
skill-gap analysis, 6-month roadmap, career simulator, career report, an AI chatbot and a
LeetCode practice tracker with streaks.

## Stack
- Frontend: HTML5, CSS3, Bootstrap 5, JavaScript, Chart.js
- Backend: Java 17, JSP, Jakarta Servlets 6, Apache Tomcat 10.1
- Database: MySQL 8 (local) / Aiven for MySQL (cloud), JDBC
- AI chatbot: Groq (Llama 3.3) or Google Gemini in the cloud, Ollama on a laptop
- Build and deploy: Maven, Docker, Render (auto-deploy from GitHub)

## Features
- Registration, login, logout confirmation, forgot/reset password by email (BCrypt hashes, 15-minute tokens)
- Emails on register, every login (with device and IP), password-reset link and password change (Brevo or Gmail)
- Student profile: branch, year, year-wise CGPA, skills and interests that change with the branch
- 32 careers across CSE/IT, ECE, EE, ME and Civil – each student sees the careers of their own branch
  (e.g. VLSI and Embedded for ECE, Structural and BIM for Civil, plus GATE/PSU for core branches)
- Aptitude test and weighted career matching, with a bonus for core careers of the branch
- **Semester-wise roadmap** from the current semester until Semester 8: modules to learn, 8 graded
  projects per career (Beginner → Capstone), certifications, internship and placement milestones
- Skill-gap analysis, career simulator and career report
- **CareerVerse AI chatbot** that knows the student's profile, top careers and LeetCode progress
- **LeetCode Practice page**
  - link a public LeetCode username (never a password)
  - solved counts (easy / medium / hard), SQL count, ranking and recent accepted problems
  - CareerVerse streak, longest streak, weekly goal and a GitHub-style 12-month heatmap
  - daily activity saved in the `leetcode_daily` table, so history stays even if LeetCode changes
  - hand-picked problems for each career (DSA for developers, SQL for data roles) with solved ticks
  - practice targets for every roadmap stage (for example "15 easy problems in Month 1") with progress bars
  - "Update my skill gap" raises DSA / SQL / language levels from solved problems

## Project layout
```
src/main/java/com/careerverse/
  config/   DB.java (connections + settings), SchemaInitializer.java (creates tables on start)
  dao/      UserDAO, ProfileDAO, LeetCodeDAO
  filter/   AuthFilter (protects /app/*)
  model/    CareerData (loads resources/careers.json – 32 careers)
  service/  CareerEngine (matching + semester roadmap), AIService, StudentContext, LeetCodeClient,
            PracticeCatalog, StreakCalculator, EmailService, EmailTemplates
  servlet/  one servlet per URL (login, register, profile, ai, leetcode ...)
  util/     PasswordUtil, TokenUtil, Html, RequestInfo
src/main/resources/careers.json   all career data – edit this to add careers or projects
src/main/webapp/
  app/dashboard.jsp   single-page dashboard
  js/catalog.js       branch groups and skill lists
  js/app.js           dashboard logic + chatbot
  js/practice.js      LeetCode practice page
  css/app.css, css/auth.css, login/register/reset pages
```

## Run on your laptop
1. Install JDK 17, Maven, MySQL 8 and Tomcat 10.1.
2. Start MySQL. The database `careerverse_ai` and all tables are created automatically on first start.
3. Put your local MySQL password in `src/main/resources/db.properties` (do not push real passwords).
4. Optional AI: put a Groq key in `groq.api.key`, or run Ollama (`ollama run llama3.2`).
5. `mvn clean package`, copy `target/careerverse.war` to Tomcat `webapps`, open
   `http://localhost:8080/careerverse/login.jsp`.

## Deploy (Aiven + Render + GitHub from VS Code)
See **[README_DEPLOYMENT.md](README_DEPLOYMENT.md)**.

## Security notes
- Passwords are stored only as BCrypt hashes; reset tokens are stored as SHA-256 hashes.
- All SQL uses PreparedStatement. Error messages in JSP pages are HTML-escaped.
- Secrets (database password, API keys, Gmail app password) live only in Render environment variables.
- The LeetCode feature uses LeetCode's public, unofficial GraphQL endpoint and only the public username.
