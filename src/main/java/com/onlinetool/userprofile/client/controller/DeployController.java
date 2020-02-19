package com.onlinetool.userprofile.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinetool.userprofile.client.library.constant.DeployEnvConstant;
import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.library.util.Call;
import com.onlinetool.userprofile.client.model.entity.DiffFileEntity;
import com.onlinetool.userprofile.client.model.object.*;
import com.onlinetool.userprofile.client.service.DeployLogger;
import com.onlinetool.userprofile.client.service.DeployService;
import com.onlinetool.userprofile.client.service.ReponsitoryService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author jiangliuer
 */
@RestController
public class DeployController extends BaseController {

    private ReponsitoryService reponsitoryService;
    private DeployService deployService;
    private Call call;
    private ObjectMapper objectMapper;
    private DeployLogger deployLogger;
    @Autowired
    public DeployController(Call call, ObjectMapper objectMapper, ReponsitoryService reponsitoryService, DeployService deployService, DeployLogger deployLogger) {
        this.call = call;
        this.objectMapper = objectMapper;
        this.reponsitoryService = reponsitoryService;
        this.deployService = deployService;
        this.deployLogger = deployLogger;
    }

    @PostMapping(value = "/diffFileByService")
    public ResultObject diffFileByService(long projectId, long taskId, long servsId) {
        ResultObject resultObject = call.invoke("servs", "get", new Object[]{servsId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS)
            return resultObject;
        ServsObject servsObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<ServsObject>(){});

        resultObject = call.invoke("task", "get", new Object[]{projectId, taskId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS) {
            return resultObject;
        }
        TaskObject taskObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<TaskObject>(){});
        String fromBranch = "";
        for (TaskDetailObject taskDetailObject : taskObject.getList()){
            if (taskDetailObject.getServsId() == servsId){
                fromBranch = taskDetailObject.getBranch();
                break;
            }
        }
        if (fromBranch.equals("")) return new ResultObject();

        List<DiffFileEntity> listDiffFileEntity;
        try {
            listDiffFileEntity = this.reponsitoryService.diffBranch(servsObject.getReponsitoryDirectory(), fromBranch, servsObject.getTargetBranch());
        } catch (IOException | GitAPIException e) {
            return new ResultObject();
        }
        return new ResultObject<>(ResultCodeConstant.SUCCESS, "succ", listDiffFileEntity);
    }

    @PostMapping(value = "/getBranchesList")
    public ResultObject getBranchesList(long servsId){
        ResultObject resultObject = call.invoke("servs", "get", new Object[]{servsId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS)
            return resultObject;
        ServsObject servsObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<ServsObject>(){});
        String[] branchList;
        try {
            branchList = this.reponsitoryService.getBranches(servsObject.getReponsitoryDirectory());
        } catch (IOException | GitAPIException e) {
            return new ResultObject(ResultCodeConstant.INTERNALER, "git连接失败");
        }
        ResultObject<String[]> resultObject2 = new ResultObject<>();
        resultObject2.setData(branchList);
        return resultObject2;
    }

    @PostMapping(value = "/getLogger")
    public ResultObject getLogger(long projectId, long taskId, long servsId, @RequestParam(value="env", required = false,defaultValue = DeployEnvConstant.ENV_PRO+"") int env){
        ResultObject resultObject = call.invoke("task", "get", new Object[]{projectId, taskId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS) {
            return resultObject;
        }
        TaskObject taskObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<TaskObject>(){});
        this.deployLogger.initDir(taskObject.getCtime());
        this.deployLogger.setEnv(env);
        File logFile = this.deployLogger.getLogFile(taskId, servsId);

        List<String> list;
        try {
            list = this.deployService.getLogger(taskId, servsId, logFile);
        } catch (IOException e) {
            list = new ArrayList<>();
        }

        return new ResultObject<>(ResultCodeConstant.SUCCESS, "获取日志成功", list);
    }

