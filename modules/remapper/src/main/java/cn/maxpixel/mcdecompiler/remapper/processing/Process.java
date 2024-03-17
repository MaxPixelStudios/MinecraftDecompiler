package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.IOException;
import java.util.function.Function;

public interface Process {
    enum Run {
        /**
         * Run before the class is remapped
         */
        BEFORE,
        /**
         * Run after the class is remapped
         */
        AFTER
    }

    String getName();// may have uses in the future, or may be removed

    default void beforeRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
    }

    default void afterRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
    }

    Function<ClassVisitor, ClassVisitor> getVisitor(DeobfuscationOptions options, ClassReader reader, ClassFileRemapper cfr);
}