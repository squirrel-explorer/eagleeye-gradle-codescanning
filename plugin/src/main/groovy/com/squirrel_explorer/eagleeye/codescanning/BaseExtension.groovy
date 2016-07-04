package com.squirrel_explorer.eagleeye.codescanning

import org.gradle.api.Project

class BaseExtension {
    Project project

    public BaseExtension(Project project) {
        this.project = project
    }

    protected File downloadFile(String fileUrl, String localDir) {
        File file = null

        if (fileUrl.startsWith('http://') ||
                fileUrl.startsWith('https://') ||
                fileUrl.startsWith('ftp://')) {
            // Local config file
            String dstFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1, fileUrl.length())
            dstFileName = localDir + File.separator + dstFileName
            File dstFile = new File(dstFileName)
            File parentDstFile = dstFile.getParentFile()
            if (null != parentDstFile) {
                if (!parentDstFile.exists()) {
                    parentDstFile.mkdirs()
                }
            }

            // Remote url
            InputStream input = null
            FileOutputStream output = null
            try {
                URL url = new URL(fileUrl)
                input = url.openStream()
                output = new FileOutputStream(dstFile)

                byte[] buffer = new byte[1024 * 8]
                int count = 0
                while ((count = input.read(buffer)) > 0) {
                    output.write(buffer, 0, count)
                }

                file = dstFile
            } catch (Exception e) {
                // TODO
            } finally {
                if (null != output) {
                    output.close()
                    output = null
                }
                if (null != input) {
                    input.close()
                    input = null
                }
            }
        }

        return file
    }
}
