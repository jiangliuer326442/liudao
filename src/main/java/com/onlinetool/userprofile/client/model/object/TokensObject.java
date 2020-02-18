package com.onlinetool.userprofile.client.model.object;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/14 下午12:15
 * @description：用户token对象
 * @modified By：
 * @version: 1.0$
 */
public class TokensObject {
    private long uid;
    private long companyId;
    private String token;
    private long timeStamp;

    public TokensObject(long uid, long companyId, String token, long timeStamp) {
        this.uid = uid;
        this.companyId = companyId;
        this.token = token;
        this.timeStamp = timeStamp;
    }

    public TokensObject(){}

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
