package com.squirrel_explorer.eagleeye.codescanning.lint

import com.squirrel_explorer.eagleeye.codescanning.BaseExtension
import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.Project

class LintExtension extends BaseExtension {
    String disable          // 需disable的规则列表
    String enable           // 需enable的规则列表
    String check            // 指定扫描的规则列表
    File lintConfig         // 规则列表配置文件
    File textOutput         // TextReporter
    File htmlOutput         // HtmlReporter
    File xmlOutput          // XmlReporter
    ArrayList<String> customRules = new ArrayList<String>()     // 自定义规则库Jar包列表
    String productFlavor    // productFlavor in build.gradle
    String buildType        // buildType in build.gradle

    public LintExtension(Project project) {
        super(project)
    }

    public void setDisable(String disable) {
        this.disable = disable
    }

    public void setEnable(String enable) {
        this.enable = enable
    }

    public void setCheck(String check) {
        this.check = check
    }

    public void setLintConfig(String lintConfig) {
        if (null != lintConfig && lintConfig.length() > 0) {
            if (lintConfig.startsWith('http://') ||
                lintConfig.startsWith('https://') ||
                lintConfig.startsWith('ftp://')) {
                this.lintConfig = FileUtils.downloadFile(lintConfig, project.buildDir.absolutePath)
            } else {
                this.lintConfig = new File(lintConfig)
            }
        }
    }

    public void setLintConfig(File lintConfig) {
        this.lintConfig = lintConfig
    }

    public void setTextOutput(String textOutput) {
        this.textOutput = FileUtils.safeCreateFile(textOutput)
    }

    public void setTextOutput(File textOutput) {
        this.textOutput = textOutput
    }

    public void setHtmlOutput(String htmlOutput) {
        this.htmlOutput = FileUtils.safeCreateFile(htmlOutput)
    }

    public void setHtmlOutput(File htmlOutput) {
        this.htmlOutput = htmlOutput
    }

    public void setXmlOutput(String xmlOutput) {
        this.xmlOutput = FileUtils.safeCreateFile(xmlOutput)
    }

    public void setXmlOutput(File xmlOutput) {
        this.xmlOutput = xmlOutput
    }

    public void setCustomRule(String customRule) {
        if (null != customRule && customRule.length() > 0) {
            String downloadDir = project.projectDir.absolutePath + '/lint-jars'
            if (customRule.startsWith('http://') ||
                    customRule.startsWith('https://') ||
                    customRule.startsWith('ftp://')) {
                File customRuleFile = FileUtils.downloadFile(customRule, downloadDir)
                if (null != customRuleFile) {
                    this.customRules.add(customRuleFile.absolutePath)
                }
            } else {
                this.customRules.add(customRule)
            }
        }
    }

    public void setProductFlavor(String productFlavor) {
        this.productFlavor = productFlavor
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType
    }
}
