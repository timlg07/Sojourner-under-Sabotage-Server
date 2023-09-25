package de.tim_greller.susserver.model.execution.instrumentation;

public class IdentityClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(byte[] bytes, String className) {
        return bytes;
    }

}
