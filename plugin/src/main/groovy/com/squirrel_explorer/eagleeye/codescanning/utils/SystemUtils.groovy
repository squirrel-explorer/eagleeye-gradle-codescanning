package com.squirrel_explorer.eagleeye.codescanning.utils

import java.lang.reflect.Field

class SystemUtils {
    static void setJavaEnv(String key, String value) {
        try {
            Class[] classes = Collections.class.getDeclaredClasses()
            Map<String, String> env = System.getenv()
            for (Class cl : classes) {
                if ('java.util.Collections$UnmodifiableMap'.equals(cl.getName())) {
                    Field field = cl.getDeclaredField('m')
                    field.setAccessible(true)
                    Object obj = field.get(env)
                    Map<String, String> map = (Map<String, String>)obj
                    map.put(key, value)
                    break
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
