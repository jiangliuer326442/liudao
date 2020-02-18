package com.onlinetool.userprofile.client.model.object;

public class DbConnectorObject {
    /**
     * 数据库链接名称
     */
    private String dbConnectorName;

    /**
     * 数据库连接类型
     */
    private String connectorType;

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
     * 是否使用ssh通道
     */
    private String isUseSsh;

    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 项目id
     */
    private long projectId;

    public String getDbConnectorName() {
        return dbConnectorName;
    }

    public void setDbConnectorName(String dbConnectorName) {
        this.dbConnectorName = dbConnectorName;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
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

    public String getIsUseSsh() {
        return isUseSsh;
    }

    public void setIsUseSsh(String isUseSsh) {
        this.isUseSsh = isUseSsh;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
