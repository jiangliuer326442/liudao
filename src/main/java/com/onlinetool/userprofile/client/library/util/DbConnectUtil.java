package com.onlinetool.userprofile.client.library.util;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.onlinetool.userprofile.client.library.constant.DbtypeConstant;
import com.onlinetool.userprofile.client.model.entity.DbConnectorEntity;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/4 下午11:05
 * @description：数据库连接工具
 * @modified By：
 * @version: 1.0$
 */
public class DbConnectUtil {

    private Connection connection = null;
    private Session session = null;
    private Statement statement = null;

    public Statement setConnection(DbConnectorEntity dbConnectorEntity, HostEntity hostEntity) throws JSchException, ClassNotFoundException, SQLException {
        String host = dbConnectorEntity.getHost();
        int port = Integer.parseInt(dbConnectorEntity.getPort());
        String user = dbConnectorEntity.getUsername();
        String password = dbConnectorEntity.getPassword();

        int localPort = 0;

        //使用ssh通道
        if(hostEntity != null){
            try {
                this.session = JschOpenHandlerUtil.getSession(hostEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true){
                int max=65535,min=1000;
                localPort = (int) (Math.random()*(max-min)+min);
                if(!NetUtil.isLoclePortUsing(localPort)){
                    break;
                }
            }
            int assigned_port = session.setPortForwardingL(localPort, dbConnectorEntity.getHost(), port);
        }
        String url = null;
        if(hostEntity != null) {
            url = "jdbc:mysql://localhost:" + Integer.toString(localPort) + "/" + "?serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true";
        }else{
            url = "jdbc:mysql://" + dbConnectorEntity.getHost() + ":" + Integer.toString(port) + "/" + "?serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true";
        }
        if(Integer.parseInt(dbConnectorEntity.getConnectorType()) == DbtypeConstant.MYSQL) {
            String driver = "com.mysql.cj.jdbc.Driver";
            Class.forName(driver);
        }
        this.connection = DriverManager.getConnection(url, user, password);
        this.statement = this.connection.createStatement();
        return this.statement;
    }

    public void close() {
        try {
            if(this.statement != null){
                this.statement.close();
            }
            if(this.connection != null){
                this.connection.close();
            }
            if(this.session != null){
                this.session.disconnect();
            }
        } catch (SQLException ignored) {

        }
    }
}
