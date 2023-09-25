package de.tim_greller.susserver.model.execution.instrumentation;

public interface IClassTransformer {
    byte[] transform(byte[] bytes, String className);
}
