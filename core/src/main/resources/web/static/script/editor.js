const token = window.location.pathname.split('/')[2];
let editor = null;
let currentFile = { type: null, name: null };
let cursorPositions = {};

const translations = {
    en: {
        title: "FlectonePulse Editor",
        documentation: "Documentation",
        logout: "Logout",
        mainConfigs: "Main Configs",
        localizations: "Localizations",
        selectFile: "Select a file to edit",
        saveChanges: "Save changes",
        successSave: "File saved successfully. Don't forget to run /flectonepulse reload on the server",
        errorSave: "Error saving file:",
        commandCopied: "Command copied to clipboard",
        successLogout: "Logged out successfully",
        errorLogout: "Logout failed",
        sessionTerminated: "Session Terminated",
        tokenExpired: "Your token is no longer valid or has expired",
        logoutLink: "Please get a new link using"
    },
    ru: {
        title: "Редактор FlectonePulse",
        documentation: "Документация",
        logout: "Выйти",
        mainConfigs: "Основные файлы",
        localizations: "Локализации",
        selectFile: "Выберите файл для редактирования",
        saveChanges: "Сохранить изменения",
        successSave: "Файл успешно сохранён. Не забудьте выполнить /flectonepulse reload на сервере",
        errorSave: "Ошибка сохранения файла:",
        commandCopied: "Команда скопирована в буфер обмена",
        successLogout: "Успешный выход из системы",
        errorLogout: "Ошибка выхода из системы",
        sessionTerminated: "Сессия завершена",
        tokenExpired: "Ваш токен больше недействителен или истёк",
        logoutLink: "Пожалуйста, получите новую ссылку с помощью"
    }
};

function hasRussianLanguage() {
    const preferredLanguages = navigator.languages;
    return preferredLanguages.some(lang => lang.startsWith('ru'));
}

function getCurrentLanguage() {
    return hasRussianLanguage() ? 'ru' : 'en';
}

function translate(key) {
    const lang = getCurrentLanguage();
    return translations[lang][key] || `[${key} not found]`;
}

function initTranslations() {

    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        const translatedText = translate(key);
        el.innerHTML = el.innerHTML.replace(`{{${key}}}`, translatedText);
    });

    document.title = translate('title');
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    const copyBtn = document.getElementById('copy-btn');

    if (toast && toastMessage) {
        toastMessage.textContent = message;
        toast.className = `toast ${type} show`;

        if (message && message.includes('/flectonepulse reload')) {
            copyBtn.style.display = 'inline-block';
        } else {
            copyBtn.style.display = 'none';
        }

        setTimeout(() => {
            toast.className = `toast ${type}`;
        }, 5000);
    } else {
        console.error('Toast or toastMessage not found');
    }
}

let hoveredLinks = 0;
let isOverTooltip = false;
let showTimer = null;
let hideTimer = null;
let tooltip = null;

function showTooltip(url, start) {
    if (tooltip && tooltip.url === url) return;
    if (tooltip) {
        tooltip.remove();
        tooltip = null;
    }
    tooltip = document.createElement('div');
    tooltip.className = 'cm-tooltip';
    tooltip.url = url;

    const iframe = document.createElement('iframe');
    iframe.src = url;
    iframe.style.width = '800px';
    iframe.style.height = '500px';
    iframe.style.border = 'none';
    iframe.onerror = () => {
        iframe.style.display = 'none';
        const fallback = document.createElement('div');
        fallback.className = 'cm-tooltip-fallback';
        tooltip.appendChild(fallback);
    };
    tooltip.appendChild(iframe);

    const urlText = document.createElement('div');
    urlText.className = 'cm-tooltip-url';
    urlText.textContent = url;

    const pos = editor.charCoords(start, 'page');
    const tooltipHeight = 510;
    const tooltipWidth = 816;
    let top = pos.bottom + 5;
    let left = pos.left;
    if (top + tooltipHeight > window.innerHeight + window.scrollY) {
        top = pos.top - tooltipHeight - 5;
    }

    if (left + tooltipWidth > window.innerWidth + window.scrollX) {
        left = window.innerWidth + window.scrollX - tooltipWidth - 5;
    }

    if (left < window.scrollX) {
        left = window.scrollX + 5;
    }

    tooltip.style.top = top + 'px';
    tooltip.style.left = left + 'px';

    document.body.appendChild(tooltip);

    tooltip.addEventListener('mouseenter', () => {
        isOverTooltip = true;
        clearTimeout(hideTimer);
    });
    tooltip.addEventListener('mouseleave', () => {
        isOverTooltip = false;
        if (hoveredLinks === 0 && tooltip) {
            hideTimer = setTimeout(hideTooltip, 2000);
        }
    });
}

function hideTooltip() {
    if (tooltip) {
        tooltip.remove();
        tooltip = null;
        isOverTooltip = false;
    }
}

