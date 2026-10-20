# CareerVerse AI – Deployment Guide

**Aiven** (MySQL database) + **Render** (website) + **GitHub** (code), with
**VS Code auto-commit**, so every change you save goes to GitHub and Render redeploys it.

```
VS Code ──(GitDoc auto-commit + push)──► GitHub repo ──(auto-deploy)──► Render (Docker + Tomcat)
                                                                              │
                                                                              ▼
                                                                        Aiven MySQL
```

---

## Step 1. Create the database on Aiven

1. Sign in at <https://console.aiven.io> and click **Create service → MySQL**.
2. Choose the **Free** plan and a region close to India (for example Singapore), name it `careerverse-db`, and create it.
3. Wait until the status says **Running**.
4. On the service **Overview** page, copy the **Service URI**. It looks like this:
   `mysql://avnadmin:AVNS_xxxxx@careerverse-db-yourname.aivencloud.com:12345/defaultdb?ssl-mode=REQUIRED`

You do **not** need to run `schema.sql`. CareerVerse creates all 9 tables in `defaultdb`
the first time it starts (`SchemaInitializer.java`). Every table has a primary key, which
Aiven requires.

> If you already have users in an old Aiven database, keep using it. Existing tables are left
> unchanged, and only the two new LeetCode tables are added.

## Step 2. Get a free AI key for the chatbot

Ollama runs only on your own laptop, so the website on Render needs a cloud model.

- **Groq (recommended, free):** go to <https://console.groq.com/keys>, then **Create API Key**, and copy the `gsk_...` key.
- **Gemini (alternative):** go to <https://aistudio.google.com/apikey>, create a key, and set `AI_PROVIDER=gemini`.

## Step 3. Put the code on GitHub from VS Code

The folder you received is already connected to
`https://github.com/GLB-25113cn084/CareerVerse_AI`, and all the new work is in one ready commit.

