package cn.maxpixel.mcdecompiler.mapping.component;

import java.util.Locale;

public enum SideSpecific implements Component {
    CLIENT,
    SERVER,
    BOTH;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}