const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

let careers = [];
let profile = {};
let chart = null;

const skills = [
    'Java',
    'Python',
    'JavaScript',
    'SQL',
    'HTML',
    'CSS',
    'Bootstrap',
    'DSA',
    'OOP',
    'REST APIs',
    'Git',
    'Testing',
    'Statistics',
    'Linear Algebra',
    'Machine Learning',
    'Pandas',
    'NumPy',
    'Excel',
    'Power BI',
    'Data Visualization',
    'Figma',
    'Design Thinking',
    'User Research',
    'Prototyping',
    'Kotlin',
    'Flutter',
    'Dart',
    'APIs',
    'SQLite',
    'Linux',
    'Networking',
    'Cybersecurity',
    'Cryptography',
    'Web Security',
    'Docker',
    'CI/CD',
    'Cloud',
    'Bash',
    'Databases',
    'Analytics',
    'Research',
    'Communication',
    'Product Thinking',
    'Selenium',
    'API Testing',
    'Technical Writing',
    'Markdown',
    'API Documentation'
];

const interests = [
    'Software Development',
    'AI / ML',
    'Data Analytics',
    'Web Development',
    'Cybersecurity',
    'UI/UX',
    'Mobile Development',
    'Cloud',
    'DevOps',
    'Product',
    'Testing',
    'Technical Writing',
    'Research',
    'Problem Solving',
    'Entrepreneurship'
];


/* =========================================================
   HTML SAFETY
   ========================================================= */

function escapeHtml(value) {

    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}


/* =========================================================
   API HELPER
   ========================================================= */

async function api(url, options = {}) {

    const response = await fetch(
        url,
        {
            ...options,
            headers: {
                ...(options.body
                    ? { 'Content-Type': 'application/json' }
                    : {}),
                ...(options.headers || {})
            }
        }
    );

    const text = await response.text();

    let data;

    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = {};
    }

    if (!response.ok) {

        throw new Error(
            data.error ||
            data.message ||
            `Request failed (${response.status})`
        );
    }

    return data;
}


/* =========================================================
   PAGE NAVIGATION
   ========================================================= */

function showPage(id) {

    $$('.page').forEach(page => {

        page.classList.toggle(
            'active',
            page.id === id
        );

    });

    $$('.navbtn').forEach(button => {

        button.classList.toggle(
            'active',
            button.dataset.page === id
        );

    });

    /*
     * Close mobile sidebar after navigation.
     */
    $('.sidebar')?.classList.remove('open');


    if (id === 'profile') {
        loadProfile();
    }

    if (id === 'careers') {
        renderCareers();
    }

    if (id === 'gap') {
        loadCareerSelect('gapCareer');
    }

    if (id === 'roadmap') {
        loadCareerSelect('roadCareer');
    }

    if (id === 'simulator') {
        loadCareerSelect('simCareer');
    }

    if (id === 'report') {
        loadReport();
    }

    window.scrollTo(0, 0);
}


$$('[data-page]').forEach(button => {

    button.onclick = () => {

        showPage(
            button.dataset.page
        );

    };

});


$('#menu')?.addEventListener(
    'click',
    () => {

        $('.sidebar')?.classList.toggle(
            'open'
        );

    }
);


/* =========================================================
   MULTI SELECT
   ========================================================= */

