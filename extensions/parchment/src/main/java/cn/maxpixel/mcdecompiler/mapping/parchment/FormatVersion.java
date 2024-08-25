package cn.maxpixel.mcdecompiler.mapping.parchment;

public record FormatVersion(int major, int minor, int patch) {
    public static FormatVersion CURRENT = new FormatVersion(1, 1, 0);

    public FormatVersion {
        if (major < 0) throw new IllegalArgumentException("Major version " + major + "must not be negative");
        if (minor < 0) throw new IllegalArgumentException("Minor version " + major + "must not be negative");
        if (patch < 0) throw new IllegalArgumentException("Patch version " + major + "must not be negative");
    }

    public static FormatVersion from(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Expected at least 2 tokens for version " + version);
        if (parts.length > 3) throw new IllegalArgumentException("Expected at most 3 tokens for version " + version);
        return new FormatVersion(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                parts.length == 3 ? Integer.parseInt(parts[2]) : 0
        );
    }

    public boolean compatibleWith(FormatVersion v) {
        return major == v.major;
    }

    @Override
    public String toString() {
        return major + "." + minor + '.' + patch;// need at least 1 string to make sure this is string concatenation
    }
}
