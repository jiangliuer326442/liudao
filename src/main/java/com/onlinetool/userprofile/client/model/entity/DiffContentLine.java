package com.onlinetool.userprofile.client.model.entity;

public class DiffContentLine {

    /**
     * 第一个字符
     * 为 - + 空格中的一种
     */
    private String operate;

    /**
     * 旧的行号
     */
    private int oldLineNum;

    /**
     * 新的行号
     */
    private int newLineNum;

    /**
     * 行内容
     */
    private String content;

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public int getOldLineNum() {
        return oldLineNum;
    }

    public void setOldLineNum(int oldLineNum) {
        this.oldLineNum = oldLineNum;
    }

    public int getNewLineNum() {
        return newLineNum;
    }

    public void setNewLineNum(int newLineNum) {
        this.newLineNum = newLineNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
