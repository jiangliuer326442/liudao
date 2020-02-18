package com.onlinetool.userprofile.client.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.onlinetool.userprofile.client.library.constant.DeployEnvConstant;
import com.onlinetool.userprofile.client.library.constant.JobOperateConstant;
import com.onlinetool.userprofile.client.library.constant.JobTypeConstant;
import com.onlinetool.userprofile.client.library.util.Call;
import com.onlinetool.userprofile.client.library.util.JschOpenHandlerUtil;
import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;
import com.onlinetool.userprofile.client.model.entity.HostEntity;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.model.object.NoticeResultObject;
import com.onlinetool.userprofile.client.model.object.ResultObject;
import com.onlinetool.userprofile.client.model.object.TaskJobObject;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import com.onlinetool.userprofile.client.service.DeployLogger;
import com.onlinetool.userprofile.client.service.DeployService;
import com.onlinetool.userprofile.client.service.ReponsitoryService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午8:32
 * @description：
 * @modified By：
 * @version: $
 */
public class DeployServiceImpl implements DeployService {

    @Value("${custom.path.reponsitory}")
    private String reponsitoryPath;
    @Value("${custom.path.tars}")
    private String tarPath;
    @Value("${custom.path.reponsitory}")
    private String reponsitoryBase;
    @Value("${custom.jobs.pm2.start}")
    private String pm2JobStartTemplate;
    @Value("${custom.jobs.pm2.reload}")
    private String pm2JobReloadTemplate;

    private Call call;
    private ReponsitoryService reponsitoryService;
    private ConfigurationService configurationService;

    private long projectId;
    private String uid;
    private String token;
    private String organizeId;
    private String timeStamp;

    @Autowired
    public DeployServiceImpl(
            ReponsitoryService reponsitoryService,
            ConfigurationService configurationService,
            Call call
    ) {
        this.reponsitoryService = reponsitoryService;
        this.configurationService = configurationService;
        this.call = call;
    }

    @Override
    public void setContext(String uid, String token, String organizeId, String timeStamp, long projectId){
        this.projectId = projectId;
        this.uid = uid;
        this.token = token;
        this.organizeId = organizeId;
        this.timeStamp = timeStamp;
    }

    @Async
    @Override
    public Future<Boolean> managerAsyncTask(int env, List<Future<Boolean>> list, long taskId, DeployLogger deployLogger) throws InterruptedException, ExecutionException {
        int jobNum = list.size();
        int finishNum = 0;
        int successNum = 0;

        while (true){
            if (finishNum >= jobNum) break;
            for (Future<Boolean> future : list){
                while (!future.isDone()){
                    Thread.sleep(300);
                }
                finishNum += 1;
                Boolean result = future.get();
                if (result) successNum += 1;
            }
        }
        NoticeResultObject noticeResultObject = deployLogger.getNoticeResultObject();
        switch (env) {
            case DeployEnvConstant.ENV_PRO:
                if (successNum == jobNum){
                    noticeResultObject.setResult(true);
                    noticeResultObject.setMessage("上线成功，请留意线上报错日志");
                    deployLogger.notifyResult(noticeResultObject);
                    ResultObject resultObject = call.invokeAsync(uid, token, organizeId, timeStamp, "task", "setdeploysuccess", new Object[]{projectId, taskId}, ResultObject.class);
                    return new AsyncResult<>(true);
                }else{
                    noticeResultObject.setResult(false);
                    noticeResultObject.setMessage("上线失败，请检查原因并重新上线");
                    deployLogger.notifyResult(noticeResultObject);
                    ResultObject resultObject = call.invokeAsync(uid, token, organizeId, timeStamp, "task", "setdeployfailed", new Object[]{projectId, taskId}, ResultObject.class);
                    return new AsyncResult<>(false);
                }
            case DeployEnvConstant.ENV_PREV:
                if (successNum == jobNum){
                    noticeResultObject.setResult(true);
                    noticeResultObject.setMessage("部署到预发布环境成功，请开始测试");
                    deployLogger.notifyResult(noticeResultObject);
                    return new AsyncResult<>(true);
                }else {
                    noticeResultObject.setResult(false);
                    noticeResultObject.setMessage("部署到预发布环境失败，请联系开发人员检查原因并重新部署");
                    deployLogger.notifyResult(noticeResultObject);
                    return new AsyncResult<>(false);
                }
            case DeployEnvConstant.ENV_TEST:
                if (successNum == jobNum){
                    noticeResultObject.setResult(true);
                    noticeResultObject.setMessage("部署到测试环境成功，请开始测试");
                    deployLogger.notifyResult(noticeResultObject);
                    return new AsyncResult<>(true);
                }else {
                    noticeResultObject.setResult(false);
                    noticeResultObject.setMessage("部署到测试环境失败，请联系开发人员检查原因并重新部署");
                    deployLogger.notifyResult(noticeResultObject);
                    return new AsyncResult<>(false);
                }
            default:
                return new AsyncResult<>(true);
        }
    }

