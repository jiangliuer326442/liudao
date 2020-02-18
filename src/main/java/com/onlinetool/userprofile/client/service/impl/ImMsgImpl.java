package com.onlinetool.userprofile.client.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.library.constant.ChatTypeConstant;
import com.onlinetool.userprofile.client.library.util.HttpClientUtil;
import com.onlinetool.userprofile.client.service.ImMsg;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class ImMsgImpl implements ImMsg {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper;

    @Autowired
    public ImMsgImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean sendMarkdown(int chatType, String chatHookUrl, String secret, String title, String markdownString) {
        if (chatHookUrl.equals("")) return false;
        if (chatType == ChatTypeConstant.WX_WORK){
            ObjectNode markdownNode = this.objectMapper.createObjectNode();
            markdownNode.put("content", markdownString);

            ObjectNode rootNode = this.objectMapper.createObjectNode();
            rootNode.put("msgtype", "markdown");
            rootNode.set("markdown", markdownNode);

            try {
                HttpClientUtil.sendPost(chatHookUrl, this.objectMapper.writeValueAsString(rootNode));
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }else if (chatType == ChatTypeConstant.DING_DING){
            if (secret != null && !secret.equals("")){
                long timestamp = System.currentTimeMillis();
                try {
                    String sign = this.mkDingDingSign(timestamp, secret);
                    chatHookUrl = chatHookUrl + "&timestamp="+Long.toString(timestamp)+"&sign="+sign;
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
                    logger.error(e.getMessage());
                    return false;
                }
            }
            ObjectNode markdownNode = this.objectMapper.createObjectNode();
            markdownNode.put("title", title);
            markdownNode.put("text", markdownString);

            ObjectNode rootNode = this.objectMapper.createObjectNode();
            rootNode.put("msgtype", "markdown");
            rootNode.set("markdown", markdownNode);

            try {
                HttpClientUtil.sendPost(chatHookUrl, this.objectMapper.writeValueAsString(rootNode));
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    private String mkDingDingSign(Long timestamp, String secret) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
    }
}
