package com.squirrel_explorer.eagleeye.codescanning.lint

import com.android.build.gradle.internal.LintGradleClient
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.LintBaseTask
import com.android.builder.model.AndroidProject
import com.android.builder.model.Variant
import com.android.tools.lint.LintCliFlags
import com.android.tools.lint.Warning
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.LintBaseline
import com.android.utils.Pair
import com.squirrel_explorer.eagleeye.codescanning.AnalysisCallback
import com.squirrel_explorer.eagleeye.codescanning.utils.SystemUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.tooling.provider.model.ToolingModelBuilder

import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * Task Id : N/A
 * Content : Base Class for Lint Task
 */
abstract class BaseLintTask extends BaseTask implements AnalysisCallback {
    protected LintOptions options
    protected IssueRegistry registry
    protected LintCliFlags flags
    protected Variant variant
    protected GlobalScope globalScope
    protected LintGradleClient client

    protected String defaultHtmlOutput = project.buildDir.absolutePath + '/outputs/lint-results.html'

    protected void preRun() {
        options = createLintOptions()

        registry = new BuiltinIssueRegistry()
        flags = new LintCliFlags()

        // 只支持Android工程，即com.android.application和com.android.library
        client = createLintGradleClient()

        setVariantName(variant.name)
    }

    private LintOptions createLintOptions() {
        LintOptions options = new LintOptions()

        Set<String> disableIds = new HashSet<String>()
        // 此处对使用java plugin但实际上却是Android工程的项目做了兼容,去除不影响实际扫描结果的LintError
        if (project.plugins.hasPlugin('java')) {
            disableIds.add('LintError')
        }
        options.setDisable(disableIds)
        options.setEnable(new HashSet<String>())
        options.setCheck(new HashSet<String>())

        return options
    }

    // 4.0.0 - latest
    private static final String SIGNATURE_4_0_0 = '[class com.android.tools.lint.client.api.IssueRegistry, class com.android.tools.lint.LintCliFlags, interface org.gradle.api.Project, interface com.android.builder.model.AndroidProject, class java.io.File, interface com.android.builder.model.Variant, class com.android.build.gradle.tasks.LintBaseTask$VariantInputs, class com.android.sdklib.BuildToolInfo]'

