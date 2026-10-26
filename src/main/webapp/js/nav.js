/* =========================================================
   CareerVerse AI - browser Back / Forward support
   ---------------------------------------------------------
   The dashboard is one page that shows and hides sections.
   This file records every section the student opens in the
   browser history, so the Back button (and the phone's back
   gesture) returns to the previous section instead of
   leaving the app. Back also closes the AI chat window.
   Refreshing keeps the open section (dashboard.jsp#/roadmap).
   Load this file AFTER app.js and practice.js.
   ========================================================= */
(function () {

    if (typeof window.showPage !== 'function') {
        return;
    }

    const openPage = window.showPage;      // the original function from app.js
    const chat = document.getElementById('aiModal');
    let restoring = false;                 // true while Back/Forward is handled
    let closingByBack = false;             // chat closed by the Back button

    function isPage(id) {
        const el = id && document.getElementById(id);
        return !!el && el.classList.contains('page');
    }

    function activePage() {
        const el = document.querySelector('.page.active');
        return el ? el.id : 'home';
    }

    function hashPage() {
        return decodeURIComponent(location.hash.replace(/^#\/?/, ''));
    }

    function show(id) {
        restoring = true;
        try {
            window.showPage(id);
        } finally {
            restoring = false;
        }
    }

    // Every call to showPage (menu, buttons, links) also adds a history entry.
    window.showPage = function (id) {
        openPage.apply(this, arguments);

        if (restoring || !isPage(id)) {
            return;
        }
        if (!history.state || history.state.page !== id || history.state.chat) {
            history.pushState({ page: id }, '', '#/' + id);
        }
    };

    // Back / Forward pressed.
    window.addEventListener('popstate', event => {
        const state = event.state || {};

        // Leaving the chat entry: close the chat window.
        if (chat && chat.classList.contains('show') && !state.chat) {
            closingByBack = true;
            bootstrap.Modal.getOrCreateInstance(chat).hide();
        }

        const id = state.page || hashPage();
        if (!isPage(id) || id === activePage()) {
            return;            // nothing to change (or a plain "#" link)
        }
        show(id);
    });

    // AI chat window: opening it adds a history entry, so Back closes it.
    if (chat && window.bootstrap) {
        chat.addEventListener('shown.bs.modal', () => {
            if (!history.state || !history.state.chat) {
                history.pushState({ page: activePage(), chat: true }, '', '#/' + activePage());
            }
        });
        chat.addEventListener('hidden.bs.modal', () => {
            if (closingByBack) {
                closingByBack = false;
                return;
            }
            if (history.state && history.state.chat) {
                history.back();    // closed with X / Esc: drop the chat entry
            }
        });
    }

    // First load: remember the starting page and reopen it after a refresh.
    const start = isPage(hashPage()) ? hashPage() : 'home';
    history.replaceState({ page: start }, '', '#/' + start);
    if (start !== 'home') {
        show(start);
    }

    // Shown again from the browser's back-forward cache: reload fresh data.
    window.addEventListener('pageshow', event => {
        if (event.persisted) {
            location.reload();
        }
    });

})();