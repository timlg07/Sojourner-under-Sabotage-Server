package de.tim_greller.susserver.model.execution.instrumentation.transformer;

import de.tim_greller.susserver.model.execution.instrumentation.adapter.TestInstrumentationAdapter;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;

public class TestClassTransformer implements IClassTransformer {

    String cutClassName;

    public TestClassTransformer(String pCutClassName) {
        cutClassName = pCutClassName;
    }

    @Override
    public byte[] transform(byte[] bytes, String testClassId) {
        final String cutClassId = testClassId.replaceAll("^[^#]+#", cutClassName + "#");
        final ClassReader classReader = new ClassReader(bytes);
        final ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor instrumentationVisitor = new TestInstrumentationAdapter(classWriter, testClassId, cutClassId);
        classReader.accept(instrumentationVisitor, 0);
        return classWriter.toByteArray();
    }
}
