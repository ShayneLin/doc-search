package com.doc.search.util;

import com.doc.search.dto.FileListWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {


    /**
     * 获取文件路径下的所有目录
     * @param dir
     * @return
     */
    public static List<File> getAllFileList(String dir){
        File file = new File(dir);
        if (!file.isDirectory()){
            return Collections.emptyList();
        }
        File[] files = file.listFiles();
        List<File> allFileList = new ArrayList<>();
        List<File> startFileList = Arrays.asList(files);
        // 将第一层的文件分为普通文件和目录，使用目录去做遍历
        List<File> dirList = startFileList.stream().filter(File::isDirectory).collect(Collectors.toList());
        List<File> fileList = startFileList.stream().filter(File::isFile).collect(Collectors.toList());
        allFileList.addAll(fileList);

        //开始遍历目录
        while (!dirList.isEmpty()){
            //如果不为空则需要继续遍历
            FileListWrapper fileListWrapper = travelFile(dirList);
            //目录则需要继续遍历
            dirList = fileListWrapper.getDirs();
            //文件则添加到集合里
            allFileList.addAll(fileListWrapper.getFiles());
        }
        return allFileList;
    }

    /**
     * 解析文件，文件放到files集合、目录放到dirs集合
     * @param dirs
     * @return
     */
    public static FileListWrapper travelFile(List<File> dirs){
        List<File> fileList = new ArrayList<>();
        List<File> dirList = new ArrayList<>();
        for (File file : dirs){
            if (file.isDirectory()){
                for (File subFile : file.listFiles()) {
                    if (subFile.isDirectory()) {
                        //记录目录，下次遍历
                        dirList.add(subFile);
                    } else {
                        //普通文件
                        fileList.add(subFile);
                    }
                }
            }
        }
        return new FileListWrapper(fileList, dirList);
    }
}
