/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2026 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.api.Action;
import cn.maxpixel.mcdecompiler.api.Directories;
import cn.maxpixel.mcdecompiler.api.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.api.util.FileUtil;
import cn.maxpixel.mcdecompiler.api.util.IOUtil;
import cn.maxpixel.mcdecompiler.api.util.JarUtil;
import cn.maxpixel.mcdecompiler.utils.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DecompilerAction implements Action {
    private static final Logger LOGGER = LogManager.getLogger();

    private final IDecompiler decompiler;
    private final DecompilationOptions options;

    public DecompilerAction() {
        this(DecompilationOptions.DEFAULT);
    }

    public DecompilerAction(DecompilationOptions options) {
        this.decompiler = Decompilers.get(options.decompilerName());
        this.options = options;
        if (decompiler == null) throw new IllegalArgumentException("Decompiler \"" + options.decompilerName() + "\" does not exist");
    }

    @Override
    public String getName() {
        return "decompiler";
    }

    @Override
    public String getDefaultOutputName() {
        return "decompiled";
    }

    @Override
    public void executeRaw(Path input, Path others, Path output) throws IOException {
        try (FileSystem jarFs = JarUtil.createZipFsOrNullIfDir(input)) {
            Path inputRoot = JarUtil.rootOrElse(jarFs, input);
            try (var ds = Files.newDirectoryStream(inputRoot)) {
                if (!ds.iterator().hasNext()) {
                    LOGGER.info("Nothing to decompile, skipping decompilation");
                    return;
                }
            }
            LOGGER.info("Decompiling using \"{}\"", decompiler.name());
            FileUtil.deleteIfExists(output);
            Path libDownloadPath = Files.createDirectories(Directories.DOWNLOAD_DIR.resolve("libs").toAbsolutePath().normalize());
            if (decompiler instanceof IExternalResourcesDecompiler erd)
                erd.extractTo(Directories.TEMP_DIR.toAbsolutePath().normalize());
            Predicate<Path> incFilter = null;
            if (decompiler instanceof ILibRecommendedDecompiler lrd) {
                ObjectSet<Path> libs = options.bundledLibs() != null ? options.bundledLibs() :
                        DownloadingUtil.downloadLibraries(options.version(), libDownloadPath);
                if (options.incrementalInput() != null && options.incrementalOutput() != null &&
                        decompiler.getSourceType() == IDecompiler.SourceType.DIRECTORY) {// TODO: Test if this works
                    var incIn = options.incrementalInput();
                    var incOut = options.incrementalOutput();
                    try (FileSystem incInFs = JarUtil.createZipFsOrNullIfDir(incIn);
                         Stream<Path> paths = FileUtil.iterateFiles(JarUtil.rootOrElse(incInFs, incIn))) {
                        ObjectOpenHashSet<String> possibleInnerClasses = new ObjectOpenHashSet<>();
                        ObjectOpenHashSet<String> mayRemove = new ObjectOpenHashSet<>();
                        try (FileSystem incOutFs = JarUtil.createZipFsOrNullIfDir(incOut)) {
                            FileUtil.copyDirectory(JarUtil.rootOrElse(incOutFs, incOut), output);
                        }
                        paths.forEach(p -> {
                            String path = incInFs == null ? incIn.relativize(p).toString() : p.toString();
                            if (path.endsWith(".class")) {
                                String fileName = p.getFileName().toString();
                                Path p2 = inputRoot.resolve(path);
                                if (Files.exists(p2)) {
                                    try {
                                        if (Arrays.equals(IOUtil.readAllBytes(p), IOUtil.readAllBytes(p2))) {
                                            synchronized (mayRemove) {
                                                mayRemove.add(path);
                                            }
                                        } else {
                                            String toAdd = fileName.lastIndexOf('$') > 0 ? path.substring(0,
                                                    path.indexOf('$', path.lastIndexOf(fileName))) : path;
                                            synchronized (possibleInnerClasses) {
                                                possibleInnerClasses.add(toAdd);
                                            }
                                        }
                                    } catch (IOException e) {
                                        throw Utils.wrapInRuntime(e);
                                    }
                                } else { // deleted classes(delete java files here)
                                    FileUtil.deleteIfExists(output.resolve(path.replace(".class", ".java")));
                                }
                            }
                        });
                        ObjectOpenHashSet<String> toRemove = new ObjectOpenHashSet<>();
                        for (String entry : mayRemove) {
                            int i = entry.indexOf('$', 1);
                            String key = i > 0 ? entry.substring(0, i) : entry.substring(0, entry.length() - 6);// Remove ".class"
                            if (!possibleInnerClasses.contains(key)) toRemove.add(entry);
                        }
                        incFilter = p -> !toRemove.contains(inputRoot.relativize(p).toString());
                    }
                    libs.add(input);
                }
                if (!libs.isEmpty()) lrd.receiveLibs(libs);
            }
            Files.createDirectories(output);
            switch (decompiler.getSourceType()) {
                case DIRECTORY -> {
                    if (jarFs == null && incFilter == null) {
                        decompiler.decompile(input, output);
                    } else {
                        Path decompileClasses = Directories.TEMP_DIR.resolve("decompileClasses").toAbsolutePath().normalize();
                        try (Stream<Path> s = optFilter(FileUtil.iterateFiles(inputRoot), incFilter)) {
                            s.forEach(p -> FileUtil.copyFile(p, decompileClasses.resolve(inputRoot.relativize(p).toString())));
                        }
                        decompiler.decompile(decompileClasses, output);
                    }
                }
                case FILE -> {
                    if (jarFs != null && incFilter == null) {
                        decompiler.decompile(input, output);
                    } else {
                        Path decompileClasses = Directories.TEMP_DIR.resolve("decompileClasses.jar").toAbsolutePath().normalize();
                        try (Stream<Path> s = optFilter(FileUtil.iterateFiles(inputRoot), incFilter);
                             FileSystem fs = JarUtil.createZipFs(FileUtil.makeParentDirs(decompileClasses), true)) {
                            s.forEach(p -> FileUtil.copyFile(p, fs.getPath(inputRoot.relativize(p).toString())));
                        }
                        decompiler.decompile(decompileClasses, output);
                    }
                }
            }
        }
    }

    private static Stream<Path> optFilter(Stream<Path> s, Predicate<Path> p) {
        return p == null ? s : s.filter(p);
    }

    @Override
    public void execute(Path input, Path others, Path output) throws IOException {
        throw new UnsupportedOperationException();
    }
}