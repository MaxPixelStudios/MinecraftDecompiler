package cn.xiaopangxie732.mcdecompiler.mapping;

public class MethodMapping extends Mapping {
	private int[] linenumber = new int[2];
	private String returnVal;
	private String[] argTypes;
	public MethodMapping() {}
	public MethodMapping(String obfuscatedName, String originalName, int linenumber1,
	                     int linenumber2, String returnVal, String[] argTypes) {
		super(obfuscatedName, originalName);
		this.linenumber[0] = linenumber1;
		this.linenumber[1] = linenumber2;
		this.returnVal = returnVal;
		this.argTypes = argTypes;
	}

	public int[] getLinenumber() {
		return linenumber;
	}
	public void setLinenumber(int linenumber1, int linenumber2) {
		this.linenumber[0] = linenumber1;
		this.linenumber[1] = linenumber2;
	}
	public String getReturnVal() {
		return returnVal;
	}
	public void setReturnVal(String returnVal) {
		this.returnVal = returnVal;
	}
	public String[] getArgTypes() {
		return argTypes;
	}
	public void setArgTypes(String[] argTypes) {
		this.argTypes = argTypes;
	}
}