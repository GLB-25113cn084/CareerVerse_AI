<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>CareerVerse AI</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet"
    >

    <link rel="stylesheet" href="../css/app.css">

    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <script>
        Chart.defaults.font.family = "Inter, system-ui, sans-serif";
        Chart.defaults.color = "#7b8598";
        Chart.defaults.backgroundColor = "rgba(99, 102, 241, 0.8)";
        Chart.defaults.elements.bar.borderRadius = 8;
        Chart.defaults.elements.bar.borderSkipped = false;
        Chart.defaults.plugins.legend.display = false;
    </script>
</head>

<body>

<div class="d-flex" id="shell">

    <!-- SIDEBAR -->
    <aside class="sidebar p-3">

        <div class="brand mb-4">
            ✦ Career<span>Verse</span> AI
        </div>

        <button class="navbtn active" data-page="home">
            🏠 Dashboard
        </button>

        <button class="navbtn" data-page="profile">
            👤 Student Profile
        </button>

        <button class="navbtn" data-page="assessment">
            🧠 Aptitude Test
        </button>

        <button class="navbtn" data-page="careers">
            💼 Career Recommendations
        </button>

        <button class="navbtn" data-page="gap">
            📊 Skill Gap
        </button>

        <button class="navbtn" data-page="roadmap">
            🗺️ Learning Roadmap
        </button>

        <button class="navbtn" data-page="practice">
            💻 LeetCode Practice
        </button>

        <button class="navbtn" data-page="simulator">
            🚀 Career Simulator
        </button>

        <button class="navbtn" data-page="report">
            📄 Career Report
        </button>

        <div class="mt-auto pt-3">
            <button type="button" class="btn btn-outline-light w-100 logout-btn"
                    data-bs-toggle="modal" data-bs-target="#logoutModal">
                🚪 Log out
            </button>
        </div>

    </aside>


    <!-- MAIN -->
    <main class="flex-grow-1">

        <!-- TOP BAR -->
        <header class="topbar d-flex align-items-center justify-content-between">

            <button
                id="menu"
                class="btn btn-outline-secondary d-lg-none"
            >
                ☰
            </button>

            <div class="searchbox">

                <input
                    id="aiQuery"
                    placeholder="Ask CareerVerse AI anything…"
                >

                <button id="askAI">
                    Ask
                </button>

            </div>


            <div class="dropdown">

                <button
                    class="profile-btn dropdown-toggle"
                    data-bs-toggle="dropdown"
                >

                    <span id="topName">
                        Student
                    </span>

                    <span id="avatar">
                        S
                    </span>

                </button>


                <ul class="dropdown-menu dropdown-menu-end">

                    <li>
                        <h6
                            class="dropdown-header"
                            id="menuName"
                        >
                            Student
                        </h6>
                    </li>

                    <li>
                        <button
                            class="dropdown-item"
                            data-page="profile"
                        >
                            Open profile
                        </button>
                    </li>

                    <li>
                        <button
                            class="dropdown-item"
                            id="openAI"
                        >
                            Ask CareerVerse AI
                        </button>
                    </li>

                    <li>
                        <hr class="dropdown-divider">
                    </li>

                    <li>
                        <button
                            type="button"
                            class="dropdown-item text-danger"
                            data-bs-toggle="modal"
                            data-bs-target="#logoutModal"
                        >
                            Log out
                        </button>
                    </li>

                </ul>

            </div>

        </header>


        <div class="container-fluid p-4">


            <!-- DASHBOARD -->
            <section id="home" class="page active">

                <h1>
                    Welcome to CareerVerse 👋
                </h1>

                <p class="text-muted">
                    Your personalized career guidance, skills and hiring roadmap.
                </p>


                <div class="hero p-4 p-md-5 rounded-4 mt-3">

                    <div>

                        <small>
                            AI CAREER GUIDANCE
                        </small>

                        <h2>
                            Discover where your skills can take you.
                        </h2>

                        <p>
                            Get career matches, accurate skill gaps,
                            career-specific learning plans,
                            project ideas and a detailed hiring simulation.
                        </p>

                        <button
                            class="btn btn-light"
                            data-page="assessment"
                        >
                            Start assessment
                        </button>

                    </div>

                </div>


                <div class="row g-3 my-2">

                    <div class="col-md-3">
                        <div class="stat s-indigo">

                            <div class="stat-icon">🎯</div>

                            <small>
                                Top Match
                            </small>

                            <b id="statCareer">
                                —
                            </b>

                            <span id="statMatch">
                                —
                            </span>

                        </div>
                    </div>


                    <div class="col-md-3">
                        <div class="stat s-green">

                            <div class="stat-icon">🛠️</div>

                            <small>
                                Skills
                            </small>

                            <b id="statSkills">
                                0
                            </b>

                            <span>
                                tracked skills
                            </span>

                        </div>
                    </div>


                    <div class="col-md-3">
                        <div class="stat s-amber">

                            <div class="stat-icon">📈</div>

                            <small>
                                Profile
                            </small>

                            <b id="statProgress">
                                0%
                            </b>

                            <span>
                                completion
                            </span>

                        </div>
                    </div>


                    <div class="col-md-3">
                        <div class="stat s-pink stat-link" data-page="practice" role="button">

                            <div class="stat-icon">🔥</div>

                            <small>
                                LeetCode Streak
                            </small>

                            <b id="statStreak">
                                —
                            </b>

                            <span id="statStreakNote">
                                link your username
                            </span>

                        </div>
                    </div>

                </div>


                <div class="row g-3">

                    <div class="col-lg-7">

                        <div class="card p-3">

                            <h5 class="card-title-x">
                                🏆 Top Career Matches
                            </h5>

                            <div id="topCareers"></div>

                        </div>

                    </div>


                    <div class="col-lg-5">

                        <div class="card p-3">

                            <h5 class="card-title-x">
                                📊 Match Distribution
                            </h5>

                            <canvas id="careerChart"></canvas>

                        </div>

                    </div>

                </div>

            </section>


            <!-- PROFILE -->
            <section id="profile" class="page">

                <h2>
                    Student Profile
                </h2>

                <p class="text-muted">
                    Your profile powers recommendations and the skill-gap engine.
                </p>


                <div class="card p-4">

                    <div class="row g-3">


                        <!-- BRANCH -->
                        <div class="col-md-6">

                            <label class="form-label">
                                Branch / Course
                            </label>

                            <select
                                id="branch"
                                class="form-select"
                            >

                                <option value="">
                                    Select branch
                                </option>

                                <option>
                                    Computer Science & Engineering
                                </option>

                                <option>
                                    Information Technology
                                </option>

                                <option>
                                    Artificial Intelligence & Machine Learning
                                </option>

                                <option>
                                    Data Science
                                </option>

                                <option>
                                    Electronics & Communication Engineering
                                </option>

                                <option>
                                    Electrical Engineering
                                </option>

                                <option>
                                    Electrical & Electronics Engineering
                                </option>

                                <option>
                                    Electronics & Instrumentation Engineering
                                </option>

                                <option>
                                    Mechanical Engineering
                                </option>

                                <option>
                                    Automobile Engineering
                                </option>

                                <option>
                                    Civil Engineering
                                </option>

                                <option>
                                    Other
                                </option>

                            </select>

                        </div>


                        <!-- CURRENT YEAR -->
                        <div class="col-md-6">

                            <label class="form-label">
                                Current year
                            </label>

                            <select
                                id="currentYear"
                                class="form-select"
                            >

                                <option value="1">
                                    1st Year
                                </option>

                                <option value="2">
                                    2nd Year
                                </option>

                                <option value="3">
                                    3rd Year
                                </option>

                                <option value="4">
                                    4th Year
                                </option>

                            </select>

                        </div>


                        <!-- INTERESTS -->
                        <div class="col-12">

                            <label class="form-label">
                                Interests
                            </label>

                            <div class="multi-select">

                                <button
                                    class="form-select text-start multi-button"
                                    type="button"
                                    id="interestBtn"
                                >
                                    Select interests
                                </button>

                                <div
                                    class="multi-menu"
                                    id="interestMenu"
                                ></div>

                            </div>

                            <div class="form-text">
                                You can select multiple interests.
                            </div>

                        </div>


                        <!-- SKILLS -->
                        <div class="col-12">

                            <label class="form-label">
                                Skills
                            </label>

                            <div class="multi-select">

                                <button
                                    class="form-select text-start multi-button"
                                    type="button"
                                    id="skillBtn"
                                >
                                    Select skills
                                </button>

                                <div
                                    class="multi-menu skill-menu"
                                    id="skillMenu"
                                ></div>

                            </div>

                            <div class="form-text">
                                You can select multiple skills and rate your current level for each one.
                            </div>

                            <div class="skill-level-info mt-2">

                                <b>
                                    Skill level guide:
                                </b>

                                <span>
                                    1 = Beginner
                                </span>

                                <span>
                                    2 = Basic
                                </span>

                                <span>
                                    3 = Intermediate
                                </span>

                                <span>
                                    4 = Advanced
                                </span>

                                <span>
                                    5 = Proficient
                                </span>

                            </div>

                        </div>


                        <!-- CGPA -->
                        <div class="col-12">

                            <h5 class="mt-3">
                                Year-wise CGPA
                            </h5>

                            <p class="text-muted small">
                                Only completed academic years are shown.
                            </p>

                            <div id="cgpaRows"></div>

                        </div>

                    </div>


                    <div class="profile-actions mt-3">

                        <button
                            class="btn btn-primary"
                            id="saveProfile"
                        >
                            Save profile
                        </button>

                        <span
                            id="profileMsg"
                            class="ms-2"
                        ></span>

                    </div>

                </div>

            </section>


            <!-- ASSESSMENT -->
            <section id="assessment" class="page">

                <h2>
                    Aptitude & Interest Assessment
                </h2>

                <p class="text-muted">
                    Rate yourself honestly from 0–100.
                </p>

                <div
                    class="card p-4"
                    id="questions"
                ></div>

            </section>


            <!-- CAREERS -->
            <section id="careers" class="page">

                <h2>
                    Career Recommendations
                </h2>

                <p class="text-muted mb-1">
                    Careers are chosen for your branch – core careers of your branch come first.
                </p>

                <p class="small mb-3" id="careerBranchNote"></p>

                <div
                    id="careerGrid"
                    class="row g-3"
                ></div>

            </section>


            <!-- SKILL GAP -->
            <section id="gap" class="page">

                <h2>
                    Accurate Skill Gap Analysis
                </h2>

                <p class="text-muted">
                    Importance, current level and gap are calculated for the selected career.
                </p>

                <div class="card p-4">

                    <select
                        id="gapCareer"
                        class="form-select mb-3"
                    ></select>

                    <div id="gapResults"></div>

                </div>

            </section>


            <!-- ROADMAP -->
            <section id="roadmap" class="page">

                <h2>
                    Semester-wise Learning Roadmap
                </h2>

                <p class="text-muted">
                    A plan from your current semester until the end of final year: what to learn,
                    what to build, which certifications to earn and when to apply for internships and placements.
                </p>

                <select
                    id="roadCareer"
                    class="form-select mb-3"
                ></select>

                <div id="roadmapResults"></div>

            </section>



            <!-- LEETCODE PRACTICE -->
            <section id="practice" class="page">

                <div class="d-flex flex-wrap justify-content-between align-items-end gap-2">
                    <div>
                        <h2>LeetCode Practice &amp; Streaks</h2>
                        <p class="text-muted mb-0">
                            Practise the problems that matter for your career, keep a daily streak
                            and let your progress update your skill gap.
                        </p>
                    </div>
                    <select id="practiceCareer" class="form-select practice-career"></select>
                </div>

                <div id="lcAlert" class="mt-3"></div>

                <!-- connect card -->
                <div class="card p-3 p-md-4 mt-3 lc-connect" id="lcConnect">
                    <div class="row g-3 align-items-center">
                        <div class="col-lg-6">
                            <h5 class="card-title-x mb-1">🔗 Connect your LeetCode account</h5>
                            <small class="text-muted">
                                Only your <b>public username</b> is used.
                                Never type your LeetCode password here.
                            </small>
                        </div>
                        <div class="col-lg-6">
                            <form id="lcForm" class="input-group" autocomplete="off">
                                <span class="input-group-text">leetcode.com/u/</span>
                                <input id="lcUsername" class="form-control" placeholder="your-username" maxlength="60" required>
                                <button id="lcLinkBtn" class="btn btn-primary" type="submit">Link</button>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- linked account bar -->
                <div class="card p-3 mt-3 d-none" id="lcAccount">
                    <div class="d-flex flex-wrap align-items-center gap-3">
                        <img id="lcAvatar" class="lc-avatar" alt="" src="">
                        <div class="me-auto">
                            <a id="lcProfileLink" target="_blank" rel="noopener" class="fw-bold fs-5 text-decoration-none"></a>
                            <small class="d-block text-muted" id="lcSynced"></small>
                        </div>
                        <a class="btn btn-warning" href="https://leetcode.com/problemset/" target="_blank" rel="noopener">Open LeetCode ↗</a>
                        <button class="btn btn-outline-primary" id="lcRefresh">⟳ Refresh</button>
                        <button class="btn btn-outline-secondary" id="lcUnlink">Change account</button>
                    </div>
                </div>

                <!-- stats -->
                <div class="row g-3 mt-1 d-none" id="lcStats">
                    <div class="col-6 col-lg-3">
                        <div class="stat s-indigo">
                            <div class="stat-icon">✅</div>
                            <small>Problems Solved</small>
                            <b id="lcSolved">0</b>
                            <span id="lcSolvedSplit"></span>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat s-pink">
                            <div class="stat-icon">🔥</div>
                            <small>CareerVerse Streak</small>
                            <b id="lcStreak">0 days</b>
                            <span id="lcLongest"></span>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat s-green">
                            <div class="stat-icon">📅</div>
                            <small>This Week</small>
                            <b id="lcWeek">0 / 5 days</b>
                            <span id="lcWeekNote"></span>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat s-amber">
                            <div class="stat-icon">🗄️</div>
                            <small>SQL Problems</small>
                            <b id="lcSql">0</b>
                            <span id="lcRank"></span>
                        </div>
                    </div>
                </div>

                <!-- heatmap -->
                <div class="card p-3 mt-3 d-none" id="lcHeatCard">
                    <div class="d-flex flex-wrap justify-content-between align-items-center mb-2">
                        <h5 class="card-title-x mb-0">🟩 Activity in the last 12 months</h5>
                        <small class="text-muted" id="lcHeatTotal"></small>
                    </div>
                    <div class="heatmap-wrap">
                        <div id="lcHeatmap" class="heatmap"></div>
                    </div>
                    <div class="heat-legend">
                        Less <i class="h0"></i><i class="h1"></i><i class="h2"></i><i class="h3"></i><i class="h4"></i> More
                    </div>
                </div>

                <div class="row g-3 mt-1">
                    <!-- roadmap targets -->
                    <div class="col-lg-7" id="lcTargetCol">
                        <div class="card p-3 h-100">
                            <h5 class="card-title-x">🎯 Roadmap Practice Targets</h5>
                            <small class="text-muted mb-2" id="lcTargetNote">
                                Targets are counted from the day you link LeetCode.
                            </small>
                            <div id="lcTargets"></div>
                        </div>
                    </div>

                    <!-- skill sync + recent -->
                    <div class="col-lg-5" id="lcSideCol">
                        <div class="card p-3 mb-3 d-none" id="lcSkillCard">
                            <h5 class="card-title-x">📊 Update My Skill Gap</h5>
                            <small class="text-muted mb-2">Suggested levels worked out from your solved problems.</small>
                            <div id="lcSkills"></div>
                            <button class="btn btn-primary mt-2" id="lcSyncSkills">Update profile skills</button>
                        </div>
                        <div class="card p-3 d-none" id="lcRecentCard">
                            <h5 class="card-title-x">⏱️ Recently Solved</h5>
                            <div id="lcRecent"></div>
                        </div>
                    </div>
                </div>

                <!-- problem list -->
                <div class="card p-3 p-md-4 mt-3">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                        <h5 class="card-title-x mb-0" id="lcPracticeTitle">📚 Practice Problems</h5>
                        <div id="lcPlans" class="d-flex flex-wrap gap-2"></div>
                    </div>
                    <div id="lcProblems"></div>
                </div>

            </section>

            <!-- SIMULATOR -->
            <section id="simulator" class="page">

                <h2>
                    Detailed Career Simulator
                </h2>

                <p class="text-muted">
                    See the journey from your current position to internship/job readiness.
                </p>

                <select
                    id="simCareer"
                    class="form-select mb-3"
                ></select>

                <div id="simResults"></div>

            </section>


            <!-- REPORT -->
            <section id="report" class="page">

                <h2>
                    Career Report
                </h2>

                <div
                    id="reportBox"
                    class="card p-4"
                ></div>

            </section>


        </div>

    </main>

