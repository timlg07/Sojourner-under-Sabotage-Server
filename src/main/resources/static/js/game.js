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
 * @typedef {Object} LogEntry
 * @property {number} orderIndex
 * @property {string} message
 * @property {string} methodName
 * @property {number} lineNumber
 * @property {string} testMethodName
 */

/**
 * @typedef {Object} TestResult
 * @property {string} testClassName
 * @property {'PASSED' | 'FAILED' | 'IGNORED'} testStatus
 * @property {number} elapsedTime
 * @property {Object<string, TestDetails>} testDetails
 * @property {Object<string, Object<string, number>>} coverage
 * @property {Object<string, Object<number, Object<string, string>>>} variables
 * @property {Object<string, Array<LogEntry>>} logs
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
 *
 * @property {'INITIAL' | 'TESTS_ACTIVE' | 'MUTATED'} state
 */

/**
 * @typedef {Object} UserGameProgressionDTO
 * @property {number} id
 * @property {number} room
 * @property {string} componentName
 * @property {number} stage
 * @property {'DOOR'|'TALK'|'TEST'|'TESTS_ACTIVE'|'DESTROYED'|'MUTATED'|'DEBUGGING'} status
 */

/** @type {string|false} */
let currentComponent;
/** @type {UserGameProgressionDTO | false} */
let gameProgress = false;
const loadingText = "loading...";
/** @type {Map<string, ComponentData>} */
const componentData = new Map();
/** @type {{monaco:Record<string, ICodeEditor>, restricted:Record<string, constrainedEditor>}} */
window.editors = {monaco:{}, restricted:{}};

const monacoContainerDebug = document.getElementById('monaco-container-debug');
window.editors.monaco.debug = monaco.editor.create(monacoContainerDebug, {
    value: '',
    language: 'java',
    theme: 'vs-dark',
    automaticLayout: true,
});

