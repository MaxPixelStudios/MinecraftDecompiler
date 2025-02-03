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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.component.Component;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingComponentTest {
    private static class TestMapping extends Mapping {
        public TestMapping(@NotNull Component @NotNull ... components) {
            super(components);
        }

        public TestMapping() {
            super();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Owned<TestMapping> getOwned() {
            return getComponent(Owned.class);
        }

        @Override
        public String getUnmappedName() {
            return "";
        }

        @Override
        public String getMappedName() {
            return "";
        }
    }

    @Test
    void testConstructorAndGet() {
        assertTrue(new TestMapping().getComponents().isEmpty());
        assertEquals(1, new TestMapping(new Descriptor.Unmapped("La;")).getComponents().size());
        assertEquals(1, new TestMapping(new Descriptor.Unmapped("La;"),
                new Descriptor.Unmapped("Lb;")).getComponents().size());
        assertEquals(2, new TestMapping(new Descriptor.Unmapped("La;"),
                new Descriptor.Mapped("Lb;")).getComponents().size());
    }

    @Test
    void testAddRemoveHasGet() {
        var du1 = new Descriptor.Unmapped("La;");
        var du2 = new Descriptor.Unmapped("Lb;");
        var dm1 = new Descriptor.Mapped("Lc;");
        var m1 = new TestMapping();
        m1.addComponent(du1);
        assertTrue(m1.hasComponent(Descriptor.Unmapped.class));
        assertEquals(1, m1.getComponents().size());
        assertSame(du1, m1.getComponent(Descriptor.Unmapped.class));
        m1.addComponent(du2);
        assertTrue(m1.hasComponent(Descriptor.Unmapped.class));
        assertEquals(1, m1.getComponents().size());
        assertSame(du2, m1.getComponent(Descriptor.Unmapped.class));
        m1.addComponent(dm1);
        assertTrue(m1.hasComponent(Descriptor.Mapped.class));
        assertEquals(2, m1.getComponents().size());
        assertSame(dm1, m1.getComponent(Descriptor.Mapped.class));
        m1.removeComponent(Descriptor.Unmapped.class);
        assertFalse(m1.hasComponent(Descriptor.Unmapped.class));
        assertTrue(m1.hasComponent(Descriptor.Mapped.class));
        assertEquals(1, m1.getComponents().size());
        assertSame(dm1, m1.getComponent(Descriptor.Mapped.class));
    }

    @Test
    void testGet() {
        var du1 = new Descriptor.Unmapped("La;");
        var dm1 = new Descriptor.Mapped("Lc;");
        var owned = new Owned<TestMapping>();
        var m1 = new TestMapping(du1, dm1, owned);
        var m2 = new TestMapping(du1, dm1);
        assertEquals(3, m1.getComponents().size());
        assertTrue(m1.getComponents().containsAll(List.of(du1, dm1, owned)));
        assertSame(du1, m1.getComponent(Descriptor.Unmapped.class));
        assertSame(owned, m1.getOwned());
        assertNull(m1.getComponent(Documented.class));
        assertNull(m2.getOwned());
        assertNotNull(m1.getComponentOptional(Documented.class));
        assertFalse(m1.getComponentOptional(Documented.class).isPresent());
        assertNotNull(m1.getComponentOptional(Descriptor.Unmapped.class));
        assertTrue(m1.getComponentOptional(Descriptor.Unmapped.class).isPresent());
        assertSame(du1, m1.getComponentOptional(Descriptor.Unmapped.class).get());
        assertThrows(NullPointerException.class, () -> m1.getOrCreateComponent(Documented.class, () -> null));
        assertNotNull(m1.getOrCreateComponent(Documented.class, Documented::new));
        assertTrue(m1.hasComponent(Documented.class));
        assertNotNull(m1.getComponent(Documented.class));
    }

    @Test
    void testValidate() {
        var du1 = new Descriptor.Unmapped("La");
        var du2 = new Descriptor.Unmapped("J");
        var dm1 = new Descriptor.Mapped("Lb;");
        var m = new TestMapping(du1);
        assertThrows(IllegalStateException.class, m::validate);
        m.addComponent(du2);
        assertDoesNotThrow(m::validate);
        assertDoesNotThrow(new TestMapping(du2)::validate);
        assertDoesNotThrow(new TestMapping(dm1)::validate);
        assertDoesNotThrow(new TestMapping(du2, dm1)::validate);
        assertThrows(IllegalStateException.class, new TestMapping(du1, dm1)::validate);
    }
}