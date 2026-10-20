/* =========================================================
   LEETCODE PRACTICE PAGE
   Talks to /app/api/leetcode (LeetCodeServlet).
   Uses $, $$, api() and escapeHtml() from app.js.
   ========================================================= */

let practiceData = null;

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function lcAlert(message, type = 'info') {
    const box = $('#lcAlert');
    if (!box) return;
    box.innerHTML = message
        ? `<div class="alert alert-${type} alert-dismissible fade show mb-0" role="alert">
               ${escapeHtml(message)}
               <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
           </div>`
        : '';
}

function formatDate(isoDay) {
    const [y, m, d] = isoDay.split('-').map(Number);
    return `${d} ${MONTHS[m - 1]} ${y}`;
}

function timeAgo(iso) {
    if (!iso) return 'never';
    const minutes = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
    if (minutes < 1) return 'just now';
    if (minutes < 60) return `${minutes} min ago`;
    const hours = Math.round(minutes / 60);
    if (hours < 24) return `${hours} h ago`;
    return `${Math.round(hours / 24)} days ago`;
}

async function loadPractice(options = {}) {
    const career = $('#practiceCareer')?.value || '';
    const params = new URLSearchParams();
    if (career) params.set('career', career);
    if (options.refresh) params.set('refresh', '1');

    if (options.refresh) {
        $('#lcRefresh').disabled = true;
        $('#lcRefresh').textContent = 'Refreshing…';
    }

    try {
        practiceData = await api('api/leetcode?' + params.toString());
        renderPractice(practiceData);
        if (practiceData.warning) lcAlert(practiceData.warning, 'warning');
        updateHomeStreak(practiceData);
    } catch (error) {
        lcAlert(error.message || 'Could not load practice data.', 'danger');
    } finally {
        if ($('#lcRefresh')) {
            $('#lcRefresh').disabled = false;
            $('#lcRefresh').textContent = '⟳ Refresh';
        }
    }
}

function renderPractice(data) {
    const linked = !!data.linked;
    $('#lcConnect').classList.toggle('d-none', linked);
    ['#lcAccount', '#lcStats', '#lcHeatCard', '#lcSkillCard', '#lcRecentCard']
        .forEach(sel => $(sel).classList.toggle('d-none', !linked));
    $('#lcTargetCol').className = linked ? 'col-lg-7' : 'col-12';
    $('#lcSideCol').classList.toggle('d-none', !linked);

    $('#lcPracticeTitle').textContent = `📚 Practice Problems for ${data.career?.name || 'your career'}`;

    if (linked) {
        const stats = data.stats || null;
        $('#lcProfileLink').textContent = '@' + data.username;
        $('#lcProfileLink').href = data.profileUrl;
        $('#lcSynced').textContent = 'Last synced ' + timeAgo(data.lastSynced);
        const avatar = stats?.avatar;
        $('#lcAvatar').src = avatar || '../images/login-bg.png';
        $('#lcAvatar').classList.toggle('d-none', !avatar);

        const solved = stats?.solved || { all: 0, easy: 0, medium: 0, hard: 0 };
        $('#lcSolved').textContent = solved.all;
        $('#lcSolvedSplit').innerHTML =
            `<span class="diff-e">${solved.easy} E</span> · <span class="diff-m">${solved.medium} M</span> · <span class="diff-h">${solved.hard} H</span>`;

        const streak = data.streak || {};
        $('#lcStreak').textContent = `${streak.current || 0} day${streak.current === 1 ? '' : 's'}`;
        $('#lcLongest').textContent = streak.atRisk
            ? '⚠️ Solve one today to keep it!'
            : `Longest: ${streak.longest || 0} days`;

        const current = (data.targets || []).find(t => t.current);
        const goal = current ? current.weeklyDays : 5;
        $('#lcWeek').textContent = `${streak.activeDaysThisWeek || 0} / ${goal} days`;
        $('#lcWeekNote').textContent = `${streak.submissionsThisWeek || 0} submissions this week`;

        $('#lcSql').textContent = stats?.sqlSolved ?? 0;
        $('#lcRank').textContent = stats?.ranking ? `Rank #${Number(stats.ranking).toLocaleString('en-IN')}` : '';

        renderHeatmap(data.heatmap || [], data.today);
        renderSkills(data.suggestedSkills || []);
        renderRecent(stats?.recent || []);
    }

    renderTargets(data.targets || [], linked);
    renderProblems(data.practice || { sections: [], plans: [] });
}

/* ---------- heatmap (GitHub style) ---------- */

function heatLevel(count) {
    if (!count) return 0;
    if (count <= 2) return 1;
    if (count <= 5) return 2;
    if (count <= 9) return 3;
    return 4;
}

