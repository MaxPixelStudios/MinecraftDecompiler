package cn.maxpixel.mcdecompiler.mapping.detector;

import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import it.unimi.dsi.fastutil.Pair;

import java.util.Optional;

public interface DetectionService {// TODO
    boolean canDetectHeader();

    Optional<MappingFormat<?, ?>> detectHeader();

    Pair<MappingFormat<?, ?>, Percentage> detectContent();

    enum Percentage {
        ZERO,
        FIFTY,
        NINETY,
        NINETY_NINE,
        ONE_HUNDRED
    }
}
