open module cn.maxpixel.mcdecompiler.mapping.detector {
    requires static org.jetbrains.annotations;
    requires transitive cn.maxpixel.mcdecompiler.mapping;
    requires it.unimi.dsi.fastutil;

    exports cn.maxpixel.mcdecompiler.mapping.detector;

    uses DetectionUnit;
    provides DetectionUnit with DefaultDetectionUnit;
}