    @Async
    @Override
    public Future<Boolean> deploy(
            long taskId,
            long servsId,
            String servName,
            String reponsitoryDirectory,
            String deployBranch,
            String deployDb,
            String[] sqls,
            boolean isJobDeployed,
            int jobType,
            List<TaskJobObject> jobs,
            String packageDir,
            String deployHost,
            String deployDirectory,
            String udtGitDirectory,
            String[] commandBeforePackage,
            String[] excludeFiles,
            String[] commandBeforeDeploy,
            String[] commandAfterDeploy,
            boolean reloadFlg,
            DeployLogger deployLogger) {
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(servsId);

        //初始化数据库相关
        boolean dbAction = false;
        if(sqls.length > 0 && !sqls[0].equals("") && deployDb != null && !deployDb.equals("")){
            DbManager dbManager = new DbManager(projectId, deployLogger, this.configurationService, deployDb);
            try {
                dbAction = dbManager.executeSqls(servsId, sqls);
            } catch (JSchException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("lineDb", deployDb);
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(1);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return new AsyncResult<>(false);
            } catch (SQLException | ClassNotFoundException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>());
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(1);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return new AsyncResult<>(false);
            } finally {
                dbManager.getDbConnectUtil().close();
            }
        }else {
            dbAction = true;
        }
        if(!dbAction) {
            return new AsyncResult<>(false);
        }

