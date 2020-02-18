package com.onlinetool.userprofile.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.library.constant.HostLogintypeConstant;
import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.HostObject;
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
 * @author ：mmzs
 * @date ：Created in 2019/11/4 下午2:39
 * @description：主机管理
 * @modified By：
 * @version: 1.0$
 */
public class HostController extends BaseController {
    private ConfigurationService configurationService;

    @Autowired
    public HostController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping(value = "/rmHost")
    public ResultObject removeHost(String hostName, long projectId){
        this.configurationService.removeHostConfig(Long.toString(projectId), hostName);
        this.configurationService.save();
        return new ResultObject();
    }

    @PostMapping(value = "/lsHost")
    public ResultObject lists(String projectStr){
        List<HostObject> list = new ArrayList<>();
        String[] projectArr = projectStr.split(",");
        for (String projectId : projectArr){
            ObjectNode hostListObjectNode = this.configurationService.getHostConfig(projectId);
            if(hostListObjectNode != null) {
                Iterator<Map.Entry<String, JsonNode>> it = hostListObjectNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    HostObject hostObject = new HostObject();
                    hostObject.setProjectId(Long.parseLong(projectId));
                    hostObject.setHostName(entry.getKey());
                    JsonNode jsonNode = entry.getValue();
                    hostObject.setUsername(jsonNode.get("Username").toString().replaceAll("\"", ""));
                    hostObject.setHost(jsonNode.get("Host").toString().replaceAll("\"", ""));
                    hostObject.setPort(Long.parseLong(jsonNode.get("Port").toString().replaceAll("\"", "")));
                    list.add(hostObject);
                }
            }
        }
        ResultObject<List<HostObject>> resultObject = new ResultObject<>();
        resultObject.setData(list);
        return resultObject;
    }

    @PostMapping(value = "/addHost")
    public ResultObject add(long projectId, @Valid HostEntity hostEntity, BindingResult bindingResult){
        ResultObject<Map<String, String>> resultObject = this._bindCheck(bindingResult);
        if (resultObject.getCode() == ResultCodeConstant.SUCCESS) {
            if(!"".equals(hostEntity.getPassword()) && !"".equals(hostEntity.getPrivateKeyPath())){
                hostEntity.setAuthenticationType(Integer.toString(HostLogintypeConstant.PWD_PLUS_PUBLICKEY));
            }else if(!"".equals(hostEntity.getPassword())){
                hostEntity.setAuthenticationType(Integer.toString(HostLogintypeConstant.PASSWORD));
            }else{
                hostEntity.setAuthenticationType(Integer.toString(HostLogintypeConstant.PUBLICKEY));
            }

            Map<String, String> retDat = new HashMap<>();
            if(hostEntity.getAuthenticationType().equals(Integer.toString(HostLogintypeConstant.PASSWORD)) && (hostEntity.getPassword() == null || hostEntity.getPassword().equals(""))){
                retDat.put("password", "主机密码必填");
                return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
            }
            if(hostEntity.getAuthenticationType().equals(Integer.toString(HostLogintypeConstant.PUBLICKEY)) && (hostEntity.getPrivateKeyPath() == null || hostEntity.getPrivateKeyPath().equals(""))){
                retDat.put("privateKeyPath", "请填写私钥在客户端服务器上的存储位置");
                return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
            }
            if(hostEntity.getAuthenticationType().equals(Integer.toString(HostLogintypeConstant.PWD_PLUS_PUBLICKEY))){
                if(hostEntity.getPassword() == null || hostEntity.getPassword().equals("")){
                    retDat.put("password", "主机密码必填");
                    return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
                }
                if(hostEntity.getPrivateKeyPath() == null || hostEntity.getPrivateKeyPath().equals("")){
                    retDat.put("privateKeyPath", "请填写私钥在客户端服务器上的存储位置");
                    return new ResultObject<Map<String, String>>(ResultCodeConstant.PARADER, "参数错误", retDat);
                }
            }
            this.configurationService.updateHostConfig(Long.toString(projectId), hostEntity);
            this.configurationService.save();

            return new ResultObject();
        }else{
            return resultObject;
        }
    }

}