    @PostMapping(value = "/deployTaskPre")
    public ResultObject deployTaskPre(String uid, String token, String organizeid, String timestamp, long projectId, long taskId, boolean reloadFlg){
        //获取任务详情
        ResultObject resultObject = call.invoke("task", "get", new Object[]{projectId, taskId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS) {
            return resultObject;
        }
        TaskObject taskObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<TaskObject>(){});
        if(!this.deployLogger.initDir(taskObject.getCtime())) return new ResultObject(ResultCodeConstant.INTERNALER, "无法创建日志文件夹");

        //日志logger
        Map<Long, String> servsArr = new HashMap<>();
        for (TaskDetailObject taskDetailObject : taskObject.getList()) {
            ServsObject servsObject = taskDetailObject.getServsObject();
            servsArr.put(taskDetailObject.getServsId(), servsObject.getServiceName());
        }
        try {
            this.deployLogger.setContext(DeployEnvConstant.ENV_PREV, taskId, servsArr, taskObject.getTaskName(), taskObject.getProjsObject().getName(), taskObject.getProjsObject().getChatType(), taskObject.getProjsObject().getChatHookUrl(), taskObject.getProjsObject().getChatHookSecret());
        } catch (IOException e) {
            return new ResultObject(ResultCodeConstant.INTERNALER, "内部错误1");
        }

        List<Future<Boolean>> deployResult = new ArrayList<>();

        this.deployService.setContext(uid, token, organizeid, timestamp, projectId);
        for (TaskDetailObject taskDetailObject : taskObject.getList()){
            ServsObject servsObject = taskDetailObject.getServsObject();
            deployResult.add(this.deployService.deploy(
                    taskId,
                    taskDetailObject.getServsId(),
                    servsObject.getServiceName(),
                    servsObject.getReponsitoryDirectory(),
                    taskDetailObject.getBranch(),
                    servsObject.getLineDb(),
                    taskDetailObject.getSqls(),
                    servsObject.getIsPrevJobDeployed(),
                    servsObject.getJobType(),
                    taskDetailObject.getJobsCommand(),
                    servsObject.getPackageDir(),
                    taskObject.getPrevHost(),
                    servsObject.getLineHostDirectory(),
                    servsObject.getUdtGitDir(),
                    servsObject.getCommandBeforePackage(),
                    servsObject.getExcludeFiles(),
                    servsObject.getCommandBeforeDeploy(),
                    servsObject.geCommandAfterDeploy(),
                    reloadFlg,
                    this.deployLogger
            ));
        }

        try {
            this.deployService.managerAsyncTask(DeployEnvConstant.ENV_PREV, deployResult, taskId, this.deployLogger);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ResultObject();
    }

    @PostMapping(value = "/deployTaskTest")
    public ResultObject deployTaskTest(String uid, String token, String organizeid, String timestamp, long projectId, long taskId, boolean reloadFlg){
        //获取任务详情
        ResultObject resultObject = call.invoke("task", "get", new Object[]{projectId, taskId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS) {
            return resultObject;
        }
        TaskObject taskObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<TaskObject>(){});
        if(!this.deployLogger.initDir(taskObject.getCtime())) return new ResultObject(ResultCodeConstant.INTERNALER, "无法创建日志文件夹");

        //日志logger
        Map<Long, String> servsArr = new HashMap<>();
        for (TaskDetailObject taskDetailObject : taskObject.getList()) {
            ServsObject servsObject = taskDetailObject.getServsObject();
            servsArr.put(taskDetailObject.getServsId(), servsObject.getServiceName());
        }
        try {
            this.deployLogger.setContext(DeployEnvConstant.ENV_TEST, taskId, servsArr, taskObject.getTaskName(), taskObject.getProjsObject().getName(), taskObject.getProjsObject().getChatType(), taskObject.getProjsObject().getChatHookUrl(), taskObject.getProjsObject().getChatHookSecret());
        } catch (IOException e) {
            return new ResultObject(ResultCodeConstant.INTERNALER, "内部错误1");
        }

        List<Future<Boolean>> deployResult = new ArrayList<>();

        this.deployService.setContext(uid, token, organizeid, timestamp, projectId);
        for (TaskDetailObject taskDetailObject : taskObject.getList()){
            ServsObject servsObject = taskDetailObject.getServsObject();
            deployResult.add(this.deployService.deploy(
                    taskId,
                    taskDetailObject.getServsId(),
                    servsObject.getServiceName(),
                    servsObject.getReponsitoryDirectory(),
                    taskDetailObject.getBranch(),
                    servsObject.getTestDb(),
                    taskDetailObject.getSqls(),
                    servsObject.getIsTestJobDeployed(),
                    servsObject.getJobType(),
                    taskDetailObject.getJobsCommand(),
                    servsObject.getPackageDir(),
                    taskObject.getTestHost(),
                    servsObject.getLineHostDirectory(),
                    servsObject.getUdtGitDir(),
                    servsObject.getCommandBeforePackage(),
                    servsObject.getExcludeFiles(),
                    servsObject.getCommandBeforeDeploy(),
                    servsObject.geCommandAfterDeploy(),
                    reloadFlg,
                    this.deployLogger
            ));
        }

        try {
            this.deployService.managerAsyncTask(DeployEnvConstant.ENV_TEST, deployResult, taskId, this.deployLogger);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ResultObject();
    }

    @PostMapping(value = "/deployTaskOnline")
    public ResultObject deployTaskOnline(String uid, String token, String organizeid, String timestamp, long projectId, long taskId){
        //获取任务详情
        ResultObject resultObject = call.invoke("task", "get", new Object[]{projectId, taskId}, ResultObject.class);
        if(resultObject.getCode() != ResultCodeConstant.SUCCESS) {
            return resultObject;
        }
        TaskObject taskObject = this.objectMapper.convertValue(resultObject.getData(), new TypeReference<TaskObject>(){});
        if(!this.deployLogger.initDir(taskObject.getCtime())) return new ResultObject(ResultCodeConstant.INTERNALER, "无法创建日志文件夹");

        //日志logger
        Map<Long, String> servsArr = new HashMap<>();
        for (TaskDetailObject taskDetailObject : taskObject.getList()) {
            ServsObject servsObject = taskDetailObject.getServsObject();
            servsArr.put(taskDetailObject.getServsId(), servsObject.getServiceName());
        }
        try {
            this.deployLogger.setContext(DeployEnvConstant.ENV_PRO, taskId, servsArr, taskObject.getTaskName(), taskObject.getProjsObject().getName(), taskObject.getProjsObject().getChatType(), taskObject.getProjsObject().getChatHookUrl(), taskObject.getProjsObject().getChatHookSecret());
        } catch (IOException e) {
            return new ResultObject(ResultCodeConstant.INTERNALER, "内部错误1");
        }

        List<Future<Boolean>> deployResult = new ArrayList<>();

        this.deployService.setContext(uid, token, organizeid, timestamp, projectId);
        for (TaskDetailObject taskDetailObject : taskObject.getList()){
            ServsObject servsObject = taskDetailObject.getServsObject();
            //清理旧日志文件
            File logFile = this.deployLogger.getLogFile(taskId, taskDetailObject.getServsId());
            if (logFile.exists()) logFile.delete();
            //对每一个微服务执行异步任务
            deployResult.add(this.deployService.deployOnline(
                taskDetailObject.getServsId(),
                servsObject.getServiceName(),
                servsObject.getReponsitoryDirectory(),
                taskDetailObject.getBranch(),
                servsObject.getTargetBranch(),
                servsObject.getLineDb(),
                taskDetailObject.getSqls(),
                servsObject.getLineJobHost(),
                servsObject.getJobType(),
                taskDetailObject.getJobsCommand(),
                servsObject.getPackageDir(),
                servsObject.getLineHost(),
                servsObject.getLineHostDirectory(),
                servsObject.getCommandBeforePackage(),
                servsObject.getExcludeFiles(),
                servsObject.getCommandBeforeDeploy(),
                servsObject.geCommandAfterDeploy(),
                this.deployLogger)
            );
        }
        try {
            this.deployService.managerAsyncTask(DeployEnvConstant.ENV_PRO, deployResult, taskId, this.deployLogger);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ResultObject();
    }

}
