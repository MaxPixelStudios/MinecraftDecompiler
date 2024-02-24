/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.common.util;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaUtil {
    @FunctionalInterface
    public interface Runnable_WithThrowable<E extends Throwable> {
        void run() throws E;
    }

    @FunctionalInterface
    public interface Function_WithThrowable<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }

    @FunctionalInterface
    public interface Consumer_WithThrowable<T, E extends Throwable> {
        void accept(T t) throws E;
    }

    public static <E extends Throwable> void rethrowAsRuntime(E throwable) {
        throw Utils.wrapInRuntime(throwable);
    }

    public static <E extends Throwable> void rethrowAsCompletion(E throwable) {
        throw new CompletionException(throwable);
    }

    public static <E extends Throwable> Runnable unwrap(Runnable_WithThrowable<E> runnableWithThrowable) {
        return unwrap(runnableWithThrowable, LambdaUtil::rethrowAsRuntime);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> Runnable unwrap(Runnable_WithThrowable<E> runnableWithThrowable, Consumer<E> exceptionHandler) {
        return () -> {
            try {
                runnableWithThrowable.run();
            } catch(Throwable e) {
                exceptionHandler.accept((E) e);
            }
        };
    }

    public static <T, R, E extends Throwable> Function<T, R> unwrap(Function_WithThrowable<T, R, E> functionWithThrowable) {
        return unwrap(functionWithThrowable, LambdaUtil::rethrowAsRuntime);
    }

    @SuppressWarnings("unchecked")
    public static <T, R, E extends Throwable> Function<T, R> unwrap(Function_WithThrowable<T, R, E> functionWithThrowable, Consumer<E> exceptionHandler) {
        return t -> {
            try {
                return functionWithThrowable.apply(t);
            } catch(Throwable e) {
                exceptionHandler.accept((E) e);
                return null;
            }
        };
    }

    public static <T, E extends Throwable> Consumer<T> unwrapConsumer(Consumer_WithThrowable<T, E> consumerWithThrowable) {
        return unwrapConsumer(consumerWithThrowable, LambdaUtil::rethrowAsRuntime);
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Throwable> Consumer<T> unwrapConsumer(Consumer_WithThrowable<T, E> consumerWithThrowable, Consumer<E> exceptionHandler) {
        return t -> {
            try {
                consumerWithThrowable.accept(t);
            } catch(Throwable e) {
                exceptionHandler.accept((E) e);
            }
        };
    }

//    @SuppressWarnings("unchecked")
//    public static <T, R, E extends Throwable> Function<T, R> unwrap(Function_WithThrowable<T, R, E> functionWithThrowable, Consumer<E> exceptionHandler) {
//        return unwrap(functionWithThrowable, e -> {
//            exceptionHandler.accept(e);
//            return null;
//        });
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <T, R, E extends Throwable> Function<T, R> unwrap(Function_WithThrowable<T, R, E> functionWithThrowable, Function<E, R> exceptionHandler) {
//        return t -> {
//            try {
//                return functionWithThrowable.apply(t);
//            } catch(Throwable e) {
//                return exceptionHandler.apply((E) e);
//            }
//        };
//    }
}