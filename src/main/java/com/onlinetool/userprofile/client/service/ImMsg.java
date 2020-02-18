package com.onlinetool.userprofile.client.service;

public interface ImMsg {
    /**
     * 发送markdown消息
     * @param chatType
     * @param chatHookUrl
     * @param secret 加密密钥
     * @param title 标题
     * @param markdownString
     * @return
     */
    boolean sendMarkdown(int chatType, String chatHookUrl, String secret, String title, String markdownString);
}