function fillMulti(
    menuId,
    buttonId,
    items,
    selected = [],
    selectedLevels = {}
) {

    const menu = $(menuId);
    const button = $(buttonId);

    if (!menu || !button) {
        return;
    }

    const selectedSet =
        new Set(
            selected.map(
                x => String(x).trim()
            )
        );


    menu.innerHTML =
        items.map(item => {

            const checked =
                selectedSet.has(item)
                    ? 'checked'
                    : '';


            /*
             * SKILLS
             */

            if (menuId === '#skillMenu') {

                const level =
                    selectedLevels[item] ?? 3;

                return `
                    <label>

                        <input
                            type="checkbox"
                            value="${escapeHtml(item)}"
                            ${checked}
                        >

                        <span>
                            ${escapeHtml(item)}
                        </span>

                        <select
                            class="skill-level"
                            data-skill="${escapeHtml(item)}"
                            title="Select your current skill level"
                        >

                            ${[1, 2, 3, 4, 5].map(n => `

                                <option
                                    value="${n}"
                                    ${Number(level) === n ? 'selected' : ''}
                                >
                                    ${n}
                                </option>

                            `).join('')}

                        </select>

                    </label>
                `;
            }


            /*
             * INTERESTS
             */

            return `
                <label>

                    <input
                        type="checkbox"
                        value="${escapeHtml(item)}"
                        ${checked}
                    >

                    <span>
                        ${escapeHtml(item)}
                    </span>

                </label>
            `;

        }).join('');


    /*
     * Update the text shown on the button.
     */

    function updateButton() {

        const values =
            [...menu.querySelectorAll(
                'input[type="checkbox"]:checked'
            )].map(
                x => x.value
            );


        if (values.length === 0) {

            button.textContent =
                menuId === '#skillMenu'
                    ? 'Select skills'
                    : 'Select interests';

            return;
        }


        /*
         * Instead of making the profile area wider,
         * show a compact count when many items are selected.
         */

        if (values.length <= 2) {

            button.textContent =
                values.join(', ');

        } else {

            button.textContent =
                `${values.length} selected`;

        }

        /*
         * Helpful tooltip containing all selected items.
         */

        button.title =
            values.join(', ');
    }


    /*
     * Open / close menu.
     */

    button.onclick = event => {

        event.stopPropagation();

        /*
         * Close other multi-select menus first.
         */

        $$('.multi-select.open').forEach(
            element => {

                if (
                    element !==
                    menu.parentElement
                ) {
                    element.classList.remove(
                        'open'
                    );
                }

            }
        );


        menu.parentElement.classList.toggle(
            'open'
        );

    };


    /*
     * Prevent clicks inside the menu from
     * closing the menu unexpectedly.
     */

    menu.onclick = event => {

        event.stopPropagation();

    };


    menu.addEventListener(
        'change',
        updateButton
    );


    updateButton();
}


/* =========================================================
   SKILL LEVEL EXPLANATION
   ========================================================= */

function addSkillLevelInfo() {

    const skillMenu =
        $('#skillMenu');

    if (!skillMenu) {
        return;
    }


    /*
     * Don't add it twice.
     */

    if (
        skillMenu.querySelector(
            '.skill-level-info'
        )
    ) {
        return;
    }


    const info =
        document.createElement('div');

    info.className =
        'skill-level-info mb-2';


    info.innerHTML = `
        <strong>
            What does the 1–5 level mean?
        </strong>

        <ul>

            <li>
                <strong>1 — Beginner:</strong>
                I have little or no experience.
            </li>

            <li>
                <strong>2 — Basic:</strong>
                I understand the fundamentals and can do simple tasks.
            </li>

            <li>
                <strong>3 — Intermediate:</strong>
                I can use this skill independently for normal projects.
            </li>

            <li>
                <strong>4 — Advanced:</strong>
                I can handle complex tasks and solve problems confidently.
            </li>

            <li>
                <strong>5 — Strong:</strong>
                I am highly confident and can build or work on advanced projects.
            </li>

        </ul>

        <div class="mt-2 text-muted">
            Tip: Choose the level that best represents your
            <b>current ability</b>, not the level you want to reach.
        </div>
    `;


    skillMenu.prepend(info);
}


/* =========================================================
   CGPA
   ========================================================= */

