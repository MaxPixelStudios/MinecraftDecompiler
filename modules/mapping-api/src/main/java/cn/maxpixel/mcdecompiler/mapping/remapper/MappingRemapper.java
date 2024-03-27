package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MappingRemapper {
    boolean hasClassMapping(String name);

    boolean isMethodStaticIdentifiable();

    @Nullable("When no corresponding mapping found") String mapClass(@NotNull String name);

    default @NotNull String mapClassOrDefault(@NotNull String name) {
        String ret = mapClass(name);
        return ret == null ? name : ret;
    }

    @Nullable("When no corresponding mapping found") String unmapClass(@NotNull String name);

    default @NotNull String unmapClassOrDefault(@NotNull String name) {
        String ret = unmapClass(name);
        return ret == null ? name : ret;
    }

    /**
     * Map a field.
     *
     * @implSpec Should directly return "name"(no copies, etc.) when mapped name is not found.
     * @param owner Owner of the method.
     * @param name Name to map.
     * @return Mapped name if present. Provided name otherwise.
     */
    @Nullable("When no corresponding mapping found") String mapField(@NotNull String owner, @NotNull String name);

    default @NotNull String mapFieldOrDefault(@NotNull String owner, @NotNull String name) {
        String ret = mapField(owner, name);
        return ret == null ? name : ret;
    }

    /**
     * Map a method.
     *
     * @apiNote When desc is null and multiple matches are found, a random match is returned.
     * @implSpec Should directly return "name"(no copies, etc.) when mapped name is not found.
     * @param owner Owner of the method.
     * @param name Name to map.
     * @param desc Descriptor of the method.
     * @return Mapped name if present. Provided name otherwise.
     */
    @Nullable("When no corresponding mapping found") String mapMethod(@NotNull String owner, @NotNull String name, @Nullable("When desc doesn't matter") String desc);

    default @NotNull String mapMethodOrDefault(@NotNull String owner, @NotNull String name, @Nullable("When desc doesn't matter") String desc) {
        String ret = mapMethod(owner, name, desc);
        return ret == null ? name : ret;
    }

    DescriptorRemapper getDescriptorRemapper();

    @Subst("I")
    default @Pattern(Constants.FIELD_DESC_PATTERN) String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String unmappedDesc) {
        return getDescriptorRemapper().mapDesc(unmappedDesc);
    }

    @Subst("()V")
    default @Pattern(Constants.METHOD_DESC_PATTERN) String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String unmappedDesc) {
        return getDescriptorRemapper().mapMethodDesc(unmappedDesc);
    }

    @Subst("I")
    default @Pattern(Constants.FIELD_DESC_PATTERN) String unmapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String mappedDesc) {
        return getDescriptorRemapper().unmapDesc(mappedDesc);
    }

    @Subst("()V")
    default @Pattern(Constants.METHOD_DESC_PATTERN) String unmapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String mappedDesc) {
        return getDescriptorRemapper().unmapMethodDesc(mappedDesc);
    }
}