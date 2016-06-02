package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class CodeScanningExtension {
    Project project

    LintExtension lint

    public CodeScanningExtension(Project project) {
        this.project = project
    }
}