function renderCGPA(
    rows = [],
    currentYear = 1
) {

    const year =
        Number(currentYear);

    /*
     * Current year 1 -> 0 rows
     * Current year 2 -> 1 row
     * Current year 3 -> 2 rows
     * Current year 4 -> 3 rows
     */

    const completedYears =
        Math.max(
            0,
            year - 1
        );


    const container =
        $('#cgpaRows');

    if (!container) {
        return;
    }


    if (completedYears === 0) {

        container.innerHTML = `
            <div class="text-muted small">

                No completed academic year yet.
                CGPA will be added after your first year.

            </div>
        `;

        return;
    }


    container.innerHTML =
        Array.from(
            {
                length:
                    completedYears
            },
            (_, index) => {

                const academicYear =
                    index + 1;


                const old =
                    rows.find(
                        x =>
                            Number(x.year) ===
                            academicYear
                    );


                return `
                    <div class="row g-2 mb-2">

                        <div class="col-4">

                            <input
                                class="form-control"
                                value="Year ${academicYear}"
                                disabled
                            >

                        </div>

                        <div class="col-8">

                            <input
                                class="form-control cgpa"
                                data-year="${academicYear}"
                                type="number"
                                min="0"
                                max="10"
                                step="0.01"
                                placeholder="Enter CGPA"
                                value="${old?.cgpa ?? ''}"
                            >

                        </div>

                    </div>
                `;

            }
        ).join('');
}


/* =========================================================
   LOAD PROFILE
   ========================================================= */

async function loadProfile() {

    try {

        profile =
            await api(
                'api/profile'
            );


        $('#branch').value =
            profile.branch || '';


        $('#currentYear').value =
            profile.currentYear || 1;


        const selectedInterests =
            (profile.interests || '')
                .split(',')
                .map(
                    x => x.trim()
                )
                .filter(Boolean);


        const selectedSkills =
            (profile.skills || [])
                .map(
                    x => x.name
                );


        const selectedLevels = {};


        (profile.skills || [])
            .forEach(skill => {

                selectedLevels[
                    skill.name
                ] =
                    Number(
                        skill.level || 3
                    );

            });


        fillMulti(
            '#interestMenu',
            '#interestBtn',
            interests,
            selectedInterests
        );


        fillMulti(
            '#skillMenu',
            '#skillBtn',
            skills,
            selectedSkills,
            selectedLevels
        );


        addSkillLevelInfo();


        renderCGPA(
            profile.cgpa || [],
            profile.currentYear || 1
        );


    } catch (error) {

        console.error(
            'Could not load profile:',
            error
        );

    }
}


/* =========================================================
   CURRENT YEAR CHANGE
   ========================================================= */

$('#currentYear')?.addEventListener(
    'change',
    () => {

        renderCGPA(
            profile.cgpa || [],
            $('#currentYear').value
        );

    }
);


/* =========================================================
   SAVE PROFILE
   ========================================================= */

$('#saveProfile')?.addEventListener(
    'click',
    async () => {

        const button =
            $('#saveProfile');

        const message =
            $('#profileMsg');


        try {

            button.disabled = true;

            button.textContent =
                'Saving...';


            /*
             * Collect selected skills + levels.
             */

            const selectedSkills =
                [
                    ...document.querySelectorAll(
                        '#skillMenu input[type="checkbox"]:checked'
                    )
                ]
                    .map(input => {

                        const row =
                            input.closest('label');


                        const levelSelect =
                            row?.querySelector(
                                '.skill-level'
                            );


                        return {

                            name:
                                input.value,

                            level:
                                Number(
                                    levelSelect?.value || 3
                                )

                        };

                    });


            /*
             * Collect interests.
             */

            const selectedInterests =
                [
                    ...document.querySelectorAll(
                        '#interestMenu input[type="checkbox"]:checked'
                    )
                ]
                    .map(
                        input =>
                            input.value
                    );


            const currentYear =
                Number(
                    $('#currentYear').value
                );


            /*
             * Collect CGPA.
             */

            const cgpa =
                [
                    ...document.querySelectorAll(
                        '.cgpa'
                    )
                ]
                    .map(input => ({

                        year:
                            Number(
                                input.dataset.year
                            ),

                        cgpa:
                            Number(
                                input.value
                            )

                    }))
                    .filter(
                        x =>
                            x.cgpa >= 0 &&
                            x.cgpa <= 10 &&
                            x.cgpa !== 0
                    );


            const result =
                await api(
                    'api/profile',
                    {

                        method:
                            'POST',

                        body:
                            JSON.stringify({

                                branch:
                                    $('#branch').value,

                                currentYear,

                                interests:
                                    selectedInterests.join(','),

                                skills:
                                    selectedSkills,

                                cgpa

                            })

                    }
                );


            if (!result.ok) {

                throw new Error(
                    result.error ||
                    'Could not save profile.'
                );

            }


            profile =
                result.profile ||
                profile;


            message.textContent =
                'Profile saved ✓';


            message.classList.remove(
                'text-danger'
            );


            message.classList.add(
                'text-success'
            );


            await loadHome();


            setTimeout(
                () => {

                    message.textContent =
                        '';

                },
                3000
            );


        } catch (error) {

            console.error(error);


            message.textContent =
                'Could not save profile';


            message.classList.remove(
                'text-success'
            );


            message.classList.add(
                'text-danger'
            );


        } finally {

            button.disabled = false;

            button.textContent =
                'Save profile';

        }

    }
);


