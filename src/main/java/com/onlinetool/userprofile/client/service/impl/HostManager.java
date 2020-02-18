package com.onlinetool.userprofile.client.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcraft.jsch.*;
import com.onlinetool.userprofile.client.library.constant.DeployEnvConstant;
import com.onlinetool.userprofile.client.library.util.JschOpenHandlerUtil;
import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.service.ConfigManagerInterface;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import com.onlinetool.userprofile.client.service.DeployLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午9:59
 * @description：主机管理
 * @modified By：
 * @version: 1.0$
 */
public class HostManager implements ConfigManagerInterface {
    private String NODENAME = "hostList";
    private ObjectMapper objectMapper;
    private long projectId;
    private String servsName;
    private List<String> hostNameList;
    private String reponsitory;
    private DeployLogger deployLogger;
    private long servsId;
    private ConfigurationService configurationService;
    private String lineHostDirectory;
    private String tarPath;

    public HostManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HostManager(
            DeployLogger deployLogger,
            String tarPath,
            long servsId,
            ConfigurationService configurationService,
            long projectId,
            String servsName,
            String packageDir,
            String reponsitory,
            List<String> hostNameList,
            String lineHostDirectory) {
        this.deployLogger = deployLogger;
        this.tarPath = tarPath;
        this.servsId = servsId;
        this.configurationService = configurationService;
        this.projectId = projectId;
        this.servsName = servsName;
        this.reponsitory = packageDir.replace("%root%", reponsitory);
        //将代码打包上传到指定服务器
        this.hostNameList = hostNameList;
        this.lineHostDirectory = lineHostDirectory;
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
    public HostEntity getService(ObjectNode rootObject, String projectId, String itemIndex) {
        ObjectNode serviceList = this.getServiceList(rootObject, projectId);
        if(serviceList != null && serviceList.has(itemIndex)){
            ObjectNode serviceNode = (ObjectNode) serviceList.get(itemIndex);
            return objectMapper.convertValue(serviceNode, HostEntity.class);
        }
        return null;
    }

    @Override
    public ObjectNode updateService(ObjectNode rootObject, String projectId, Object object) {
        HostEntity hostEntity = (HostEntity) object;
        ObjectNode serviceList = this.getServiceList(rootObject, projectId);
        if(serviceList == null) serviceList = this.objectMapper.createObjectNode();
        serviceList = hostEntity.toObjectNode(serviceList);
        ObjectNode dbconnectorList;
        if(rootObject.has(NODENAME)) {
            dbconnectorList = (ObjectNode) rootObject.get(NODENAME);
        }else{
            dbconnectorList = this.objectMapper.createObjectNode();
        }
        dbconnectorList = (ObjectNode) dbconnectorList.set(projectId, serviceList);
        rootObject.set(NODENAME, dbconnectorList);
        return rootObject;
    }

    //忘服务器上上传文件
    public boolean upload(String[] excludeFiles, String[] commandBeforeDeploy, String[] commandAfterDeploy){
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(this.servsId);
        File reponsitoryDir = new File(reponsitory);

        //打包上传
        String originTarName = "reponsitory_"+Long.toString(projectId)+"_"+servsName+".tar";
        String originTarPath = this.tarPath + File.separator + originTarName;

        StringBuilder exeShell = new StringBuilder("tar zcf " + originTarPath);
        for (String excludeFile : excludeFiles){
            //非线上环境不排除.git目录
            if (deployLogger.getEnv() != DeployEnvConstant.ENV_PRO && excludeFile.equals(".git")) continue;
            exeShell.append(" --exclude=").append(reponsitoryDir.getParentFile().getName() + File.separator + excludeFile);
        }
        exeShell.append(" ").append(reponsitoryDir.getName());
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(3);
            consoleObject.setStatus("up");
            consoleObject.setTitle(exeShell.toString());
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        try {
            Process process = Runtime.getRuntime().exec(exeShell.toString() ,null, reponsitoryDir.getParentFile());
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                {
                    put("cmd", exeShell.toString());
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
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(3);
            consoleObject.setStatus("down");
            consoleObject.setTitle(exeShell.toString());
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        for (String hostName:hostNameList){

            String targetPath = "/tmp/onlinetool";
            HostEntity hostEntity = this.configurationService.getHostConfigByHostName(Long.toString(projectId), hostName);

            String schShel = "scp " + originTarPath + " " + hostEntity.getUsername() + "@" + hostEntity.getHost() + ":" + targetPath;
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(4);
                consoleObject.setStatus("up");
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                consoleObject.setTitle(schShel);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}

            Session session = null;
            try {
                session = JschOpenHandlerUtil.getSession(hostEntity);
            } catch (JSchException | IOException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("sftp", hostEntity.getUsername() + "@" + hostEntity.getHost() + " -p " + hostEntity.getPort());
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(hostName);
                    consoleObject.setStage(4);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return false;
            }
            ChannelSftp openChannelSftp = JschOpenHandlerUtil.openChannelSftp(session);
            try {
                openChannelSftp.mkdir(targetPath);
            } catch (SftpException ignored) {}
            try {
                openChannelSftp.cd(targetPath);
                openChannelSftp.put(originTarPath, targetPath, ChannelSftp.OVERWRITE);
            } catch (SftpException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("hostName", hostName);
                        put("originTarPath", originTarPath);
                        put("targetPath", targetPath);
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(hostName);
                    consoleObject.setStage(4);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return false;
            } finally {
                openChannelSftp.disconnect();
            }
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(4);
                consoleObject.setStatus("down");
                consoleObject.setTitle(schShel);
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}

            String tarSHell = "tar xf "+targetPath+File.separator+originTarName+" --strip-components 1 -C " + this.lineHostDirectory + " --remove-files";

            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(5);
                consoleObject.setStatus("up");
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                consoleObject.setTitle(tarSHell);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            ChannelExec openchannelExec = JschOpenHandlerUtil.openChannelExec(session, tarSHell);
            openchannelExec.disconnect();
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(5);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                consoleObject.setTitle(tarSHell);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}

            StringBuilder sb = new StringBuilder("rm -f "+targetPath+File.separator+originTarName);
            if(commandBeforeDeploy != null && commandBeforeDeploy.length>0) {
                for (String cmd : commandBeforeDeploy){
                    sb.append(" && ").append(cmd);
                }
            }

            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(6);
                consoleObject.setStatus("up");
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                consoleObject.setTitle(sb.toString());
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            openchannelExec = JschOpenHandlerUtil.openChannelExec(session, sb.toString());
            openchannelExec.disconnect();
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setHostName(hostName);
                consoleObject.setStage(6);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                consoleObject.setTitle(sb.toString());
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}

            session.disconnect();
        }
        new File(originTarPath).delete();
        return true;
    }
}
