package com.onlinetool.userprofile.client.model.entity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午5:09
 * @description：错误详细信息
 * @modified By：
 * @version: １.0$
 */
public class ExceptionDetail {

    /**
     * 错误内容
     */
    private String errorMsg;

    /**
     * 错误堆栈
     */
    private String stackTrace;

    /**
     * 其他输出
     */
    private Map<String, String> otherInfo;

    public ExceptionDetail(Exception e) {
        this.errorMsg = e.getMessage();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        this.stackTrace = sw.toString();
    }

    public ExceptionDetail(Exception e, Map<String, String> info) {
        this.errorMsg = e.getMessage();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        this.stackTrace = sw.toString();
        this.otherInfo = info;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public Map<String, String> getOtherInfo() {
        return otherInfo;
    }
}