    private LintGradleClient createLintGradleClient() {
        // 获取Gradle工程的variant
        BaseVariantImpl variantImpl = null
        if (project.plugins.hasPlugin('com.android.application')) {
            variantImpl = project.android.applicationVariants.toArray()[0]
        } else if (project.plugins.hasPlugin('com.android.library')) {
            variantImpl = project.android.libraryVariants.toArray()[0]
        }

        if (variantImpl == null) {
            return null
        }

        // 获取variant的详细数据
        BaseVariantData variantData = null
        try {
            Method method = BaseVariantImpl.getDeclaredMethod('getVariantData')
            if (method != null) {
                // BaseVariantImpl#getVariantData()是protected方法
                method.setAccessible(true)
                variantData = method.invoke(variantImpl)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

        if (variantData == null) {
            return null
        }

        // 创建AndroidProject
        globalScope = variantData.scope.globalScope
        String modelName = AndroidProject.class.getName()
        ToolingModelBuilder modelBuilder = globalScope.toolingRegistry.getBuilder(modelName)
        AndroidProject modelProject = (AndroidProject)modelBuilder.buildAll(modelName, project)

        if (modelProject == null) {
            return null
        }

        Collection<Variant> variantList = modelProject.getVariants()
        if (variantList == null || variantList.isEmpty()) {
            return null
        }
        variant = variantList.toArray()[0]

        // 创建LintGradleClient
        LintGradleClient lintClient = null
        try {
            Class clazz = Class.forName('com.android.build.gradle.internal.LintGradleClient')
            if (clazz != null) {
                Constructor[] constructors = clazz.getConstructors()
                if (constructors != null && constructors.length > 0) {
                    for (Constructor constructor : constructors) {
                        String signature = constructor.getParameterTypes().toString()
                        if (SIGNATURE_4_0_0.equals(signature)) {
                            lintClient = (LintGradleClient)constructor.newInstance(
                                    registry,
                                    flags,
                                    project,
                                    modelProject,
                                    null,
                                    variant,
                                    new LintBaseTask.VariantInputs(variantData.getScope()),
                                    null)
                            break
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
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

    private boolean syncLintOptions() {
        LintOptions pluginLintOptions = globalScope.extension.lintOptions

        pluginLintOptions.disable = options.disable
        pluginLintOptions.enable = options.enable
        pluginLintOptions.check = options.check
        pluginLintOptions.textReport = options.textReport
        if (options.textOutput != null) {
            pluginLintOptions.textOutput(options.textOutput)
        }
        pluginLintOptions.htmlReport = options.htmlReport
        if (options.htmlOutput != null) {
            pluginLintOptions.htmlOutput = options.htmlOutput
        }
        pluginLintOptions.xmlReport = options.xmlReport
        if (options.xmlOutput != null) {
            pluginLintOptions.xmlOutput = options.xmlOutput
        }
        pluginLintOptions.abortOnError = false

        boolean ret = true
        try {
            Class clazz = Class.forName('com.android.build.gradle.tasks.LintBaseTask')
            if (clazz == null) {
                ret = false
            } else {
                Method method = clazz.getDeclaredMethod('syncOptions', LintOptions.class, LintGradleClient.class, LintCliFlags.class, Variant.class, Project.class, File.class, boolean.class, boolean.class)
                if (method == null) {
                    ret = false
                } else {
                    method.setAccessible(true)
                    method.invoke(null, pluginLintOptions, client, flags, variant, project, null, true, false)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
            ret = false
        }

        return ret
    }

    protected Pair<List<Warning>, LintBaseline> analyze() {
        Pair<List<Warning>, LintBaseline> warnings = null

        if (syncLintOptions()) {
            try {
                warnings = client.run(registry)
            } catch (IOException e) {
                e.printStackTrace()
            }
        }

        return warnings
    }

    @Override
    void onRulesDisabled(Set<String> disable) {
        if (disable != null && !disable.isEmpty()) {
            options.getDisable().addAll(disable)
        }
    }

    @Override
    void onRulesEnabled(Set<String> enable) {
        if (enable != null && !enable.isEmpty()) {
            options.getEnable().addAll(enable)
        }
    }

    @Override
    void onRulesChecked(Set<String> check) {
        if (check != null && !check.isEmpty()) {
            options.getCheck().addAll(check)
        }
    }

    @Override
    void onTextReport(File textOutput) {
        if (textOutput != null) {
            options.setTextReport(true)
            options.textOutput(textOutput)
        }
    }

    @Override
    void onHtmlReport(File htmlOutput) {
        if (htmlOutput != null) {
            options.setHtmlReport(true)
            options.setHtmlOutput(htmlOutput)
        }
    }

    @Override
    void onXmlReport(File xmlOutput) {
        if (xmlOutput != null) {
            options.setXmlReport(true)
            options.setXmlOutput(xmlOutput)
        }
    }

    @Override
    void onCustomRulesAdded(Set<String> customJars) {
        // 合并用户通过在codescanning->lint closure和dependencies两处设置的自定义规则库Jar包列表
        ArrayList<String> combinedCustomRules = new ArrayList<String>()
        ArrayList<String> customRulesFromDep = parseCustomRulesFromDep()
        if (customRulesFromDep != null) {
            combinedCustomRules.addAll(customRulesFromDep)
        }
        if (customJars != null) {
            combinedCustomRules.addAll(customJars)
        }
        if (combinedCustomRules.isEmpty()) {
            return
        }

        // 提取用户已经在ANDROID_LINT_JARS环境变量中预设置的自定义规则库Jar包列表
        HashSet<String> lintClassPaths = new HashSet<>()
        String lintClassPath = System.getenv('ANDROID_LINT_JARS')
        if (lintClassPath != null && !lintClassPath.isEmpty()) {
            lintClassPaths.addAll(lintClassPath.split(File.pathSeparator))
        }

        // 将新设置的和已有的合并
        StringBuilder sb = new StringBuilder()
        for (String ruleJar : combinedCustomRules) {
            if (ruleJar.endsWith('.jar') && !lintClassPaths.contains(ruleJar)) {
                sb.append(ruleJar).append(File.pathSeparator)
            }
        }

        if (sb.length() > 0) {
            // 去掉待设置环境变量字符串首尾的':'
            if (lintClassPath != null && !lintClassPath.isEmpty()) {
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
            SystemUtils.setJavaEnv('ANDROID_LINT_JARS', lintClassPath)
        }
    }

    private static final String INKER_LINT_CONFIG = 'inkerLint'

    /**
     * 解析Gradle配置里的inkerLint依赖,那些是eagleeye plugin的自定义规则库依赖,形如:
     * build.gradle :
     * configurations {
     *     inkerLint
     * }
     *
     * dependencies {
     *     inkerLint 'com.squirrel-explorer.eagleeye:lint_rules_allinone:1.0.1'
     * }
     *
     * @return  自定义规则库Jar包在本地maven仓库里的路径
     */
    private ArrayList<String> parseCustomRulesFromDep() {
        // 自定义configuration名称必须是'inkerLint'
        if (project.configurations.findByName(INKER_LINT_CONFIG) == null) {
            return null
        }

        // 获取dependencies里'inkerLint'配置的依赖
        HashSet<String> depSet = new HashSet<String>()
        Configuration configuration = project.configurations.getByName(INKER_LINT_CONFIG)
        configuration.dependencies.each { dependency ->
            depSet.add(dependency.group + dependency.name)
        }
        if (depSet.isEmpty()) {
            return null
        }

        // 从已解析好的依赖中找到'inkerLint'的,获取其在本地maven仓库中的路径
        ArrayList<String> customRulesFromDep = new ArrayList<String>()
        configuration.resolvedConfiguration.resolvedArtifacts.each { artifact ->
            ModuleVersionIdentifier moduleVersionId = artifact.moduleVersion.id
            if (depSet.contains(moduleVersionId.group + moduleVersionId.name)) {
                customRulesFromDep.add(artifact.file.absolutePath)
            }
        }

        return customRulesFromDep.isEmpty() ? null : customRulesFromDep
    }
}
