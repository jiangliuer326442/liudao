package com.onlinetool.userprofile.client.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.validation.constraints.NotBlank;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午5:21
 * @description：数据库连接管理
 * @modified By：
 * @version: 1.0$
 */
public class DbConnectorEntity {

    @JsonProperty("DbConnectName")
    @NotBlank(message = "连接名称必填")
    /**
     * 数据库连接名
     */
    private String dbConnectName;

    @JsonProperty("ConnectorType")
    @NotBlank(message = "数据库类型必填")
    /**
     * 数据库连接类型
     */
    private String connectorType;

    @JsonProperty("Host")
    @NotBlank(message = "数据库主机地址必填")
    /**
     * 主机地址
     */
    private String host;

    @JsonProperty("Port")
    @NotBlank(message = "数据库所用端口号必填")
    /**
     * 端口号
     */
    private String port;

    @JsonProperty("Username")
    @NotBlank(message = "数据库连用用户名必填")
    /**
     * 用户名
     */
    private String username;

    @JsonProperty("Password")
    @NotBlank(message = "数据库连用密码必填")
    /**
     * 密码
     */
    private String password;

    @JsonProperty("IsUseSsh")
    @NotBlank(message = "是否使用ssh通道必选")
    /**
     * 是否使用ssh通道
     */
    private String isUseSsh = "0";

    @JsonProperty("HostName")
    /**
     * 主机名称
     */
    private String hostName;

    public String getDbConnectName() {
        return dbConnectName;
    }

    public void setDbConnectName(String dbConnectName) {
        this.dbConnectName = dbConnectName;
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public ObjectNode toObjectNode(ObjectNode serviceList){
        ObjectMapper mapper = new ObjectMapper();
        Field[] field = this.getClass().getDeclaredFields();
        String dbConnectName = null;

        ObjectNode serviceRow = mapper.createObjectNode();

        for (Field item : field) {
            String name = item.getName();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            String value = null;
            try {
                Method m = this.getClass().getMethod("get" + name);
                value = (String) m.invoke(this);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if ("DbConnectName".equals(name)) {
                dbConnectName = value;
            }
            serviceRow.put(name, value);
        }
        serviceList.set(dbConnectName, serviceRow);

        return serviceList;
    }
}
