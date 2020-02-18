package com.onlinetool.userprofile.client.model.object;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/22 下午10:45
 * @description：主机对象
 * @modified By：
 * @version: 1.0$
 */
public class HostObject {
    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private long port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 项目id
     */
    private long projectId;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getPort() {
        return port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
