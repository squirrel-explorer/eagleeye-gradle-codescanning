package com.squirrel_explorer.eagleeye.codescanning

import com.android.builder.model.AndroidProject
import com.android.builder.model.Variant
import com.android.tools.lint.*
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.IssueRegistry
import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import java.lang.reflect.Constructor
import java.lang.reflect.Field

/**
 * Task Id : N/A
 * Content : Base class for Lint task
 */
public abstract class BaseLintTask extends DefaultTask {
    protected IssueRegistry registry
    protected LintCliFlags flags
    protected LintCliClient client

    protected String defaultOutput

    private static final String SEPARATOR = ','

    public void initialize(String productFlavor, String buildType) {
        registry = new BuiltinIssueRegistry()
        flags = new LintCliFlags()
        client = null

        if (project.plugins.hasPlugin('java')) {
            client = new LintCliClient(flags, 'java-cli')
        } else {
            client = createLintGradleClient(productFlavor, buildType)
            if (null == client) {
                throw new GradleException('No proper constructor of \'com.android.build.gradle.internal.LintGradleClient\' is declared')
            }
        }
    }

    // 2.0.0 - latest
    private static final String SIGNATURE_2_0_0 = '[class com.android.tools.lint.client.api.IssueRegistry, class com.android.tools.lint.LintCliFlags, interface org.gradle.api.Project, interface com.android.builder.model.AndroidProject, class java.io.File, interface com.android.builder.model.Variant, class com.android.sdklib.BuildToolInfo]'
    // 1.5.0
    private static final String SIGNATURE_1_5_0 = '[class com.android.tools.lint.client.api.IssueRegistry, class com.android.tools.lint.LintCliFlags, interface org.gradle.api.Project, interface com.android.builder.model.AndroidProject, class java.io.File, class java.lang.String, class com.android.sdklib.BuildToolInfo]'
    // 1.1.0 - 1.3.0
    private static final String SIGNATURE_1_1_0 = '[class com.android.tools.lint.client.api.IssueRegistry, class com.android.tools.lint.LintCliFlags, interface org.gradle.api.Project, interface com.android.builder.model.AndroidProject, class java.io.File, class java.lang.String]'

