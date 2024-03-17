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

package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.app.Directories;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.mcdecompiler.remapper.variable.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClassProcessor {
    private static final ObjectArrayList<Supplier<Process>> BEFORE = new ObjectArrayList<>();
    private static final ObjectArrayList<Supplier<Process>> AFTER = new ObjectArrayList<>();

    private final ObjectArrayList<Process> before;
    private final ObjectArrayList<Process> after;
    private final DeobfuscationOptions options;

    public ClassProcessor(DeobfuscationOptions options) {
        this.options = Objects.requireNonNull(options);
        this.before = BEFORE.stream().map(Supplier::get).collect(ObjectArrayList.toListWithExpectedSize(BEFORE.size()));
        this.after = AFTER.stream().map(Supplier::get).collect(ObjectArrayList.toListWithExpectedSize(AFTER.size()));
    }

    public static void addProcess(Process.Run run, Supplier<Process> process) {
        switch (Objects.requireNonNull(run)) {
            case BEFORE -> BEFORE.add(Objects.requireNonNull(process));
            case AFTER -> AFTER.add(Objects.requireNonNull(process));
        }
    }

    public void beforeRunning(ClassFileRemapper mappingRemapper) throws IOException {
        CoreProcess.INSTANCE.beforeRunning(options, mappingRemapper);
        for (Process process : after) {
            process.beforeRunning(options, mappingRemapper);
        }
        for (Process process : before) {
            process.beforeRunning(options, mappingRemapper);
        }
    }

    public void afterRunning(ClassFileRemapper mappingRemapper) throws IOException {
        CoreProcess.INSTANCE.afterRunning(options, mappingRemapper);
        for (Process process : after) {
            process.afterRunning(options, mappingRemapper);
        }
        for (Process process : before) {
            process.afterRunning(options, mappingRemapper);
        }
    }

    public ClassVisitor getVisitor(ClassWriter writer, ClassReader reader, ClassFileRemapper mappingRemapper) {
        ClassVisitor cv = writer;
        for (Process process : after) {
            cv = process.getVisitor(options, reader, mappingRemapper).apply(cv);
        }
        cv = CoreProcess.INSTANCE.getVisitor(options, reader, mappingRemapper).apply(cv);
        for (Process process : before) {
            cv = process.getVisitor(options, reader, mappingRemapper).apply(cv);
        }
        return cv;
    }

    private enum CoreProcess implements Process {
        INSTANCE;
        private final ForgeFlowerAbstractParametersRecorder recorder = new ForgeFlowerAbstractParametersRecorder();

        @Override
        public String getName() {
            return "core";
        }

        @Override
        public void beforeRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) {
            if (options.rvn) recorder.startRecord();
        }

        @Override
        public void afterRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
            if (options.rvn) recorder.endRecord(Directories.TEMP_DIR.resolve(Constants.FERNFLOWER_ABSTRACT_PARAMETER_NAMES));
        }

        @Override
        public Function<ClassVisitor, ClassVisitor> getVisitor(DeobfuscationOptions options, ClassReader reader, ClassFileRemapper cfr) {
            return parent -> {
                String className = reader.getClassName();
                int access = reader.getAccess();
                ClassVisitor cv = parent;
                VariableNameHandler handler = new VariableNameHandler();
                if (cfr.remapper instanceof ClassifiedMappingRemapper cmr) {
                    ClassMapping<? extends Mapping> cm = cmr.getClassMappingUnmapped(className);
                    if (cm != null) {
                        MappingVariableNameProvider provider = new MappingVariableNameProvider(cm, cmr);
                        if (provider.omitThis()) handler.setOmitThis();
                        handler.addProvider(provider);
                    }
                }
                if ((access & Opcodes.ACC_RECORD) != 0) {
                    RecordNameRemapper r = new RecordNameRemapper(cv);
                    cv = r;
                    handler.addProvider(r);
                }
                cv = new VariableNameProcessor(cv, recorder, handler, cfr.map(className), options.rvn);
                cv = new ClassRemapper(cv, cfr);
                cv = new IndyRemapper(cv, cfr);
                ExtraClassesInformation eci = cfr.eci;
                if (eci.dontRemap.containsKey(className)) {
                    ObjectSet<String> skipped = eci.dontRemap.get(className);
                    if (!skipped.isEmpty()) {
                        cv = new MixinClassRemapper(cv, cfr.remapper, eci, options.refMap, skipped, className);
                    }
                } else {
                    cv = new MixinClassRemapper(cv, cfr.remapper, eci, options.refMap, ObjectSets.emptySet(), className);
                }
                return new RuntimeParameterAnnotationFixer(cv, className, access);
            };
        }
    }
}