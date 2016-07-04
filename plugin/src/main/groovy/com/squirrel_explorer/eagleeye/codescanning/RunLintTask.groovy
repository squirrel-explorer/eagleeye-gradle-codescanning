package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
/**
 * Task Id : runLint
 * Content : Run Android lint
 */
public class RunLintTask extends BaseLintTask {
    @Input
    private LintExtension lint

    public void setLintConfiguration(LintExtension lint) {
        this.lint = lint

        this.defaultOutput = project.buildDir.absolutePath + '/outputs/lint-results.html'
    }

    @TaskAction
    public void runLint() {
        initialize(lint.productFlavor, lint.buildType)

        // Disable
        Set<String> suppressedIds = flags.getSuppressedIds()
        if (project.plugins.hasPlugin('java')) {
            suppressedIds.add('LintError')
        }
        addIds(suppressedIds, lint.disable)

        // Enable
        addIds(flags.getEnabledIds(), lint.enable)

        // Check
        flags.setExactCheckedIds(createIdSet(lint.check))

        // LintConfig
        if (null != lint.lintConfig) {
            flags.setDefaultConfiguration(lint.lintConfig)
        }

        addReporters(lint.textOutput, lint.htmlOutput, lint.xmlOutput)

        addCustomRules(lint.customRuleJars)

        scan()
    }
}
