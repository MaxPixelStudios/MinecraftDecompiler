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

package cn.maxpixel.mcdecompiler.decompiler.thread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class ExternalJarClassLoader extends URLClassLoader {
    public ExternalJarClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(name.startsWith("cn.maxpixel.mcdecompiler.decompiler.thread")) {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    URL resource = getParent().getResource(name.replace('.', '/').concat(".class"));
                    try (InputStream in = resource.openStream()) {
                        byte[] bytes = new byte[4096];
                        int len = 0;
                        while (true) {
                            int read = in.read(bytes, len, bytes.length - len);
                            if (read < 0) break;
                            if ((len += read) >= bytes.length) {
                                bytes = Arrays.copyOf(bytes, bytes.length * 2);
                            }
                        }
                        c = defineClass(name, bytes, 0, len);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(e.getMessage(), e);
                    }
                }
                if (resolve) resolveClass(c);
                return c;
            }
        }
        return super.loadClass(name, resolve);
    }
}