</div>


<!-- AI MODAL -->
<div
    class="modal fade"
    id="aiModal"
>

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">

                <h5 class="mb-0">
                    ✦ CareerVerse AI
                    <small class="d-block text-muted fs-6 fw-normal">Your personal career mentor</small>
                </h5>

                <button
                    id="chatClear"
                    class="btn btn-sm btn-outline-secondary ms-auto me-2"
                    type="button"
                >
                    New chat
                </button>

                <button
                    class="btn-close"
                    data-bs-dismiss="modal"
                ></button>

            </div>


            <div class="modal-body">

                <div
                    id="chatMessages"
                    class="chat"
                ></div>


                <div id="chatSuggestions" class="chat-suggestions mt-3">
                    <button type="button" class="chip">What should I learn next for my top career?</button>
                    <button type="button" class="chip">Suggest a project for my resume</button>
                    <button type="button" class="chip">Which LeetCode topics should I practise this week?</button>
                    <button type="button" class="chip">How do I prepare for internship interviews?</button>
                </div>

                <div class="input-group mt-3">

                    <input
                        id="chatInput"
                        maxlength="2000"
                        class="form-control"
                        placeholder="Ask anything about careers, skills, projects, interviews…"
                    >

                    <button
                        id="chatSend"
                        class="btn btn-primary"
                    >
                        Send
                    </button>

                </div>

            </div>

        </div>

    </div>

</div>


<!-- LOGOUT CONFIRMATION -->
<div class="modal fade" id="logoutModal" tabindex="-1" aria-labelledby="logoutTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content logout-modal">
            <div class="modal-body text-center p-4">
                <div class="logout-icon">👋</div>
                <h5 id="logoutTitle" class="fw-bold mt-3">Are you sure you want to log out?</h5>
                <p class="text-muted mb-4">You will need to log in again to see your career matches and roadmap.</p>
                <div class="d-flex gap-2 justify-content-center">
                    <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">Cancel</button>
                    <a href="../logout" class="btn btn-danger px-4">Yes, log out</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script src="../js/catalog.js"></script>

<script src="../js/app.js"></script>

<script src="../js/practice.js"></script>

</body>
</html>
