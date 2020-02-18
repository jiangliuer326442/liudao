package com.onlinetool.userprofile.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlinetool.userprofile.client.library.constant.ChatTypeConstant;
import com.onlinetool.userprofile.client.library.constant.DeployEnvConstant;
import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.library.util.HttpClientUtil;
import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.model.object.NoticeResultObject;
import com.onlinetool.userprofile.client.model.object.ResultObject;
import com.onlinetool.userprofile.client.model.object.ServsObject;
import com.onlinetool.userprofile.client.service.DeployLogger;
import com.onlinetool.userprofile.client.service.ImMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Struct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午4:50
 * @description：项目部署上线日志管理
 * @modified By：
 * @version: 1.0$
 */
public class DeployLoggerImpl implements DeployLogger {

    @Value("${custom.path.logs}")
    private String logsPath;

    private String JOBSNODE = "jobs";
    private String SQLSNODE = "sqls";
    private String MERGEDBRANCHES = "mergedBranches";
    private String COMMITIDS = "commitids";

    private ObjectNode rootObject;

    /**
     * 当前所属环境
     */
    private int env;

    /**
     * 日志基础路径
     */
    private String logBasePath;

    /**
     * 微服务建值对
     */
    private Map<Long, String> servsMap;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目沟通方式
     */
    private int chatType;

    /**
     * 项目沟通webhook
     */
    private String chatHookUrl;
    private String chatSecret;
    private SimpMessagingTemplate simpMessageSendingOperations;
    private ObjectMapper objectMapper;
    private ImMsg imMsg;

    private static final String TOPIC = "/topic/service";

    /**
     * 控制台输出键值对
     */
    private Map<Long, ConsoleObject> consoleObjectMap = new HashMap<>();
    private NoticeResultObject noticeResultObject = null;

    @Autowired
    public DeployLoggerImpl(ObjectMapper objectMapper, SimpMessagingTemplate simpMessageSendingOperations, ImMsg imMsg) {
        this.objectMapper = objectMapper;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.imMsg = imMsg;
    }

    @Override
    public void setContext(int env, long taskId, Map<Long, String> servsArr, String taskName, String projectName, int chatType, String chatHookUrl, String chatSecret) throws IOException {
        rootObject = this.objectMapper.createObjectNode();

        ObjectNode mergedBranches = this.objectMapper.createObjectNode();
        ObjectNode commitIDsTest = this.objectMapper.createObjectNode();
        ObjectNode commitIDsPrev = this.objectMapper.createObjectNode();
        ObjectNode commitIDsPro = this.objectMapper.createObjectNode();
        ObjectNode executedSqlsTest = this.objectMapper.createObjectNode();
        ObjectNode executedSqlsPro = this.objectMapper.createObjectNode();
        ObjectNode executedJobsTest = this.objectMapper.createObjectNode();
        ObjectNode executedJobsPrev = this.objectMapper.createObjectNode();
        ObjectNode executedJobsPro = this.objectMapper.createObjectNode();

        this.env = env;
        this.servsMap = servsArr;
        this.consoleObjectMap.clear();
        for(Map.Entry<Long, String> entry : servsArr.entrySet()){
            long servsId = entry.getKey();
            mergedBranches.put(Long.toString(servsId), "");
            commitIDsTest.put(Long.toString(servsId), "");
            commitIDsPrev.put(Long.toString(servsId), "");
            commitIDsPro.put(Long.toString(servsId), "");
            executedSqlsTest.set(Long.toString(servsId), this.objectMapper.createArrayNode());
            executedSqlsPro.set(Long.toString(servsId), this.objectMapper.createArrayNode());
            executedJobsTest.set(Long.toString(servsId), this.objectMapper.createArrayNode());
            executedJobsPrev.set(Long.toString(servsId), this.objectMapper.createArrayNode());
            executedJobsPro.set(Long.toString(servsId), this.objectMapper.createArrayNode());
            if (!consoleObjectMap.containsKey(servsId)){
                ConsoleObject consoleObject = new ConsoleObject(taskId, servsId, null, 0, ConsoleObject.LEVEL_INFO, null);
                consoleObjectMap.put(servsId, consoleObject);
            }
        }

        noticeResultObject = new NoticeResultObject();
        noticeResultObject.setEnv(env);
        noticeResultObject.setTaskId(taskId);

        ObjectNode envAddedJobs = this.objectMapper.createObjectNode();
        envAddedJobs.set("test", executedJobsTest);
        envAddedJobs.set("pre", executedJobsPrev);
        envAddedJobs.set("pro", executedJobsPro);
        ObjectNode envExecutedSqls = this.objectMapper.createObjectNode();
        envExecutedSqls.set("test", executedSqlsTest);
        envExecutedSqls.set("online", executedSqlsPro);
        ObjectNode envCommitIDs = this.objectMapper.createObjectNode();
        envCommitIDs.set("test", commitIDsTest);
        envCommitIDs.set("pre", commitIDsPrev);
        envCommitIDs.set("pro", commitIDsPro);
        rootObject.set(MERGEDBRANCHES, mergedBranches);
        rootObject.set(COMMITIDS, envCommitIDs);
        rootObject.set(SQLSNODE, envExecutedSqls);
        rootObject.set(JOBSNODE, envAddedJobs);

        File taskConfigFile = getTaskConfig(taskId);
        if (!taskConfigFile.exists()){
            this.saveTaskConfig(taskConfigFile);
        }else{
            rootObject = (ObjectNode) this.objectMapper.readTree(taskConfigFile);
        }

        this.taskName = taskName;
        this.projectName = projectName;
        this.chatType = chatType;
        this.chatHookUrl = chatHookUrl;
        this.chatSecret = chatSecret;
    }

