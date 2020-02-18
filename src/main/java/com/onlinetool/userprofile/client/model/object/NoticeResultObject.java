package com.onlinetool.userprofile.client.model.object;

/**
 * 部署结果通知
 */
public class NoticeResultObject {
    /**
     * 任务id
     */
    private long taskId;

    /**
     * 上线结果
     */
    private boolean result;

    /**
     * 部署环境
     */
    private int env;

    private String status = "notice";

    /**
     * 消息内容
     */
    private String message;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
