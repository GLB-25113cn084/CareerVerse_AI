/* =========================================================
   CareerVerse AI - Dark / Light mode switch
   ---------------------------------------------------------
   Dark mode  = "Nebula" theme   (css/theme.css)
   Light mode = warm light theme (css/theme-light.css)
   The choice is saved in the browser, so it is remembered
   the next time the student opens the site.
   Load this file in <head>, after the two theme stylesheets
   and after Chart.js.
   ========================================================= */
(function () {

    const KEY = 'cv-theme';

    function savedMode() {
        try {
            return localStorage.getItem(KEY) === 'light' ? 'light' : 'dark';
        } catch (e) {
            return 'dark';
        }
    }

    function saveMode(mode) {
        try {
            localStorage.setItem(KEY, mode);
        } catch (e) {
            /* private window - the choice just isn't remembered */
        }
    }

    function chartColours(dark) {
        if (!window.Chart) {
            return;
        }
        const bar = dark ? 'rgba(255, 94, 138, 0.85)' : 'rgba(165, 87, 4, 0.85)';
        Chart.defaults.color = dark ? '#a99fc9' : '#7a5a3a';
        Chart.defaults.borderColor = dark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(165, 87, 4, 0.12)';
        Chart.defaults.backgroundColor = bar;

        // Recolour charts that are already on the page.
        Object.values(Chart.instances || {}).forEach(chart => {
            chart.data.datasets.forEach(ds => { ds.backgroundColor = bar; });
            chart.update();
        });
    }

    function applyMode(mode) {
        const dark = mode !== 'light';
        const root = document.documentElement;
        root.setAttribute('data-bs-theme', dark ? 'dark' : 'light');
        root.setAttribute('data-cv-theme', dark ? 'dark' : 'light');

        const darkCss = document.getElementById('darkThemeCss');
        const lightCss = document.getElementById('lightThemeCss');
        if (darkCss) darkCss.disabled = !dark;
        if (lightCss) lightCss.disabled = dark;

        chartColours(dark);

        const sw = document.getElementById('themeSwitch');
        if (sw) sw.checked = dark;
        const label = document.getElementById('themeLabel');
        if (label) label.textContent = dark ? '🌙 Dark mode' : '☀️ Light mode';
    }

    // Apply straight away (in <head>) so the page never flashes the wrong theme.
    applyMode(savedMode());

    document.addEventListener('DOMContentLoaded', () => {
        applyMode(savedMode());

        const sw = document.getElementById('themeSwitch');
        const row = document.getElementById('themeRow');

        if (sw) {
            sw.addEventListener('change', () => {
                const mode = sw.checked ? 'dark' : 'light';
                saveMode(mode);
                applyMode(mode);
            });
        }

        // Keep the dropdown open while the switch is used.
        if (row) {
            row.addEventListener('click', event => event.stopPropagation());
        }
    });

    window.cvSetTheme = mode => {
        saveMode(mode);
        applyMode(mode);
    };

})();