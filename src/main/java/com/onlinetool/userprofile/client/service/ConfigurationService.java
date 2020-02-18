package com.onlinetool.userprofile.client.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.model.entity.DbConnectorEntity;
import com.onlinetool.userprofile.client.model.entity.HostEntity;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/21 下午12:20
 * @description：
 * @modified By：
 * @version: $
 */
public interface ConfigurationService {

    /**
     * 移除数据库链接配置
     * @param projectId
     * @param dbConnectorName
     */
    void removeDbConnectorConfig(String projectId, String dbConnectorName);

    /**
     * 更新数据库连接信息
     * @param proejctId
     * @param dbConnectorEntity
     */
    void updateDbConnectorConfig(String proejctId, DbConnectorEntity dbConnectorEntity);

    /**
     * 获取数据库连接列表
     * @param projectId 项目id
     * @return
     */
    ObjectNode getDbConnectorConfig(String projectId);

    /**
     * 获取指定名称的数据库连接
     * @param projectId 项目id
     * @param connectorName 连接名称
     * @return
     */
    DbConnectorEntity getDbConnectorConfigByConnectorId(String projectId, String connectorName);

    /**
     * 移除主机配置
     * @param projectId
     * @param hostName
     */
    void removeHostConfig(String projectId, String hostName);

    /**
     * 更新数据库连接信息
     * @param proejctId
     * @param hostEntity
     */
    void updateHostConfig(String proejctId, HostEntity hostEntity);

    /**
     * 获取主机连接列表
     * @param projectId 项目id
     * @return
     */
    ObjectNode getHostConfig(String projectId);

    /**
     * 获取指定名称的主机连接
     * @param projectId 项目id
     * @param hostName 主机名称
     * @return
     */
    HostEntity getHostConfigByHostName(String projectId, String hostName);
    /**
     * 配置文件写回到文件
     */
    void save();
}
