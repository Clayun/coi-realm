package com.mcylm.coi.realm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class MapUtils {

    public static void extractMap(File zipFile, File destDir) throws Exception {
        // 创建目标目录
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // 打开 ZIP 文件
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));

        extractFile(zipIn, destDir.getAbsolutePath());
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws Exception {
        // 创建输出文件流
        FileOutputStream fos = new FileOutputStream(filePath);

        // 从 ZIP 文件中读取数据并写入输出文件流
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = zipIn.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        // 关闭输出文件流
        fos.close();
    }

}
