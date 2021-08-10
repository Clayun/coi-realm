package com.mcylm.coi.realm.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class FileUtils {

    //写入文件
    public static boolean saveFile(String string,String path) {

        PrintStream stream=null;
        try {

            stream=new PrintStream(path);//写入的文件path
            stream.print(string);//写入的字符串
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String createFileByPath(String path){
        try{

            File file = new File(path);
            File fileParent = file.getParentFile();

            if(!fileParent.exists()){
                fileParent.mkdirs();
            }

            if(file.exists()){
                return "文件已存在，不能重复创建";
            }
            file.createNewFile();


        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return "文件创建成功";
    }
}
