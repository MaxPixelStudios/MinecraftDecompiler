import cn.maxpixel.mcdecompiler.mapping.detector.DefaultDetectionUnit;
import cn.maxpixel.mcdecompiler.mapping.detector.DetectionUnit;

open module cn.maxpixel.mcdecompiler.mapping.detector {
    requires transitive cn.maxpixel.mcdecompiler.mapping;
    requires it.unimi.dsi.fastutil;

    exports cn.maxpixel.mcdecompiler.mapping.detector;

    uses DetectionUnit;
    provides DetectionUnit with DefaultDetectionUnit;
}