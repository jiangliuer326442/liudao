package com.onlinetool.userprofile.client.model.object;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author ：mmzs
 * @date ：Created in 2019/11/14 下午3:47
 * @description：任务对象
 * @modified By：
 * @version: 1.0$
 */
public class TaskObject {
    /**
     * 任务id
     */
    private long taskId;

    /**
     * 项目信息
     */
    private ProjsObject projsObject;

    /**
     * 任务标题
     */
    private String taskName;

    /**
     * 任务详细列表
     */
    private List<TaskDetailObject> list;

    /**
     * 任务状态
     */
    private int status;

    /**
     * 测试环境部署主机
     */
    private String testHost;

    /**
     * 预发布环境部署主机
     */
    private String prevHost;

    /**
     * 任务的创建时间
     */
    private Timestamp ctime;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public ProjsObject getProjsObject() {
        return projsObject;
    }

    public void setProjsObject(ProjsObject projsObject) {
        this.projsObject = projsObject;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<TaskDetailObject> getList() {
        return list;
    }

    public void setList(List<TaskDetailObject> list) {
        this.list = list;
    }

    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTestHost() {
        return testHost;
    }

    public void setTestHost(String testHost) {
        this.testHost = testHost;
    }

    public String getPrevHost() {
        return prevHost;
    }

    public void setPrevHost(String prevHost) {
        this.prevHost = prevHost;
    }
}
