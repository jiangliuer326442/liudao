package com.onlinetool.userprofile.client.service;

import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.model.object.TaskJobObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/10 下午8:26
 * @description：项目部署上线管理
 * @modified By：
 * @version: 1.0$
 */
public interface DeployService {

    /**
     * 设置环境
     * @param projectId
     */
    void setContext(String uid, String token, String organizeId, String timeStamp, long projectId);

    /**
     * 管理异步上线任务
     * @param env 部署环境
     * @param list 异步任务列表
     * @param taskId 任务id
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    Future<Boolean> managerAsyncTask(int env, List<Future<Boolean>> list, long taskId, DeployLogger deployLogger) throws InterruptedException, ExecutionException;

    /**
     * 微服务部署到线上
     * @param servsId 微服务id
     * @param servName　微服务名称
     * @param reponsitoryDirectory　源代码所在目录
     * @param fromBranch　待合并分支
     * @param toBranch 合并到的分支
     * @param lineDb 线上数据库
     * @param sqls 上线需要执行的ｓｑｌ语句数组
     * @param packageDir 需要打包上线的目录
     * @param lineHost 线上主机列表
     * @param lineHostDirectory 线上部署的目录
     * @param commandBeforePackage 打包前执行的命令
     * @param excludeFiles 打包排除文件
     * @return
     */
    Future<Boolean> deployOnline(
            long servsId,
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
            DeployLogger deployLogger
    );

    /**
     * 部署到测试环境/预发布环境
     * @param taskId 任务id
     * @param servsId 微服务id
     * @param servName
     * @param reponsitoryDirectory
     * @param deployBranch
     * @param deployDb
     * @param sqls
     * @param packageDir
     * @param deployHost
     * @param deployDirectory
     * @param udtGitDirectory 测试、预发布环境服务器上的git目录
     * @param commandBeforePackage
     * @param excludeFiles
     * @param commandBeforeDeploy
     * @param commandAfterDeploy
     * @param reloadFlg 是否完全重新部署到测试环境
     * @param deployLogger
     * @return
     */
    Future<Boolean> deploy(
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
            DeployLogger deployLogger
    );

    /**
     * 获取日志内容
     * @param taskId
     * @param servsId
     * @param logFile
     * @return
     * @throws IOException
     */
    List<String> getLogger(long taskId, long servsId, File logFile) throws IOException;
}
