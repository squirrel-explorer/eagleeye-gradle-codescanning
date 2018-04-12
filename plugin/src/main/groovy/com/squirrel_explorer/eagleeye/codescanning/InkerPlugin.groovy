package com.squirrel_explorer.eagleeye.codescanning

import com.squirrel_explorer.eagleeye.codescanning.lint.ApiCheckExtension
import com.squirrel_explorer.eagleeye.codescanning.lint.ApiCheckTask
import com.squirrel_explorer.eagleeye.codescanning.lint.LintExtension
import com.squirrel_explorer.eagleeye.codescanning.lint.RunLintTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.compile.JavaCompile

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
            RunLintTask runLint = project.tasks.create('runLint', RunLintTask.class)
            runLint.setLintConfiguration(lintExt)

            adjustTaskDependsOn(project, runLint)
        }
    }

    private void addApiCheckTask(Project project) {
        project.inker.extensions.create('apicheck', ApiCheckExtension, project)

        if (project == project.rootProject) {
            apiCheckExt = project.inker.extensions.getByType(ApiCheckExtension.class)
            ApiCheckTask apiCheck = project.getTasks().create('apicheck', ApiCheckTask.class)
            apiCheck.setApiCheckConfiguration(apiCheckExt)

            adjustTaskDependsOn(project, apiCheck)
        }
    }

    private void adjustTaskDependsOn(Project project, Task task) {
        TaskCollection<Task> compileTasks = project.tasks.withType(JavaCompile.class)
        if (compileTasks != null && !compileTasks.isEmpty()) {
            for (Task t : compileTasks) {
                String taskName = t.name.toLowerCase()
                if (taskName.contains('debug') && !taskName.contains('test')) {
                    task.dependsOn(t.name)
                    break
                }
            }
        }
    }
}
