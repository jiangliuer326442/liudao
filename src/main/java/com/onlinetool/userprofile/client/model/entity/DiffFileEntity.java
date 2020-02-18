package com.onlinetool.userprofile.client.model.entity;

import org.eclipse.jgit.diff.DiffEntry;

import java.util.List;

/**
 * 文件差异比较diff
 */
public class DiffFileEntity {
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 修改类型
     */
    private DiffEntry.ChangeType changeType;

    /**
     * 行内容
     */
    private List<DiffContentLine> diffContentLine;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(DiffEntry.ChangeType changeType) {
        this.changeType = changeType;
    }

    public List<DiffContentLine> getDiffContentLine() {
        return diffContentLine;
    }

    public void setDiffContentLine(List<DiffContentLine> diffContentLine) {
        this.diffContentLine = diffContentLine;
    }
}
