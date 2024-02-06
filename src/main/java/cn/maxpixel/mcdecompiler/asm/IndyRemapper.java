package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.Info;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class IndyRemapper extends ClassVisitor {
    private final ClassifiedMappingRemapper mappingRemapper;

    public IndyRemapper(ClassVisitor classVisitor, ClassifiedMappingRemapper mappingRemapper) {
        super(Info.ASM_VERSION, classVisitor);
        this.mappingRemapper = mappingRemapper;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new Remapper(super.visitMethod(access, name, descriptor, signature, exceptions));
    }

    public class Remapper extends MethodVisitor {
        public Remapper(MethodVisitor methodVisitor) {
            super(Info.ASM_VERSION, methodVisitor);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            // We only remap name here. Other things are remapped by ASM's ClassRemapper
            String newName = name;
            if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
                Type interfaceMethodType = (Type) bootstrapMethodArguments[0];
                newName = mappingRemapper.mapMethodName(Type.getReturnType(descriptor).getInternalName(), name, interfaceMethodType.getDescriptor());
            }
            super.visitInvokeDynamicInsn(newName, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }
}