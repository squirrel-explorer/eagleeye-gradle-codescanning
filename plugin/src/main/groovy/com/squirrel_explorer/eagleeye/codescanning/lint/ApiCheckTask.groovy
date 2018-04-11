package com.squirrel_explorer.eagleeye.codescanning.lint

import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task Id : apiCheck
 * Content : Run Android lint to check missing api
 */
class ApiCheckTask extends BaseLintTask {
    @Input
    private ApiCheckExtension apicheck

    void setApiCheckConfiguration(ApiCheckExtension apicheck) {
        this.apicheck = apicheck
    }

    @TaskAction
    void apiCheck() {
        preRun()

        analyze()
    }

    @Override
    protected void preRun() {
        super.preRun()

        onRulesDisabled(null)
        onRulesEnabled(null)
        Set<String> check = new HashSet<>()
        check.add('MissingApiChecker')
        onRulesChecked(check)

        boolean needDefaultReporter = true

        if (apicheck.textOutput != null) {
            onTextReport(apicheck.textOutput)
            needDefaultReporter = false
        }

        if (apicheck.htmlOutput != null) {
            onHtmlReport(apicheck.htmlOutput)
            needDefaultReporter = false
        }

        if (apicheck.xmlOutput != null) {
            onXmlReport(apicheck.xmlOutput)
            needDefaultReporter = false
        }

        if (needDefaultReporter) {
            onHtmlReport(FileUtils.safeCreateFile(defaultHtmlOutput))
        }

        if (apicheck.customRules != null && apicheck.customRules.isEmpty()) {
            onCustomRulesAdded(apicheck.apicheckRules)
        }
    }
}
