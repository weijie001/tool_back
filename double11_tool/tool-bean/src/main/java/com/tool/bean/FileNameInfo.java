package com.tool.bean;

import java.util.List;

public class FileNameInfo {
    private int type;
    private List<String> fileNames;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
