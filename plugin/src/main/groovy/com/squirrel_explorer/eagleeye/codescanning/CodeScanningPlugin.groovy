package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Plugin
import org.gradle.api.Project

class CodeScanningPlugin implements Plugin<Project> {
    CodeScanningExtension codeScanning;

    @Override
    void apply(Project project) {
        codeScanning = project.extensions.create("codescanning", CodeScanningExtension, project)
        codeScanning.lint = project.codescanning.extensions.create("lint", LintExtension, project)

        addRunLintTask(project)
    }

    private void addRunLintTask(Project project) {
        RunLintTask runLint = project.getTasks().create("runLint", RunLintTask.class);
        runLint.setLintConfiguration(codeScanning.lint)
    }
}
