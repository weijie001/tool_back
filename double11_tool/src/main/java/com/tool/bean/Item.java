package com.tool.bean;


import java.io.Serializable;

public class Item implements Serializable {
    /**
     * id
     */
    private int itemId;
    /**
     * 类型1资源,2道具
     */
    private int itemType;
    /**
     * 名字
     */
    private String name;
    /**
     * 描述
     */
    private String desc;

    /**
     * 产出
     */
    private String output;
    /**
     * 使用类型
     */

    private int useType;
    /**
     * 是否可使用
     **/
    private int canUse;

    private String effect;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getUseType() {
        return useType;
    }

    public void setUseType(int useType) {
        this.useType = useType;
    }

    public int getCanUse() {
        return canUse;
    }

    public void setCanUse(int canUse) {
        this.canUse = canUse;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}
