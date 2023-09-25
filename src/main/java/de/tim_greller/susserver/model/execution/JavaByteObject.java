package de.tim_greller.susserver.model.execution;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class JavaByteObject extends SimpleJavaFileObject {
    private final ByteArrayOutputStream outputStream;

    public JavaByteObject(String name) {
        super(URI.create("bytes:///" + name + name.replaceAll("\\.", "/")), Kind.CLASS);
        outputStream = new ByteArrayOutputStream();
    }

    //overriding this to provide our OutputStream to which the
    // bytecode can be written.
    @Override
    public OutputStream openOutputStream() {
        return outputStream;
    }

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }
}
