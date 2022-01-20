/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.namespaced.*;
import cn.maxpixel.mcdecompiler.mapping.paired.*;
import cn.maxpixel.mcdecompiler.mapping.proguard.ProguardFieldMapping;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public abstract class AbstractMappingWriter {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Writer");

    protected final ObjectArrayList<String> buf = new ObjectArrayList<>();
    protected final ClassifiedMappingRemapper remapper;

    private final ReentrantLock lock = new ReentrantLock();

    public AbstractMappingWriter() {
        this(null);
    }

    public AbstractMappingWriter(ClassifiedMappingRemapper remapper) {
        this.remapper = remapper;
    }

    protected final boolean notDescImpl(AbstractMapping mapping) {
        return !(mapping instanceof Descriptor || mapping instanceof Descriptor.Mapped);
    }

    private void ensurePackageGenerator() {
        if(!(getGenerator() instanceof PackageMappingGenerator))
            throw new UnsupportedOperationException("This type of mapping doesn't support package mappings");
    }

    private void ensureCapacity(int size) {
        synchronized(buf) {
            buf.ensureCapacity(buf.size() + size);
        }
    }

    /**
     * Write a paired package to a paired mapping
     * @param pkg The package to write
     */
    public final void writePairedPackage(PairedMapping pkg) {
        ensurePackageGenerator();
        if(getGenerator().isPaired()) {
            synchronized(buf) {
                buf.add(((PackageMappingGenerator) getGenerator()).generatePackage(pkg));
            }
        } else throw new UnsupportedOperationException("Use writeNamespacedPackage(s)");
    }

    /**
     * Write a namespaced package to a paired mapping
     * @param pkg The package to write
     * @param unmapped The unmapped name's namespace
     * @param mapped The mapped name's namespace
     */
    public final void writePairedPackage(NamespacedMapping pkg, String unmapped, String mapped) {
        writePairedPackage(new PairedMapping(pkg.getName(unmapped), pkg.getName(mapped)));
    }

    /**
     * Write paired packages to a paired mapping
     * @param packages Packages to write
     */
    public final void writePairedPackages(Collection<PairedMapping> packages) {
        ensureCapacity(packages.size());
        packages.parallelStream()
                .forEach(this::writePairedPackage);
    }

    /**
     * Write namespaced packages to a paired mapping
     * @param packages Packages to write
     * @param unmapped The unmapped name's namespace
     * @param mapped The mapped name's namespace
     */
    public final void writePairedPackages(Collection<NamespacedMapping> packages, String unmapped, String mapped) {
        ensureCapacity(packages.size());
        packages.parallelStream()
                .forEach(pkg -> writePairedPackage(pkg, unmapped, mapped));
    }

    /**
     * Write a namespaced package to a namespaced mapping
     * @param pkg The package to write
     */
    public final void writeNamespacedPackage(NamespacedMapping pkg) {
        ensurePackageGenerator();
        if(getGenerator().isNamespaced()) {
            synchronized(buf) {
                buf.add(((PackageMappingGenerator) getGenerator()).generatePackage(pkg));
            }
        } else throw new UnsupportedOperationException("Use writePairedPackage(s)");
    }

    /**
     * Write a paired package to a namespaced mapping
     * @param pkg The package to write
     * @param unmapped The unmapped name's namespace
     * @param mapped The mapped name's namespace
     */
    public final void writeNamespacedPackage(PairedMapping pkg, String unmapped, String mapped) {
        writeNamespacedPackage(new NamespacedMapping(new String[] {unmapped, mapped},
                new String[] {pkg.getUnmappedName(), pkg.getMappedName()}));
    }

    /**
     * Write namespaced packages to a namespaced mapping
     * @param packages Packages to write
     */
    public final void writeNamespacedPackages(Collection<NamespacedMapping> packages) {
        ensureCapacity(packages.size());
        packages.parallelStream()
                .forEach(this::writeNamespacedPackage);
    }

    /**
     * Write paired packages to a namespaced mapping
     * @param packages Packages to write
     * @param unmapped The unmapped name's namespace
     * @param mapped The mapped name's namespace
     */
    public final void writeNamespacedPackages(Collection<PairedMapping> packages, String unmapped, String mapped) {
        ensureCapacity(packages.size());
        packages.parallelStream()
                .forEach(pkg -> writeNamespacedPackage(pkg, unmapped, mapped));
    }

    public final void writePairedMapping(PairedClassMapping pcm) {
        if(getGenerator().isPaired()) {
            ensureCapacity(pcm.getFieldMap().size() + pcm.getMethods().size());
            boolean needLock = needLock();
            if(needLock) lock.lock();
            try {
                synchronized(buf) {
                    buf.add(getGenerator().asPaired().generateClass(pcm));
                }
                pcm.getFields().parallelStream().map(getGenerator().asPaired()::generateField).forEach(field -> {
                    synchronized(buf) {
                        buf.add(field);
                    }
                });
                pcm.getMethods().parallelStream().map(getGenerator().asPaired()::generateMethod).forEach(method -> {
                    synchronized(buf) {
                        buf.add(method);
                    }
                });
            } finally {
                if(needLock) lock.unlock();
            }
        } else throw new UnsupportedOperationException("Use writeNamespacedMapping(s)");
    }

    public final void writePairedMapping(NamespacedClassMapping ncm, String unmapped, String mapped) {
        PairedClassMapping pcm = new PairedClassMapping(ncm.getName(unmapped), ncm.getName(mapped));
        ncm.getFields().forEach(nfm -> {
            if(nfm instanceof Descriptor desc && remapper != null) {
                pcm.addField(new ProguardFieldMapping(nfm.getName(unmapped), nfm.getName(mapped),
                        remapper.mapToMapped(Type.getType(desc.getUnmappedDescriptor()))));
            } else pcm.addField(new PairedFieldMapping(nfm.getName(unmapped), nfm.getName(mapped)));
        });
        ncm.getMethods().forEach(nmm ->
                pcm.addMethod(new UnmappedDescriptoredPairedMethodMapping(nmm.getName(unmapped), nmm.getName(mapped),
                        nmm.getUnmappedDescriptor())));
        writePairedMapping(pcm);
    }

    public final void writePairedMappings(Collection<PairedClassMapping> mappings) {
        ensureCapacity(mappings.size());
        mappings.parallelStream()
                .forEach(this::writePairedMapping);
    }

    public final void writePairedMappings(Collection<NamespacedClassMapping> mappings, String unmapped, String mapped) {
        ensureCapacity(mappings.size());
        mappings.parallelStream()
                .forEach(ncm -> writePairedMapping(ncm, unmapped, mapped));
    }

    public final void writeNamespacedMapping(NamespacedClassMapping ncm) {
        if(getGenerator().isNamespaced()) {
            ensureCapacity(ncm.getFields().size() + ncm.getMethods().size());
            boolean needLock = needLock();
            if(needLock) lock.lock();
            try {
                synchronized(buf) {
                    buf.add(getGenerator().asNamespaced().generateClass(ncm));
                }
                ncm.getFields().parallelStream().map(getGenerator().asNamespaced()::generateField).forEach(field -> {
                    synchronized(buf) {
                        buf.add(field);
                    }
                });
                ncm.getMethods().parallelStream().map(getGenerator().asNamespaced()::generateMethod).forEach(method -> {
                    synchronized(buf) {
                        buf.add(method);
                    }
                });
            } finally {
                if(needLock) lock.unlock();
            }
        } else throw new UnsupportedOperationException("Use writePairedMapping(s)");
    }

    public final void writeNamespacedMapping(PairedClassMapping pcm, String unmapped, String mapped) {
        NamespacedClassMapping ncm = new NamespacedClassMapping(new String[] {unmapped, mapped},
                new String[] {pcm.getUnmappedName(), pcm.getMappedName()});
        pcm.getFields().forEach(pfm -> {
            if(pfm instanceof Descriptor.Mapped desc && remapper != null) {
                ncm.addField(new UnmappedDescriptoredNamespacedFieldMapping(new String[] {unmapped, mapped},
                        new String[] {pfm.getUnmappedName(), pfm.getMappedName()},
                        remapper.mapToUnmapped(Type.getType(desc.getMappedDescriptor()))));
            } else {
                ncm.addField(new NamespacedFieldMapping(new String[] {unmapped, mapped},
                        new String[] {pfm.getUnmappedName(), pfm.getMappedName()}));
            }
        });
        pcm.getMethods().forEach(pmm -> {
            String unmappedDesc;
            if(pmm instanceof Descriptor desc) unmappedDesc = desc.getUnmappedDescriptor();
            else if(remapper != null) unmappedDesc = remapper.getUnmappedDescByMappedDesc(pmm.asMappedDescriptor().getMappedDescriptor());
            else throw new UnsupportedOperationException();
            ncm.addMethod(new NamespacedMethodMapping(new String[] {unmapped, mapped},
                    new String[] {pmm.getUnmappedName(), pmm.getMappedName()}, unmappedDesc));
        });
        writeNamespacedMapping(ncm);
    }

    public final void writeNamespacedMappings(Collection<NamespacedClassMapping> mappings) {
        ensureCapacity(mappings.size());
        mappings.parallelStream()
                .forEach(this::writeNamespacedMapping);
    }

    public final void writeNamespacedMappings(Collection<PairedClassMapping> mappings, String unmapped, String mapped) {
        ensureCapacity(mappings.size());
        mappings.parallelStream()
                .forEach(pcm -> writeNamespacedMapping(pcm, unmapped, mapped));
    }

    public final void writeTo(OutputStream os) throws IOException {
        synchronized(buf) {
            String header = getHeader();
            if(!header.isEmpty()) os.write(header.getBytes(StandardCharsets.UTF_8));
            os.write(String.join("\n", buf).getBytes(StandardCharsets.UTF_8));
            buf.clear();
        }
    }

    public final void writeTo(Writer writer) throws IOException {
        synchronized(buf) {
            String header = getHeader();
            if(!header.isEmpty()) writer.write(header);
            writer.write(String.join("\n", buf));
            buf.clear();
        }
    }

    public final void writeTo(WritableByteChannel os) throws IOException {
        synchronized(buf) {
            String header = getHeader();
            if(!header.isEmpty()) os.write(ByteBuffer.wrap(header.concat("\n").getBytes(StandardCharsets.UTF_8)));
            os.write(ByteBuffer.wrap(String.join("\n", buf).getBytes(StandardCharsets.UTF_8)));
            buf.clear();
        }
    }

    protected abstract MappingGenerator getGenerator();

    protected abstract boolean needLock();

    protected String getHeader() {
        return "";
    }

    public interface MappingGenerator {
        default boolean isPaired() {
            return this instanceof PairedMappingGenerator;
        }
        default boolean isNamespaced() {
            return this instanceof NamespacedMappingGenerator;
        }

        default PairedMappingGenerator asPaired() {
            return (PairedMappingGenerator) this;
        }
        default NamespacedMappingGenerator asNamespaced() {
            return (NamespacedMappingGenerator) this;
        }
    }

    public interface PairedMappingGenerator extends MappingGenerator {
        String generateClass(PairedClassMapping mapping);
        String generateMethod(PairedMethodMapping mapping);
        String generateField(PairedFieldMapping mapping);
    }

    public interface NamespacedMappingGenerator extends MappingGenerator {
        String generateClass(NamespacedClassMapping mapping);
        String generateMethod(NamespacedMethodMapping mapping);
        String generateField(NamespacedFieldMapping mapping);
    }

    public interface PackageMappingGenerator {
        String generatePackage(AbstractMapping mapping);
    }
}