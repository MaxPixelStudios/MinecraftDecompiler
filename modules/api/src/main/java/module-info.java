open module cn.maxpixel.mcdecompiler.api {
    requires cn.maxpixel.mcdecompiler.common.app;
    requires cn.maxpixel.mcdecompiler.utils;
    requires cn.maxpixel.rewh.logging.core;
    requires com.google.gson;
    requires it.unimi.dsi.fastutil;
    requires static org.jetbrains.annotations;

    exports cn.maxpixel.mcdecompiler.api;
    exports cn.maxpixel.mcdecompiler.api.extension;
}