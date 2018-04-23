package com.squirrel_explorer.eagleeye.codescanning.utils

/**
 * Created by squirrel-explorer on 16/7/27.
 */
public class FileUtils {
    public static File downloadFile(String fileUrl, String localDir) {
        File file = null

        // Validate url format
        if (fileUrl.startsWith('http://') ||
                fileUrl.startsWith('https://') ||
                fileUrl.startsWith('ftp://')) {
            // Local config file
            String dstFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1, fileUrl.length())
            dstFileName = localDir + File.separator + dstFileName
            File dstFile = FileUtils.safeCreateFile(dstFileName)

            if (dstFile != null) {
                InputStream input = null
                FileOutputStream output = null
                try {
                    // Remote url
                    URL url = new URL(fileUrl)
                    input = url.openStream()
                    output = new FileOutputStream(dstFile)

                    // Download contents
                    byte[] buffer = new byte[1024 * 8]
                    int count = 0
                    while ((count = input.read(buffer)) > 0) {
                        output.write(buffer, 0, count)
                    }

                    file = dstFile
                } catch (Exception e) {
                    // TODO
                } finally {
                    if (output != null) {
                        output.close()
                        output = null
                    }
                    if (input != null) {
                        input.close()
                        input = null
                    }
                }
            }
        }

        return file
    }

    public static void checkIntegrity(File file) {
        if (file == null) {
            return
        }

        File parent = file.getParentFile()
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
    }

    public static File safeCreateFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null
        }

        File file = new File(filename)
        checkIntegrity(file)
        return file
    }
}