    private LintCliClient createLintGradleClient(String productFlavor, String buildType) {
        GradleConnector gradleConn = GradleConnector.newConnector()
        gradleConn.forProjectDirectory(project.projectDir)
        ProjectConnection prjConn = gradleConn.connect()
        AndroidProject androidProject = prjConn.getModel(AndroidProject.class)
        if (null == androidProject) {
            throw new GradleException('No valid Android project.')
        }

        Collection<Variant> variantList = androidProject.getVariants()
        if (null == variantList || variantList.isEmpty()) {
            throw new GradleException('No variant defined.')
        }
        String targetVariantName = new StringBuilder().append(productFlavor).append(buildType).toString()
        Variant variant = null
        for (int i = 0; i < variantList.size(); i++) {
            if (variantList.getAt(i).getName().equalsIgnoreCase(targetVariantName)) {
                variant = variantList.getAt(i)
                break
            }
        }
        if (null == variant) {
            variant = variantList.getAt(0)
        }

        LintCliClient lintClient = null

        try {
            Class clazz = Class.forName('com.android.build.gradle.internal.LintGradleClient')
            if (null != clazz) {
                Constructor[] constructors = clazz.getConstructors()
                if (null != constructors && constructors.length > 0) {
                    for (Constructor constructor : constructors) {
                        String signature = constructor.getParameterTypes().toString()
                        if (SIGNATURE_2_0_0.equals(signature)) {
                            lintClient = (LintCliClient)constructor.newInstance(
                                    registry,
                                    flags,
                                    project,
                                    androidProject,
                                    null,
                                    variant,
                                    null)
                            break
                        } else if (SIGNATURE_1_5_0.equals(signature)) {
                            lintClient = (LintCliClient)constructor.newInstance(
                                    registry,
                                    flags,
                                    project,
                                    androidProject,
                                    null,
                                    variant.getName(),
                                    null)
                            break
                        } else if (SIGNATURE_1_1_0.equals(signature)) {
                            lintClient = (LintCliClient)constructor.newInstance(
                                    registry,
                                    flags,
                                    project,
                                    androidProject,
                                    null,
                                    variant.getName())
                            break
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
            lintClient = null
        }

        return lintClient
    }

    /*
    private int compareVersion(String version1, String version2) {
        int[] digits1 = extraceVersions(version1)
        int[] digits2 = extraceVersions(version2)

        if (null == digits1 || 0 == digits1.length) {
            if (null == digits2 || 0 == digits2.length) {
                return 0
            } else {
                return -1
            }
        } else {
            if (null == digits2 || 0 == digits2.length) {
                return 1
            } else {
                int size = (digits1.length < digits2.length ? digits1.length : digits2.length)
                int ret = 0
                for (int i = 0; i < size; i++) {
                    ret = digits1[i] - digits2[i]
                    if (0 != ret) {
                        break
                    }
                }
                if (0 == ret) {
                    if (digits1.length > size) {
                        ret = 1
                    } else {
                        ret = -1
                    }
                }
                return ret
            }
        }
    }

    private int[] extraceVersions(String version) {
        if (null == version || version.isEmpty()) {
            return null
        }

        String digitalVersion = version.split('-')[0].trim()
        if (null == digitalVersion || digitalVersion.isEmpty()) {
            return null
        }

        String[] digitalVersions = digitalVersion.split('\\.')
        if (null == digitalVersions || 0 == digitalVersions.length) {
            return null
        }

        int[] digits = new int[digitalVersions.length]
        for (int i = 0; i < digitalVersions.length; i++) {
            try {
                digits[i] = Integer.parseInt(digitalVersions[i])
            } catch (Exception e) {
                digits[i] = 0
            }
        }
        return digits
    }
    */

    protected void addReporters(File textOutput, File htmlOutput, File xmlOutput) {
        if (null == client) {
            return
        }

        List<Reporter> reporters = flags.getReporters()
        Reporter reporter
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
            File htmlReport = FileUtils.safeCreateFile(defaultOutput)
            if (null != htmlReport) {
                reporter = new HtmlReporter(client, htmlReport)
                reporters.add(reporter)
            }
        }
    }

    protected void addCustomRules(String customRuleJars) {
        if (null == customRuleJars || customRuleJars.isEmpty()) {
            return
        }

        // Generate lint class paths
        HashSet<String> lintClassPaths = new HashSet<>()
        String lintClassPath = System.getenv('ANDROID_LINT_JARS')
        if (null != lintClassPath && !lintClassPath.isEmpty()) {
            lintClassPaths.addAll(lintClassPath.split(File.pathSeparator))
        }

        StringBuilder sb = new StringBuilder()
        String[] customRuleJarList = customRuleJars.split(SEPARATOR)
        for (String ruleJar : customRuleJarList) {
            if (ruleJar.endsWith('.jar') && !lintClassPaths.contains(ruleJar)) {
                sb.append(ruleJar).append(File.pathSeparator)
            }
        }

        if (sb.length() > 0) {
            if (null != lintClassPath && !lintClassPath.isEmpty()) {
                lintClassPath += File.pathSeparator + sb.toString()
            } else {
                lintClassPath = sb.toString()
            }

            if (lintClassPath.startsWith(File.pathSeparator)) {
                lintClassPath = lintClassPath.substring(File.pathSeparator.length())
            }
            if (lintClassPath.endsWith(File.pathSeparator)) {
                lintClassPath = lintClassPath.substring(0, lintClassPath.length() - File.pathSeparator.length())
            }
            setenv('ANDROID_LINT_JARS', lintClassPath)
        }
    }

    protected void scan() {
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

        for (String id : idList.split(SEPARATOR)) {
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
        for (String id : idList.split(SEPARATOR)) {
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

    protected void setenv(String key, String value) {
        try {
            Class[] classes = Collections.class.getDeclaredClasses()
            Map<String, String> env = System.getenv()
            for (Class cl : classes) {
                if ('java.util.Collections$UnmodifiableMap'.equals(cl.getName())) {
                    Field field = cl.getDeclaredField('m')
                    field.setAccessible(true)
                    Object obj = field.get(env)
                    Map<String, String> map = (Map<String, String>)obj
                    map.put(key, value)
                    break
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
