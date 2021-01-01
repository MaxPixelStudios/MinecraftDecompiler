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

package cn.maxpixel.mcdecompiler.util;

import java.util.function.Consumer;

public class LambdaUtil {
    @FunctionalInterface
    public interface ConsumerWithThrows<T, E extends Throwable> {
        void accept(T t) throws E;
    }
    @SuppressWarnings("unchecked")
    public static <T, E extends Throwable> Consumer<T> handleThrowable(ConsumerWithThrows<T, E> consumerWithThrows, Consumer<E> exceptionHandler) {
        return t -> {
            try {
                consumerWithThrows.accept(t);
            } catch(Throwable throwable) {
                exceptionHandler.accept((E) throwable);
            }
        };
    }
    public static <E extends Throwable> void rethrowAsRuntime(E throwable) {
        throw new RuntimeException(throwable);
    }
    public static <T, E extends Throwable> Consumer<T> handleThrowable(ConsumerWithThrows<T, E> consumerWithThrows) {
        return handleThrowable(consumerWithThrows, e -> {}); // Do nothing
    }
    public static <T extends AutoCloseable> void handleAutoCloseable(T resource, Consumer<T> consumer) throws Exception {
        try(T autoCloseable = resource) {
            consumer.accept(autoCloseable);
        }
    }
    public static <T extends AutoCloseable> void handleAutoCloseable(T resource, Consumer<T> consumer, Consumer<Exception> exceptionHandler) {
        try(T autoCloseable = resource) {
            consumer.accept(autoCloseable);
        } catch(Exception e) {
            exceptionHandler.accept(e);
        }
    }
}