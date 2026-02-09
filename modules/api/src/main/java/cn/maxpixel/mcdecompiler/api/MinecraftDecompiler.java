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

package cn.maxpixel.mcdecompiler.api;

import cn.maxpixel.mcdecompiler.api.extension.ExtensionManager;
import cn.maxpixel.mcdecompiler.api.util.DataMap;
import cn.maxpixel.mcdecompiler.api.util.FileUtil;
import cn.maxpixel.mcdecompiler.api.util.JarUtil;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair.of;

/**
 * The core class of the application.
 * <p>
 * This class accepts actions and execute them. Starting the execution, the class will receive an input,
 * which can either be a jar or a directory, and pass it to actions. The actions will be executed in the
 * order when they were added. For each action, it will receive input from the output of the previous action,
 * or the input of this class if it is the first action. Its output will be passed as input to the next action.
 * You can request an intermediate output if you need the output of the action. An intermediate output consists of
 * the output of the action and the others in the state when the action is just finished.
 */
public class MinecraftDecompiler {
    private static final Logger LOGGER = LogManager.getLogger();
    static {
        ExtensionManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteIfExists(Directories.TEMP_DIR)));
    }

    {
        FileUtil.deleteIfExists(Directories.TEMP_DIR);
    }

    private final ObjectArrayList<ObjectObjectImmutablePair<Action, Optional<Path>>> actions = new ObjectArrayList<>();
    private final DataMap dataMap = new DataMap();

    public void add(Action action) {
        add(action, false);
    }

    public void add(int i, Action action) {
        add(i, action, false);
    }

    public void add(Action action, boolean createIntermediateOutput) {
        actions.add(of(Objects.requireNonNull(action, "Action cannot be null"),
                createIntermediateOutput ? Optional.empty() : null));
    }

    public void add(int i, Action action, boolean createIntermediateOutput) {
        actions.add(i, of(Objects.requireNonNull(action, "Action cannot be null"),
                createIntermediateOutput ? Optional.empty() : null));
    }

    public void add(Action action, Path intermediateOutput) {
        actions.add(of(Objects.requireNonNull(action, "Action cannot be null"),
                Optional.of(intermediateOutput)));
    }

    public void add(int i, Action action, Path intermediateOutput) {
        actions.add(i, of(Objects.requireNonNull(action, "Action cannot be null"),
                Optional.of(intermediateOutput)));
    }

    public void execute(Path input) throws Exception {
        ExtensionManager.setup();
        Files.createDirectories(Directories.TEMP_DIR);
        Path actionInput = input.toAbsolutePath().normalize();
        try (FileSystem fs = JarUtil.createZipFsOrNullIfDir(actionInput)) {
            Path root = JarUtil.rootOrElse(fs, actionInput);
            for (int i = 0; i < actions.size(); i++) {
                var action = actions.get(i).left();
                try {
                    action.preprocess(root);
                } catch (Exception e) {
                    LOGGER.fatal("Error when action \"\" is preprocessing", action.getName(), e);
                    for (int j = 0; j < actions.size(); j++) try {
                        actions.get(j).left().close();
                    } catch (Exception ex) {
                        LOGGER.warn("Error when cleaning up action \"{}\"", action.getName(), ex);
                        e.addSuppressed(ex);
                    }
                    throw e;
                }
            }
            ExtensionManager.onPreprocess(fs, Directories.TEMP_DIR, dataMap);
        }
        Path others = Directories.TEMP_DIR.resolve("others.jar").toAbsolutePath().normalize();
        for (int i = 0; i < actions.size(); i++) {
            var pair = actions.get(i);
            var action = pair.left();
            Path output = Directories.TEMP_DIR.resolve(action.getDefaultOutputName()).toAbsolutePath().normalize();
            try {
                action.executeRaw(actionInput, others, output);
            } catch (Exception e) {
                LOGGER.fatal("Error when executing action \"{}\"", action.getName(), e);
                for (int j = i; j < actions.size(); j++) try {
                    actions.get(j).left().close();
                } catch (Exception ex) {
                    LOGGER.warn("Error when cleaning up action \"{}\"", action.getName(), ex);
                    e.addSuppressed(ex);
                }
                throw e;
            }
            try {
                action.close();
            } catch (Exception e) {
                LOGGER.warn("Error when cleaning up action \"{}\"", action.getName(), e);
            }
            if (pair.right() != null) {
                Path intermediateOutput = pair.right().orElseGet(() -> {
                    String inName = input.getFileName().toString();
                    int index = inName.lastIndexOf('.');
                    return Path.of((index != -1 ? inName.substring(0, index) : inName) + '_' + action.getDefaultOutputName());
                });
                try {
                    FileUtil.deleteIfExists(intermediateOutput);
                    if (Files.isDirectory(output)) FileUtil.copyDirectory(output, intermediateOutput);
                    else try (FileSystem ofs = JarUtil.createZipFs(output)) {
                        FileUtil.copyDirectory(ofs.getPath(""), intermediateOutput);
                    }
                    try (FileSystem ofs = JarUtil.createZipFs(others);
                         DirectoryStream<Path> ds = Files.newDirectoryStream(ofs.getPath(""))) {
                        for (var entry : ds) {
                            FileUtil.copyDirectory(entry, intermediateOutput);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error when generating intermediate output for action \"{}\" at \"{}\"",
                            action.getName(), intermediateOutput.toAbsolutePath().normalize(), e);
                }
            }
            actionInput = output;
        }
    }
}