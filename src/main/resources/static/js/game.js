/**
 * @typedef TestDetails
 * @property {string} className
 * @property {number} elapsedTime
 * @property {number} startTime
 * @property {string} methodName
 * @property {string} testSuiteName
 * @property {string} accessDenied
 * @property {string} actualTestResult
 * @property {string} expectedTestResult
 * @property {string} testStatus
 * @property {string} trace
 */

/**
 * @typedef {Object} TestResult
 * @property {string} testClassName
 * @property {string} testStatus
 * @property {number} elapsedTime
 * @property {Object<string, TestDetails>} testDetails
 * @property {Object<string, Object<string, number>>} coverage
 * @property {string} message
 */

/**
 * @typedef {Object} SourceDTO
 * @property {string} cutComponentName
 * @property {string} className
 * @property {string} sourceCode
 * @property {Array<Range>} editable
 */

/**
 * @typedef {Object} ComponentData
 * @property {SourceDTO} test
 * @property {SourceDTO} cut
 * @property {TestResult} testResult
 */

/** @type {string|false} */
let currentComponent;
const loadingText = "loading...";
/** @type {Map<string, ComponentData>} */
const componentData = new Map();

const monacoContainerDebug = document.getElementById('monaco-container-debug');
window.monacoEditorDebug = monaco.editor.create(monacoContainerDebug, {
    value: '',
    language: 'java',
    theme: 'vs-dark',
    automaticLayout: true,
});

const monacoContainerTest = document.getElementById('monaco-container-test');
window.monacoEditorTest = monaco.editor.create(monacoContainerTest, {
    value: '',
    language: 'java',
    theme: 'vs-dark',
    automaticLayout: true,
});

const uiOverlay = document.getElementById('ui-overlay');
uiOverlay.style.display = 'none';

const result = document.getElementById('execution-result');
function renderResult(content) {
    result.innerHTML = content;
    const editorContainers = document.querySelectorAll('.monaco-editor-container');
    editorContainers.forEach(el => el.style.height = '0');
    monacoEditorTest.layout();
    monacoEditorDebug.layout();
    editorContainers.forEach(el => el.style.height = 'initial');
}
function saveTest(componentName) {
    const saveButton = document.getElementById('editor-save-btn');
    saveButton.disabled = true;
    saveButton.innerText = "Saving...";
    const test = window.monacoEditorTest.getValue();
    fetch(`/api/components/${componentName}/test/src`, {
        method: 'PUT',
        headers: jsonHeader,
        body: JSON.stringify({code: test}),
    }).then(res => {
        if (res.status === 401) {
            renderResult(`<p class="clr-error">Your session has expired.
                <a href="/login" target="_blank" rel="noopener">Login again.</a></p>`);
        }
        saveButton.disabled = false;
        saveButton.innerText = res.ok ? "Test Saved!" : "Save Failed";
        setTimeout(() => {
            saveButton.innerText = "Save Test";
        }, 3e3);
        if (res.ok) autoSave.lastSave = Date.now();
    }).catch(e => {
        console.error(e);
        saveButton.disabled = false;
        saveButton.innerText = "Save Failed";
        setTimeout(() => {
            saveButton.innerText = "Save Test";
        }, 3e3);
    });
}

/**
 * @param {Array<Range>} editableRanges
 */
function constrain(editableRanges) {
    const model = window.monacoEditorTest.getModel();
    const constrainedInstance = constrainedEditor(monaco);
    constrainedInstance.initializeIn(window.monacoEditorTest);
    constrainedInstance.addRestrictionsTo(model, editableRanges.map(range => ({
        range: [range.startLine, range.startColumn, range.endLine, range.endColumn],
        allowMultiline: true
    })));
}

