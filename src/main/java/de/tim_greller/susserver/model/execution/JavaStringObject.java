package de.tim_greller.susserver.model.execution;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class JavaStringObject extends SimpleJavaFileObject {
    private final String source;

    public JavaStringObject(String name, String source) {
        super(URI.create("string:///" + name.replaceAll("\\.", "/") +
                Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return source;
    }
}