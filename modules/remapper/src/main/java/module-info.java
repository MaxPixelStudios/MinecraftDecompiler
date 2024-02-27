open module cn.maxpixel.mcdecompiler.remapper {
    requires transitive cn.maxpixel.mcdecompiler.common.app;
    requires transitive cn.maxpixel.mcdecompiler.mapping;

    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires it.unimi.dsi.fastutil;
    requires cn.maxpixel.rewh.logging.core;
    requires joptsimple;

    exports cn.maxpixel.mcdecompiler.remapper;
    exports cn.maxpixel.mcdecompiler.remapper.processing;
    exports cn.maxpixel.mcdecompiler.remapper.variable;

    uses cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor.Process;
}