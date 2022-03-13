/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.Info;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.*;

public final class Logging {
    private static final Logger PARENT;
    private static final StackWalker WALKER = StackWalker.getInstance();
    private static final LogConfiguration CONFIG = LogConfiguration.fromInputStream(
            Logging.class.getClassLoader().getResourceAsStream("logging.json"));

    static {
        if(!Info.IS_DEV) {
            AnsiConsole.systemInstall();
            Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
        }
        PARENT = Logger.getLogger("cn.maxpixel.mcdecompiler");
        PARENT.setUseParentHandlers(false);
        PARENT.setLevel(CONFIG.globalLevel);
        StdoutHandler handler = new StdoutHandler();
        handler.setLevel(CONFIG.globalLevel);
        PARENT.addHandler(handler);
    }

    private Logging() {}

    public static Logger getLogger() {
        return Logger.getLogger(WALKER.walk(stream -> stream.limit(3).map(StackWalker.StackFrame::getClassName)
                .filter(s -> !s.startsWith("cn.maxpixel.mcdecompiler.util.Logging")).toArray(String[]::new)[0]));
    }

    public static Logger getLogger(String name) {
        if(name == null) return getLogger();
        Logger logger = Logger.getLogger(name);
        logger.setParent(PARENT);
        logger.setLevel(Level.ALL);
        return logger;
    }

    public static Logger setFilter(Logger logger, Filter filter) {
        logger.setFilter(filter);
        return logger;
    }

    public static Logger setLevel(Logger logger, Level level) {
        logger.setLevel(level);
        return logger;
    }

    private static final class LogConfiguration {
        private static final class LevelJsonSerializer implements JsonSerializer<Level>, JsonDeserializer<Level> {
            @Override
            public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return switch(json.getAsString().toUpperCase()) {
                    case "OFF" -> Level.OFF;
                    case "SEVERE" -> Level.SEVERE;
                    case "WARNING" -> Level.WARNING;
                    case "INFO" -> Level.INFO;
                    case "CONFIG" -> Level.CONFIG;
                    case "FINE" -> Level.FINE;
                    case "FINER" -> Level.FINER;
                    case "FINEST" -> Level.FINEST;
                    case "ALL" -> Level.ALL;
                    default -> throw new JsonParseException("Unknown log level");
                };
            }

            @Override
            public JsonElement serialize(Level src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.getName());
            }
        }
        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Level.class, new LevelJsonSerializer())
                .create();

//        public final Style style = null;
//        public String format;TODO
        @SerializedName("global_level")
        public final Level globalLevel = Level.INFO;

        public static LogConfiguration fromInputStream(InputStream is) {
            try(is) {
                return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), LogConfiguration.class);
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        }
    }

    private static final class StdoutHandler extends Handler {
        {
            setFormatter(new LogFormatter());
        }

        private boolean doneHeader;

        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;
            String msg;
            try {
                msg = getFormatter().format(record);
            } catch (Exception ex) {
                reportError(null, ex, ErrorManager.FORMAT_FAILURE);
                return;
            }

            try {
                if (!doneHeader) {
                    System.out.print(getFormatter().getHead(this));
                    doneHeader = true;
                }
                System.out.print(msg);
            } catch (Exception ex) {
                reportError(null, ex, ErrorManager.WRITE_FAILURE);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private static final class LogFormatter extends Formatter {
        private static final ZoneId zone = ZoneId.systemDefault();
        @Override
        public String format(LogRecord record) {
            Ansi ansi = Ansi.ansi();
            if(record.getLevel() == Level.FINEST) ansi.fgGreen();
            else if(record.getLevel() == Level.FINER) ansi.fgBlue();
            else if(record.getLevel() == Level.FINE) ansi.fgCyan();
            else if(record.getLevel() == Level.CONFIG) ansi.fg(Ansi.Color.WHITE);
            else if(record.getLevel() == Level.INFO) ansi.fgBright(Ansi.Color.WHITE);
            else if(record.getLevel() == Level.WARNING) ansi.fgBrightYellow();
            else if(record.getLevel() == Level.SEVERE) ansi.fgBrightRed();
            else ansi.fgGreen();
            // [time] [name] [level] [source] msg\n
            ansi.a('[');
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.formatTo(LocalDateTime.ofInstant(record.getInstant(), zone), ansi);
            return ansi.a("] [")
                    .a(record.getLoggerName())
                    .a("] [")
                    .a(record.getLevel())
                    .a("] ")
                    .a(getMessage(record.getSourceClassName(), record.getSourceMethodName(), record.getMessage(), record.getParameters(), record.getThrown()))
                    .newline().reset()
                    .toString();
        }

        private static StringBuilder getMessage(String sourceClass, String sourceMethod, String message, Object[] params, Throwable thrown) {
            StringBuilder builder = new StringBuilder();
            if(sourceClass != null) {
                builder.append("[").append(sourceClass);
                if(sourceMethod != null) builder.append('/').append(sourceMethod);
                builder.append("] ");
            } else if(sourceMethod != null) builder.append("[Method ").append(sourceMethod).append("] ");
            if(message != null) {
                if(params != null && params.length > 0) {
                    if(params[params.length - 1] instanceof Throwable) {
                        thrown = (Throwable) params[params.length - 1];
                        params = Arrays.copyOf(params, params.length - 1);
                        for(int i = 0; i < params.length; i++) {
                            Object o = params[i];
                            if(o instanceof Supplier s) params[i] = s.get();
                        }
                    }
                    message = String.format(MessageFormat.format(message, params), params);
                }
                builder.append(message);
            }
            if(thrown != null) {
                builder.append('\n');
                StringWriter writer = new StringWriter();
                thrown.printStackTrace(new PrintWriter(writer));
                builder.append(writer.getBuffer());
            }
            return builder;
        }
    }
}