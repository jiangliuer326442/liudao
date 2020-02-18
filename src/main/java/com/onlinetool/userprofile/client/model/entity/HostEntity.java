package com.onlinetool.userprofile.client.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午5:10
 * @description：主机管理
 * @modified By：
 * @version: 1.0$
 */
public class HostEntity {

    @JsonProperty("HostName")
    @NotBlank(message = "主机名称必填")
    @Length(min = 2, max = 15, message = "主机名不合规范")
    /**
     * 主机名称
     */
    private String hostName;

    @JsonProperty("Host")
    @NotBlank(message = "主机地址不能为空")
    /**
     * 主机地址
     */
    private String host;

    @JsonProperty("Port")
    @NotBlank(message = "端口号必填")
    /**
     * 端口号
     */
    private String port;

    @JsonProperty(value = "AuthenticationType")
    /**
     * 认证方式
     */
    private String authenticationType;

    @JsonProperty(value = "Username")
    @NotBlank(message = "用户名必填")
    /**
     * 用户名
     */
    private String username;

    @JsonProperty(value = "Password")
    /**
     * 密码
     */
    private String password;

    @JsonProperty(value = "PrivateKeyPath")
    /**
     * 私钥所在地址
     */
    private String privateKeyPath;

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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
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

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public ObjectNode toObjectNode(ObjectNode serviceList){
        ObjectMapper mapper = new ObjectMapper();
        Field[] field = this.getClass().getDeclaredFields();
        String hostName = null;

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
            if ("HostName".equals(name)) {
                hostName = value;
            }
            serviceRow.put(name, value);
        }
        serviceList.set(hostName, serviceRow);

        return serviceList;
    }

    @Override
    public String toString() {
        return "HostEntity{" +
                "hostName='" + hostName + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", authenticationType='" + authenticationType + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                '}';
    }
}