    @Override
    public String getMergedBranche(long servsId) {
        String branch = this.rootObject.get(MERGEDBRANCHES).get(Long.toString(servsId)).toString().replace("\"", "");
        return branch;
    }

    @Override
    public boolean addJobs(ConsoleObject consoleObject, long servsId, String jobName) {
        List<String> ls = this.getJobs(servsId);
        if (!ls.contains(jobName)){
            String envTag = "";
            if (env == DeployEnvConstant.ENV_TEST){
                envTag = "test";
            }else if(env == DeployEnvConstant.ENV_PREV){
                envTag = "pre";
            }else {
                envTag = "pro";
            }

            ls.add(jobName);
            ArrayNode as = this.objectMapper.createArrayNode();
            for (String s : ls){
                as.add(s);
            }
            ObjectNode jobNode = (ObjectNode)this.rootObject.get(JOBSNODE);
            ObjectNode servsObject = (ObjectNode)jobNode.get(envTag);
            servsObject.set(Long.toString(servsId), as);
            File taskConfigFile = getTaskConfig(consoleObject.getTaskId());
            this.saveTaskConfig(taskConfigFile);
            return true;
        }
        return false;
    }

    @Override
    public boolean addExecuteSql(ConsoleObject consoleObject, long servsId, String sql){
        List<String> ls = this.getExecutedSqls(servsId);
        if (!ls.contains(sql)){
            String envTag = "";
            if (env == DeployEnvConstant.ENV_TEST){
                envTag = "test";
            }else {
                envTag = "online";
            }

            ls.add(sql);
            ArrayNode as = this.objectMapper.createArrayNode();
            for (String s : ls){
                as.add(s);
            }
            ObjectNode sqlNode = (ObjectNode)this.rootObject.get(SQLSNODE);
            ObjectNode servsObject = (ObjectNode)sqlNode.get(envTag);
            servsObject.set(Long.toString(servsId), as);
            File taskConfigFile = getTaskConfig(consoleObject.getTaskId());
            this.saveTaskConfig(taskConfigFile);
            return true;
        }
        return false;
    }

    @Override
    public void setCommitID(ConsoleObject consoleObject, String commitID) {
        long servsId = consoleObject.getServsId();

        String envTag = "";
        if (env == DeployEnvConstant.ENV_TEST){
            envTag = "test";
        }else if(env == DeployEnvConstant.ENV_PREV){
            envTag = "pre";
        }else {
            envTag = "pro";
        }
        ObjectNode commitidNode = (ObjectNode)this.rootObject.get(COMMITIDS);
        ObjectNode servsObject = (ObjectNode)commitidNode.get(envTag);
        servsObject.put(Long.toString(servsId), commitID);
        File taskConfigFile = getTaskConfig(consoleObject.getTaskId());
        this.saveTaskConfig(taskConfigFile);
    }

    @Override
    public List<String> getJobs(long servsId) {
        List<String> ls = new ArrayList<>();

        JsonNode jobNode = this.rootObject.get(JOBSNODE);
        String envTag = "";
        if (env == DeployEnvConstant.ENV_TEST){
            envTag = "test";
        }else if(env == DeployEnvConstant.ENV_PREV){
            envTag = "pre";
        }else {
            envTag = "pro";
        }
        if (!jobNode.has(envTag)) return ls;
        JsonNode envNode = jobNode.get(envTag);
        if (!envNode.has(Long.toString(servsId))) return ls;
        ArrayNode jobListArrnode = (ArrayNode)envNode.get(Long.toString(servsId));
        for (JsonNode jsonNode : jobListArrnode){
            String s = jsonNode.toString().replace("\"", "");
            ls.add(s);
        }
        return ls;
    }

    @Override
    public List<String> getExecutedSqls(long servsId){
        List<String> ls = new ArrayList<>();

        JsonNode sqlNode = this.rootObject.get(SQLSNODE);
        String envTag = "";
        if (env == DeployEnvConstant.ENV_TEST){
            envTag = "test";
        }else {
            envTag = "online";
        }
        if (!sqlNode.has(envTag)) return ls;
        JsonNode envNode = sqlNode.get(envTag);
        if (!envNode.has(Long.toString(servsId))) return ls;
        ArrayNode sqlListArrnode = (ArrayNode)envNode.get(Long.toString(servsId));
        for (JsonNode jsonNode : sqlListArrnode){
            String s = jsonNode.toString().replace("\"", "");
            ls.add(s);
        }
        return ls;
    }

