package com.onlinetool.userprofile.client.library.constant;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/8/25 下午10:25
 * @description：结果码
 * @modified By：
 * @version: $
 */
public class ResultCodeConstant {

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 参数错误
     */
    public static final int PARADER = 1002;

    /**
     * 用户未登录或无权限
     */
    public static final int FORBIDDEN = 401;

    /**
     * 服务器内部错误
     */
    public static final int INTERNALER = 500;

    /**
     * 邮箱注册到缓存
     */
    public static final int REGISTER_TO_MAIL_CACHE = 1001;
}

