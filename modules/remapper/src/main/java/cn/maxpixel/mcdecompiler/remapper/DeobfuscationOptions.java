package cn.maxpixel.mcdecompiler.remapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class DeobfuscationOptions {
    public static final DeobfuscationOptions DEFAULT = new DeobfuscationOptions();

    public final boolean includeOthers;
    public final boolean rvn;
    public final boolean reverse;
    public final Set<Path> extraJars;
    public final Set<String> extraClasses;
    public final Map<String, Map<String, String>> refMap;

    public DeobfuscationOptions() {
        this(true, false, false);
    }

    public DeobfuscationOptions(boolean includeOthers, boolean rvn, boolean reverse) {
        this(includeOthers, rvn, reverse, ObjectSets.emptySet(), ObjectSets.emptySet(), Object2ObjectMaps.emptyMap());
    }

    public DeobfuscationOptions(boolean includeOthers, boolean rvn, boolean reverse, Set<Path> extraJars,
                                Set<String> extraClasses, Map<String, Map<String, String>> refMap) {
        this.includeOthers = includeOthers;
        this.rvn = rvn;
        this.reverse = reverse;
        this.extraJars = extraJars;
        this.extraClasses = extraClasses;
        this.refMap = refMap;
    }
}