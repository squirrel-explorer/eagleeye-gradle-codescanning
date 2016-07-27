package com.squirrel_explorer.eagleeye.codescanning

import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.Project

class LintExtension extends BaseExtension {
    String disable
    String enable
    String check
    File lintConfig
    File textOutput
    File htmlOutput
    File xmlOutput
    String customRuleJars
    String productFlavor
    String buildType

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

    public void setCustomRuleJars(String customRuleJars) {
        this.customRuleJars = customRuleJars
    }

    public void setProductFlavor(String productFlavor) {
        this.productFlavor = productFlavor
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType
    }
}
