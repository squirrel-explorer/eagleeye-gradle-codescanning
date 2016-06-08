package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class LintExtension {
    Project project

    String disable
    String enable
    String check
    File lintConfig
    File textOutput
    File htmlOutput
    File xmlOutput
    String additionalRuleJars
    String productFlavor
    String buildType

    public LintExtension(Project project) {
        this.project = project
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
                // Local config file
                String dstFileName = lintConfig.substring(lintConfig.lastIndexOf('/') + 1, lintConfig.length())
                dstFileName = project.buildDir.absolutePath + File.separator + dstFileName
                File dstFile = new File(dstFileName)
                File parentDstFile = dstFile.getParentFile()
                if (null != parentDstFile) {
                    if (!parentDstFile.exists()) {
                        parentDstFile.mkdirs();
                    }
                }

                // Remote url
                InputStream input = null
                FileOutputStream output = null
                try {
                    URL url = new URL(lintConfig)
                    input = url.openStream()
                    output = new FileOutputStream(dstFile)

                    byte[] buffer = new byte[1024 * 4]
                    int count = 0
                    while ((count = input.read(buffer)) > 0) {
                        output.write(buffer, 0, count)
                    }

                    this.lintConfig = dstFile
                } catch (Exception e) {
                    this.lintConfig = null
                } finally {
                    if (null != output) {
                        output.close()
                    }
                    if (null != input) {
                        input.close()
                    }
                }
            } else {
                this.lintConfig = new File(lintConfig)
            }
        }
    }

    public void setLintConfig(File lintConfig) {
        this.lintConfig = lintConfig
    }

    public void setTextOutput(String textOutput) {
        if (null != textOutput && textOutput.length() > 0) {
            this.textOutput = new File(textOutput)
        }
    }

    public void setTextOutput(File textOutput) {
        this.textOutput = textOutput
    }

    public void setHtmlOutput(String htmlOutput) {
        if (null != htmlOutput && htmlOutput.length() > 0) {
            this.htmlOutput = new File(htmlOutput)
        }
    }

    public void setHtmlOutput(File htmlOutput) {
        this.htmlOutput = htmlOutput
    }

    public void setXmlOutput(String xmlOutput) {
        if (null != xmlOutput && xmlOutput.length() > 0) {
            this.xmlOutput = new File(xmlOutput)
        }
    }

    public void setXmlOutput(File xmlOutput) {
        this.xmlOutput = xmlOutput
    }

    public void setAdditionalRuleJars(String additionalRuleJars) {
        this.additionalRuleJars = additionalRuleJars
    }

    public void setProductFlavor(String productFlavor) {
        this.productFlavor = productFlavor
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType
    }
}