/* =========================================================
   APTITUDE QUESTIONS
   ========================================================= */

const questions = [

    [
        'Logical Reasoning',
        'logical'
    ],

    [
        'Programming',
        'programming'
    ],

    [
        'Communication',
        'communication'
    ],

    [
        'Mathematics',
        'mathematics'
    ],

    [
        'Creativity',
        'creativity'
    ],

    [
        'Analytical Thinking',
        'analytical'
    ]

];


if ($('#questions')) {

    $('#questions').innerHTML =
        questions.map(
            (q, i) => `

                <div class="mb-4">

                    <div class="d-flex justify-content-between">

                        <b>
                            ${i + 1}.
                            I enjoy
                            ${q[0].toLowerCase()}.
                        </b>

                        <span id="v${i}">
                            70
                        </span>

                    </div>

                    <input
                        class="form-range"
                        id="q${i}"
                        type="range"
                        min="0"
                        max="100"
                        value="70"
                    >

                </div>

            `
        ).join('') +

        `

            <button
                class="btn btn-primary"
                id="submitAssessment"
            >
                Save assessment & update matches
            </button>

            <span
                id="assessmentMsg"
                class="ms-2"
            ></span>

        `;


    questions.forEach(
        (_, i) => {

            $('#q' + i).oninput =
                event => {

                    $('#v' + i)
                        .textContent =
                        event.target.value;

                };

        }
    );


    $('#submitAssessment').onclick =
        async () => {

            const button =
                $('#submitAssessment');

            const message =
                $('#assessmentMsg');


            try {

                button.disabled = true;

                button.textContent =
                    'Saving...';


                const values =
                    Object.fromEntries(
                        questions.map(
                            (q, i) => [

                                q[1],

                                Number(
                                    $('#q' + i).value
                                )

                            ]
                        )
                    );


                await api(
                    'api/assessment',
                    {

                        method:
                            'POST',

                        body:
                            JSON.stringify(
                                values
                            )

                    }
                );


                message.textContent =
                    'Assessment saved ✓';


                message.className =
                    'text-success ms-2';


                await loadHome();


                showPage(
                    'careers'
                );


            } catch (error) {

                console.error(error);


                message.textContent =
                    'Could not save assessment';


                message.className =
                    'text-danger ms-2';


            } finally {

                button.disabled = false;

                button.textContent =
                    'Save assessment & update matches';

            }

        };

}


/* =========================================================
   CAREERS
   ========================================================= */

async function loadCareers() {

    if (careers.length) {
        return careers;
    }


    careers =
        await api(
            'api/careers'
        );


    return careers;
}


