package de.tim_greller.susserver.model.execution.instrumentation.transformer;

public interface IClassTransformer {
    byte[] transform(byte[] bytes, String classId);
}
