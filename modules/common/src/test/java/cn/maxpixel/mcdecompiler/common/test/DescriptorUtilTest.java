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

package cn.maxpixel.mcdecompiler.common.test;

import cn.maxpixel.mcdecompiler.common.util.DescriptorUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DescriptorUtilTest {
    @Test
    void testGetMethodReturnDescriptor() {
        assertEquals("V", DescriptorUtil.getMethodReturnDescriptor("()V"));
        assertEquals("F", DescriptorUtil.getMethodReturnDescriptor("(IJLa;)F"));
        assertEquals("Lawds/wwss;", DescriptorUtil.getMethodReturnDescriptor("(ISFJ)Lawds/wwss;"));
        assertThrows(IllegalArgumentException.class, () -> DescriptorUtil.getMethodReturnDescriptor("(I"));
    }

    @Test
    void testGetArgumentCount() {
        assertEquals(0, DescriptorUtil.getArgumentCount("()V"));
        assertEquals(1, DescriptorUtil.getArgumentCount("(Ljava/lang/String;)V"));
        assertEquals(1, DescriptorUtil.getArgumentCount("([Ljava/lang/String;)F"));
        assertEquals(2, DescriptorUtil.getArgumentCount("([Ljava/lang/String;Ljava/lang/Integer;)I"));
        assertEquals(3, DescriptorUtil.getArgumentCount("([Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Long;)Z"));
    }

    @Test
    void testThrowInvalid() {
        assertThrowsExactly(IllegalArgumentException.class, () -> DescriptorUtil.throwInvalid(true), "Invalid method descriptor");
        assertThrowsExactly(IllegalArgumentException.class, () -> DescriptorUtil.throwInvalid(false), "Invalid descriptor");
    }
}