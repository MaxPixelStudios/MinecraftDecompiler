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