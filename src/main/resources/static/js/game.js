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

document.getElementById('editor-close-btn').addEventListener('click', () => {
    uiOverlay.style.display = 'none';
    window.unityInstance.SendMessage('BrowserInterface', 'OnEditorClose');
});

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
    const cutClassId = window.cutClassName + '#' + window.userId;
    const linesVisited = Object.entries(coverage[cutClassId]);
    const model = window.monacoEditorDebug.getModel();
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


const authHeader = {'Authorization': `Bearer ${window.token}`};
const jsonHeader = {'Content-Type': 'application/json', ...authHeader};
window.openEditor = async function (componentName) {
    window.monacoEditorDebug.setValue("loading...");
    window.monacoEditorTest.setValue("loading...");
    document.getElementById('ui-overlay').style.display = "initial";
    window.monacoEditorDebug.layout();
    window.monacoEditorTest.layout();

    fetch(`/api/components/${componentName}/cut/src`, {headers: authHeader})
        .then(res => res.json())
        .then(json => {
            window.monacoEditorDebug.setValue(json.sourceCode);
            window.cutClassName = json.className;
        });

    fetch(`/api/components/${componentName}/test/src`, {headers: authHeader})
        .then(res => res.json())
        .then(test => {
            window.monacoEditorTest.setValue(test.sourceCode);
            constrain(test.editable);
            addTestEditorChangeListener();
            window.testClassName = test.className;
        });

    function addTestEditorChangeListener() {
        const inactivityTimeoutUntilSave = 5e3; // after 5 seconds of inactivity, auto save
        const maxAutosaveInterval = 3e4; // auto save at least 30 seconds after a change
        let timeout = null;
        let lastSave = null;
        window.monacoEditorTest.onDidChangeModelContent(_ => {
            if (!lastSave) {
                // start tracking
                lastSave = Date.now();
            } else if (timeout) {
                clearTimeout(timeout);
                const noSaveFor = Date.now() - lastSave;
                if (noSaveFor > maxAutosaveInterval) {
                    timeout = null;
                    saveTest(componentName);
                    return;
                }
            }
            timeout = setTimeout(() => {
                timeout = null;
                saveTest(componentName);
            }, inactivityTimeoutUntilSave);
        });
    }

    const execBtn = document.getElementById('editor-execute-btn');
    execBtn.addEventListener('click', () => {
        const code = window.monacoEditorTest.getValue();
        renderResult('<p>Executing test...</p>');
        execBtn.disabled = true;
        fetch(`/api/components/${componentName}/test/execute`, {
            method: 'POST',
            headers: jsonHeader,
            body: JSON.stringify({code}),
        }).then(res => {
            if (res.status === 401) {
                renderResult(`<p class="clr-error">Your session has expired.
                    <a href="/login" target="_blank" rel="noopener">Login again.</a></p>`);
                return;
            }
            res.json().then(obj => {
                console.log(obj);
                execBtn.disabled = false;

                renderCoverage(obj.coverage);

                if (!res.ok) {
                    renderResult(`
                        <p class="clr-error"><strong>Failed to execute test.</strong></p>
                        <pre class="clr-error">${obj.message}</pre>
                    `);
                    return;
                }

                const elapsed = `<br><small>Elapsed time: ${obj.elapsedTime} ms</small>`;
                if (obj.testStatus === 'PASSED') {
                    renderResult(`<p class="clr-success">Test Passed! ${elapsed}</p>`);
                    return;
                }
                const details = obj.testDetails.test;
                if (details.accessDenied != null) {
                    renderResult(`
                        <p class="clr-error"><strong>Access Denied!</strong><br>${details.accessDenied} ${elapsed}</p>
                    `);
                    return;
                }
                let resultString = `
                  <p class="clr-error"><strong>${details.className} Failed!</strong></p>
                `;
                if (details.expectedTestResult != null || details.actualTestResult != null) {
                    resultString += `
                        <div class="clr-success flex"><p>Expected value:</p> <pre>${details.expectedTestResult}</pre></div>
                        <div class="clr-error flex"><p>Actual value:</p> <pre>${details.actualTestResult}</pre></div>
                    `;
                }
                if (details.trace != null) {
                   resultString += `<br><small>Trace: <pre>${details.trace}</pre></small>`;
                }
                resultString += elapsed;
                renderResult(resultString);
            })
          })
          .catch(e => {
              console.error(e);
              execBtn.disabled = false;
              renderResult(`<p class="clr-error"><strong>Failed to execute test due to network issues.</strong></p>`);
          });
    });

    document.getElementById('editor-save-btn').addEventListener('click', () => {
        saveTest(componentName);
    });
};