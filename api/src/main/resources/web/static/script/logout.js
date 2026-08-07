const translations = {
    en: {
        title: "FlectonePulse Editor",
        sessionTerminated: "Session Terminated",
        successLogout: "Logged out successfully",
        tokenExpired: "Your token is no longer valid or has expired",
        logoutLink: "Please get a new link using",
    },
    ru: {
        title: "Редактор FlectonePulse",
        sessionTerminated: "Сессия завершена",
        successLogout: "Успешный выход из системы",
        tokenExpired: "Ваш токен больше недействителен или истёк",
        logoutLink: "Пожалуйста, получите новую ссылку с помощью",
    }
};

function getCurrentLanguage() {
    const preferredLanguages = navigator.languages || [navigator.language];
    return preferredLanguages.some(lang => lang.startsWith('ru')) ? 'ru' : 'en';
}

function initPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const message = urlParams.get('message');
    const lang = getCurrentLanguage();
    const locale = translations[lang];
    const icon = document.getElementById('logout-icon');
    const title = document.getElementById('logout-title');
    const messageEl = document.getElementById('logout-message');

    document.title = locale.title;

    if (message === 'logged_out') {
        title.textContent = locale.successLogout;
        icon.className = "fas fa-check-circle";
        icon.style.color = "#42f5c5";
    } else {
        title.textContent = message === 'token_expired' ? locale.tokenExpired : locale.sessionTerminated;
        icon.className = "fas fa-exclamation-triangle";
        icon.style.color = "#ff5e7d";
    }

    messageEl.innerHTML = `${locale.logoutLink} <a class="clickable-link" href="#" onclick="copyToClipboard(event, '/flectonepulse editor')">/flectonepulse editor</a>`;
}

function copyToClipboard(event, text) {
    event.preventDefault();
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
}

document.addEventListener('DOMContentLoaded', initPage);