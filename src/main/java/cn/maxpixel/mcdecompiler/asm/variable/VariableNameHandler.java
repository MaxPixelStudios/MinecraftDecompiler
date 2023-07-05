package cn.maxpixel.mcdecompiler.asm.variable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VariableNameHandler {
    private final ObjectArrayList<VariableNameProvider> providers = new ObjectArrayList<>();
    private boolean omitThis;

    public void addProvider(VariableNameProvider provider) {
        this.providers.add(provider);
    }

    public VariableNameProvider.RenameFunction handleMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (providers.isEmpty()) return VariableNameProvider.RenameFunction.NOP;
        ObjectArrayList<VariableNameProvider.RenameFunction> functions = new ObjectArrayList<>();
        for (VariableNameProvider provider : providers) {
            VariableNameProvider.RenameFunction function = provider.forMethod(access, name, descriptor, signature, exceptions);
            if (function != VariableNameProvider.RenameFunction.NOP) functions.add(function);
        }
        return (originalName, descriptor1, signature1, start, end, index) -> {
            for (VariableNameProvider.RenameFunction function : functions) {
                String n = function.getName(originalName, descriptor1, signature1, start, end, index);
                if (n != null) return n;
            }
            return null;
        };
    }

    public VariableNameProvider.RenameAbstractFunction handleAbstractMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (providers.isEmpty()) return VariableNameProvider.RenameAbstractFunction.NOP;
        ObjectArrayList<VariableNameProvider.RenameAbstractFunction> functions = new ObjectArrayList<>();
        for (VariableNameProvider provider : providers) {
            VariableNameProvider.RenameAbstractFunction function = provider.forAbstractMethod(access, name, descriptor, signature, exceptions);
            if (function != VariableNameProvider.RenameAbstractFunction.NOP) functions.add(function);
        }
        return (index, type) -> {
            for (VariableNameProvider.RenameAbstractFunction function : functions) {
                String n = function.getName(index, type);
                if (n != null) return n;
            }
            return null;
        };
    }

    public void setOmitThis() {
        this.omitThis = true;
    }

    public boolean omitThis() {
        return omitThis;
    }
}