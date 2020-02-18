package com.onlinetool.userprofile.client.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcraft.jsch.JSchException;
import com.onlinetool.userprofile.client.library.util.DbConnectUtil;
import com.onlinetool.userprofile.client.library.util.LevenshiteinDistance;
import com.onlinetool.userprofile.client.model.entity.DbConnectorEntity;
import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.service.ConfigManagerInterface;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import com.onlinetool.userprofile.client.service.DeployLogger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午9:05
 * @description：
 * @modified By：
 * @version: $
 */
public class DbManager implements ConfigManagerInterface {
    private String NODENAME = "databaseList";
    private ObjectMapper objectMapper;

    private DeployLogger deployLogger;
    private DbConnectorEntity dbConnectorEntity;
    private HostEntity hostEntity;
    private DbConnectUtil dbConnectUtil;

    DbManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DbManager(long projectId, DeployLogger deployLogger, ConfigurationService configurationService, String dbHostName){
        this.deployLogger = deployLogger;
        dbConnectorEntity = configurationService.getDbConnectorConfigByConnectorId(Long.toString(projectId), dbHostName);
        hostEntity = null;
        if("1".equals(dbConnectorEntity.getIsUseSsh())){
            hostEntity = configurationService.getHostConfigByHostName(Long.toString(projectId),  dbConnectorEntity.getHostName());
        }
        dbConnectUtil = new DbConnectUtil();
    }

    @Override
    public ObjectNode removeService(ObjectNode rootObject, String projectId, String serviceName) {
        ObjectNode serviceList = this.getServiceList(rootObject, projectId);
        if(serviceList == null) return rootObject;
        serviceList.remove(serviceName);
        ObjectNode dbconnectorList = (ObjectNode) rootObject.get(NODENAME);
        dbconnectorList = (ObjectNode) dbconnectorList.set(projectId, serviceList);
        rootObject.set(NODENAME, dbconnectorList);
        return rootObject;
    }

    @Override
    public ObjectNode getServiceList(ObjectNode rootObject, String projectId) {
        if(rootObject.has(NODENAME)){
            JsonNode jsonNode = rootObject.get(NODENAME);
            if(jsonNode.has(projectId)){
                return (ObjectNode) jsonNode.get(projectId);
            }
        }
        return null;
    }

    @Override
    public DbConnectorEntity getService(ObjectNode rootObject, String projectId, String itemIndex) {
        ObjectNode serviceList = this.getServiceList(rootObject, projectId);
        if(serviceList != null && serviceList.has(itemIndex)){
            ObjectNode serviceNode = (ObjectNode) serviceList.get(itemIndex);
            return objectMapper.convertValue(serviceNode, DbConnectorEntity.class);
        }
        return null;
    }

    @Override
    public ObjectNode updateService(ObjectNode rootObject, String projectId, Object object) {
        DbConnectorEntity dbConnectorEntity = (DbConnectorEntity) object;
        ObjectNode serviceList = this.getServiceList(rootObject, projectId);
        if(serviceList == null) serviceList = this.objectMapper.createObjectNode();
        serviceList = dbConnectorEntity.toObjectNode(serviceList);
        ObjectNode dbconnectorList = null;
        if(rootObject.has(NODENAME)) {
            dbconnectorList = (ObjectNode) rootObject.get(NODENAME);
        }else{
            dbconnectorList = this.objectMapper.createObjectNode();
        }
        dbconnectorList = (ObjectNode) dbconnectorList.set(projectId, serviceList);
        rootObject.set(NODENAME, dbconnectorList);
        return rootObject;
    }

    public boolean executeSqls(long servId, String[] sqls) throws JSchException, SQLException, ClassNotFoundException {
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(servId);

        List<String> executedSqls = deployLogger.getExecutedSqls(servId);

        Statement statement = dbConnectUtil.setConnection(dbConnectorEntity, hostEntity);
a:        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[i];
            if(sql.equals("")) {
                continue;
            }
            for (String oldSql : executedSqls){
                Float simility = LevenshiteinDistance.getSimilarityRatio(oldSql, sql);
                if (simility > 0.9) continue a;
            }
            try {
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(3);
                    consoleObject.setStatus("up");
                    consoleObject.setTitle("执行sql语句:" + sql);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("dbName", dbConnectorEntity.getDbConnectName());
                        put("sql", sql);
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(3);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return false;
            }
            deployLogger.addExecuteSql(consoleObjectInit, servId, sql);
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(3);
                consoleObject.setStatus("down");
                consoleObject.setTitle("执行sql语句:" + sql);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
        }
        return true;
    }

    public DbConnectUtil getDbConnectUtil() {
        return dbConnectUtil;
    }
}
