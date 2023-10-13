package de.tim_greller.susserver.model.execution.instrumentation;

import static org.springframework.asm.Opcodes.ASM7;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;

public class InstrumentationAdapter extends ClassVisitor {

    private final String classId;

    public InstrumentationAdapter(final ClassWriter pClassWriter, final String pClassId) {
        super(ASM7, pClassWriter);
        classId = pClassId;
    }

    @Override
    public MethodVisitor visitMethod(
            final int pAccess,
            final String pName,
            final String pDescriptor,
            final String pSignature,
            final String[] pExceptions) {
        final MethodVisitor mv =
                super.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);

        return new MethodVisitor(ASM7, mv) {
            @Override
            public void visitLineNumber(final int pLine, final Label pStart) {
                super.visitLineNumber(pLine, pStart);
                CoverageTracker.trackLine(pLine, classId);
                visitLdcInsn(pLine);
                visitLdcInsn(classId);
                visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(CoverageTracker.class),
                        "trackLineVisit",
                        "(ILjava/lang/String;)V",
                        false);
            }
        };
    }
}
