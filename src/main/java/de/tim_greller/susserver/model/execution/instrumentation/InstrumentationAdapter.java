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
            final String pMethodName,
            final String pDescriptor,
            final String pSignature,
            final String[] pExceptions) {
        final MethodVisitor mv = super.visitMethod(pAccess, pMethodName, pDescriptor, pSignature, pExceptions);

        return new MethodVisitor(ASM7, mv) {
            @Override
            public void visitLineNumber(final int pLine, final Label pStart) {
                super.visitLineNumber(pLine, pStart);
                InstrumentationTracker.trackLine(pLine, classId);
                visitLdcInsn(pLine);
                visitLdcInsn(classId);
                visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(InstrumentationTracker.class),
                        "trackLineVisit",
                        "(ILjava/lang/String;)V",
                        false);
            }

            @Override
            public void visitVarInsn(int opcode, int varIndex) {
                super.visitVarInsn(opcode, varIndex);

                if (opcode >= 54 && opcode <= 58) { // visitVarInsn STORE Opcodes
                    String descriptor = switch (opcode) {
                        case Opcodes.ISTORE -> "I";
                        case Opcodes.FSTORE -> "F";
                        case Opcodes.DSTORE -> "D";
                        case Opcodes.LSTORE -> "J";
                        default -> "Ljava/lang/Object;";
                    };
                    int loadOpcode = opcode - 33;
                    visitVarInsn(loadOpcode, varIndex);
                    visitLdcInsn(varIndex);
                    visitLdcInsn(classId);
                    visitLdcInsn(pMethodName);
                    visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            Type.getInternalName(InstrumentationTracker.class),
                            "trackVar",
                            "("+descriptor+"ILjava/lang/String;Ljava/lang/String;)V",
                            false);
                }
            }

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
                                           int index) {
                super.visitLocalVariable(name, descriptor, signature, start, end, index);
                // todo: visit method does not work here?
                InstrumentationTracker.trackVarDef(index, name, descriptor, classId, pMethodName);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if (owner.equals("java/io/PrintStream") && (name.equals("println") || name.equals("print"))) {
                    visitLdcInsn(classId);
                    visitLdcInsn(pMethodName);
                    String type = switch (descriptor) {
                        case "(Ljava/lang/String;)V" -> "Ljava/lang/String;";
                        case "(I)V" -> "I";
                        case "(F)V" -> "F";
                        case "(D)V" -> "D";
                        case "(J)V" -> "J";
                        case "(C)V" -> "C";
                        case "(Z)V" -> "Z";
                        case "(B)V" -> "B";
                        case "(S)V" -> "S";
                        case "([I)V" -> "[I";
                        case "([F)V" -> "[F";
                        case "([D)V" -> "[D";
                        case "([J)V" -> "[J";
                        case "([C)V" -> "[C";
                        case "([Z)V" -> "[Z";
                        case "([B)V" -> "[B";
                        case "([S)V" -> "[S";
                        case "()V"  -> "";
                        default -> "Ljava/lang/Object;";
                    };
                    super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            Type.getInternalName(Debug.class),
                            "log",
                            "("+type+"Ljava/lang/String;Ljava/lang/String;)V",
                            false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (opcode == Opcodes.GETSTATIC
                        && owner.equals("java/lang/System")
                        && name.equals("out")
                        && desc.equals("Ljava/io/PrintStream;")) {
                    return;
                }

                super.visitFieldInsn(opcode, owner, name, desc);
            }
        };
    }
}
