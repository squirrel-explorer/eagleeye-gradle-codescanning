package com.squirrel_explorer.eagleeye.codescanning

import com.android.build.gradle.internal.LintGradleClient
import com.android.builder.model.AndroidProject
import com.android.builder.model.Variant
import com.android.tools.lint.*
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.IssueRegistry
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

/**
 * Task Id : runLint
 * Content : Run Android lint
 */
public class RunLintTask extends DefaultTask {
    @Input
    private LintExtension lint;

    public void setLintConfiguration(LintExtension lint) {
        this.lint = lint;
    }

    @TaskAction
    public void runLint() {
        IssueRegistry registry = new BuiltinIssueRegistry();
        LintCliFlags flags = new LintCliFlags();

        GradleConnector gradleConn = GradleConnector.newConnector();
        gradleConn.forProjectDirectory(project.projectDir);
        ProjectConnection prjConn = gradleConn.connect();
        AndroidProject androidProject = prjConn.getModel(AndroidProject.class);

        Collection<Variant> variantList = androidProject.getVariants();
        if (null == variantList || variantList.isEmpty()) {
            throw new GradleException('No variant defined.')
            return;
        }
        String targetVariantName = new StringBuilder().append(lint.productFlavor).append(lint.buildType).toString();
        Variant variant = null;
        for (int i = 0; i < variantList.size(); i++) {
            if (variantList.getAt(i).getDisplayName().equalsIgnoreCase(targetVariantName)) {
                variant = variantList.getAt(i);
                break;
            }
        }
        if (null == variant) {
            variant = variantList.getAt(0);
        }

        LintGradleClient client = new LintGradleClient(
                registry,
                flags,
                project,
                androidProject,
                null,
                variant,
                null
        )

        // Disable
        Set<String> suppressedIds = flags.getSuppressedIds()
        addIds(suppressedIds, lint.disable)
        suppressedIds.add('LintError')

        // Enable
        addIds(flags.getEnabledIds(), lint.enable)

        // Check
        addIds(flags.getExactCheckedIds(), lint.check)

        // LintConfig
        if (null != lint.lintConfig) {
            flags.setDefaultConfiguration(lint.lintConfig)
        }

        // Reporters
        List<Reporter> reporters = flags.getReporters();
        Reporter reporter;
        boolean needDefaultReporter = true
        if (null != lint.textOutput) {
            reporter = new TextReporter(client, flags, new FileWriter(lint.textOutput), true)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (null != lint.htmlOutput) {
            reporter = new HtmlReporter(client, lint.htmlOutput)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (null != lint.xmlOutput) {
            reporter = new XmlReporter(client, lint.xmlOutput)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (needDefaultReporter) {
            File htmlReport = new File(project.buildDir.absolutePath + '/outputs/lint-results.html');
            File htmlReportParent = htmlReport.getParentFile()
            if (null != htmlReportParent) {
                if (!htmlReportParent.exists()) {
                    htmlReportParent.mkdirs();
                }
            }
            reporter = new HtmlReporter(client, htmlReport)
            reporters.add(reporter)
        }

        // Scanning-directories
        ArrayList<File> srcDirs = Arrays.asList(project.projectDir)

        // Scan
        try {
            client.run(registry, srcDirs)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private void addIds(Set<String> ids, String idList) {
        if (null == ids || null == idList || 0 == idList.length()) {
            return
        }

        for (String id : idList.split(',')) {
            if (null == id) {
                continue
            }

            id = id.trim()
            if (0 == id.length()) {
                continue
            }

            ids.add(id)
        }
    }
}
