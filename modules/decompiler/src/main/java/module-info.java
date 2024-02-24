open module cn.maxpixel.mcdecompiler.decompiler {
    requires transitive cn.maxpixel.mcdecompiler.common.app;

    requires cn.maxpixel.rewh.logging.core;
    requires it.unimi.dsi.fastutil;
    requires static cfr;
    requires static org.jetbrains.java.decompiler;

    exports cn.maxpixel.mcdecompiler.decompiler;
}