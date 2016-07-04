package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task Id : apiCheck
 * Content : Run Android lint to check missing api
 */
public class ApiCheckTask extends BaseLintTask {
    @Input
    private ApiCheckExtension apicheck

    public void setApiCheckConfiguration(ApiCheckExtension apicheck) {
        this.apicheck = apicheck

        this.defaultOutput = project.buildDir.absolutePath + '/outputs/apicheck-results.html'
    }

    @TaskAction
    public void apiCheck() {
        initialize(apicheck.productFlavor, apicheck.buildType)

        // Check
        flags.setExactCheckedIds(createIdSet('MissingApiChecker'))

        addReporters(apicheck.textOutput, apicheck.htmlOutput, apicheck.xmlOutput)

        addCustomRules(apicheck.apicheckRuleJar)

        scan()
    }
}
