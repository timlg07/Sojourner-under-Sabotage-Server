package de.tim_greller.susserver.exception;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import lombok.Getter;

@Getter
public class CompilationException extends Exception {

    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public CompilationException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super("Compilation failed.");
        this.diagnostics = diagnostics;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder(super.getMessage());
        final String nl = "\n\n";
        sb.append(nl);
        diagnostics.forEach(diagnostic -> sb.append(diagnostic.toString()).append(nl));
        return sb.toString();
    }
}
