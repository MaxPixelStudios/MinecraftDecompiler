package cn.maxpixel.mcdecompiler.common.app.util;

import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {
    public static void waitFor(Process pro) {
        Logger logger = LogManager.getLogger("Process PID: " + pro.pid());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
            Thread inT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = in.readLine()) != null) logger.debug("{}", ins);// avoid unexpected interpolation
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            });
            Thread errT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = err.readLine()) != null) logger.warn("{}", ins);
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            });
            inT.setDaemon(true);
            errT.setDaemon(true);
            inT.start();
            errT.start();
            pro.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Exception thrown", e);
        }
    }
}
