package com.onlinetool.userprofile.client.library.util;

import com.jcraft.jsch.*;
import com.onlinetool.userprofile.client.library.constant.HostLogintypeConstant;
import com.onlinetool.userprofile.client.model.entity.HostEntity;

import java.io.IOException;
import java.util.Properties;

/**
 * @author ：mmzs
 * @date ：Created in 2019/11/4 下午3:55
 * @description：jsch验证
 * @modified By：
 * @version: 1.0$
 */
public class JschOpenHandlerUtil {

    public static Session getSession(HostEntity hostEntity) throws JSchException, IOException {
        Session session = null;
        JSch jsch = new JSch();
        if(hostEntity.getPrivateKeyPath() != null && !hostEntity.getPrivateKeyPath().equals("")) {
            jsch.addIdentity(hostEntity.getPrivateKeyPath());
        }
        session = jsch.getSession(hostEntity.getUsername(), hostEntity.getHost(), Integer.parseInt(hostEntity.getPort()));
        noCheckHostKey(session);
        if(hostEntity.getPassword() != null && !hostEntity.getPassword().equals("")) {
            session.setPassword(hostEntity.getPassword());
        }
        // 这个设置很重要，必须要设置等待时长为大于等于2分钟
        session.connect(120 * 1000);
        if (!session.isConnected()) {
            throw new IOException("We can't connection to[" + hostEntity.getHost() + "]");
        }
        return session;
    }

    /**
     * 不作检查主机键值
     *
     * @param session
     */
    private static void noCheckHostKey(Session session) {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
    }

    /**
     * 连接shell
     *
     * @param session
     *            session
     * @return {@link ChannelShell}
     */
    public static ChannelShell openChannelShell(Session session) {
        ChannelShell channel = null;
        try {
            channel = (ChannelShell) session.openChannel("shell");
            channel.setEnv("LANG", "en_US.UTF-8");
            channel.setAgentForwarding(false);
            channel.setPtySize(500, 500, 1000, 1000);
            channel.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (channel == null) {
            throw new IllegalArgumentException("The channle init was wrong");
        }
        return channel;
    }

    /**
     * 连接exec
     *
     * @param session
     *            session
     * @return {@link ChannelExec}
     */
    public static ChannelExec openChannelExec(Session session,final String command) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (channel == null) {
            throw new IllegalArgumentException("The channle init was wrong");
        }
        return channel;
    }

    /**
     * 连接sftp
     *
     * @param session
     * @return {@link ChannelSftp}
     */
    public static ChannelSftp openChannelSftp(Session session) {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }
}
