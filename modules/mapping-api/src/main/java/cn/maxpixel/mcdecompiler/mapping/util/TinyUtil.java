package cn.maxpixel.mcdecompiler.mapping.util;

public final class TinyUtil {
    private TinyUtil() {
        throw new AssertionError("No instances");
    }

    public static String escape(String unescaped) {
        StringBuilder sb = new StringBuilder(unescaped.length() + 16);
        int mark = 0;
        for (int i = 0; i < unescaped.length(); i++) {
            char escaped = switch (unescaped.charAt(i)) {
                case '\\' -> '\\';
                case '\n' -> 'n';
                case '\r' -> 'r';
                case '\t' -> 't';
                case '\0' -> '0';
                default -> '\0';
            };
            if (escaped != 0) {
                if (mark < i) sb.append(unescaped, mark, i);
                mark = i + 1;
                sb.append('\\').append(escaped);
            }
        }
        return sb.append(unescaped, mark, unescaped.length()).toString();
    }

    public static String unescape(String escaped) {
        return unescape(escaped, 0);
    }

    public static String unescape(String escaped, int beginIndex) {
        StringBuilder sb = new StringBuilder(escaped.length());
        int mark = beginIndex;
        for (int i = escaped.indexOf('\\', beginIndex); i >= 0; i = escaped.indexOf('\\', mark)) {
            char unescaped = switch (escaped.charAt(++i)) {
                case '\\' -> '\\';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case '0' -> '\0';
                default -> throw new IllegalArgumentException("Unknown escape character: \\" + escaped.charAt(i));
            };
            if (mark < i - 1) sb.append(escaped, mark, i - 1);
            mark = i + 1;
            sb.append(unescaped);
        }
        return sb.append(escaped, mark, escaped.length()).toString();
    }
}