function updateLinks() {
    editor.operation(() => {
        const urlRE = /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)/g;
        editor.getAllMarks().forEach(mark => mark.clear());
        let text = editor.getValue();
        let match;
        while ((match = urlRE.exec(text)) !== null) {
            let start = editor.posFromIndex(match.index);
            let end = editor.posFromIndex(match.index + match[0].length);
            let url = match[0];

            const preferredLanguages = navigator.languages;
            const hasRussian = preferredLanguages.some(lang => lang.startsWith('ru'));
            if (!hasRussian) {
                url = url.replace("flectone.net/pulse/", "flectone.net/en/pulse/")
            }

            let link = document.createElement('a');
            link.href = url;
            link.textContent = url;
            link.className = 'cm-url';
            link.target = '_blank';
            link.addEventListener('mouseenter', () => {
                hoveredLinks++;
                clearTimeout(hideTimer);
                if (!tooltip || (tooltip && tooltip.url !== url)) {
                    if (tooltip) {
                        tooltip.remove();
                        tooltip = null;
                    }
                    showTimer = setTimeout(() => {
                        if (hoveredLinks > 0) {
                            showTooltip(url, start);
                        }
                    }, 300);
                }
            });
            link.addEventListener('mouseleave', () => {
                hoveredLinks--;
                clearTimeout(showTimer);
                if (hoveredLinks === 0 && !isOverTooltip && tooltip) {
                    hideTimer = setTimeout(hideTooltip, 2000);
                }
            });
            editor.markText(start, end, { replacedWith: link });
        }
    });
}

function initEditor() {
    const textarea = document.getElementById('yaml-editor');
    editor = CodeMirror.fromTextArea(textarea, {
        lineNumbers: true,
        mode: 'yaml',
        theme: 'dracula',
        indentUnit: 2,
        tabSize: 2,
        lineWrapping: true,
        autoCloseBrackets: true,
        matchBrackets: true,
        extraKeys: {
            'Ctrl-S': saveFile,
            'Cmd-S': saveFile,
            'Ctrl-F': function(cm) { cm.execCommand('findPersistent'); },
            'Cmd-F': function(cm) { cm.execCommand('findPersistent'); }
        }
    });

    editor.on('change', updateLinks);
    editor.on('cursorActivity', () => {
        if (tooltip) {
            hideTooltip();
        }
    });
    updateLinks();

    setTimeout(() => {
        editor.refresh();
    }, 100);
}

async function loadFile(fileType, fileName) {
    try {
        if (currentFile.type && currentFile.name) {
            const oldKey = `${currentFile.type}/${currentFile.name}`;
            const selections = editor.listSelections();
            const scrollInfo = editor.getScrollInfo();
            cursorPositions[oldKey] = {
                selections: selections,
                scrollTop: scrollInfo.top,
                scrollLeft: scrollInfo.left
            };
        }

        document.querySelectorAll('.file').forEach(el => {
            el.classList.remove('active');
        });

        const fileElement = document.querySelector(`.file[data-type="${fileType}"][data-name="${fileName}"]`);
        if (fileElement) fileElement.classList.add('active');

        document.getElementById('current-file').textContent = fileType === 'main' ? fileName : `${fileType}/${fileName}`;

        const response = await fetch(`/editor/file/${token}/${fileType}/${encodeURIComponent(fileName)}`);
        if (!response.ok) throw new Error(await response.text());

        const content = await response.text();
        editor.setValue(content);
        editor.clearHistory();

        const newKey = `${fileType}/${fileName}`;
        if (cursorPositions[newKey]) {
            const savedState = cursorPositions[newKey];
            editor.setSelections(savedState.selections);

            const line = savedState.selections[0].anchor.line;
            const pos = { line: line, ch: 0 };
            const coords = editor.charCoords(pos, "local");
            const scroller = editor.getScrollerElement();
            const viewportHeight = scroller.clientHeight;
            const targetScrollTop = coords.top - (viewportHeight / 2);
            editor.scrollTo(null, targetScrollTop);
        }

        currentFile = { type: fileType, name: fileName };
    } catch (error) {
        console.error('Failed to load file:', error.message);
    }
}

async function saveFile() {
    if (!currentFile.type || !currentFile.name) {
        showToast(translate('selectFile'), 'error');
        return;
    }

    try {
        const content = editor.getValue();
        const response = await fetch(`/editor/save/${token}/${currentFile.type}/${encodeURIComponent(currentFile.name)}`, {
            method: 'POST',
            body: content
        });

        const result = await response.json();

        if (response.ok) {
            showToast(translate("successSave"), 'success');
        } else {
            throw new Error(result.error || 'Failed to save file');
        }
    } catch (error) {
        console.error('Error saving file:', error.message);
        showToast(`${translate('errorSave')} ${error.message}`, 'error');
    }
}

async function logout() {
    try {
        const response = await fetch(`/logout/${token}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });

        const result = await response.json();

        if (result.success) {
            showToast(result.message, 'success');
            window.location.href = result.redirect || '/';
        } else {
            showToast(result.error || translate('errorLogout'), 'error');
        }
    } catch (error) {
        console.error('Logout error:', error);
        showToast(translate('errorLogout'), 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initTranslations();
    initEditor();

    document.querySelectorAll('.file').forEach(el => {
        el.addEventListener('click', () => {
            const type = el.dataset.type;
            const name = el.dataset.name;
            loadFile(type, name);
        });
    });

    document.getElementById('save-btn').addEventListener('click', saveFile);

    document.getElementById('logout-btn').addEventListener('click', logout);

    const copyBtn = document.getElementById('copy-btn');
    if (copyBtn) {
        copyBtn.addEventListener('click', () => {
            const command = '/flectonepulse reload';

            const textarea = document.createElement('textarea');
            textarea.value = command;
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';

            document.body.appendChild(textarea);

            textarea.select();
            document.execCommand('copy');

            document.body.removeChild(textarea);

            showToast(translate('commandCopied'), 'success');
        });
    } else {
        console.error('copyBtn not found');
    }

    const firstFile = document.querySelector('.file');
    if (firstFile) {
        loadFile(firstFile.dataset.type, firstFile.dataset.name);
    }
});