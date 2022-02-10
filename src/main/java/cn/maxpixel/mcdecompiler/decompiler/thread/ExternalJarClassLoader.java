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

import cn.maxpixel.mcdecompiler.util.Utils;

import java.net.URL;
import java.net.URLClassLoader;

public class ExternalJarClassLoader extends URLClassLoader {
    public ExternalJarClassLoader(URL[] urls, ClassLoader parent) throws ReflectiveOperationException {
        super(concatUrls(urls, Utils.getClassPath()), parent);
    }

    private static URL[] concatUrls(URL[] urls1, URL[] urls2) {
        URL[] ret = new URL[urls1.length + urls2.length];
        System.arraycopy(urls1, 0, ret, 0, urls1.length);
        System.arraycopy(urls2, 0, ret, urls1.length, urls2.length);
        return ret;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(name.startsWith("cn.maxpixel.mcdecompiler.decompiler.thread")) {
            synchronized(getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if(c == null) {
                    c = findClass(name);
                }
                if(resolve) resolveClass(c);
                return c;
            }
        }
        return super.loadClass(name, resolve);
    }
}