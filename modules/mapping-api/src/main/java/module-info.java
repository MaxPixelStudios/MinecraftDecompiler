open module cn.maxpixel.mcdecompiler.mapping {
    requires static transitive org.jetbrains.annotations;
    requires it.unimi.dsi.fastutil;

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