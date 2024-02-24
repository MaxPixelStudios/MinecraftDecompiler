package cn.maxpixel.mcdecompiler.common.app;

import java.nio.file.Path;

public enum DecompilerType {
    FERNFLOWER,
    FORGEFLOWER,
    VINEFLOWER,
    CFR,
    USER_DEFINED;
    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public Path getDownloadedDecompilerPath() {
        if (this == DecompilerType.USER_DEFINED) throw new UnsupportedOperationException();
        return Directories.DOWNLOAD_DIR.resolve("decompiler").resolve(this + ".jar");
    }
}