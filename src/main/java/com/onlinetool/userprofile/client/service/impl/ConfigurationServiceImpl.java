package com.onlinetool.userprofile.client.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.model.entity.DbConnectorEntity;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.service.ConfigManagerInterface;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/21 下午12:22
 * @description：服务配置相关实现
 * @modified By：
 * @version: 1.0$
 */
public class ConfigurationServiceImpl implements ConfigurationService, InitializingBean {

    @Value("${custom.path.config}")
    private String configurationPath;

    private ConfigManagerInterface servsService;
    private ConfigManagerInterface dbconnectService;
    private ConfigManagerInterface hostService;
    private ObjectMapper objectMapper;
    private File file;
    private ObjectNode rootObject;

    @Autowired
    public ConfigurationServiceImpl(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
    }

    @Override
    public DbConnectorEntity getDbConnectorConfigByConnectorId(String projectId, String connectorName) {
        this.dbconnectService = new DbManager(this.objectMapper);
        return (DbConnectorEntity) this.dbconnectService.getService(rootObject, projectId, connectorName);
    }

    @Override
    public ObjectNode getDbConnectorConfig(String projectId) {
        this.dbconnectService = new DbManager(this.objectMapper);
        return this.dbconnectService.getServiceList(rootObject, projectId);
    }

    @Override
    public void removeDbConnectorConfig(String projectId, String dbConnectorName) {
        this.dbconnectService = new DbManager(this.objectMapper);
        this.rootObject = this.dbconnectService.removeService(rootObject, projectId, dbConnectorName);
    }

    @Override
    public void updateDbConnectorConfig(String proejctId, DbConnectorEntity dbConnectorEntity) {
        this.dbconnectService = new DbManager(this.objectMapper);
        this.rootObject = this.dbconnectService.updateService(rootObject, proejctId, dbConnectorEntity);
    }

    @Override
    public void removeHostConfig(String projectId, String hostName) {
        this.hostService = new HostManager(this.objectMapper);
        this.rootObject = this.hostService.removeService(rootObject, projectId, hostName);
    }

    @Override
    public void updateHostConfig(String proejctId, HostEntity hostEntity) {
        this.hostService = new HostManager(this.objectMapper);
        this.rootObject = this.hostService.updateService(rootObject, proejctId, hostEntity);
    }

    @Override
    public ObjectNode getHostConfig(String projectId) {
        this.hostService = new HostManager(this.objectMapper);
        return this.hostService.getServiceList(rootObject, projectId);
    }

    @Override
    public HostEntity getHostConfigByHostName(String projectId, String hostName) {
        this.hostService = new HostManager(this.objectMapper);
        return (HostEntity) this.hostService.getService(rootObject, projectId, hostName);
    }

    @Override
    public void save(){
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.objectMapper.writeValue(outputStream, rootObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        file = new File(this.configurationPath);
        if(file.exists()){
            rootObject = (ObjectNode) this.objectMapper.readTree(file);
        }else{
            rootObject = this.objectMapper.createObjectNode();
        }
    }
}
