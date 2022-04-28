/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.ClassifiedDeobfuscator;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cn.maxpixel.mcdecompiler.decompiler.ForgeFlowerDecompiler.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

@ApiStatus.Experimental
public final class ClassProcessor {
    private static final ServiceLoader<Process> LOADER = ServiceLoader.load(Process.class);
    private static final Process[] BEFORE = LOADER.stream().map(ServiceLoader.Provider::get)
            .filter(pro -> pro.getState() == Process.State.BEFORE).toArray(Process[]::new);
    private static final Process[] AFTER = LOADER.stream().map(ServiceLoader.Provider::get)
            .filter(pro -> pro.getState() == Process.State.AFTER).toArray(Process[]::new);

    private ClassProcessor() {
        throw new AssertionError("No instances");
    }

    public static void registerCommandLineOptions(OptionParser parser) {
        CoreProcess.INSTANCE.registerCommandLineOptions(parser::accepts, parser::accepts);
        for(Process process : LOADER) {
            process.registerCommandLineOptions((option) -> parser.accepts(process.getName() + '.' + option),
                    (option, description) -> parser.accepts(process.getName() + '.' + option, description));
        }
    }

    public static void acceptCommandLineValues(OptionSet options) {
        CoreProcess.INSTANCE.acceptCommandLineValues(options::has, options::hasArgument, options::valueOf, options::valuesOf);
        for(Process process : LOADER) {
            process.acceptCommandLineValues(options::has, options::hasArgument, options::valueOf, options::valuesOf);
        }
    }

    public static void fetchOptions() {
        for(Process process : LOADER) {
            process.fetchOptions();
        }
    }

    public static void beforeRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, @Nullable String targetNamespace,
                                     ClassifiedMappingRemapper mappingRemapper) throws IOException {
        CoreProcess.INSTANCE.beforeRunning(options, targetNamespace, mappingRemapper);
        for(Process process : LOADER) {
            process.beforeRunning(options, targetNamespace, mappingRemapper);
        }
    }

    public static void afterRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, @Nullable String targetNamespace,
                                     ClassifiedMappingRemapper mappingRemapper) throws IOException {
        CoreProcess.INSTANCE.afterRunning(options, targetNamespace, mappingRemapper);
        for(Process process : LOADER) {
            process.afterRunning(options, targetNamespace, mappingRemapper);
        }
    }

    public static ClassVisitor getVisitor(ClassWriter writer, ClassifiedDeobfuscator.DeobfuscateOptions options, ClassReader reader,
                                   ClassMapping<? extends Mapping> mapping, String targetNamespace,
                                   ClassifiedMappingRemapper mappingRemapper) {
        ClassVisitor cv = writer;
        for(Process process : AFTER) {
            cv = process.getVisitor(options, reader, mapping, targetNamespace, mappingRemapper).apply(cv);
        }
        cv = CoreProcess.INSTANCE.getVisitor(options, reader, mapping, targetNamespace, mappingRemapper).apply(cv);
        for(Process process : BEFORE) {
            cv = process.getVisitor(options, reader, mapping, targetNamespace, mappingRemapper).apply(cv);
        }
        return cv;
    }

    public interface Process {
        enum State {
            /**
             * Run before the class is remapped
             */
            BEFORE,
            /**
             * Run after the class is remapped
             */
            AFTER,
            /**
             * <b>THIS IS INTERNALLY USED BY {@link CoreProcess}</b><br>
             * Others that use this state will be skipped
             */
            @ApiStatus.Internal
            CORE
        }

        String getName();

        State getState();

        default void registerCommandLineOptions(Function<String, OptionSpecBuilder> accept,
                                                BiFunction<String, String, OptionSpecBuilder> acceptWithDescription) {
        }

        default <V> void acceptCommandLineValues(Predicate<OptionSpec<?>> has, Predicate<OptionSpec<?>> hasArgument,
                                                 Function<OptionSpec<V>, V> valueOf, Function<OptionSpec<V>, List<V>> valuesOf) {
        }

        /**
         * Your own logic to fetch options of your process
         */
        default void fetchOptions() {
        }

        default void beforeRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, @Nullable String targetNamespace,
                           ClassifiedMappingRemapper mappingRemapper) throws IOException {
        }

        default void afterRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, @Nullable String targetNamespace,
                          ClassifiedMappingRemapper mappingRemapper) throws IOException {
        }

        Function<ClassVisitor, ClassVisitor> getVisitor(ClassifiedDeobfuscator.DeobfuscateOptions options, ClassReader reader,
                                                        ClassMapping<? extends Mapping> mapping, @Nullable String targetNamespace,
                                                        ClassifiedMappingRemapper mappingRemapper);
    }

    private enum CoreProcess implements Process {
        INSTANCE;

        @Override
        public String getName() {
            return "core";
        }

        @Override
        public State getState() {
            return State.CORE;
        }

        @Override
        public void beforeRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, String targetNamespace,
                                  ClassifiedMappingRemapper mappingRemapper) {
            if(options.rvn()) VariableNameGenerator.startRecord();
        }

        @Override
        public void afterRunning(ClassifiedDeobfuscator.DeobfuscateOptions options, String targetNamespace,
                                 ClassifiedMappingRemapper mappingRemapper) throws IOException {
            if(options.rvn()) VariableNameGenerator.endRecord(Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES));
        }

        @Override
        public Function<ClassVisitor, ClassVisitor> getVisitor(ClassifiedDeobfuscator.DeobfuscateOptions options, ClassReader reader,
                                                               ClassMapping<? extends Mapping> mapping, String targetNamespace,
                                                               ClassifiedMappingRemapper mappingRemapper) {
            return parent -> {
                ClassVisitor cv = parent;
                if(options.rvn()) cv = new VariableNameGenerator(cv);
                if((reader.getAccess() & Opcodes.ACC_RECORD) != 0) cv = new RecordNameRemapper(cv);
                if(mapping.mapping instanceof NameGetter.Namespaced ngn) {
                    ngn.setMappedNamespace(targetNamespace);
                    cv = new LVTRemapper(cv, (ClassMapping<NamespacedMapping>) mapping, mappingRemapper);
                }
                return new RuntimeParameterAnnotationFixer(new ClassRemapper(cv, mappingRemapper));
            };
        }
    }
}