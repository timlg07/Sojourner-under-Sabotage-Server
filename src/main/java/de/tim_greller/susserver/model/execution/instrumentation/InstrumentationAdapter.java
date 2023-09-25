package de.tim_greller.susserver.model.execution.instrumentation;

import static org.objectweb.asm.Opcodes.ASM7;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InstrumentationAdapter extends ClassVisitor {

    private final String className;

    public InstrumentationAdapter(final ClassWriter pClassWriter, final String pClassName) {
        super(ASM7, pClassWriter);
        className = pClassName;
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
                CoverageTracker.trackLine(pLine, className);
                visitLdcInsn(pLine);
                visitLdcInsn(className);
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
