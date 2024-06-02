package cn.maxpixel.mcdecompiler.mapping.detector;

import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@ApiStatus.OverrideOnly
public interface DetectionUnit {
    default boolean canDetectHeader() {
        return false;
    }

    default Predicate<String> getLineFilter() {
        return s -> !s.startsWith("#");
    }

    default Optional<MappingFormat<?, ?>> detectHeader(String firstLine) {
        return Optional.empty();
    }

    Pair<MappingFormat<?, ?>, Percentage> detectContent(List<String> contents);

    enum Percentage {
        ZERO,
        FIFTY,
        NINETY,
        NINETY_NINE,
        ONE_HUNDRED;

        public boolean isHigherThan(Percentage p) {
            return ordinal() > p.ordinal();
        }
    }
}
