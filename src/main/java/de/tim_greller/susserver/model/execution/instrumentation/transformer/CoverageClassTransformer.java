package de.tim_greller.susserver.model.execution.instrumentation.transformer;

import de.tim_greller.susserver.model.execution.instrumentation.adapter.CutInstrumentationAdapter;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;

public class CoverageClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(byte[] bytes, String classId) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor instrumentationVisitor = new CutInstrumentationAdapter(classWriter, classId);
        classReader.accept(instrumentationVisitor, 0);
        return classWriter.toByteArray();
    }
}