function renderCoverage(coverage) {
    const model = window.monacoEditorDebug.getModel();
    const cutClassId = window.cutClassName + '#' + window.userId;
    const linesVisited = Object.entries(coverage?.[cutClassId] ?? {});
    const decorations = linesVisited.map(cov => {
        const line = parseInt(cov[0]);
        return {
            range: new monaco.Range(line, 1, line, 1),
            options: {
                isWholeLine: true,
                className: 'covered covered-' + cov[1],
                glyphMarginClassName: 'covered-glyph',
            }
        };
    });

    window.cutDecorations = model.deltaDecorations(
      window.cutDecorations ?? [],
      decorations
    );
}

/** @param {TestResult} obj */
function renderTestResultObject(obj) {
    renderCoverage(obj.coverage);

    let r = `<strong>${obj.testClassName} </strong>`;

    if (obj.testStatus === 'PASSED') {
        r += `<div class="clr-success">All tests passed.</div>`;
    } else if (obj.testStatus === 'FAILED') {
        r += `<div class="clr-error">There are test failures.</div>`;
    }

    r += '<ul>';
    for (const [fn, details] of Object.entries(obj.testDetails)) {
        r += '<li>';
        r += `${details.className}::<strong>${fn} </strong>`;
        if (details.accessDenied != null) {
            r +=`<span class="clr-error">Access Denied!<br>${details.accessDenied}</span>`;
        } else if (details.expectedTestResult != null || details.actualTestResult != null) {
            r += `
                <div class="clr-success flex"><p>Expected value:</p> <pre>${details.expectedTestResult}</pre></div>
                <div class="clr-error flex"><p>Actual value:</p> <pre>${details.actualTestResult}</pre></div>
            `;
        } else if (details.trace != null) {
            r += `<br><small>Trace: <pre>${details.trace}</pre></small>`;
        } else {
            r += `<span class="clr-success">Passed!</span>`;
        }
        r += '</li>';
    }
    r += '</ul>';
    renderResult(r + `<br><small>Elapsed time: ${obj.elapsedTime} ms</small>`);
}


const autoSave = {
    inactivityTimeoutUntilSave: 3e3, // after 3 seconds of inactivity, auto save
    maxAutosaveInterval: 2e4, // auto save at least 20 seconds after a change
    timeout: null,
    lastSave: null
}
function enqueueAutoSave (componentName) {
    autoSave.timeout = setTimeout(() => {
        autoSave.timeout = null;
        saveTest(componentName);
    }, autoSave.inactivityTimeoutUntilSave);
}
window.monacoEditorTest.onDidChangeModelContent(_ => {
    const componentName = currentComponent;
    if (!componentName || window.monacoEditorTest.getValue() === loadingText) {
        // reset
        if (autoSave.timeout) clearTimeout(autoSave.timeout);
        autoSave.timeout = null;
        autoSave.lastSave = null;
    } else if (!autoSave.lastSave) {
        // start tracking
        autoSave.lastSave = Date.now();
    } else if (autoSave.timeout) {
        clearTimeout(autoSave.timeout);
        const notSavedForMs = Date.now() - autoSave.lastSave;
        if (notSavedForMs > autoSave.maxAutosaveInterval) {
            // still editing, but not saved for a longer time -> save now
            autoSave.timeout = null;
            saveTest(componentName);
        } else {
            // push back auto-save longer, because the code is still being edited and was saved recently
            enqueueAutoSave(componentName);
        }
    } else { // tracking changes, change happened, no save in queue
        enqueueAutoSave(componentName);
    }
});

document.getElementById('editor-close-btn').addEventListener('click', () => {
    if (currentComponent) saveTest(currentComponent); // auto save on close
    currentComponent = false;
    autoSave.lastSave = null;
    uiOverlay.style.display = 'none';
    document.getElementById('unity-canvas').focus();
    window.unityInstance.SendMessage('BrowserInterface', 'OnEditorClose');
});

