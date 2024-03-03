package cn.maxpixel.mcdecompiler.remapper.variable;

import org.objectweb.asm.Type;

public class LambdaRenamer extends Renamer {
    private final Renamer owner;
    private final int skips;
    private boolean prepared;

    public LambdaRenamer(Renamer owner, int skips) {
        this.owner = owner;
        this.skips = skips;
    }

    @Override
    public void prepare() {
        if (prepared) throw new IllegalStateException("Already prepared");
        vars.putAll(owner.vars);
        prepared = true;
    }

    @Override
    public String addExistingName(String name, int index) {
        if (!prepared) throw new IllegalStateException("Not prepared");
        if (index < skips) return name;
        return super.addExistingName(name, index);
    }

    @Override
    public String getVarName(Type type, int index) {
        if (!prepared) throw new IllegalStateException("Not prepared");
        if (index < skips) return null;
        return super.getVarName(type, index);
    }
}