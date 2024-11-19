package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TsrgV1MappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.TSRG_V1;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
        for (ClassMapping<PairedMapping> cls : mappings.classes) {
            lines.add(cls.mapping.unmappedName + ' ' + cls.mapping.mappedName);
            cls.getFields().parallelStream().forEach(field -> {
                MappingUtil.checkOwner(field.getOwned(), cls);
                synchronized (lines) {
                    lines.add('\t' + field.unmappedName + ' ' + field.mappedName);
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                String unmappedDesc = MappingUtil.Paired.checkSlimSrgMethod(cls, method, remapper);
                synchronized (lines) {
                    lines.add('\t' + method.unmappedName + ' ' + unmappedDesc + ' ' + method.mappedName);
                }
            });
        }
        mappings.packages.parallelStream().forEach(pkg -> {
            synchronized (lines) {
                lines.add(pkg.unmappedName + "/ " + pkg.mappedName + '/');
            }
        });
        return lines;
    }
}