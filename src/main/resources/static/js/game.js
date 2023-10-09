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
        .then(json => json.sourceCode)
        .then(cut => window.monacoEditorDebug.setValue(cut));

    fetch(`/api/components/${componentName}/test/src`, {headers: authHeader})
        .then(res => res.json())
        .then(test => {
            window.monacoEditorTest.setValue(test.sourceCode);
            constrain(test.editable);
            addTestEditorChangeListener();
        });

    function addTestEditorChangeListener() {
        const inactivityTimeoutUntilSave = 5e3; // after 5 seconds of inactivity, auto save
        const maxAutosaveInterval = 3e4; // auto save at least 30 seconds after a change
        let timeout = null;
        let lastSave = null;
        window.monacoEditorTest.onDidChangeModelContent(e => {
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
        const result = document.getElementById('execution-result');
        result.innerHTML = '<p>Executing test...</p>';
        execBtn.disabled = true;
        fetch(`/api/components/${componentName}/test/execute`, {
            method: 'POST',
            headers: jsonHeader,
            body: JSON.stringify({code}),
        })
            .then(res => res.json().then(obj => {
                console.log(obj);
                execBtn.disabled = false;

                if (!res.ok) {
                    result.innerHTML = `
                        <p class="clr-error"><strong>Failed to execute test.</strong></p>
                        <pre class="clr-error">${obj.message}</pre>
                    `;
                    return;
                }

                const elapsed = `<br><small>Elapsed time: ${obj.elapsedTime} ms</small>`;
                if (obj.testStatus === 'PASSED') {
                    result.innerHTML = `<p class="clr-success">Test Passed! ${elapsed}</p>`;
                    return;
                }
                const details = obj.testDetails.test;
                if (details.accessDenied != null) {
                    result.innerHTML = `
                        <p class="clr-error"><strong>Access Denied!</strong><br>${details.accessDenied} ${elapsed}</p>
                    `;
                    return;
                }
                result.innerHTML = `
                  <p class="clr-error"><strong>${details.className} Failed!</strong></p>
                `;
                if (details.expectedTestResult != null || details.actualTestResult != null) {
                    result.innerHTML += `
                        <div class="clr-success flex"><p>Expected value:</p> <pre>${details.expectedTestResult}</pre></div>
                        <div class="clr-error flex"><p>Actual value:</p> <pre>${details.actualTestResult}</pre></div>
                    `;
                }
                if (details.trace != null) {
                    result.innerHTML += `<br><small>Trace: <pre>${details.trace}</pre></small>`;
                }
                result.innerHTML += elapsed;
            }))
            .catch(e => {
                console.error(e);
                execBtn.disabled = false;
                // TODO: what happens if session expires?
                result.innerHTML = `<p class="clr-error"><strong>Failed to execute test due to network issues.</strong></p>`;
            });
    });

    document.getElementById('editor-save-btn').addEventListener('click', () => {
        saveTest(componentName);
    });
};