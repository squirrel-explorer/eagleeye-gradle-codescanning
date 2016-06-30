package com.squirrel_explorer.eagleeye.codescanning

import com.android.build.gradle.internal.LintGradleClient
import com.android.builder.model.AndroidProject
import com.android.builder.model.Variant
import com.android.tools.lint.HtmlReporter
import com.android.tools.lint.LintCliClient
import com.android.tools.lint.LintCliFlags
import com.android.tools.lint.Reporter
import com.android.tools.lint.TextReporter
import com.android.tools.lint.XmlReporter
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.IssueRegistry
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

/**
 * Task Id : N/A
 * Content : Base class for Lint task
 */
public abstract class BaseLintTask extends DefaultTask {
    protected IssueRegistry registry
    protected LintCliFlags flags
    protected LintCliClient client

    protected File textOutput
    protected File htmlOutput
    protected File xmlOutput
    protected String defaultOutput

    protected String productFlavor
    protected String buildType

    public void initialize() {
        registry = new BuiltinIssueRegistry();
        flags = new LintCliFlags();
        client = null;

        if (project.plugins.hasPlugin('java')) {
            client = new LintCliClient(flags, 'java-cli')
        } else {
            GradleConnector gradleConn = GradleConnector.newConnector();
            gradleConn.forProjectDirectory(project.projectDir);
            ProjectConnection prjConn = gradleConn.connect();
            AndroidProject androidProject = prjConn.getModel(AndroidProject.class);
            if (null == androidProject) {
                throw new GradleException('No valid Android project.')
            }

            Collection<Variant> variantList = androidProject.getVariants();
            if (null == variantList || variantList.isEmpty()) {
                throw new GradleException('No variant defined.')
            }
            String targetVariantName = new StringBuilder().append(productFlavor).append(buildType).toString();
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

            client = new LintGradleClient(
                    registry,
                    flags,
                    project,
                    androidProject,
                    null,
                    variant,
                    null
            )
        }

        // Reporters
        List<Reporter> reporters = flags.getReporters();
        Reporter reporter;
        boolean needDefaultReporter = true
        if (null != textOutput) {
            reporter = new TextReporter(client, flags, new FileWriter(textOutput), true)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (null != htmlOutput) {
            reporter = new HtmlReporter(client, htmlOutput)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (null != xmlOutput) {
            reporter = new XmlReporter(client, xmlOutput)
            reporters.add(reporter)
            needDefaultReporter = false
        }
        if (needDefaultReporter) {
            File htmlReport = new File(defaultOutput);
            File htmlReportParent = htmlReport.getParentFile()
            if (null != htmlReportParent) {
                if (!htmlReportParent.exists()) {
                    htmlReportParent.mkdirs();
                }
            }
            reporter = new HtmlReporter(client, htmlReport)
            reporters.add(reporter)
        }
    }

    public void scan() {
        // Scanning-directories
        ArrayList<File> srcDirs = Arrays.asList(project.projectDir)

        // Scan
        try {
            if (null != client) {
                client.run(registry, srcDirs)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    protected void addIds(Set<String> ids, String idList) {
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

    protected Set<String> createIdSet(String idList) {
        if (null == idList || idList.isEmpty()) {
            return null
        }

        HashSet<String> ids = new HashSet<>()
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

        return ids.isEmpty() ? null : ids
    }
}
