package com.squirrel_explorer.eagleeye.codescanning.lint

import com.squirrel_explorer.eagleeye.codescanning.utils.ConfigUtils
import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task Id : runLint
 * Content : Run Android lint
 */
class RunLintTask extends BaseLintTask {
    @Input
    private LintExtension lint

    void setLintConfiguration(LintExtension lint) {
        this.lint = lint
    }

    @TaskAction
    void runLint() {
        preRun()

        analyze()
    }

    @Override
    protected void preRun() {
        super.preRun()

        if (lint.disable != null && !lint.disable.isEmpty()) {
            onRulesDisabled(ConfigUtils.parseIds(lint.disable))
        }

        if (lint.enable != null && !lint.enable.isEmpty()) {
            onRulesEnabled(ConfigUtils.parseIds(lint.enable))
        }

        if (lint.check != null && !lint.check.isEmpty()) {
            onRulesChecked(ConfigUtils.parseIds(lint.check))
        }

        boolean needDefaultReporter = true

        if (lint.textOutput != null) {
            onTextReport(lint.textOutput)
            needDefaultReporter = false
        }

        if (lint.htmlOutput != null) {
            onHtmlReport(lint.htmlOutput)
            needDefaultReporter = false
        }

        if (lint.xmlOutput != null) {
            onXmlReport(lint.xmlOutput)
            needDefaultReporter = false
        }

        if (needDefaultReporter) {
            onHtmlReport(FileUtils.safeCreateFile(defaultHtmlOutput))
        }

        if (lint.customRules != null && lint.customRules.isEmpty()) {
            onCustomRulesAdded(lint.customRules)
        }
    }
}