async function renderCareers() {

    try {

        await loadCareers();


        $('#careerGrid').innerHTML =
            careers.map(
                career => `

                    <div class="col-md-6 col-xl-4">

                        <div class="card career-card p-3">

                            <div class="fs-2">
                                ${escapeHtml(career.icon)}
                            </div>

                            <h5>
                                ${escapeHtml(career.name)}
                            </h5>

                            <p class="text-muted small">
                                ${escapeHtml(career.description)}
                            </p>

                            <div>

                                <b>
                                    Languages:
                                </b>

                                ${career.languages
                                    .map(escapeHtml)
                                    .join(', ')}

                            </div>

                            <div class="mt-2">

                                <b>
                                    Good projects:
                                </b>

                                ${career.projects
                                    .map(
                                        p =>
                                            escapeHtml(
                                                p.title
                                            )
                                    )
                                    .join(' · ')}

                            </div>

                            <button
                                class="btn btn-outline-primary mt-3"
                                onclick="selectCareer('${escapeHtml(career.id)}')"
                            >
                                Use this career
                            </button>

                        </div>

                    </div>

                `
            ).join('');


    } catch (error) {

        console.error(error);


        $('#careerGrid').innerHTML = `

            <div class="alert alert-danger">

                Could not load career recommendations.
                Please refresh and try again.

            </div>

        `;

    }
}


/* =========================================================
   CAREER SELECT
   ========================================================= */

async function loadCareerSelect(id) {

    try {

        await loadCareers();


        const select =
            $('#' + id);


        if (!select) {
            return;
        }


        const previous =
            select.value;


        select.innerHTML =
            careers.map(
                career =>
                    `
                        <option value="${escapeHtml(career.id)}">

                            ${escapeHtml(
                                career.name
                            )}

                        </option>
                    `
            ).join('');


        if (
            previous &&
            careers.some(
                c =>
                    c.id === previous
            )
        ) {

            select.value =
                previous;

        }


        if (id === 'gapCareer') {
            await loadGap();
        }


        if (id === 'roadCareer') {
            await loadRoadmap();
        }


        if (id === 'simCareer') {
            await loadSimulator();
        }


    } catch (error) {

        console.error(
            'Could not load career select:',
            error
        );

    }
}


window.selectCareer =
    async id => {

        showPage(
            'gap'
        );


        await loadCareerSelect(
            'gapCareer'
        );


        $('#gapCareer').value =
            id;


        await loadGap();

    };


$('#gapCareer')?.addEventListener(
    'change',
    loadGap
);


/* =========================================================
   SKILL GAP
   ========================================================= */

async function loadGap() {

    const id =
        $('#gapCareer')?.value;


    if (!id) {
        return;
    }


    try {

        const data =
            await api(
                'api/career?id=' +
                encodeURIComponent(id)
            );


        $('#gapResults').innerHTML =
            data.skillGap.map(
                item => {

                    let badgeClass =
                        'text-bg-warning';


                    if (
                        item.status ===
                        'Strong'
                    ) {

                        badgeClass =
                            'text-bg-success';

                    }


                    return `

                        <div class="row align-items-center mb-3">

                            <div class="col-md-3">

                                <b>
                                    ${escapeHtml(
                                        item.skill
                                    )}
                                </b>

                                <small class="d-block text-muted">

                                    Importance
                                    ${item.importance}%

                                </small>

                            </div>


                            <div class="col-md-6">

                                <div class="gapbar">

                                    <i
                                        style="width:${item.current}%"
                                    ></i>

                                </div>

                                <small>

                                    Current
                                    ${item.current}%
                                    · Gap
                                    ${item.gap}%

                                </small>

                            </div>


                            <div class="col-md-3">

                                <span
                                    class="badge ${badgeClass}"
                                >
                                    ${escapeHtml(
                                        item.status
                                    )}
                                </span>

                            </div>

                        </div>

                    `;

                }
            ).join('');


    } catch (error) {

        console.error(error);


        $('#gapResults').innerHTML = `

            <div class="alert alert-danger">

                Could not load skill-gap analysis.

            </div>

        `;

    }
}


/* =========================================================
   ROADMAP
   ========================================================= */

$('#roadCareer')?.addEventListener(
    'change',
    loadRoadmap
);


