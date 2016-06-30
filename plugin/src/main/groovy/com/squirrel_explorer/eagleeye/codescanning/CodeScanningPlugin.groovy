package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Plugin
import org.gradle.api.Project

class CodeScanningPlugin implements Plugin<Project> {
    CodeScanningExtension codeScanning;

    @Override
    void apply(Project project) {
        codeScanning = project.extensions.create("codescanning", CodeScanningExtension, project)
        codeScanning.lint = project.codescanning.extensions.create("lint", LintExtension, project)
        codeScanning.apicheck = project.codescanning.extensions.create("apicheck", ApiCheckExtension, project)

        addRunLintTask(project)
        addApiCheckTask(project)
    }

    private void addRunLintTask(Project project) {
        RunLintTask runLint = project.getTasks().create("runLint", RunLintTask.class);
        runLint.setLintConfiguration(codeScanning.lint)
    }

    private void addApiCheckTask(Project project) {
        ApiCheckTask apiCheck = project.getTasks().create("apiCheck", ApiCheckTask.class);
        apiCheck.setApiCheckConfiguration(codeScanning.apicheck)
    }
}
