package com.squirrel_explorer.eagleeye.codescanning.utils

import java.util.logging.Level
import java.util.logging.Logger

class LogUtils {
    private static Logger logger = Logger.getLogger('Inker')
    private static StringBuilder sb = new StringBuilder()
    private static boolean enabled = false

    private static final Level ERROR = new Level('ERROR', 10000)
    private static final Level WARNING = new Level('WARNING', 10001)
    private static final Level INFO = new Level('INFO', 10002)

    static boolean isEnabled() {
        return enabled
    }

    static void enable(boolean enabled) {
        LogUtils.enabled = enabled
    }

    static void e(String tag, String msg) {
        if (enabled) {
            sb.delete(0, sb.length())
            sb.append(tag).append(' : ').append(msg)
            logger.log(ERROR, sb.toString())
        }
    }

    static void w(String tag, String msg) {
        if (enabled) {
            sb.delete(0, sb.length())
            sb.append(tag).append(' : ').append(msg)
            logger.log(WARNING, sb.toString())
        }
    }

    static void i(String tag, String msg) {
        if (enabled) {
            sb.delete(0, sb.length())
            sb.append(tag).append(' : ').append(msg)
            logger.log(INFO, sb.toString())
        }
    }
}
