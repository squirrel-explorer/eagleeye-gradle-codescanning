package com.squirrel_explorer.eagleeye.codescanning.utils

import java.lang.reflect.Field;

class ConfigUtils {
    private static final String SEPARATOR = ','

    static Set<String> parseIds(String idList) {
        if (idList == null || idList.isEmpty()) {
            return null
        }

        Set<String> idSet = new HashSet<>()
        for (String id : idList.split(SEPARATOR)) {
            if (id == null) {
                continue
            }

            id = id.trim()
            if (id.isEmpty()) {
                continue
            }

            idSet.add(id)
        }

        return idSet
    }

}
