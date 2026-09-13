# CareerVerse AI — cloud deployment

This version is prepared for deployment on a cloud service such as Railway. Users only need a browser; they do not need Java, Maven, MySQL or Tomcat installed.

## 1. Push this project to GitHub

Push the contents of this folder to your GitHub repository. Keep `schema.sql` in the repository.

## 2. Create a Railway project

Create a new Railway project and add:
- a MySQL database service
- a service connected to this GitHub repository

Railway detects the Dockerfile and builds the Java/Tomcat application.

## 3. Add application variables

In the CareerVerse service, add these variables using the values supplied by your Railway MySQL service:

DB_URL=jdbc:mysql://<MYSQLHOST>:<MYSQLPORT>/<MYSQLDATABASE>?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=<MYSQLUSER>
DB_PASSWORD=<MYSQLPASSWORD>

Optional email variables:
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<your email>
MAIL_PASSWORD=<your app password>

Do not put real passwords in GitHub.

## 4. Create the database tables

Open Railway's MySQL database interface and run the contents of `schema.sql`.

## 5. Generate the public website URL

After the service deploys, use Railway's Networking/Public Networking option to generate a public domain for the web service.

Open that HTTPS URL from any phone or laptop browser.

## AI chatbot note

The current AIQueryServlet calls Ollama at `http://localhost:11434`. That works only when Ollama is running on the same machine/container. The normal website can deploy without the AI chatbot, but the chatbot endpoint will need a cloud AI service or a separate Ollama service before it can work online.
