package com.squirrel_explorer.eagleeye.codescanning.lint

import com.squirrel_explorer.eagleeye.codescanning.BaseExtension
import com.squirrel_explorer.eagleeye.codescanning.utils.FileUtils
import org.gradle.api.Project

import java.security.DigestInputStream
import java.security.MessageDigest

class ApiCheckExtension extends BaseExtension {
    File apiCheckConfig         // Missing API检查的配置文件(包括hide API & removed API)
    boolean hideApi = true      // 检查hide API
    boolean removedApi = true   // 检查removed API
    File textOutput             // TextReporter
    File htmlOutput             // HtmlReporter
    File xmlOutput              // XmlReporter
    ArrayList<String> apicheckRules = new ArrayList<String>()   // 自定义规则库Jar包列表
    String productFlavor        // productFlavor in build.gradle
    String buildType            // buildType in build.gradle

    public ApiCheckExtension(Project project) {
        super(project)
    }

    public void setApiCheckConfig(String apiCheckConfig) {
        if (null != apiCheckConfig && apiCheckConfig.length() > 0) {
            if (apiCheckConfig.startsWith('http://') ||
                    apiCheckConfig.startsWith('https://') ||
                    apiCheckConfig.startsWith('ftp://')) {
                this.apiCheckConfig = FileUtils.downloadFile(apiCheckConfig, project.buildDir.absolutePath)
            } else {
                this.apiCheckConfig = new File(apiCheckConfig)
            }
        }
        downloadApiCheckConfig()
    }

    public void setApiCheckConfig(File apiCheckConfig) {
        this.apiCheckConfig = apiCheckConfig
        downloadApiCheckConfig()
    }

    /**
     * 下载Missing API Check配置文件里真正对应各版本hide/removed API信息的文件
     * 该配置文件格式形如:
     * <hide methods apilevel 15>
     * <hashcode>
     * <hide methods apilevel 16>
     * <hashcode>
     * ......
     * <removed methods apilevel 15>
     * <hashcode>
     * <removed methods apilevel 16>
     * <hashcode>
     * ......
     */
    private void downloadApiCheckConfig() {
        if (null != apiCheckConfig) {
            ArrayList<String> apiCheckList = new ArrayList<String>()
            ArrayList<String> apiCheckMd5List = new ArrayList<String>()

            // 解析配置文件内容,按行保存在apiCheckList中(包含各版本hide/removed API信息的文件及其md5)
            FileReader fr = null
            BufferedReader br = null
            try {
                fr = new FileReader(apiCheckConfig)
                br = new BufferedReader(fr)

                String str = null
                while (null != (str = br.readLine())) {
                    apiCheckList.add(str)
                }
            } catch (Exception e) {
                // TODO
            } finally {
                if (null != br) {
                    br.close()
                    br = null
                }
                if (null != fr) {
                    fr.close()
                    fr = null
                }
            }

            if (!apiCheckList.isEmpty() && (0 == apiCheckList.size() % 2)) {
                // 将各版本hide/removed API信息的文件及其md5分离
                // 奇数行:文件
                // 偶数行:文件md5
                int i
                for (i = 1; i < apiCheckList.size(); i += 2) {
                    apiCheckMd5List.add(apiCheckList.get(i))
                }
                apiCheckList.removeAll(apiCheckMd5List)

                int bufSize = 1024 * 16
                String localDir = project.buildDir.absolutePath + File.separator + 'missing_api_database'
                String fileUrl
                File file
                MessageDigest md5
                FileInputStream input
                DigestInputStream digestInput
                BigInteger bi
                String checksum
                for (i = 0; i < apiCheckList.size(); i++) {
                    fileUrl = apiCheckList.get(i)
                    file = new File(localDir + File.separator +
                            fileUrl.substring(fileUrl.lastIndexOf('/') + 1, fileUrl.length()))

                    if (!file.exists()) {
                        // 如果API信息文件还未下载,则直接下载
                        FileUtils.downloadFile(fileUrl, localDir)
                    } else {
                        // 如果API信息文件已下载过,则先对本地文件取md5,再与配置文件里的md5做比较,
                        // 如果不一致则说明有更新,需重新下载
                        try {
                            md5 = MessageDigest.getInstance("MD5")
                            input = new FileInputStream(file)
                            digestInput = new DigestInputStream(input, md5)
                            byte[] buffer =new byte[bufSize]
                            while (digestInput.read(buffer) > 0) {}
                            md5 = digestInput.getMessageDigest()
                            bi = new BigInteger(1, md5.digest())
                            checksum = bi.toString(16)
                        } catch (Exception e) {
                            e.printStackTrace()
                        } finally {
                            if (null != input) {
                                input.close()
                                input = null
                            }
                        }

                        if (!apiCheckMd5List.get(i).equals(checksum)) {
                            FileUtils.downloadFile(fileUrl, localDir)
                        }
                    }
                }
            }
        }
    }

    public void setHideApi(boolean hideApi) {
        this.hideApi = hideApi
    }

    public void setRemovedApi(boolean removedApi) {
        this.removedApi = removedApi
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

    public void setApicheckRuleJar(String apicheckRule) {
        if (null != apicheckRule && apicheckRule.length() > 0) {
            String downloadDir = project.projectDir.absolutePath + '/lint-jars'
            if (apicheckRule.startsWith('http://') ||
                    apicheckRule.startsWith('https://') ||
                    apicheckRule.startsWith('ftp://')) {
                File customRuleFile = FileUtils.downloadFile(apicheckRule, downloadDir)
                if (null != customRuleFile) {
                    this.apicheckRules.add(customRuleFile.absolutePath)
                }
            } else {
                this.apicheckRules.add(apicheckRule)
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
