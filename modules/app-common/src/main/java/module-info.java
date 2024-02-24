open module cn.maxpixel.mcdecompiler.common.app {
    requires transitive cn.maxpixel.mcdecompiler.common;

    requires it.unimi.dsi.fastutil;
    requires java.net.http;
    requires cn.maxpixel.rewh.logging.core;
    requires transitive com.google.gson;

    exports cn.maxpixel.mcdecompiler.common.app;
    exports cn.maxpixel.mcdecompiler.common.app.util;
}