const monacoContainerTest = document.getElementById('monaco-container-test');
window.editors.monaco.test = monaco.editor.create(monacoContainerTest, {
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
    editors.monaco.test.layout();
    editors.monaco.debug.layout();
    editorContainers.forEach(el => el.style.height = 'initial');
}

function sessionExpired(saveButton) {
    renderResult(`<p class="clr-error">Your session has expired.
                <a href="/login" target="_blank" rel="noopener">Login again.</a></p>`);
    saveButton.disabled = false;
    saveButton.innerText = "Save Failed";
    setTimeout(() => {
        saveButton.innerText = "Save";
    }, 3e3);
}

async function save(componentName = currentComponent) {
    let noSaveFailure = true;
    const saveButton = document.getElementById('editor-save-btn');
    saveButton.disabled = true;
    saveButton.innerText = "Saving...";
    const data = componentData.get(componentName);

    // 1 ─ Save test
    if (gameProgress?.status === 'TEST' || gameProgress?.status === 'DEBUGGING') {
        const test = window.editors.monaco.test.getValue();
        await fetch(`/api/components/${componentName}/test/src`, {
            method: 'PUT',
            headers: jsonHeader,
            body: JSON.stringify({code: test}),
        }).then(res => {
            if (res.status === 401) {
                sessionExpired(saveButton);
                return;
            }
            noSaveFailure &= res.ok;
        }).catch(e => {
            console.error(e);
            noSaveFailure = false;
        });

        // 1.2 ─ Update in local cache
        data.test.sourceCode = test;
    }

    // 2 ─ Save cut (only in debug mode, after a component was mutated)
    if (gameProgress?.status === 'DEBUGGING') {
        const cut = window.editors.monaco.debug.getValue();
        await fetch(`/api/components/${componentName}/cut/src`, {
            method: 'PUT',
            headers: jsonHeader,
            body: JSON.stringify({code: cut}),
        }).then(res => {
            if (res.status === 401) {
                sessionExpired(saveButton);
                return;
            }
            noSaveFailure &= res.ok;
        }).catch(e => {
            console.error(e);
            noSaveFailure = false;
        });

        // 2.2 ─ Update in local cache
        data.cut.sourceCode = cut;
    }

    // 3 ─ Update component data
    componentData.set(componentName, data);

    // 4 ─ Update save button state
    if (noSaveFailure) {
        saveButton.disabled = false;
        saveButton.innerText = "Saved!";
        autoSave.lastSave = Date.now();
    } else {
        saveButton.disabled = false;
        saveButton.innerText = "Save Failed";
    }
    setTimeout(() => {
        saveButton.innerText = "Save";
    }, 3e3);
}

/**
 * @param {Array<Range>} editableRanges
 * @param {string} editorName
 */
function constrain(editableRanges, editorName = 'test') {
    const editor = editors.monaco[editorName];
    let constrainedInstance;

    if (editors.restricted[editorName]) {
        constrainedInstance = editors.restricted[editorName];
        constrainedInstance.removeRestrictionsIn(editor.getModel());
    } else {
        constrainedInstance = constrainedEditor(monaco);
        editors.restricted[editorName] = constrainedInstance;
        constrainedInstance.initializeIn(editor);
    }

    constrainedInstance.addRestrictionsTo(editor.getModel(), editableRanges.map(range => ({
        range: [range.startLine, range.startColumn, range.endLine, range.endColumn],
        allowMultiline: true
    })));
}

function renderCoverage(coverage) {
    const model = window.editors.monaco.debug.getModel();
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

/**
 * @param {Object<string, Object<number, Object<string, string>>>} variables
 */
function renderDebugValues(variables) {
    const cutClassId = window.cutClassName + '#' + window.userId;
    const data = variables?.[cutClassId] ?? {};
    const hints = [];
    for (const [line, vars] of Object.entries(data)) {
        let sep = '//';
        for (const [varName, value] of Object.entries(vars)) {
            const shortName = varName.split('/').pop();
            hints.push({
                kind: monaco.languages.InlayHintKind.Type,
                position: { column: Number.MAX_VALUE, lineNumber: parseInt(line) },
                label: `${sep} ${shortName} = ${value}`,
                paddingLeft: true,
                tooltip: `The variable ${shortName} is assigned to the value "${value}" here.`,
            });
            sep = ',';
        }
    }

    if (window.disposeHints) window.disposeHints.dispose();
    window.disposeHints = monaco.languages.registerInlayHintsProvider("java", {
        provideInlayHints(model, range, token) {
            const dispose = () => {};
            if (model === window.editors.monaco.debug.getModel()) {
                return {hints, dispose};
            } else {
                return {hints: [], dispose};
            }
        },
    });
}

/**
 * @param {Object<string, Array<LogEntry>>} logs
 */
function renderLogs(logs) {
    const cutClassId = window.cutClassName + '#' + window.userId;
    const data = logs?.[cutClassId] ?? [];
    const decorations = [];
    for (const log of data) {
        const range = new monaco.Range(log.lineNumber, 1, log.lineNumber, 1);
        const decoration = {
            range,
            options: {
                isWholeLine: true,
                className: 'log',
                glyphMarginClassName: 'log-glyph',
                hoverMessage: {
                    value: `<i>${log.testMethodName}:</i> ${log.message}`,
                    supportHtml: true
                },
            }
        };
        decorations.push(decoration);
    }
    window.logDecorations = window.editors.monaco.debug.deltaDecorations(
      window.logDecorations ?? [],
      decorations
    );
}

/** @param {TestResult} obj */
function renderTestResultObject(obj) {
    renderCoverage(obj.coverage);
    renderDebugValues(obj.variables);
    renderLogs(obj.logs);

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
        r += `<details><summary>Log messages</summary>`;
        r += `<ul>`;

        const logs = obj.logs[window.cutClassName + '#' + window.userId] ?? [];
        logs.sort((a, b) => a.orderIndex - b.orderIndex);
        for (const log of logs) {
            if (log.testMethodName !== fn) continue;
            r += `<li><!--${log.orderIndex}-->[${log.methodName}:${log.lineNumber}] <strong><code>${log.message}</code></strong></li>`;
        }
        r += `</ul></details>`;

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
        save(componentName);
    }, autoSave.inactivityTimeoutUntilSave);
}
window.editors.monaco.test.onDidChangeModelContent(onContentChanged);
window.editors.monaco.debug.onDidChangeModelContent(onContentChanged);
function onContentChanged() {
    const componentName = currentComponent;
    if (!componentName || window.editors.monaco.test.getValue() === loadingText) {
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
            save(componentName);
        } else {
            // push back auto-save longer, because the code is still being edited and was saved recently
            enqueueAutoSave(componentName);
        }
    } else { // tracking changes, change happened, no save in queue
        enqueueAutoSave(componentName);

        // hide variable value hints, as they can get confusing while editing
        if (window.disposeHints) window.disposeHints.dispose();
    }

    // disable activate button, because the tests need to be executed again
    disableActivateButton();
}

