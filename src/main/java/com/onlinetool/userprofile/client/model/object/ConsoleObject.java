package com.onlinetool.userprofile.client.model.object;

import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/21 下午10:04
 * @description：控制台输出调试
 * @modified By：
 * @version: 1.0$
 */
public class ConsoleObject implements Cloneable {
    public final static String LEVEL_INFO = "command";
    public final static String LEVEL_WARM = "warning";
    public final static String LEVEL_ERROR = "error";
    public final static String LEVEL_SUCCESS = "success";

    /**
     * 任务id
     */
    private long taskId;

    /**
     * 微服务id
     */
    private long servsId;

    /**
     * 任务关联主机名称
     */
    private String hostName;

    /**
     * 所属阶段
     */
    private long stage;

    /**
     * 输出等级
     */
    private String level;

    /**
     * 标题
     */
    private String title;

    private String status;

    private ExceptionDetail exceptionDetail;

    public ConsoleObject(long taskId, long servsId, String hostName, long stage, String level, String title) {
        this.taskId = taskId;
        this.servsId = servsId;
        this.hostName = hostName;
        this.stage = stage;
        this.level = level;
        this.title = title;
    }

    public ExceptionDetail getExceptionDetail() {
        return exceptionDetail;
    }

    public String getTitle() {
        return title;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getServsId() {
        return servsId;
    }

    public String getHostName() {
        return hostName;
    }

    public long getStage() {
        return stage;
    }

    public String getLevel() {
        return level;
    }

    public void setStage(long stage) {
        this.stage = stage;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setExceptionDetail(ExceptionDetail exceptionDetail) {
        this.title = exceptionDetail.getErrorMsg();
        this.exceptionDetail = exceptionDetail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
