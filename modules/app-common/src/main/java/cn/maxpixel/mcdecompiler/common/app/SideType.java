package cn.maxpixel.mcdecompiler.common.app;

public enum SideType {
    CLIENT,
    SERVER;
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}