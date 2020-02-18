package com.onlinetool.userprofile.client.library.util;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午6:31
 * @description：端口号是否被占用检测
 * @modified By：
 * @version: 1.0$
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetUtil {

    /***
     *  true:already in using  false:not using
     * @param port
     */
    public static boolean isLoclePortUsing(int port){
        boolean flag = true;
        try {
            flag = isPortUsing("127.0.0.1", port);
        } catch (UnknownHostException ignored) {
        }
        return flag;
    }
    /***
     *  true:already in using  false:not using
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host,int port) throws UnknownHostException{
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(theAddress,port);
            flag = true;
        } catch (IOException ignored) {

        }
        return flag;
    }
}
