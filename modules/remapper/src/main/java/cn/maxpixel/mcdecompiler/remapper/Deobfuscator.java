package cn.maxpixel.mcdecompiler.remapper;

import cn.maxpixel.mcdecompiler.remapper.processing.ClassFileRemapper;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import org.objectweb.asm.Opcodes;

public abstract class Deobfuscator {
    public static final int ASM_VERSION = Opcodes.ASM9;
    protected static final Logger LOGGER = LogManager.getLogger();

    static {
        ClassProcessor.fetchOptions();
    }

    protected final DeobfuscationOptions options;
    protected ClassFileRemapper remapper;

    protected Deobfuscator(DeobfuscationOptions options) {
        this.options = options;
    }

    /**
     * Release the remapper so that memory can be cleaned up after the deobfuscation.
     */
    public final void releaseRemapper() {
        this.remapper = null;
    }
}