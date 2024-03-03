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

package cn.maxpixel.mcdecompiler.remapper.variable;

import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;

public final class ForgeFlowerAbstractParametersRecorder {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ObjectArrayList<String> generated = new ObjectArrayList<>();
    private boolean recording;

    public void startRecord() {
        if (recording) throw new IllegalStateException("Record already started");
        generated.clear();
        LOGGER.trace("Cleared previously generated abstract parameter names(if any)");
        recording = true;
        LOGGER.debug("Started to record the generated abstract method parameter names");
    }

    public boolean isRecording() {
        return recording;
    }

    public void endRecord(@NotNull Path writeTo) throws IOException {
        if (!recording) throw new IllegalStateException("Record not started yet");
        FileUtil.deleteIfExists(writeTo);
        Files.writeString(FileUtil.ensureFileExist(writeTo), String.join("\n", generated),
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.debug("Saved record to {}", writeTo);
        recording = false;
        LOGGER.debug("Ended record");
    }

    public void record(String className, String methodName, String methodDescriptor,
                       VariableNameProvider.RenameAbstractFunction provider, boolean omitThis) {
        LOGGER.trace("Record of abstract parameter names started for method {}{} in class {}",
                methodName, methodDescriptor, className);
        StringJoiner joiner = new StringJoiner(" ").add(className).add(methodName).add(methodDescriptor);
        Renamer renamer = new Renamer();
        Type[] types = Type.getArgumentTypes(methodDescriptor);
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            String name = provider != null ? provider.getName(omitThis ? i : i + 1, type) : null;
            joiner.add(name != null ? renamer.addExistingName(name, i) : renamer.getVarName(type, i));
        }
        generated.add(joiner.toString());
        LOGGER.trace("Record of abstract parameter names completed for method {}{1} in class {}",
                methodName, methodDescriptor, className);
    }
}