    @Override
    public String getCommitID(long servsId) {
        String commitid = null;

        JsonNode commitidNode = this.rootObject.get(COMMITIDS);
        String envTag = "";
        if (env == DeployEnvConstant.ENV_TEST){
            envTag = "test";
        }else if(env == DeployEnvConstant.ENV_PREV){
            envTag = "pre";
        }else {
            envTag = "pro";
        }
        if (!commitidNode.has(envTag)) return commitid;
        JsonNode envNode = commitidNode.get(envTag);
        if (!envNode.has(Long.toString(servsId))) return commitid;
        commitid = envNode.get(Long.toString(servsId)).toString().replace("\"", "");
        return commitid.equals("") ? null : commitid;
    }

    public void setMergedBranch(ConsoleObject consoleObject, String branch){
        ObjectNode mergedBranches = (ObjectNode) this.rootObject.get(MERGEDBRANCHES);
        mergedBranches.put(Long.toString(consoleObject.getServsId()), branch);
        rootObject.set(MERGEDBRANCHES, mergedBranches);
        File taskConfigFile = getTaskConfig(consoleObject.getTaskId());
        this.saveTaskConfig(taskConfigFile);
    }

    @Override
    public File getTaskConfig(long taskId) {
        File taskCconfigFile = new File(logBasePath + File.separator + "task_"+Long.toString(taskId)+"_config");
        return taskCconfigFile;
    }

    @Override
    public void saveTaskConfig(File taskCconfigFile) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(taskCconfigFile);
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
    public ConsoleObject getConsoleObject(long servsId) {
        return consoleObjectMap.get(servsId);
    }

    @Override
    public NoticeResultObject getNoticeResultObject(){
        return noticeResultObject;
    }

    @Override
    public void notifyResult(NoticeResultObject noticeResultObject){
        simpMessageSendingOperations.convertAndSend(TOPIC, noticeResultObject);
    }

    @Override
    public void log(ConsoleObject consoleObject) {
        File logFile = this.getLogFile(consoleObject.getTaskId(), consoleObject.getServsId());
        simpMessageSendingOperations.convertAndSend(TOPIC, consoleObject);
        this.appendFile(logFile, consoleObject);
        if(consoleObject.getLevel().equals(ConsoleObject.LEVEL_ERROR)){
            ExceptionDetail exceptionDetail = consoleObject.getExceptionDetail();
            StringBuilder stringBuilder = new StringBuilder("#### <font color=\"warning\">'" + taskName + "'上线出现问题</font>\n");
            stringBuilder.append("> 项目:<font color='comment'>"+projectName+"</font>\n");
            stringBuilder.append("> 微服务:<font color='comment'>"+this.servsMap.get(consoleObject.getServsId())+"</font>\n");
            stringBuilder.append("##### 概述： **"+consoleObject.getTitle() + "**\n");
            stringBuilder.append("##### 参数：\n");
            for(Map.Entry<String, String> entry : consoleObject.getExceptionDetail().getOtherInfo().entrySet()){
                String mapKey = entry.getKey();
                String mapValue = entry.getValue();
                stringBuilder.append("> ").append(mapKey).append(":<font color='comment'>").append(mapValue).append("</font>\n");
            }
            stringBuilder.append("\n\n");
            stringBuilder.append("##### 详细：\n");
            stringBuilder.append("> " + consoleObject.getExceptionDetail().getStackTrace().substring(0, 1024));
            this.imMsg.sendMarkdown(chatType, chatHookUrl, chatSecret, "项目 "+projectName+" 微服务 "+this.servsMap.get(consoleObject.getServsId())+" 部署失败", stringBuilder.toString());
        }
    }

    @Override
    public boolean initDir(Timestamp time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String logsPath = this.logsPath+File.separator+formatter.format(time);
        File logFile = new File(logsPath);
        if (!logFile.exists() && !logFile.mkdir()){
            return false;
        }
        this.logBasePath = logsPath;
        return true;
    }

    @Override
    public File getLogFile(long taskId, long servsId) {
        if (env == DeployEnvConstant.ENV_TEST){
            return new File(logBasePath + File.separator + "deploy_test_" + Long.toString(taskId) + "_" + Long.toString(servsId) + ".log");
        }else if (env == DeployEnvConstant.ENV_PREV){
            return new File(logBasePath + File.separator + "deploy_prev_" + Long.toString(taskId) + "_" + Long.toString(servsId) + ".log");
        }else if (env == DeployEnvConstant.ENV_PRO){
            return new File(logBasePath + File.separator + "deploy_pro_" + Long.toString(taskId) + "_" + Long.toString(servsId) + ".log");
        }
        return null;
    }

    @Override
    public void setEnv(int env) {
        this.env = env;
    }

    @Override
    public int getEnv() {
        return this.env;
    }

    private void appendFile(File f, Object beanObject) {
        String appendContent = "";
        try {
            appendContent = this.objectMapper.writeValueAsString(beanObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(appendContent);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