function renderHeatmap(cells, todayIso) {
    const counts = new Map(cells.map(([day, count]) => [day, count]));
    const today = todayIso ? new Date(todayIso + 'T00:00:00Z') : new Date();
    const start = new Date(today);
    start.setUTCDate(start.getUTCDate() - 364);
    start.setUTCDate(start.getUTCDate() - start.getUTCDay()); // back to Sunday

    let html = '';
    let months = '';
    let lastMonth = -1;
    let total = 0;
    let week = 0;

    for (let d = new Date(start); d <= today; d.setUTCDate(d.getUTCDate() + 1)) {
        const iso = d.toISOString().slice(0, 10);
        const count = counts.get(iso) || 0;
        total += count;
        if (d.getUTCDay() === 0) {
            if (d.getUTCMonth() !== lastMonth && d.getUTCDate() <= 7) {
                months += `<span style="grid-column:${week + 1}">${MONTHS[d.getUTCMonth()]}</span>`;
                lastMonth = d.getUTCMonth();
            }
            week++;
        }
        const label = `${count} submission${count === 1 ? '' : 's'} on ${formatDate(iso)}`;
        html += `<i class="h${heatLevel(count)}" title="${label}" style="grid-column:${week};grid-row:${d.getUTCDay() + 1}"></i>`;
    }

    $('#lcHeatmap').innerHTML = `<div class="heat-months" style="grid-template-columns:repeat(${week},minmax(0,1fr))">${months}</div>
        <div class="heat-grid" style="grid-template-columns:repeat(${week},minmax(0,1fr))">${html}</div>`;
    $('#lcHeatTotal').textContent = `${total} submissions · ${cells.filter(c => c[1] > 0).length} active days`;
}

/* ---------- targets ---------- */

function renderTargets(targets, linked) {
    $('#lcTargetNote').textContent = linked
        ? 'Counted from the day you linked LeetCode. Finish one stage to unlock the next.'
        : 'Link your LeetCode username to start tracking these targets.';

    $('#lcTargets').innerHTML = targets.map(stage => `
        <div class="lc-stage ${stage.current ? 'current' : ''} ${stage.complete ? 'complete' : ''}">
            <div class="d-flex justify-content-between align-items-center">
                <b>${stage.complete ? '✅' : stage.current ? '▶️' : '⏳'} ${escapeHtml(stage.stage)}
                    <span class="badge text-bg-light ms-1">${escapeHtml(stage.duration)}</span></b>
                <span class="fw-bold">${stage.percent}%</span>
            </div>
            ${stage.targets.map(t => `
                <div class="lc-target">
                    <div class="d-flex justify-content-between"><small>${escapeHtml(t.label)}</small>
                        <small>${t.done} / ${t.goal}</small></div>
                    <div class="progress" role="progressbar" aria-valuenow="${t.percent}" aria-valuemin="0" aria-valuemax="100">
                        <div class="progress-bar metric-${escapeHtml(t.metric)}" style="width:${t.percent}%"></div>
                    </div>
                </div>`).join('')}
            <small class="text-muted">Weekly target: about ${stage.problemsPerWeek} problems on ${stage.weeklyDays} days a week</small>
        </div>`).join('');
}

/* ---------- skill-gap link ---------- */

function renderSkills(list) {
    if (!list.length) {
        $('#lcSkills').innerHTML = '<small class="text-muted">Solve a few problems and refresh to get suggestions.</small>';
        $('#lcSyncSkills').classList.add('d-none');
        return;
    }
    $('#lcSkills').innerHTML = list.map(s => `
        <div class="d-flex justify-content-between align-items-center border-bottom py-2">
            <div><b>${escapeHtml(s.skill)}</b><small class="d-block text-muted">${escapeHtml(s.reason)}</small></div>
            <div class="text-end text-nowrap">
                <span class="badge text-bg-light">Now ${s.current || 0}/5</span>
                ${s.canRaise ? `→ <span class="badge text-bg-success">${s.suggested}/5</span>` : '<span class="badge text-bg-secondary">✓</span>'}
            </div>
        </div>`).join('');
    $('#lcSyncSkills').classList.toggle('d-none', !list.some(s => s.canRaise));
}

function renderRecent(list) {
    $('#lcRecent').innerHTML = list.length
        ? list.slice(0, 8).map(r => `
            <a class="lc-recent" href="https://leetcode.com/problems/${encodeURIComponent(r.slug)}/" target="_blank" rel="noopener">
                <span>✔ ${escapeHtml(r.title)}</span>
                <small class="text-muted">${new Date(r.timestamp * 1000).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}</small>
            </a>`).join('')
        : '<small class="text-muted">No accepted submissions yet.</small>';
}

/* ---------- problems ---------- */

