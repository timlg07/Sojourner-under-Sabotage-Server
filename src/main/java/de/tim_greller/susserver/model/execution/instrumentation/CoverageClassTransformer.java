package de.tim_greller.susserver.model.execution.instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CoverageClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(byte[] bytes, String className) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor instrumentationVisitor = new InstrumentationAdapter(classWriter, className);
        classReader.accept(instrumentationVisitor, 0);
        return classWriter.toByteArray();
    }
}
