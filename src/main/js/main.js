import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import 'monaco-editor/esm/vs/basic-languages/java/java.contribution';
window.editor = monaco.editor.create(document.getElementById('container'), {
    value:
`public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}`,
    language: 'java',
    theme: 'vs-dark',
    "semanticHighlighting.enabled": true,
    copyWithSyntaxHighlighting: false,
    automaticLayout: true,
});