package com.squirrel_explorer.eagleeye.codescanning

import com.squirrel_explorer.eagleeye.codescanning.lint.ApiCheckExtension
import com.squirrel_explorer.eagleeye.codescanning.lint.LintExtension
import org.gradle.api.Project

class InkerExtension extends BaseExtension {
    Project project

    LintExtension lint
    ApiCheckExtension apicheck

    public InkerExtension(Project project) {
        super(project)
    }
}
