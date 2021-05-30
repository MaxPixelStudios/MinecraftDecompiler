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

import cn.maxpixel.mcdecompiler.MinecraftDecompilerCommandLine;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NetworkUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    public static class Net {
        public enum Method {
            GET,
            POST,
            PUT,
            DELETE,
            HEAD,
            OPTIONS,
            TRACE
        }
        public static class Connection implements Closeable {
            private InputStream stream;
            private final HttpURLConnection connection;
            private Connection(HttpURLConnection connection) {
                this.connection = connection;
                try {
                    this.connection.connect();
                } catch (IOException e) {
                    LOGGER.error("Error when connecting to the Internet", e);
                }
                try {
                    this.stream = this.connection.getInputStream();
                } catch (IOException e) {
                    this.stream = this.connection.getErrorStream();
                }
            }
            public InputStream asStream() {
                return stream;
            }
            public byte[] asByteArray() {
                try {
                    byte[] b = new byte[8192];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(32, stream.available()));
                    for(int len = stream.read(b); len != -1; len = stream.read(b))
                        baos.write(b, 0, len);
                    return baos.toByteArray();
                } catch (IOException e) {
                    LOGGER.error("Error when reading stream", e);
                } finally {
                    try {
                        stream.close();
                    }catch(Throwable ignored) {}
                }
                return null;
            }
            public String asString() {
                return new String(asByteArray(), StandardCharsets.UTF_8);
            }
            public Reader asReader() {
                return new InputStreamReader(stream, StandardCharsets.UTF_8);
            }
            public BufferedInputStream asStreamBuffered() {
                return stream instanceof BufferedInputStream ? (BufferedInputStream) stream
                        : new BufferedInputStream(stream);
            }
            public ReadableByteChannel asChannel() {
                return Channels.newChannel(stream);
            }
            public BufferedReader asReaderBuffered() {
                return new BufferedReader(asReader());
            }
            @Override
            public void close() throws IOException {
                this.stream.close();
                this.connection.disconnect();
            }
        }
        private HttpURLConnection connection;
        private Net(URL url, Proxy proxy, Method method, Duration timeout, Map<String, List<String>> headers, byte[] reqData) {
            try {
                connection = (HttpURLConnection) url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
                connection.setRequestMethod(method.name());
                connection.setReadTimeout((int) timeout.toMillis());
                connection.setConnectTimeout((int) timeout.toMillis());
                if(!(headers == null || headers.isEmpty())) {
                    Map<String, List<String>> requestProperties = connection.getRequestProperties();
                    headers.forEach((key, value) -> requestProperties.merge(key, value, (ov, nv) -> {ov.addAll(nv); return ov;}));
                }
                if(reqData != null) {
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(reqData);
                }
            } catch (IOException e) {
                LOGGER.error("Error when opening connection", e);
            }
        }
        public Connection connect() {
            return new Connection(connection);
        }
        public CompletableFuture<Connection> connectAsync() {
            return CompletableFuture.supplyAsync(this::connect);
        }
        public static class NetBuilder {
            private final URL url;
            private Proxy proxy;
            private Method method;
            private Duration timeout;
            private final Map<String, List<String>> headers;
            private byte[] data;
            private NetBuilder(URL url) {
                this.url = url;
                this.proxy = MinecraftDecompilerCommandLine.INTERNAL_PROXY;
                this.method = Method.GET;
                this.timeout = Duration.ofSeconds(10);
                this.headers = new HashMap<>();
                this.data = null;
            }
            public static NetBuilder newBuilder(URL url) {
                return new NetBuilder(url);
            }
            public NetBuilder proxy(Proxy proxy) {
                if(!(this.proxy == null || this.proxy == Proxy.NO_PROXY)) throw new RuntimeException("Proxy already set");
                this.proxy = Objects.requireNonNull(proxy, "proxy cannot be null");
                return this;
            }
            public NetBuilder requestMethod(Method method) {
                if(!(this.method == null || this.method == Method.GET)) throw new RuntimeException("Method already set");
                this.method = Objects.requireNonNull(method, "method cannot be null");
                return this;
            }
            public NetBuilder timeout(Duration timeout) {
                this.timeout = Objects.requireNonNull(timeout, "timeout cannot be null");
                return this;
            }
            public NetBuilder header(String key, String value) {
                headers.merge(Objects.requireNonNull(key), ObjectLists.singleton(Objects.requireNonNull(value)), (oldVal, newVal) -> {oldVal.addAll(newVal); return oldVal;});
                return this;
            }
            public NetBuilder header(String key, String... value) {
                headers.merge(Objects.requireNonNull(key), Arrays.asList(Objects.requireNonNull(value)), (oldVal, newVal) -> {oldVal.addAll(newVal); return oldVal;});
                return this;
            }
            public NetBuilder setHeader(String key, String value) {
                headers.put(Objects.requireNonNull(key), ObjectLists.singleton(Objects.requireNonNull(value)));
                return this;
            }
            public NetBuilder setHeader(String key, String... value) {
                headers.put(Objects.requireNonNull(key), Arrays.asList(Objects.requireNonNull(value)));
                return this;
            }
            public NetBuilder dataSend(byte[] data) {
                this.data = Objects.requireNonNull(data, "data cannot be null");
                return this;
            }
            public Net build() {
                return new Net(url, proxy, method, timeout, headers, data);
            }
            public Connection connect() {
                return build().connect();
            }
            public CompletableFuture<Connection> connectAsync() {
                return build().connectAsync();
            }
        }
    }
    public static Net.NetBuilder newBuilder(String url) {
        try {
            return new Net.NetBuilder(new URL(url));
        } catch (MalformedURLException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
    public static Net.NetBuilder newBuilder(URL url) {
        return new Net.NetBuilder(url);
    }
}