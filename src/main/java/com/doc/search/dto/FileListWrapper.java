package com.doc.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class FileListWrapper{
    /**
     * 普通文件
     */
    private List<File> files;

    /**
     * 目录
     */
    private List<File> dirs;
}