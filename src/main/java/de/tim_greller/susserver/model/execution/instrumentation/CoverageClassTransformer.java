package de.tim_greller.susserver.model.execution.instrumentation;

import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;

public class CoverageClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(byte[] bytes, String classId) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor instrumentationVisitor = new InstrumentationAdapter(classWriter, classId);
        classReader.accept(instrumentationVisitor, 0);
        return classWriter.toByteArray();
    }
}
