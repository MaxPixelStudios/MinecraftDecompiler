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

package cn.maxpixel.mcdecompiler.mapping.component;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * The component that holds documentation about a class, field, method, or parameter.
 *
 * @apiNote The content is a list of strings, which each element represents exactly <b>one</b> line,
 * so {@code \n} and {@code \r} are not allowed. An empty line must be represented by {@code ""}.
 * {@code null} elements are prohibited
 */
public class Documented implements Component {
    /**
     * The contents
     */
    public final ObjectArrayList<@NotNull String> contents = new ObjectArrayList<>();

    /**
     * No-arg constructor
     */
    public Documented() {}

    /**
     * Constructor
     * @param contentString The content string which will be set by {@link #setContentString(String)}
     */
    public Documented(String contentString) {
        setContentString(contentString);
    }

    /**
     * Gets the contents
     * @return The contents
     */
    public List<@NotNull String> getContents() {
        return contents;
    }

    /**
     * Join the contents with {@code \n}
     * @return the joined string
     */
    public @NotNull String getContentString() {
        if (contents.isEmpty()) return "";
        return String.join("\n", contents);
    }

    /**
     * Breaks the string into lines and sets them as contents. An empty string will simply be ignored
     * @param content the string
     */
    public void setContentString(@NotNull String content) {
        int mark = 0;
        for (int i = content.indexOf('\n'); i >= 0; i = content.indexOf('\n', mark)) {
            contents.add(content.substring(mark, content.charAt(i - 1) == '\r' ? i - 1 : i));
            mark = i + 1;
        }
        if (mark < content.length()) contents.add(content.substring(mark));
    }

    @Override
    public void validate() throws IllegalStateException {
        for (String content : contents) {
            if (content.indexOf('\n') != -1 || content.indexOf('\r') != -1)
                throw new IllegalStateException("The document contains invalid characters");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Documented that)) return false;
        return contents.equals(that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contents);
    }
}