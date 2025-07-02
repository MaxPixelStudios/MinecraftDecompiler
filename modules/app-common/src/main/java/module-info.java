open module cn.maxpixel.mcdecompiler.common.app {
    requires static transitive org.jetbrains.annotations;

    requires it.unimi.dsi.fastutil;
    requires java.net.http;
    requires cn.maxpixel.rewh.logging.core;
    requires transitive com.google.gson;
    requires transitive cn.maxpixel.mcdecompiler.utils;

    exports cn.maxpixel.mcdecompiler.common.app;
    exports cn.maxpixel.mcdecompiler.common.app.util;
}