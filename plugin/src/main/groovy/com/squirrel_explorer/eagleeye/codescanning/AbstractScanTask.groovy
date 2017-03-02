package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.DefaultTask

import java.lang.reflect.Field

/**
 * Created by squirrel-explorer on 2016/9/29.
 */
public abstract class AbstractScanTask extends DefaultTask {
    protected String defaultOutput

    private static final String SEPARATOR = ','

    protected void addIds(Set<String> ids, String idList) {
        if (null == ids || null == idList || 0 == idList.length()) {
            return
        }

        for (String id : idList.split(SEPARATOR)) {
            if (null == id) {
                continue
            }

            id = id.trim()
            if (0 == id.length()) {
                continue
            }

            ids.add(id)
        }
    }

    protected Set<String> createIdSet(String idList) {
        if (null == idList || idList.isEmpty()) {
            return null
        }

        HashSet<String> ids = new HashSet<>()
        for (String id : idList.split(SEPARATOR)) {
            if (null == id) {
                continue
            }

            id = id.trim()
            if (0 == id.length()) {
                continue
            }

            ids.add(id)
        }

        return ids.isEmpty() ? null : ids
    }

    protected void setenv(String key, String value) {
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
