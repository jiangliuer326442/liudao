package com.onlinetool.userprofile.client.library.util;

import hprose.client.HproseHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/22 下午10:21
 * @description：调用远程方法
 * @modified By：
 * @version: 1.0$
 */
public class Call {

    @Value("${custom.call.url}")
    private String callUrl;

    @Value("${custom.version}")
    private String versionCode;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public <T> T invokeAsync(String uid, String token, String organizeId, String timeStamp, String url,String method, Object[] args, Class<T> returnType) {
        HproseHttpClient client = new HproseHttpClient(callUrl + url + "?uid="+uid+"&token="+token+"&organizeid="+organizeId+"&timestamp="+timeStamp+"&version_code="+this.versionCode);
        client.setTimeout(5000);

        T result = null;
        try {
            result = client.invoke(method, args, returnType);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.info(throwable.getMessage());
            return null;
        }
        return result;
    }

    public <T> T invoke(String url,String method, Object[] args, Class<T> returnType) {
        HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String uid = request.getParameter("uid");
        String token = request.getParameter("token");
        String organizeId = request.getParameter("organizeid");
        String timeStamp = request.getParameter("timestamp");

        HproseHttpClient client = new HproseHttpClient(callUrl + url + "?uid="+uid+"&token="+token+"&organizeid="+organizeId+"&timestamp="+timeStamp+"&version_code="+this.versionCode);
        client.setTimeout(5000);

        T result = null;
        try {
            result = client.invoke(method, args, returnType);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.info(throwable.getMessage());
            return null;
        }
        return result;
    }
}
