/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.test.mappings;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingTest {
    @Test
    void testPaired() {
        Descriptor.Unmapped du = new Descriptor.Unmapped("La;");
        Descriptor.Mapped dm = new Descriptor.Mapped("Lb;");
        Owned<PairedMapping> o = new Owned<>();

        PairedMapping m1 = new PairedMapping();
        assertNull(m1.unmappedName);
        assertNull(m1.mappedName);
        assertTrue(m1.getComponents().isEmpty());
        assertThrows(IllegalStateException.class, m1::validate);

        PairedMapping m2 = new PairedMapping(du);
        assertNull(m2.unmappedName);
        assertNull(m2.mappedName);
        assertEquals(1, m2.getComponents().size());
        assertThrows(IllegalStateException.class, m2::validate);

        PairedMapping m3 = new PairedMapping("a");
        assertEquals("a", m3.unmappedName);
        assertEquals("a", m3.mappedName);
        assertTrue(m3.getComponents().isEmpty());
        assertDoesNotThrow(m3::validate);

        PairedMapping m4 = new PairedMapping("a", du);
        assertEquals("a", m4.unmappedName);
        assertEquals("a", m4.mappedName);
        assertEquals(1, m4.getComponents().size());

        PairedMapping m5 = new PairedMapping("a", "b");
        assertEquals("a", m5.unmappedName);
        assertEquals("b", m5.mappedName);
        assertTrue(m5.getComponents().isEmpty());

        PairedMapping m6 = new PairedMapping("a", "b", du);
        assertEquals("a", m6.unmappedName);
        assertEquals("b", m6.mappedName);
        assertEquals(1, m6.getComponents().size());

        m1.unmappedName = "a";
        m1.mappedName = "b";
        assertEquals("a", m1.unmappedName);
        assertEquals("b", m1.mappedName);
        assertSame(m1.unmappedName, m1.getUnmappedName());
        assertSame(m1.mappedName, m1.getMappedName());
        assertDoesNotThrow(m1::validate);

        m2.setUnmappedName("a");
        m2.setMappedName("b");
        assertEquals("a", m2.getUnmappedName());
        assertEquals("b", m2.getMappedName());
        assertSame(m2.unmappedName, m2.getUnmappedName());
        assertSame(m2.mappedName, m2.getMappedName());
        assertDoesNotThrow(m2::validate);

        assertSame(o, new PairedMapping(o).getOwned());

        m6.reverse();
        assertTrue(m6.hasComponent(Descriptor.Mapped.class));
        assertEquals("La;", m6.getComponent(Descriptor.Mapped.class).descriptor);
        assertEquals("b", m6.unmappedName);
        assertEquals("a", m6.mappedName);
        assertEquals(1, m6.getComponents().size());

        m6.reverse();
        assertTrue(m6.hasComponent(Descriptor.Unmapped.class));
        assertEquals("La;", m6.getComponent(Descriptor.Unmapped.class).descriptor);
        assertEquals("a", m6.unmappedName);
        assertEquals("b", m6.mappedName);
        assertEquals(1, m6.getComponents().size());

        PairedMapping m7 = new PairedMapping("a", "b", du, dm);
        assertEquals(2, m7.getComponents().size());
        m7.reverse();
        assertEquals(2, m7.getComponents().size());
        assertTrue(m7.hasComponent(Descriptor.Unmapped.class));
        assertTrue(m7.hasComponent(Descriptor.Mapped.class));
        assertEquals("La;", m7.getComponent(Descriptor.Mapped.class).descriptor);
        assertEquals("Lb;", m7.getComponent(Descriptor.Unmapped.class).descriptor);
        assertEquals("b", m7.unmappedName);
        assertEquals("a", m7.mappedName);
        m7.reverse();
        assertEquals(2, m7.getComponents().size());
        assertTrue(m7.hasComponent(Descriptor.Unmapped.class));
        assertTrue(m7.hasComponent(Descriptor.Mapped.class));
        assertEquals("La;", m7.getComponent(Descriptor.Unmapped.class).descriptor);
        assertEquals("Lb;", m7.getComponent(Descriptor.Mapped.class).descriptor);
        assertEquals("a", m7.unmappedName);
        assertEquals("b", m7.mappedName);
    }

    @Test
    void testNamespaced() {

    }
}