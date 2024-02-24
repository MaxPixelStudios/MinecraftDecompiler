package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.util.DescriptorUtil;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MappingRemapper {
    @NotNull String mapClass(@NotNull String name);

    @NotNull String unmapClass(@NotNull String name);

    /**
     * Map a field.
     *
     * @implSpec Should directly return "name"(no copies, etc.) when mapped name is not found.
     * @param owner Owner of the method.
     * @param name Name to map.
     * @return Mapped name if present. Provided name otherwise.
     */
    @NotNull String mapField(@NotNull String owner, @NotNull String name);

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
    @NotNull String mapMethod(@NotNull String owner, @NotNull String name, @Nullable("When desc doesn't matter") String desc);

    @Subst("I")
    default @Pattern(Constants.FIELD_DESC_PATTERN) String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String unmappedDesc) {
        return mapDesc(unmappedDesc, true);
    }

    @Subst("()V")
    default @Pattern(Constants.METHOD_DESC_PATTERN) String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String unmappedDesc) {
        return mapMethodDesc(unmappedDesc, true);
    }

    @Subst("I")
    default @Pattern(Constants.FIELD_DESC_PATTERN) String unmapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String mappedDesc) {
        return mapDesc(mappedDesc, false);
    }

    @Subst("()V")
    default @Pattern(Constants.METHOD_DESC_PATTERN) String unmapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String mappedDesc) {
        return mapMethodDesc(mappedDesc, false);
    }

    @Subst("I")
    private String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String desc, boolean map) {
        int i = 0;
        if (desc.charAt(0) == '[') while (desc.charAt(++i) == '[');
        return switch (desc.charAt(i)) {
            case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S' -> desc;
            case 'L' -> {
                StringBuilder ret = new StringBuilder(desc.length()).append(desc, 0, ++i);
                int j = desc.indexOf(';', i + 1);// skip 'L' and the first char
                if (j < 0) DescriptorUtil.throwInvalid(true);
                yield ret.append(map ? mapClass(desc.substring(i, j)) : unmapClass(desc.substring(i, j)))
                        .append(desc, j, desc.length()).toString();
            }
            default -> DescriptorUtil.throwInvalid(true);
        };
    }

    @Subst("()V")
    private String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String desc, boolean map) {
        if (desc.length() == 3 || desc.indexOf('L') < 0) return desc;// no need to map
        StringBuilder ret = new StringBuilder(desc.length());
        int start = 0;
        for (int i = 1; i < desc.length(); i++) {
            switch (desc.charAt(i)) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S', '[', ')' -> {} // no op
                case 'L' -> {
                    ret.append(desc, start, ++i);
                    start = desc.indexOf(';', i + 1);// skip 'L'(++i) and the first char
                    if (start < 0) DescriptorUtil.throwInvalid(true);
                    ret.append(map ? mapClass(desc.substring(i, start)) : unmapClass(desc.substring(i, start)));
                }
                default -> DescriptorUtil.throwInvalid(true);
            }
        }
        return ret.append(desc, start, desc.length()).toString();
    }
}