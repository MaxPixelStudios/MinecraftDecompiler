/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.mapping.detector;

import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class FormatDetector {
    private static final ObjectArrayList<DetectionUnit> DETECTION_UNITS = new ObjectArrayList<>(ServiceLoader.load(DetectionUnit.class).iterator());

    public static void registerDetectionUnit(DetectionUnit unit) {
        DETECTION_UNITS.add(unit);
    }

    public static MappingFormat<?, ?> tryDetecting(Path mappingPath) {
        try (Stream<String> lines = Files.lines(mappingPath, StandardCharsets.UTF_8)) {
            return tryDetecting(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingFormat<?, ?> tryDetecting(Stream<String> lines) {
        List<String> list = lines.toList();

        DetectionUnit.Percentage highest = DetectionUnit.Percentage.ZERO;
        MappingFormat<?, ?> ret = null;
        for (DetectionUnit detectionUnit : DETECTION_UNITS) {
            List<String> filtered = list.stream().filter(detectionUnit.getLineFilter()).limit(10).toList();
            if (detectionUnit.canDetectHeader()) {
                var result = detectionUnit.detectHeader(filtered.get(0));
                if (result.isPresent()) return result.get();
            }
            var result = detectionUnit.detectContent(filtered);
            var r = result.right();
            if (r == DetectionUnit.Percentage.ONE_HUNDRED) return result.left();
            else if (r != DetectionUnit.Percentage.ZERO && r.isHigherThan(highest)) {
                highest = r;
                ret = Objects.requireNonNull(result.left());
            }
        }
        if (highest == DetectionUnit.Percentage.ZERO) throw new RuntimeException("Failed to detect mapping format");
        return ret;
    }
}