async function loadRoadmap() {

    const id =
        $('#roadCareer')?.value;


    if (!id) {
        return;
    }


    try {

        const data =
            await api(
                'api/career?id=' +
                encodeURIComponent(id)
            );


        $('#roadmapResults').innerHTML =
            data.roadmap.map(
                (stage, index) => `

                    <div class="road">

                        <h5>

                            ${index + 1}.
                            ${escapeHtml(
                                stage.title
                            )}

                            <span class="badge text-bg-light">

                                ${escapeHtml(
                                    stage.duration
                                )}

                            </span>

                        </h5>


                        <p>
                            ${escapeHtml(
                                stage.goal
                            )}
                        </p>


                        <b>
                            Languages/tools:
                        </b>

                        ${stage.languages
                            .map(escapeHtml)
                            .join(', ')}


                        <ul>

                            ${stage.tasks
                                .map(
                                    task =>
                                        `
                                            <li>
                                                ${escapeHtml(
                                                    task
                                                )}
                                            </li>
                                        `
                                )
                                .join('')}

                        </ul>


                        <b>
                            Hiring checkpoint:
                        </b>

                        ${escapeHtml(
                            stage.checkpoint
                        )}


                        <div class="mt-2">

                            <b>
                                Projects:
                            </b>

                            ${stage.projects
                                .map(
                                    project =>
                                        `
                                            ${escapeHtml(
                                                project.title
                                            )}
                                            (${escapeHtml(
                                                project.level
                                            )})
                                        `
                                )
                                .join(' · ')}

                        </div>

                    </div>

                `
            ).join('');


    } catch (error) {

        console.error(error);


        $('#roadmapResults').innerHTML = `

            <div class="alert alert-danger">

                Could not load learning roadmap.

            </div>

        `;

    }
}


/* =========================================================
   CAREER SIMULATOR
   ========================================================= */

$('#simCareer')?.addEventListener(
    'change',
    loadSimulator
);


async function loadSimulator() {

    const id =
        $('#simCareer')?.value;


    if (!id) {
        return;
    }


    try {

        const data =
            await api(
                'api/career?id=' +
                encodeURIComponent(id)
            );


        const total =
            data.simulator.length;


        $('#simResults').innerHTML =
            data.simulator.map(
                (stage, index) => {

                    const progress =
                        total > 0
                            ? Math.round(
                                ((index + 1) /
                                    total) *
                                100
                            )
                            : 0;


                    return `

                        <div class="sim-stage">

                            <div class="d-flex justify-content-between">

                                <h5>

                                    Stage ${index + 1}:
                                    ${escapeHtml(
                                        stage.title
                                    )}

                                </h5>


                                <span class="badge text-bg-primary">

                                    ${escapeHtml(
                                        stage.duration
                                    )}

                                </span>

                            </div>


                            <p>

                                <b>
                                    Goal:
                                </b>

                                ${escapeHtml(
                                    stage.goal
                                )}

                            </p>


                            <p>
                                <b>
                                    What you do:
                                </b>
                            </p>


                            <ul>

                                ${stage.tasks
                                    .map(
                                        task =>
                                            `
                                                <li>
                                                    ${escapeHtml(
                                                        task
                                                    )}
                                                </li>
                                            `
                                    )
                                    .join('')}

                            </ul>


                            <p>

                                <b>
                                    Checkpoint:
                                </b>

                                ${escapeHtml(
                                    stage.checkpoint
                                )}

                            </p>


                            <div class="progress">

                                <div
                                    class="progress-bar"
                                    style="width:${progress}%"
                                >

                                    ${progress}%

                                </div>

                            </div>

                        </div>

                    `;

                }
            ).join('');


    } catch (error) {

        console.error(error);


        $('#simResults').innerHTML = `

            <div class="alert alert-danger">

                Could not load career simulator.

            </div>

        `;

    }
}


/* =========================================================
   HOME DASHBOARD
   ========================================================= */