function renderProblems(practice) {
    $('#lcPlans').innerHTML = (practice.plans || []).map(p =>
        `<a class="btn btn-sm btn-outline-warning" href="${escapeHtml(p.url)}" target="_blank" rel="noopener">${escapeHtml(p.name)} ↗</a>`
    ).join('');

    $('#lcProblems').innerHTML = (practice.sections || []).map(section => {
        const done = section.problems.filter(p => p.solved).length;
        return `
        <div class="lc-section">
            <div class="d-flex justify-content-between"><h6 class="fw-bold mb-2">${escapeHtml(section.name)}</h6>
                <small class="text-muted">${done}/${section.problems.length} solved</small></div>
            <div class="lc-problems">
                ${section.problems.map(p => `
                    <a class="lc-problem ${p.solved ? 'solved' : ''}" href="${escapeHtml(p.url)}" target="_blank" rel="noopener">
                        <span>${p.solved ? '✅' : '⬜'} ${escapeHtml(p.title)}</span>
                        <span class="badge diff-badge ${escapeHtml(p.difficulty.toLowerCase())}">${escapeHtml(p.difficulty)}</span>
                    </a>`).join('')}
            </div>
        </div>`;
    }).join('');
}

/* ---------- home + roadmap integration ---------- */

function updateHomeStreak(data) {
    if (!$('#statStreak')) return;
    if (!data || !data.linked) {
        $('#statStreak').textContent = '—';
        $('#statStreakNote').textContent = 'link your LeetCode';
        return;
    }
    const s = data.streak || {};
    $('#statStreak').textContent = `${s.current || 0} day${s.current === 1 ? '' : 's'}`;
    $('#statStreakNote').textContent = s.atRisk ? 'solve one today!' : `${data.stats?.solved?.all ?? 0} solved on LeetCode`;
}

async function loadHomeStreak() {
    try {
        updateHomeStreak(await api('api/leetcode?cached=1'));
    } catch {
        updateHomeStreak(null);
    }
}

/** Adds the LeetCode target of each stage under the matching roadmap card. */
async function addRoadmapTargets(careerId) {
    try {
        const data = await api('api/leetcode?cached=1&career=' + encodeURIComponent(careerId));
        const cards = $$('#roadmapResults .road');
        (data.targets || []).forEach((stage, i) => {
            if (!cards[i]) return;
            const box = document.createElement('div');
            box.className = 'road-lc mt-3';
            box.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <b>💻 LeetCode target</b>
                    <small>${data.linked ? stage.percent + '% done' : '<a href="#" data-goto-practice>Link LeetCode</a>'}</small>
                </div>
                ${stage.targets.map(t => `
                    <small class="d-flex justify-content-between mt-1"><span>${escapeHtml(t.label)}</span><span>${t.done} / ${t.goal}</span></small>
                    <div class="progress progress-thin"><div class="progress-bar metric-${escapeHtml(t.metric)}" style="width:${t.percent}%"></div></div>`).join('')}
                <small class="text-muted d-block mt-1">≈ ${stage.problemsPerWeek} problems a week, on ${stage.weeklyDays} days</small>`;
            cards[i].appendChild(box);
        });
        $$('[data-goto-practice]').forEach(a => a.addEventListener('click', e => { e.preventDefault(); showPage('practice'); }));
    } catch (error) {
        console.warn('Roadmap LeetCode targets unavailable:', error);
    }
}

/* ---------- events ---------- */

$('#practiceCareer')?.addEventListener('change', () => loadPractice());

$('#lcForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const username = $('#lcUsername').value.trim();
    if (!username) return;
    const button = $('#lcLinkBtn');
    button.disabled = true;
    button.textContent = 'Checking…';
    try {
        const result = await api('api/leetcode', {
            method: 'POST',
            body: JSON.stringify({ action: 'link', username })
        });
        lcAlert(result.warning || result.message, result.warning ? 'warning' : 'success');
        $('#lcUsername').value = '';
        await loadPractice();
    } catch (error) {
        lcAlert(error.message, 'danger');
    } finally {
        button.disabled = false;
        button.textContent = 'Link';
    }
});

$('#lcRefresh')?.addEventListener('click', () => loadPractice({ refresh: true }));

$('#lcUnlink')?.addEventListener('click', async () => {
    if (!confirm('Remove this LeetCode account from CareerVerse? Your saved streak history will be cleared.')) return;
    try {
        const result = await api('api/leetcode', { method: 'POST', body: JSON.stringify({ action: 'unlink' }) });
        lcAlert(result.message, 'info');
        await loadPractice();
    } catch (error) {
        lcAlert(error.message, 'danger');
    }
});

$('#lcSyncSkills')?.addEventListener('click', async () => {
    try {
        const result = await api('api/leetcode', { method: 'POST', body: JSON.stringify({ action: 'sync-skills' }) });
        lcAlert(result.message + ' Open Skill Gap to see the change.', 'success');
        await loadPractice();
        if (typeof loadHome === 'function') loadHome();
    } catch (error) {
        lcAlert(error.message, 'danger');
    }
});
