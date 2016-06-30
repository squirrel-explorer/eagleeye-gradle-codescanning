package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

import java.security.DigestInputStream
import java.security.MessageDigest

class ApiCheckExtension extends BaseExtension {
    File apiCheckConfig
    boolean hideApi = true
    boolean removedApi = true
    File textOutput
    File htmlOutput
    File xmlOutput
    String productFlavor
    String buildType

    public ApiCheckExtension(Project project) {
        super(project)
    }

    public void setApiCheckConfig(String apiCheckConfig) {
        if (null != apiCheckConfig && apiCheckConfig.length() > 0) {
            if (apiCheckConfig.startsWith('http://') ||
                    apiCheckConfig.startsWith('https://') ||
                    apiCheckConfig.startsWith('ftp://')) {
                this.apiCheckConfig = downloadFile(apiCheckConfig, project.buildDir.absolutePath)
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

    private void downloadApiCheckConfig() {
        if (null != apiCheckConfig) {
            ArrayList<String> apiCheckList = new ArrayList<>()
            ArrayList<String> apiCheckMd5List = new ArrayList<>()

            FileReader fr = null
            BufferedReader br = null
            try {
                fr = new FileReader(apiCheckConfig)
                br = new BufferedReader(fr);

                String str = null
                while (null != (str = br.readLine())) {
                    apiCheckList.add(str);
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
                        downloadFile(fileUrl, localDir)
                    } else {
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
                            downloadFile(fileUrl, localDir)
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

    public void setProductFlavor(String productFlavor) {
        this.productFlavor = productFlavor
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType
    }
}
