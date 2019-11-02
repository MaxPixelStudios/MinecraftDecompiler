package cn.xiaopangxie732.mcdecompiler;

public interface Info {
	enum MappingType {
		CLIENT,
		SERVER;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}