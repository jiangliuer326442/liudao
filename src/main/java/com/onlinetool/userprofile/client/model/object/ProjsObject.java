package com.onlinetool.userprofile.client.model.object;

import java.io.Serializable;
import java.util.List;

public class ProjsObject implements Serializable {

    /**
     * 项目id
     */
    private long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目图标
     */
    private String icon;

    /**
     * 项目沟通方式
     */
    private int chatType;

    /**
     * 项目沟通webhook
     */
    private String chatHookUrl;

    /**
     * 聊天钩子私密钥
     */
    private String chatHookSecret;

    /**
     * 管理员
     */
    private List<Long> admin;

    /**
     * 开发人员
     */
    private List<Long> developer;

    /**
     * 测试人员
     */
    private List<Long> tester;

    /**
     * 铲平经理
     */
    private List<Long> pm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getChatHookUrl() {
        return chatHookUrl;
    }

    public void setChatHookUrl(String chatHookUrl) {
        this.chatHookUrl = chatHookUrl;
    }

    public String getChatHookSecret() {
        return chatHookSecret;
    }

    public void setChatHookSecret(String chatHookSecret) {
        this.chatHookSecret = chatHookSecret;
    }

    public List<Long> getAdmin() {
        return admin;
    }

    public void setAdmin(List<Long> admin) {
        this.admin = admin;
    }

    public List<Long> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<Long> developer) {
        this.developer = developer;
    }

    public List<Long> getTester() {
        return tester;
    }

    public void setTester(List<Long> tester) {
        this.tester = tester;
    }

    public List<Long> getPm() {
        return pm;
    }

    public void setPm(List<Long> pm) {
        this.pm = pm;
    }
}
