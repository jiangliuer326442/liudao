package com.onlinetool.userprofile.client.service;

import com.onlinetool.userprofile.client.model.entity.DiffFileEntity;
import com.onlinetool.userprofile.client.model.object.ServsObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/18 上午8:18
 * @description：
 * @modified By：
 * @version: $
 */
public interface ReponsitoryService {

    /**
     * 获取该目录的分支列表
     * @param directory git仓库目录
     * @return
     */
    String[] getBranches(String directory) throws IOException, GitAPIException;

    /**
     * 分支合并
     * @param servId 微服务id
     * @param reponsitory　git仓库位置
     * @param fromBranch　起始分支
     * @param toBranch　终了分支
     * @param deployLogger 日志服务器
     * @return　是否分支合并成功
     * @throws IOException
     * @throws GitAPIException
     */
    boolean mergeBranch(
            long servId,
            String reponsitory,
            String fromBranch,
            String toBranch,
            DeployLogger deployLogger
    ) throws GitAPIException;

    String checkoutBranch(
            long servId,
            String reponsitory,
            String branch,
            DeployLogger deployLogger
    ) throws GitAPIException;

    List<DiffFileEntity> diffBranch(
            String reponsitoryDirectory,
            String fromBranch,
            String targetBranch
    ) throws IOException, GitAPIException;
}
