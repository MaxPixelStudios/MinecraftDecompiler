open module cn.maxpixel.mcdecompiler.mapping {
    requires it.unimi.dsi.fastutil;
    requires transitive cn.maxpixel.mcdecompiler.common;

    exports cn.maxpixel.mcdecompiler.mapping;
    exports cn.maxpixel.mcdecompiler.mapping.collection;
    exports cn.maxpixel.mcdecompiler.mapping.component;
    exports cn.maxpixel.mcdecompiler.mapping.format;
    exports cn.maxpixel.mcdecompiler.mapping.generator;
    exports cn.maxpixel.mcdecompiler.mapping.processor;
    exports cn.maxpixel.mcdecompiler.mapping.remapper;
    exports cn.maxpixel.mcdecompiler.mapping.trait;
    exports cn.maxpixel.mcdecompiler.mapping.util;
}