package com.onlinetool.userprofile.client.model.object;

import java.util.List;

/**
 * @author ：mmzs
 * @date ：Created in 2019/11/14 下午3:49
 * @description：任务详情
 * @modified By：
 * @version: 1.0$
 */
public class TaskDetailObject {

    /**
     * 微服务id
     */
    private long servsId;

    private ServsObject servsObject;

    /**
     * 需要上线的分支
     */
    private String branch;

    /**
     * 需要执行的sql语句列表
     */
    private String[] sqls;

    /**
     * 需要执行的job语句列表
     */
    private List<String> jobs;

    /**
     * 上线任务相关job列表
     */
    private List<TaskJobObject> jobsCommand;

    public long getServsId() {
        return servsId;
    }

    public void setServsId(long servsId) {
        this.servsId = servsId;
    }

    public ServsObject getServsObject() {
        return servsObject;
    }

    public void setServsObject(ServsObject servsObject) {
        this.servsObject = servsObject;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String[] getSqls() {
        return sqls;
    }

    public void setSqls(String[] sqls) {
        this.sqls = sqls;
    }

    public List<TaskJobObject> getJobsCommand() {
        return jobsCommand;
    }

    public void setJobsCommand(List<TaskJobObject> jobsCommand) {
        this.jobsCommand = jobsCommand;
    }

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }
}