        //切到指定分支 返回该分支的commitid
        String newCommitId;
        try {
            newCommitId = this.reponsitoryService.checkoutBranch(servsId, reponsitoryDirectory, deployBranch, deployLogger);
        } catch (GitAPIException e) {
            ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                {
                    put("deployBranch", deployBranch);
                }
            });
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                consoleObject.setExceptionDetail(exceptionDetail);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            return new AsyncResult<>(false);
        }
        if(newCommitId == null) {
            return new AsyncResult<>(false);
        }
        //清理旧日志文件
        File logFile = deployLogger.getLogFile(taskId, servsId);
        if (reloadFlg){
            if (logFile.exists()) logFile.delete();
        }else {
            String oldCommitId = deployLogger.getCommitID(servsId);
            //两次commitID相等的不再部署
            if (oldCommitId.equals(newCommitId)){
                return new AsyncResult<>(true);
            }else{
                if (logFile.exists()) logFile.delete();
            }
        }                    try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("down");
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        HostEntity hostEntity = this.configurationService.getHostConfigByHostName(Long.toString(projectId), deployHost);

        Session session;
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
                consoleObject.setHostName(deployHost);
                consoleObject.setStage(4);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                consoleObject.setExceptionDetail(exceptionDetail);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            return new AsyncResult<>(false);
        }
        String commandBeforePackageLineCommand = "";
        if (commandBeforePackage.length > 0) {
            commandBeforePackageLineCommand = "&& " + String.join(" && ", commandBeforePackage);
        }
        String copyDeployDirectoyCommand = "";
        if (!udtGitDirectory.equals(deployDirectory)){
            copyDeployDirectoyCommand = "&& cp -r " + udtGitDirectory + " " + deployDirectory;
        }
        packageDir = packageDir.replace("%root%", udtGitDirectory);
        if (!packageDir.equals(deployDirectory)){
            copyDeployDirectoyCommand = "&& cp -r " + packageDir + " " + deployDirectory;
        }
        String beforeDeployCommand = "";
        if(commandBeforeDeploy != null && commandBeforeDeploy.length>0) {
            for (String cmd : commandBeforeDeploy){
                beforeDeployCommand = "&& " + cmd;
            }
        }
        String execShell = "cd " + udtGitDirectory + " && git checkout . && git clean -df && git fetch && git checkout " + deployBranch + " && git pull origin "+ deployBranch + " " + commandBeforePackageLineCommand + " " + copyDeployDirectoyCommand + " " + beforeDeployCommand;

        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setHostName(deployHost);
            consoleObject.setStage(6);
            consoleObject.setStatus("up");
            consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
            consoleObject.setTitle(execShell);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        JschOpenHandlerUtil.openChannelExec(session, execShell);
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setHostName(deployHost);
            consoleObject.setStage(6);
            consoleObject.setStatus("down");
            consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
            consoleObject.setTitle(execShell);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        if (isJobDeployed) {
            String[] taskJobList = new String[jobs.size()];
            List<String> addedJobs = deployLogger.getJobs(servsId);
            for (int i = 0; i < jobs.size(); i++) {
                TaskJobObject taskJobEntity = jobs.get(i);
                if (jobType == JobTypeConstant.PM2) {
                    if (taskJobEntity.getOperateType() == JobOperateConstant.CREATE_JOB && !addedJobs.contains(taskJobEntity.getName())) {
                        String cmd = pm2JobStartTemplate.
                                replace("%base%", taskJobEntity.getScript()).
                                replace("%interpreter%", taskJobEntity.getInterpreter()).
                                replace("%name%", taskJobEntity.getName()).
                                replace("%args%", taskJobEntity.getArgs());
                        deployLogger.addJobs(consoleObjectInit, servsId, taskJobEntity.getName());
                        taskJobList[i] = cmd;
                    } else if (taskJobEntity.getOperateType() == JobOperateConstant.UPDATE_JOB || addedJobs.contains(taskJobEntity.getName())) {
                        String cmd = pm2JobReloadTemplate.
                                replace("%name%", taskJobEntity.getName());
                        taskJobList[i] = cmd;
                    }
                }
            }
            String jobsShell = String.join(" && ", taskJobList);
            if (!jobsShell.equals("")){
                jobsShell = "cd " + deployDirectory + " && " + jobsShell;
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(deployHost);
                    consoleObject.setStage(6);
                    consoleObject.setStatus("up");
                    consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                    consoleObject.setTitle(jobsShell);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                JschOpenHandlerUtil.openChannelExec(session, jobsShell);
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(deployHost);
                    consoleObject.setStage(6);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                    consoleObject.setTitle(jobsShell);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
            }
        }

        deployLogger.setCommitID(consoleObjectInit, newCommitId);
        return new AsyncResult<>(true);
    }

    @Async
    @Override
    public Future<Boolean> deployOnline(
            long servId,
            String servName,
            String reponsitoryDirectory,
            String fromBranch,
            String toBranch,
            String lineDb,
            String[] sqls,
            String lineJobHost,
            int jobType,
            List<TaskJobObject> jobs,
            String packageDir,
            List<String> lineHost,
            String lineHostDirectory,
            String[] commandBeforePackage,
            String[] excludeFiles,
            String[] commandBeforeDeploy,
            String[] commandAfterDeploy,
            DeployLogger deployLogger) {
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(servId);

        //分支合并
        String mergedBranch = deployLogger.getMergedBranche(servId);
        Boolean mergeResult = mergedBranch.equals(fromBranch);
        if (!mergeResult) {
            try {
                mergeResult = this.reponsitoryService.mergeBranch(servId, reponsitoryDirectory, fromBranch, toBranch, deployLogger);
            } catch (GitAPIException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>() {
                    {
                        put("fromBranch", fromBranch);
                        put("toBranch", toBranch);
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(1);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {
                }
                return new AsyncResult<>(false);
            }
        }
        if(!mergeResult) {
            return new AsyncResult<>(false);
        }

        //在资源目录中执行shell命令
        if (commandBeforePackage.length > 0) {
            String cmd = String.join(" && ", commandBeforePackage);
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(2);
                consoleObject.setStatus("up");
                consoleObject.setTitle(cmd);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {
            }
            try {
                File reponsitoryDir = new File(reponsitoryBase + File.separator + reponsitoryDirectory);
                Process process = Runtime.getRuntime().exec(cmd,null, reponsitoryDir);
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("cmd", cmd);
                    }
                });
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(2);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return new AsyncResult<>(false);
            }
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(2);
                consoleObject.setStatus("down");
                consoleObject.setTitle(cmd);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {
            }
        }

        boolean dbAction = false;
        if(sqls.length > 0 && !sqls[0].equals("") && lineDb != null && !lineDb.equals("")){
            DbManager dbManager = new DbManager(projectId, deployLogger, this.configurationService, lineDb);
            try {
                dbAction = dbManager.executeSqls(servId, sqls);
            } catch (JSchException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                    {
                        put("lineDb", lineDb);
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
                return new AsyncResult<>(false);
            } catch (SQLException | ClassNotFoundException e) {
                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>());
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setStage(3);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return new AsyncResult<>(false);
            } finally {
                dbManager.getDbConnectUtil().close();
            }
        }else {
            dbAction = true;
        }
        if(!dbAction) {
            return new AsyncResult<>(false);
        }

        HostManager hostManager = new HostManager(
                deployLogger,
                tarPath,
                servId,
                this.configurationService,
                projectId,
                servName,
                packageDir,
                this.reponsitoryPath + File.separator + reponsitoryDirectory,
                lineHost,
                lineHostDirectory
        );
        boolean uploadResult = hostManager.upload(excludeFiles, commandBeforeDeploy, commandAfterDeploy);

        if(!uploadResult) {
            return new AsyncResult<>(false);
        }

        //连接线上job机上线
        if (lineJobHost != null && !lineJobHost.equals("")) {
            HostEntity hostEntity = this.configurationService.getHostConfigByHostName(Long.toString(projectId), lineJobHost);
            Session session;
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
                    consoleObject.setHostName(lineJobHost);
                    consoleObject.setStage(6);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                    consoleObject.setExceptionDetail(exceptionDetail);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                return new AsyncResult<>(false);
            }
            String[] taskJobList = new String[jobs.size()];
            List<String> addedJobs = deployLogger.getJobs(servId);
            for (int i = 0; i < jobs.size(); i++) {
                TaskJobObject taskJobEntity = jobs.get(i);
                if (jobType == JobTypeConstant.PM2) {
                    if (taskJobEntity.getOperateType() == JobOperateConstant.CREATE_JOB && !addedJobs.contains(taskJobEntity.getName())) {
                        String cmd = pm2JobStartTemplate.
                                replace("%base%", taskJobEntity.getScript()).
                                replace("%interpreter%", taskJobEntity.getInterpreter()).
                                replace("%name%", taskJobEntity.getName()).
                                replace("%args%", taskJobEntity.getArgs());
                        deployLogger.addJobs(consoleObjectInit, servId, taskJobEntity.getName());
                        taskJobList[i] = cmd;
                    } else if (taskJobEntity.getOperateType() == JobOperateConstant.UPDATE_JOB || addedJobs.contains(taskJobEntity.getName())) {
                        String cmd = pm2JobReloadTemplate.
                                replace("%name%", taskJobEntity.getName());
                        taskJobList[i] = cmd;
                    }
                }
            }
            String jobsShell = String.join(" && ", taskJobList);
            if (!jobsShell.equals("")){
                jobsShell = "cd " + lineHostDirectory + " && " + jobsShell;
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(lineJobHost);
                    consoleObject.setStage(6);
                    consoleObject.setStatus("up");
                    consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                    consoleObject.setTitle(jobsShell);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
                JschOpenHandlerUtil.openChannelExec(session, jobsShell);
                try {
                    ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                    consoleObject.setHostName(lineJobHost);
                    consoleObject.setStage(6);
                    consoleObject.setStatus("down");
                    consoleObject.setLevel(ConsoleObject.LEVEL_INFO);
                    consoleObject.setTitle(jobsShell);
                    deployLogger.log(consoleObject);
                } catch (CloneNotSupportedException ignored) {}
            }
        }


//        if(commandAfterDeploy != null){
//            String cmd = String.join(" && ", commandAfterDeploy);
//            ConsoleObject consoleObject = new ConsoleObject(ConsoleObject.LEVEL_INFO, cmd);
//            deployLogger.setConsoleObject(consoleObject);
//            deployLogger.log();
//            try {
//                Runtime.getRuntime().exec(cmd, null, new File(reponsitoryDirectory));
//            } catch (IOException e) {
//                ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>());
//                consoleObject = new ConsoleObject(ConsoleObject.LEVEL_ERROR, exceptionDetail);
//                deployLogger.setConsoleObject(consoleObject);
//                deployLogger.log();
//                return new AsyncResult<>(false);
//            }
//        }

        return new AsyncResult<>(true);
    }

    @Override
    public List<String> getLogger(long taskId, long servsId, File logFile) throws IOException {
        List<String> list = new ArrayList<>();
        InputStreamReader inputReader = new InputStreamReader(new FileInputStream(logFile));
        BufferedReader bf = new BufferedReader(inputReader);
        // 按行读取字符串
        String str;
        while ((str = bf.readLine()) != null) {
            list.add(str);
        }
        return list;
    }
}
