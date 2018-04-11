package com.squirrel_explorer.eagleeye.codescanning

import com.squirrel_explorer.eagleeye.codescanning.lint.ApiCheckExtension
import com.squirrel_explorer.eagleeye.codescanning.lint.ApiCheckTask
import com.squirrel_explorer.eagleeye.codescanning.lint.LintExtension
import com.squirrel_explorer.eagleeye.codescanning.lint.RunLintTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class InkerPlugin implements Plugin<Project> {
    LintExtension lintExt
    ApiCheckExtension apiCheckExt

    @Override
    void apply(Project project) {
        project.extensions.create('inker', InkerExtension, project)

        addRunLintTask(project)
        addApiCheckTask(project)
    }

    private void addRunLintTask(Project project) {
        project.inker.extensions.create('lint', LintExtension, project)

        project.afterEvaluate {
            lintExt = project.inker.extensions.getByType(LintExtension.class)
            RunLintTask runLint = project.getTasks().create('runLint', RunLintTask.class)
            runLint.setLintConfiguration(lintExt)
        }
    }

    private void addApiCheckTask(Project project) {
        project.inker.extensions.create('apicheck', ApiCheckExtension, project)

        if (project == project.rootProject) {
            apiCheckExt = project.inker.extensions.getByType(ApiCheckExtension.class)
            ApiCheckTask apiCheck = project.getTasks().create('apicheck', ApiCheckTask.class)
            apiCheck.setApiCheckConfiguration(apiCheckExt)
        }
    }
}
