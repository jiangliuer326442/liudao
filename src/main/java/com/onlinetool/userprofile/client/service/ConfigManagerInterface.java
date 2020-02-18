package com.onlinetool.userprofile.client.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午5:33
 * @description：微服务管理
 * @modified By：
 * @version: 1.0$
 */
public interface ConfigManagerInterface {

    /**
     * 获取项目下微服务列表
     * @param rootObject 跟对象
     * @param projectId 项目id
     * @return 微服务配置列表
     */
    ObjectNode getServiceList (ObjectNode rootObject, String projectId);

    /**
     * 获取微服务配置
     * @param rootObject 跟对象
     * @param projectId 项目id
     * @param itemIndex 微服务名称
     * @return
     */
    Object getService (ObjectNode rootObject, String projectId, String itemIndex);

    /**
     * 一出微服务
     * @param rootObject
     * @param projectId
     * @param serviceName
     * @return
     */
    ObjectNode removeService(ObjectNode rootObject, String projectId, String serviceName);

    /**
     * 微服务相关配置
     * @param  projectId 项目id
     * @param object 羡慕bean
     */
    ObjectNode updateService (ObjectNode rootObject, String projectId, Object object);
}