document.getElementById('editor-close-btn').addEventListener('click', () => {
    if (currentComponent) save(currentComponent); // auto save on close
    currentComponent = false;
    autoSave.lastSave = null;
    uiOverlay.style.display = 'none';
    document.getElementById('unity-canvas').focus();
    window.unityInstance.SendMessage('BrowserInterface', 'OnEditorClose');
});

const execBtn = document.getElementById('editor-execute-btn');
execBtn.addEventListener('click', async () => {
    const componentName = currentComponent;
    if (!componentName) {
        renderResult(`<p class="clr-error">There is no component loaded currently.</p>`);
        return;
    }
    const code = window.editors.monaco.test.getValue();
    renderResult('<p>Executing test...</p>');
    execBtn.disabled = true;

    if (gameProgress?.status === 'DEBUGGING') {
        await save(componentName); // save CUT
    }

    return fetch(`/api/components/${componentName}/test/execute`, {
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
            updateActivateButtonState(componentName);
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

    if (!'state' in data) {
        // TODO: maybe get state via API?
        data.state = 'INITIAL';
    }

    componentData.set(componentName, data);
    return data;
}

function updateActivateButtonState(componentName) {
    const btn = document.getElementById('editor-activate-test-btn');
    const data = componentData.get(componentName);

    // can be activated if not already active and tests passed under the original CUT
    const isActivated = data.state === 'TESTS_ACTIVE';
    const testsPassed = data.testResult?.testStatus === 'PASSED';
    const canActivate = !isActivated && testsPassed;

    btn.disabled = !canActivate;
    btn.innerText = canActivate ? "Activate Test" :
                    isActivated ? "Test Activated" : "tests need to pass to activate";
}

function disableActivateButton() {
    const btn = document.getElementById('editor-activate-test-btn');
    btn.disabled = true;
    btn.innerText = "tests need to pass to activate";
}

window.openEditor = async function (componentName) {
    const saveButton = document.getElementById('editor-save-btn');
    const activateButton = document.getElementById('editor-activate-test-btn');
    saveButton.disabled = true;
    execBtn.disabled = true;
    activateButton.disabled = true;
    constrain([], 'debug');
    constrain([], 'test');

    renderResult('');
    currentComponent = componentName;
    window.editors.monaco.debug.setValue(loadingText);
    window.editors.monaco.test.setValue(loadingText);
    document.getElementById('ui-overlay').style.display = "initial";
    window.editors.monaco.debug.layout();
    window.editors.monaco.test.layout();

    const currentComponentData = await getComponentData(componentName);

    window.editors.monaco.debug.setValue(currentComponentData.cut.sourceCode);
    if (currentComponentData.state === 'MUTATED') {
        constrain(currentComponentData.cut.editable, 'debug');
    } else { // make it not editable if not attacked/mutated.
        constrain([], 'debug');
    }
    window.cutClassName = currentComponentData.cut.className;

    window.editors.monaco.test.setValue(currentComponentData.test.sourceCode);
    constrain(currentComponentData.test.editable, 'test');
    window.testClassName = currentComponentData.test.className;
    autoSave.lastSave = null;

    if (currentComponentData.testResult) {
        renderTestResultObject(currentComponentData.testResult);
    }

    saveButton.addEventListener('click', () => {
        save(componentName);
    });

    activateButton.addEventListener('click', () => {
        const event = new ComponentTestsActivatedEvent(componentName);
        window.es.sendEvent(event);
        const data = componentData.get(componentName); // should always be present at this point
        data.state = 'TESTS_ACTIVE';
        componentData.set(componentName, data);

        constrain([], 'test');
        execBtn.disabled = true;
        updateActivateButtonState(componentName);
        renderResult(`<p>Test activated for ${componentName}.</p>`);
    });

    updateActivateButtonState(componentName);

    if (currentComponentData.state !== 'TESTS_ACTIVE') {
        execBtn.disabled = false;
        saveButton.disabled = false;
    }

    if (gameProgress?.status === 'MUTATED' || gameProgress?.status === 'DESTROYED') {
        const debugBtn = document.getElementById('editor-start-debugging-btn');
        debugBtn.classList.remove('hidden');
        debugBtn.addEventListener('click', () => {
            es.sendEvent(new DebugStartEvent(componentName));
            debugBtn.classList.add('hidden');
        });
    }
};


window.es = new EventSystem();
es.registerHandler('*', console.log);
es.registerHandler(
  'MutatedComponentTestsFailedEvent',
  /** @param {{executionResult:TestResult, cutSource:SourceDTO, testSource:SourceDTO, componentName:string}} evt */
  evt => {
      window.unityInstance.SendMessage('BrowserInterface', 'OnMutatedComponentTestsFailed', evt.componentName);
      /** @type {ComponentData} */
      const data = {
          testResult: evt.executionResult,
          cut: evt.cutSource,
          test: evt.testSource,
          state: 'MUTATED',
      };
      componentData.set(evt.componentName, data);
  }
);
es.registerHandler(
  'ComponentDestroyedEvent',
  /** @param {{executionResult:TestResult, cutSource:SourceDTO, componentName:string, autoGeneratedTestSource:SourceDTO}} evt */
  evt => {
      window.unityInstance.SendMessage('BrowserInterface', 'OnComponentDestroyed', evt.componentName);
      const data = {
          testResult: evt.executionResult,
          cut: evt.cutSource,
          test: evt.autoGeneratedTestSource,
          state: 'MUTATED',
      };
      componentData.set(evt.componentName, data);
  }
);
es.registerHandler(
  'ComponentTestsExtendedEvent',
  /** @param {{componentName:string, addedTestMethodName:string}} evt */
  evt => {
      fetch(`/api/components/${evt.componentName}/test/src`, {headers: authHeader})
        .then(res => res.json())
        .then(/** @param {SourceDTO} test */ test => {
            const data = componentData.get(evt.componentName);
            data.test = test;
            componentData.set(evt.componentName, data);
            console.log('Test for ' + evt.componentName + ' extended with ' + evt.addedTestMethodName);

            if (currentComponent === evt.componentName) {
                window.editors.monaco.test.setValue(test.sourceCode);
                constrain(test.editable, 'test');
            }
        });
  }
);
es.registerHandler(
  'GameProgressionChangedEvent',
  /** @param {{progression:UserGameProgressionDTO}} evt */
  evt => {
      gameProgress = evt.progression;
      window.unityInstance.SendMessage('BrowserInterface', 'OnGameProgressionChanged', JSON.stringify(evt.progression));

      console.log('Game progression changed: ', evt.progression);
  }
);
es.registerHandler(
  'ComponentFixedEvent',
  /** @param {{componentName:string}} evt */
  evt => {
      const data = componentData.get(evt.componentName);
      data.state = 'INITIAL';
      componentData.set(evt.componentName, data);
      alert('Congratulations! You fixed the component ' + evt.componentName + '.');
      window.unityInstance.SendMessage('BrowserInterface', 'OnComponentFixed', evt.componentName);
  }
);
