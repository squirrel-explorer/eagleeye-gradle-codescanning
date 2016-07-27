package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class BaseExtension {
    Project project

    public BaseExtension(Project project) {
        this.project = project
    }
}
