package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class CodeScanningExtension extends BaseExtension {
    Project project

    LintExtension lint
    ApiCheckExtension apicheck

    public CodeScanningExtension(Project project) {
        super(project)
    }
}
