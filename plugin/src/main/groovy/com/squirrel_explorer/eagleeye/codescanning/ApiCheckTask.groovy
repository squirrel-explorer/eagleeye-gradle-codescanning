package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task Id : apiCheck
 * Content : Run Android lint to check missing api
 */
public class ApiCheckTask extends BaseLintTask {
    @Input
    private ApiCheckExtension apicheck;

    public void setApiCheckConfiguration(ApiCheckExtension apicheck) {
        this.apicheck = apicheck;
        this.textOutput = apicheck.textOutput
        this.htmlOutput = apicheck.htmlOutput
        this.xmlOutput = apicheck.xmlOutput
        this.defaultOutput = project.buildDir.absolutePath + '/outputs/apicheck-results.html'
        this.productFlavor = apicheck.productFlavor
        this.buildType = apicheck.buildType
    }

    @TaskAction
    public void apiCheck() {
        initialize()

        // Check
        flags.setExactCheckedIds(createIdSet('MissingApiChecker'))

        scan()
    }
}
