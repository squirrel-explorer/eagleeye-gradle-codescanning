package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class BaseExtension {
    protected Project project

    public BaseExtension(Project project) {
        this.project = project
    }
}
