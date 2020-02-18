package com.onlinetool.userprofile.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.model.object.NoticeResultObject;
import freemarker.template.TemplateHashModelEx2;
import jdk.nashorn.internal.ir.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午4:51
 * @description： 项目部署日志管理器
 * @modified By：
 * @version: $
 */
public interface DeployLogger {

    /**
     * 设置上下文
     * @param taskId 任务id
     * @param servsArr 微服务数组
     * @param taskName 任务名称
     * @param projectName　项目名称
     * @param chatType　消息工具
     * @param chatHookUrl　维系地址
     * @param chatSecret 私钥
     */
    void setContext(int env, long taskId, Map<Long, String> servsArr, String taskName, String projectName, int chatType, String chatHookUrl, String chatSecret) throws IOException;

    ConsoleObject getConsoleObject(long servsId);

    NoticeResultObject getNoticeResultObject();

    /**
     * 初始化日志目录
     * @param time 日志创建时间
     * @return
     */
    boolean initDir(Timestamp time);

    /**
     * 记录日志
     * @param consoleObject
     */
    void log(ConsoleObject consoleObject);

    /**
     * 通知部署结果
     * @param noticeResultObject
     */
    void notifyResult(NoticeResultObject noticeResultObject);

    /**
     * 上线过程日志文件获取日志文件
     * @param taskId
     * @param servsId
     * @return
     */
    File getLogFile(long taskId, long servsId);

    /**
     * 上线过程中关键节点记录文件
     * 文件名 task_1_config
     * {
     *     "mergedBranches":{ //各个微服务合并掉的分支
     *          "feed": "200112_test1_onlinetool",
     *          "social": "200112_test1_onlinetool",
     *     },
     *     "sqls":{ //各个环境上线过程中已经执行掉的sql语句
     *          "test":{
     *              "feed": [
     *                  "create table .......",
     *                  "alter table ...."
     *              ],
     *              "social": [
     *                  "insert into ....."
     *              ]
     *          },
     *          "online":{
     *          },
     *     }
     * }
     * @param taskId 任务id
     * @return
     */
    File getTaskConfig(long taskId);

    /**
     * 添加执行sql语句
     * @param servsId 微服务id
     * @param sql sql语句
     * @return
     */
    boolean addExecuteSql(ConsoleObject consoleObject, long servsId, String sql);

    /**
     * 获取待执行的sql数组
     * @param servsId
     * @return
     */
    List<String> getExecutedSqls(long servsId);

    /**
     * 添加需要创建的job
     * @param servsId 微服务id
     * @param jobName job名称
     * @return
     */
    boolean addJobs(ConsoleObject consoleObject, long servsId, String jobName);

    /**
     * 获取已经创建过的job
     * @param servsId
     * @return
     */
    List<String> getJobs(long servsId);

    /**
     * 设置已经合并的分支
     * @param consoleObject
     * @param branch
     */
    void setMergedBranch(ConsoleObject consoleObject, String branch);

    /**
     * 获取微服务合并的分支
     * @param servsId
     * @return
     */
    String getMergedBranche(long servsId);

    /**
     * 设置commitID
     * @param consoleObject
     * @param commitID
     */
    void setCommitID(ConsoleObject consoleObject, String commitID);

    /**
     * 获取commitID
     * @param servsId
     * @return
     */
    String getCommitID(long servsId);

    /**
     * 写任务配置文件
     */
    void saveTaskConfig(File file);

    /**
     * 设置环境变量
     * @param env
     */
    void setEnv(int env);

    int getEnv();
}