async function loadHome() {

    try {

        const p =
            await api(
                'api/profile'
            );


        profile = p;


        $('#statSkills').textContent =
            (p.skills || []).length;


        const recommendations =
            await api(
                'api/recommendations'
            );


        if (
            !Array.isArray(
                recommendations
            ) ||
            recommendations.length === 0
        ) {

            $('#statCareer').textContent =
                'Complete profile';


            $('#statMatch').textContent =
                '—';


            $('#topCareers').innerHTML = `

                <div class="text-muted">

                    Complete your profile and aptitude test
                    to receive career matches.

                </div>

            `;


            if (chart) {

                chart.destroy();
                chart = null;

            }


            return;
        }


        const top =
            recommendations[0];


        $('#statCareer').textContent =
            top.name;


        $('#statMatch').textContent =
            top.match + '% match';


        $('#topCareers').innerHTML =
            recommendations
                .slice(0, 5)
                .map(
                    career => `

                        <div
                            class="d-flex justify-content-between border-bottom py-2"
                        >

                            <span>

                                ${escapeHtml(
                                    career.icon
                                )}

                                ${escapeHtml(
                                    career.name
                                )}

                            </span>


                            <b>
                                ${career.match}%
                            </b>

                        </div>

                    `
                )
                .join('');


        const values =
            recommendations.slice(
                0,
                6
            );


        if (chart) {

            chart.destroy();
            chart = null;

        }


        if ($('#careerChart')) {

            chart =
                new Chart(
                    $('#careerChart'),
                    {

                        type: 'bar',

                        data: {

                            labels:
                                values.map(
                                    x => x.name
                                ),

                            datasets: [

                                {

                                    label:
                                        'Match %',

                                    data:
                                        values.map(
                                            x => x.match
                                        )

                                }

                            ]

                        },

                        options: {

                            responsive: true,

                            indexAxis: 'y'

                        }

                    }
                );

        }


    } catch (error) {

        console.error(
            'Home loading error:',
            error
        );


        $('#statCareer').textContent =
            'Unavailable';


        $('#statMatch').textContent =
            '—';


        $('#topCareers').innerHTML = `

            <div class="alert alert-warning">

                Career matches could not be loaded.
                Please try refreshing the page.

            </div>

        `;

    }
}


/* =========================================================
   CAREER REPORT
   ========================================================= */

async function loadReport() {

    try {

        const p =
            await api(
                'api/profile'
            );


        const recommendations =
            await api(
                'api/recommendations'
            );


        const top =
            recommendations[0];


        const skills =
            p.skills || [];


        const interests =
            p.interests ||
            'Not added';


        $('#reportBox').innerHTML = `

            <h4>
                CareerVerse AI Report
            </h4>


            <p>

                <b>
                    Branch:
                </b>

                ${escapeHtml(
                    p.branch ||
                    'Not added'
                )}

            </p>


            <p>

                <b>
                    Current year:
                </b>

                ${p.currentYear || 1}

            </p>


            <p>

                <b>
                    Top career:
                </b>

                ${escapeHtml(
                    top?.name ||
                    'Complete your profile'
                )}

            </p>


            <p>

                <b>
                    Match:
                </b>

                ${top?.match ?? '—'}%

            </p>


            <p>

                <b>
                    Skills tracked:
                </b>

                ${skills.length}

            </p>


            <p>

                <b>
                    Interests:
                </b>

                ${escapeHtml(
                    interests
                )}

            </p>


            <hr>


            <h5>
                Your Skills
            </h5>


            ${
                skills.length

                    ? `

                        <ul>

                            ${skills
                                .map(
                                    skill =>
                                        `
                                            <li>

                                                ${escapeHtml(
                                                    skill.name
                                                )}

                                                — Level
                                                ${skill.level}/5

                                            </li>
                                        `
                                )
                                .join('')}

                        </ul>

                    `

                    : `

                        <p class="text-muted">

                            No skills added yet.

                        </p>

                    `
            }


            <hr>


            <p>

                Use <b>Skill Gap</b> to identify
                what you need to improve.

            </p>


            <p>

                Use the <b>Learning Roadmap</b>
                and <b>Career Simulator</b>
                to plan your journey.

            </p>

        `;


    } catch (error) {

        console.error(error);


        $('#reportBox').innerHTML = `

            <div class="alert alert-danger">

                Could not generate your career report.
                Please complete your profile and try again.

            </div>

        `;

    }
}
async function ask(question) {

    const box =
        $('#chatMessages');


    if (!box) {
        return;
    }


    box.innerHTML += `

        <div class="msg user-msg">

            ${escapeHtml(
                question
            )}

        </div>

    `;


    /*
     * Loading message
     */

    box.innerHTML += `

        <div
            class="msg ai-msg"
            id="aiLoading"
        >

            CareerVerse AI is thinking...

        </div>

    `;


    box.scrollTop =
        box.scrollHeight;


    try {

        const result =
            await api(
                'api/ai',
                {

                    method:
                        'POST',

                    body:
                        JSON.stringify({

                            query:
                                question

                        })

                }
            );


        $('#aiLoading')?.remove();



        const answer =
            result.answer ||
            result.message ||
            'I could not generate an answer right now.';


        box.innerHTML += `

            <div class="msg ai-msg">

                ${escapeHtml(
                    answer
                )}

            </div>

        `;


    } catch (error) {

        console.error(
            'AI request failed:',
            error
        );

        $('#aiLoading')?.remove();

        box.innerHTML += `
            <div class="msg ai-msg">
                CareerVerse AI is currently unavailable.
                Please make sure Ollama is running and try again.
            </div>
        `;
    }   


    box.scrollTop =
        box.scrollHeight;
}

