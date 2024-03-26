package de.tim_greller.susserver.model.execution.instrumentation.transformer;

public class IdentityClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(byte[] bytes, String classId) {
        return bytes;
    }

}
