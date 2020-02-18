package com.onlinetool.userprofile.client.model.object;

import java.io.Serializable;
import java.util.List;

/**
 * @author ：mmzs
 * @date ：Created in 2019/11/8 下午2:32
 * @description：微服务对象
 * @modified By：
 * @version: 1.0$
 */
public class ServsObject implements Serializable {

    /**
     * 微服务id
     */
    private long serviceId;

    /**
     * 微服务名称
     */
    private String serviceName;

    /**
     * 代码仓库在客户端工具所在服务器上的路径
     */
    private String reponsitoryDirectory;

    /**
     * 微服务主分支
     */
    private String targetBranch;

    /**
     * 线上部署主机
     */
    private List<String> lineHost;

    /**
     * 测试环境部署主机
     */
    private List<String> testHost;

    /**
     * 线上环境部署目录
     */
    private String lineHostDirectory;

    /**
     * 线上数据库
     */
    private String lineDb;

    /**
     * 测试数据库
     */
    private String testDb;

    /**
     * 打包前执行的命令
     */
    private String[] commandBeforePackage;

    /**
     * 需要打包的目录
     */
    private String packageDir;

    /**
     * job管理类型
     */
    private int jobType;

    /**
     * 线上job服务器
     */
    private String lineJobHost;

    /**
     * 是否在测试环境自动部署job
     */
    private boolean isTestJobDeployed;

    /**
     * 是否在预发布环境部署job
     */
    private boolean isPrevJobDeployed;

    /**
     * 非线上环境服务器git所在的目录
     */
    private String udtGitDir;

    /**
     * 打包需要排除的文件
     */
    private String[] excludeFiles;

    /**
     * 部署完成后在各个服务器上执行的命令
     */
    private String[] commandBeforeDeploy;

    /**
     * 上线后执行的命令
     */
    private String[] commandAfterDeploy;

    public String[] getCommandAfterDeploy() {
        return commandAfterDeploy;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getReponsitoryDirectory() {
        return reponsitoryDirectory;
    }

    public void setReponsitoryDirectory(String reponsitoryDirectory) {
        this.reponsitoryDirectory = reponsitoryDirectory;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public List<String> getLineHost() {
        return lineHost;
    }

    public void setLineHost(List<String> lineHost) {
        this.lineHost = lineHost;
    }

    public String getLineHostDirectory() {
        return lineHostDirectory;
    }

    public void setLineHostDirectory(String lineHostDirectory) {
        this.lineHostDirectory = lineHostDirectory;
    }

    public String getLineDb() {
        return lineDb;
    }

    public void setLineDb(String lineDb) {
        this.lineDb = lineDb;
    }

    public List<String> getTestHost() {
        return testHost;
    }

    public void setTestHost(List<String> testHost) {
        this.testHost = testHost;
    }

    public String getTestDb() {
        return testDb;
    }

    public void setTestDb(String testDb) {
        this.testDb = testDb;
    }

    public String[] getCommandBeforePackage() {
        return commandBeforePackage;
    }

    public void setCommandBeforePackage(String[] commandBeforePackage) {
        this.commandBeforePackage = commandBeforePackage;
    }

    public String getUdtGitDir() {
        return udtGitDir;
    }

    public void setUdtGitDir(String udtGitDir) {
        this.udtGitDir = udtGitDir;
    }

    public void setPackageDir(String packageDir) {
        this.packageDir = packageDir;
    }

    public String getPackageDir() {
        return this.packageDir;
    }

    public String[] getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(String[] excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    public String[] getCommandBeforeDeploy() {
        return this.commandBeforeDeploy;
    }

    public void setCommandBeforeDeploy(String[] commandBeforeDeploy) {
        this.commandBeforeDeploy = commandBeforeDeploy;
    }

    public void setCommandAfterDeploy(String[] commandAfterDeploy) {
        this.commandAfterDeploy = commandAfterDeploy;
    }

    public String[] geCommandAfterDeploy() {
        return this.commandAfterDeploy;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) {
        this.jobType = jobType;
    }

    public String getLineJobHost() {
        return lineJobHost;
    }

    public void setLineJobHost(String lineJobHost) {
        this.lineJobHost = lineJobHost;
    }

    public boolean getIsTestJobDeployed() {
        return isTestJobDeployed;
    }

    public void setIsTestJobDeployed(boolean testJobDeployed) {
        isTestJobDeployed = testJobDeployed;
    }

    public boolean getIsPrevJobDeployed() {
        return isPrevJobDeployed;
    }

    public void setIsPrevJobDeployed(boolean prevJobDeployed) {
        isPrevJobDeployed = prevJobDeployed;
    }
}
