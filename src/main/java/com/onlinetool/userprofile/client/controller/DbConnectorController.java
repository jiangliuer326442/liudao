package com.onlinetool.userprofile.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.model.entity.DbConnectorEntity;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.DbConnectorObject;
import com.onlinetool.userprofile.client.model.object.ResultObject;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

@RestController
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/5 上午7:17
 * @description：数据库管理入口
 * @modified By：
 * @version: 1.0$
 */
public class DbConnectorController extends BaseController {
    private ConfigurationService configurationService;

    @Autowired
    public DbConnectorController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping(value = "/rmDbConnector")
    public ResultObject removeDbConnector(String dbConnectorName, long projectId){
        this.configurationService.removeDbConnectorConfig(Long.toString(projectId), dbConnectorName);
        this.configurationService.save();
        return new ResultObject();
    }

    @PostMapping(value = "/lsDbConnector")
    public ResultObject lists(String projectStr){
        List<DbConnectorObject> list = new ArrayList<>();
        String[] projectArr = projectStr.split(",");
        for (String projectId : projectArr) {
            ObjectNode dcConnectorListObjectNode = this.configurationService.getDbConnectorConfig(projectId);
            if (dcConnectorListObjectNode != null) {
                Iterator<Map.Entry<String, JsonNode>> it = dcConnectorListObjectNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    DbConnectorObject dbConnectorObject = new DbConnectorObject();
                    dbConnectorObject.setProjectId(Long.parseLong(projectId));
                    dbConnectorObject.setDbConnectorName(entry.getKey());
                    JsonNode jsonNode = entry.getValue();
                    dbConnectorObject.setConnectorType(jsonNode.get("ConnectorType").toString().replaceAll("\"", ""));
                    dbConnectorObject.setHost(jsonNode.get("Host").toString().replaceAll("\"", ""));
                    dbConnectorObject.setHostName(jsonNode.get("HostName").toString().replaceAll("\"", ""));
                    dbConnectorObject.setIsUseSsh(jsonNode.get("IsUseSsh").toString().replaceAll("\"", ""));
                    dbConnectorObject.setPort(Long.parseLong(jsonNode.get("Port").toString().replaceAll("\"", "")));
                    dbConnectorObject.setUsername(jsonNode.get("Username").toString().replaceAll("\"", ""));
                    list.add(dbConnectorObject);
                }
            }
        }
        ResultObject<List<DbConnectorObject>> resultObject = new ResultObject<>();
        resultObject.setData(list);
        return resultObject;
    }

    @PostMapping(value = "/addDbConnector")
    public ResultObject add(long projectId, @Valid DbConnectorEntity dbConnectorEntity, BindingResult bindingResult){
        ResultObject<Map<String, String>> resultObject = this._bindCheck(bindingResult);
        if (resultObject.getCode() == ResultCodeConstant.SUCCESS) {
            Map<String, String> retDat = new HashMap<>();

            if(dbConnectorEntity.getIsUseSsh().equals("1")){
                if(dbConnectorEntity.getHostName() == null || dbConnectorEntity.getHostName().equals("")){
                    retDat.put("hostName", "使用ssh通道，主机名必填");
                    return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
                }
                HostEntity hostEntity = this.configurationService.getHostConfigByHostName(Long.toString(projectId), dbConnectorEntity.getHostName());
                if(hostEntity == null){
                    retDat.put("hostName", "ssh通道所用主机不存在或已删除");
                    return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
                }
            }
            this.configurationService.updateDbConnectorConfig(Long.toString(projectId), dbConnectorEntity);
            this.configurationService.save();
            return new ResultObject();
        }else{
            return resultObject;
        }
    }
}
