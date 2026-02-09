open module cn.maxpixel.mcdecompiler.api {
    uses cn.maxpixel.mcdecompiler.api.extension.Extension;
    requires static transitive org.jetbrains.annotations;
    requires transitive cn.maxpixel.mcdecompiler.utils;
    requires transitive com.google.gson;
    requires java.net.http;
    requires cn.maxpixel.rewh.logging.core;
    requires it.unimi.dsi.fastutil;

    exports cn.maxpixel.mcdecompiler.api;
    exports cn.maxpixel.mcdecompiler.api.extension;
    exports cn.maxpixel.mcdecompiler.api.util;
}