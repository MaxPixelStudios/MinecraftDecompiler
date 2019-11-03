package cn.maxpixel.mcdecompiler;

public interface Info {
	static String getMappingPath(String version, Info.MappingType type) {
		return "downloads/" + version + "/" + type + "_mappings.txt";
	}
	static String getMcJarPath(String version, Info.MappingType type) {
		return "downloads/" + version + "/" + type + ".jar";
	}
	static String getDeobfuscateJarPath(String version) {
		return "target/" + version + "/" + version + "_deobfuscated.jar";
	}
	String TEMP_PATH = "temp";
	enum MappingType {
		CLIENT,
		SERVER;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}