const execBtn = document.getElementById('editor-execute-btn');
execBtn.addEventListener('click', () => {
    const componentName = currentComponent;
    if (!componentName) {
        renderResult(`<p class="clr-error">There is no component loaded currently.</p>`);
        return;
    }
    const code = window.monacoEditorTest.getValue();
    renderResult('<p>Executing test...</p>');
    execBtn.disabled = true;
    fetch(`/api/components/${componentName}/test/execute`, {
        method: 'POST',
        headers: jsonHeader,
        body: JSON.stringify({code}),
    }).then(res => {
        if (res.status === 401) {
            renderResult(`<p class="clr-error">Your session has expired. <a href="/login">Login again.</a></p>`);
            return;
        }

        res.json().then(/** @param {TestResult} obj */ obj => {
            console.log(obj);
            execBtn.disabled = false;

            if (!res.ok) {
                renderResult(`
                        <p class="clr-error"><strong>Failed to execute tests.</strong></p>
                        <pre class="clr-error">${obj.message}</pre>
                    `);
                return;
            }

            const data = componentData.get(componentName);
            if (data) { // should always be true, as the component is loaded before executing
                data.testResult = obj;
                componentData.set(componentName, data);
            }

            renderTestResultObject(obj);
        })
    })
    .catch(e => {
        console.error(e);
        execBtn.disabled = false;
        renderResult(`<p class="clr-error"><strong>Failed to execute test due to network issues.</strong></p>`);
    });
});

const authHeader = {'Authorization': `Bearer ${window.token}`, ...window.csrfHeader};
const jsonHeader = {'Content-Type': 'application/json', ...authHeader};

/**
 * @param {string} componentName
 * @return {Promise<ComponentData>}
 */
async function getComponentData(componentName) {
    let data = {};

    if (componentData.has(componentName)) {
        data = componentData.get(componentName);
    }

    if (!data.cut) {
        await fetch(`/api/components/${componentName}/cut/src`, {headers: authHeader})
          .then(res => res.json())
          .then(/** @param {SourceDTO} json */json => {
              data.cut = json;
          });
    }

    if (!data.test) {
        await fetch(`/api/components/${componentName}/test/src`, {headers: authHeader})
          .then(res => res.json())
          .then(/** @param {SourceDTO} test */test => {
              data.test = test;
          });
    }

    componentData.set(componentName, data);
    return data;
}

window.openEditor = async function (componentName) {
    renderResult('');
    currentComponent = componentName;
    window.monacoEditorDebug.setValue(loadingText);
    window.monacoEditorTest.setValue(loadingText);
    document.getElementById('ui-overlay').style.display = "initial";
    window.monacoEditorDebug.layout();
    window.monacoEditorTest.layout();

    const componentData = await getComponentData(componentName);

    window.monacoEditorDebug.setValue(componentData.cut.sourceCode);
    window.cutClassName = componentData.cut.className;

    window.monacoEditorTest.setValue(componentData.test.sourceCode);
    constrain(componentData.test.editable);
    window.testClassName = componentData.test.className;
    autoSave.lastSave = null;

    if (componentData.testResult) {
        renderTestResultObject(componentData.testResult);
    }

    document.getElementById('editor-save-btn').addEventListener('click', () => {
        saveTest(componentName);
    });

    document.getElementById('editor-activate-test-btn').addEventListener('click', () => {
        const event = new ComponentTestsActivatedEvent(componentName);
        window.es.sendEvent(event);
        renderResult(`<p>Test activated for ${componentName}.</p>`);
    });
};


window.es = new EventSystem();
es.registerHandler('*', console.log);
es.registerHandler('MutatedComponentTestsFailedEvent', evt => {
    window.unityInstance.SendMessage('BrowserInterface', 'OnMutatedComponentTestsFailed', evt.componentName);
    /** @type {ComponentData} */
    const data = {
        testResult: evt.executionResult,
        cut: evt.cutSource,
        test: evt.testSource
    };
    componentData.set(evt.componentName, data);
});
es.sendEvent(new GameStartedEvent());
