package com.onlinetool.userprofile.client.model.object;

/**
 * 上线任务job管理
 */
public class TaskJobObject {
    /**
     * job操作类型:
     * 1 创建job
     * 2 重启job
     */
    private int operateType;

    /**
     * job名称
     */
    private String name;

    /**
     * 脚本路径
     */
    private String script;

    /**
     * 脚本参数
     */
    private String args;

    /**
     * 脚本解释器
     */
    private String interpreter;

    public int getOperateType() {
        return operateType;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }
}
