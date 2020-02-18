package com.onlinetool.userprofile.client.model.object;

import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;

import java.io.Serializable;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/8/25 下午6:47
 * @description：结果json返回标准格式
 * @modified By：
 * @version: 1.0$
 */
public class ResultObject<T> implements Serializable {
    /**
     * 结果码
     */
    private int code = ResultCodeConstant.SUCCESS;

    /**
     * 描述
     */
    private String message = "";

    /**
     * 结果
     */
    private T data = null;

    public ResultObject() {
    }

    public ResultObject(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultObject(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResultObject{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