function openAI() {

    const modal =
        document.getElementById(
            'aiModal'
        );


    if (modal) {

        new bootstrap.Modal(
            modal
        ).show();


        setTimeout(
            () => {

                $('#chatInput')?.focus();

            },
            300
        );

    }
}


/* =========================================================
   AI SEARCH BOX
   ========================================================= */

$('#askAI')?.addEventListener(
    'click',
    () => {

        const question =
            $('#aiQuery')
                .value
                .trim();


        if (question) {

            openAI();

            ask(question);


            $('#aiQuery').value =
                '';

        }

    }
);


/* =========================================================
   OPEN AI FROM MENU
   ========================================================= */

$('#openAI')?.addEventListener(
    'click',
    openAI
);


/* =========================================================
   CHAT SEND
   ========================================================= */

$('#chatSend')?.addEventListener(
    'click',
    () => {

        const question =
            $('#chatInput')
                .value
                .trim();


        if (question) {

            ask(question);


            $('#chatInput').value =
                '';

        }

    }
);


/* =========================================================
   CHAT ENTER KEY
   ========================================================= */

$('#chatInput')?.addEventListener(
    'keydown',
    event => {

        if (
            event.key ===
            'Enter'
        ) {

            event.preventDefault();

            $('#chatSend').click();

        }

    }
);


/* =========================================================
   CLOSE MULTI-SELECT WHEN CLICKING OUTSIDE
   ========================================================= */

document.addEventListener(
    'click',
    () => {

        $$('.multi-select.open')
            .forEach(
                element => {

                    element.classList.remove(
                        'open'
                    );

                }
            );

    }
);


/* =========================================================
   INITIALIZATION
   ========================================================= */

(async function init() {

    /*
     * Load logged-in user.
     */

    try {

        const me =
            await api(
                '../api/me'
            );


        if (me) {

            $('#topName').textContent =
                me.name;


            $('#menuName').textContent =
                me.name;


            $('#avatar').textContent =
                me.name
                    .charAt(0)
                    .toUpperCase();

        }


    } catch (error) {

        console.error(
            'Could not load user:',
            error
        );

    }


    /*
     * Load careers.
     */

    try {

        await loadCareers();

    } catch (error) {

        console.error(
            'Could not load careers:',
            error
        );

    }


    /*
     * Load dashboard.
     */

    await loadHome();

})();