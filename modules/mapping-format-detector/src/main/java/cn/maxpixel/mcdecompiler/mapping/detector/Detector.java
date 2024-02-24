package cn.maxpixel.mcdecompiler.mapping.detector;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Detector {
    public static MappingFormat.Classified<? extends Mapping> tryDetectingMappingType(String mappingPath) {
        try (Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8)) {
            return tryDetectingMappingType(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingFormat.Classified<? extends Mapping> tryDetectingMappingType(BufferedReader reader) {
        try {
            reader.mark(512);
            MappingFormat.Classified<? extends Mapping> result = tryDetectingMappingType(reader.lines());
            reader.reset();
            return result;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingFormat.Classified<? extends Mapping> tryDetectingMappingType(Stream<String> lines) {
        List<String> list = lines.filter(s -> !s.startsWith("#")).limit(2).toList();
        String s = list.get(0);
        if (s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return MappingFormats.SRG;
        else if (s.startsWith("v1")) return MappingFormats.TINY_V1;
        else if (s.startsWith("tiny\t2\t0")) return MappingFormats.TINY_V2;
        else if (s.startsWith("tsrg2")) return MappingFormats.TSRG_V2;
        else if (s.endsWith(":")) return MappingFormats.PROGUARD;
        s = list.get(1);
        if (s.startsWith("\t")) return MappingFormats.TSRG_V1;
        else return MappingFormats.CSRG;
    }
}