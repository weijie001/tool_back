package com.tool.bean;

public enum FileTypeEnum {
    CONFIG(1),
    PROTO(2);
    private int type;

    FileTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