1. Install **Git** (<https://git-scm.com>) and **VS Code**.
2. Unzip `CareerVerse_AI.zip` and open the `CareerVerse_AI` folder with **File → Open Folder**.
3. When VS Code suggests the recommended extensions, click **Install All**. These are GitDoc and Java.
4. Sign in to GitHub: click the **Accounts** icon (bottom-left), then **Sign in with GitHub**.
5. Open the **Source Control** panel (Ctrl+Shift+G) and click **Sync Changes** (or **Publish Branch**).
   This pushes the new version to GitHub.
   - If VS Code says the push was rejected because GitHub has newer commits, click **Sync** again. It pulls first and then pushes.

> If you would rather start from your own copy of the repository, copy everything from this folder
> (except `.git`) into your existing `CareerVerse_AI` folder and then commit and push.

### Turn on auto-commit (every save goes to GitHub)

This folder already has `.vscode/settings.json` with GitDoc switched on:

| Setting | Meaning |
|---|---|
| `files.autoSave: afterDelay` | Files save themselves 2 seconds after you stop typing |
| `gitdoc.enabled: true` | GitDoc commits your saved changes automatically |
| `gitdoc.autoCommitDelay: 60000` | It waits 60 seconds after the last change, so one edit session becomes one commit |
| `gitdoc.autoPush: onCommit` | Each commit is pushed to GitHub straight away |
| `gitdoc.commitValidationLevel: error` | Nothing is committed while VS Code shows errors in your code |

You will see **GitDoc** in the bottom status bar. Click it to switch auto-commit on or off.

⚠️ Every push makes Render rebuild the site, which takes about 3–5 minutes. When you plan to make
many small edits, switch GitDoc off, finish the edits, then switch it back on.

## Step 4. Create the website on Render

1. Sign in at <https://dashboard.render.com> with your GitHub account.
2. Click **New → Blueprint**, select the `CareerVerse_AI` repository, and click **Apply**.
   Render reads `render.yaml` and creates a Docker web service called **careerverse-ai**.
   (Or use **New → Web Service**, pick the repository, and choose **Language: Docker**, **Instance: Free**.)
3. Render asks for the secret values. Fill in:

| Key | Value |
|---|---|
| `DB_URL` | the Aiven **Service URI** from Step 1 (it already includes the user and password) |
| `DB_USER`, `DB_PASSWORD` | leave empty when you use the Service URI |
| `AI_PROVIDER` | `groq` |
| `GROQ_API_KEY` | your `gsk_...` key |
| `APP_URL` | your Render address, e.g. `https://careerverse-ai.onrender.com` (you can add it after the first deploy) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail address and a Gmail **App Password** (optional) |

4. Click **Deploy**. The first build takes about 5–8 minutes. When the logs show
   `CareerVerse: database tables are ready.`, open the `.onrender.com` link.

**Auto-deploy:** `autoDeploy: true` means every push to the `main` branch on GitHub redeploys the site.
If you created the service by hand, check that **Settings → Build & Deploy → Auto-Deploy** is **On Commit**.

### Already have a Render service?
Open the existing service, go to **Environment**, add `AI_PROVIDER`, `GROQ_API_KEY` and `APP_URL`, set
`DB_URL` to your Aiven URL, and push the code. The service redeploys by itself.

## Step 5. Check that everything works

1. Register a new account and log in.
2. **Dashboard:** the profile completion card and the LeetCode streak card show values.
3. **CareerVerse AI** (Ask box or menu): ask "What should I learn next?". It should answer using your profile.
4. **LeetCode Practice:** type your public LeetCode username (for example `tanvi_codes`) and click **Link**.
   Your stats, streak, heatmap and targets appear. Click a problem to open it on LeetCode.
5. **Learning Roadmap:** every stage now shows its LeetCode target with a progress bar.

## Troubleshooting

| Problem | Fix |
|---|---|
| Login says "Login failed" | `DB_URL` is wrong, or the Aiven service is powered off. Check the Render logs for `could not create tables`. |
| `Public Key Retrieval is not allowed` | Use the Aiven Service URI, or add `&allowPublicKeyRetrieval=true` to a jdbc URL. |
| Chatbot says "AI is unavailable … Groq" | `GROQ_API_KEY` is missing or mistyped. Fix it in Render → Environment and save; Render restarts the site. |
| LeetCode says "No user called …" | Use the username from your profile link `leetcode.com/u/<username>`, not your display name or email. |
| LeetCode stats don't refresh | LeetCode sometimes blocks automated requests. The page keeps showing your last saved data; try **Refresh** later. |
| First page load is slow | The Render free plan sleeps after 15 minutes without visitors. The first visit wakes it up, which takes about 50 seconds. |
| Reset-password email not sent | Use a Gmail App Password. Some free hosting plans block outgoing email ports; the rest of the site still works. |
| Aiven free service powered off | Aiven can power off free services that sit idle. Open the Aiven console and click **Power on**. |

## Environment variables reference

| Variable | Used for |
|---|---|
| `DB_URL` | Aiven Service URI (`mysql://...`) or a JDBC URL (`jdbc:mysql://host:port/defaultdb?sslMode=REQUIRED`) |
| `DB_USER`, `DB_PASSWORD` | Only needed with a JDBC URL |
| `AI_PROVIDER` | `groq`, `gemini` or `ollama` (empty = choose automatically from the keys that are set) |
| `GROQ_API_KEY`, `GROQ_MODEL` | Groq chatbot (default model `llama-3.3-70b-versatile`) |
| `GEMINI_API_KEY`, `GEMINI_MODEL` | Gemini chatbot (default model `gemini-2.5-flash`) |
| `OLLAMA_URL`, `OLLAMA_MODEL` | Local Ollama (laptop only) |
| `APP_URL` | Public address used in password-reset links |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Email sending |
| `JAVA_OPTS` | Memory limits for the free Render plan (already set in `render.yaml`) |

Never write real passwords or keys into `db.properties`, `render.yaml` or any other file in GitHub.
