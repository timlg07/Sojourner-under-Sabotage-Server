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
}
