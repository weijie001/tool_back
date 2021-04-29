package com.tool.bean;


import java.io.Serializable;

public class ItemAward implements Serializable {
    private int itemId;
    private int num;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
