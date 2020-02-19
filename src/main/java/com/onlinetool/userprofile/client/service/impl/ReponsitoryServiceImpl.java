package com.onlinetool.userprofile.client.service.impl;

import com.onlinetool.userprofile.client.model.entity.DiffContentLine;
import com.onlinetool.userprofile.client.model.entity.DiffFileEntity;
import com.onlinetool.userprofile.client.model.entity.ExceptionDetail;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.service.ConfigurationService;
import com.onlinetool.userprofile.client.service.DeployLogger;
import com.onlinetool.userprofile.client.service.ReponsitoryService;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/18 上午8:19
 * @description：
 * @modified By：
 * @version: $
 */
public class ReponsitoryServiceImpl implements ReponsitoryService {

    @Value("${custom.path.reponsitory}")
    private String reponsitoryBase;

    private static final String REF_REMOTES = "refs/remotes/origin/";

    private Map<String, Git> gitMap = new HashMap<>();
    private Git getByReponsitory(String directory) throws IOException {
        if(!gitMap.containsKey(directory)){
            File directoryFile = new File(this.reponsitoryBase + File.separator + directory);
            if(directoryFile.exists() && directoryFile.isDirectory() && directoryFile.canWrite()){
                Git git = Git.open(directoryFile);
                gitMap.put(directory, git);
                return git;
            }
        }
        return gitMap.get(directory);
    }

    private String getCurrentBranch(String directory) throws IOException {
        Git git = getByReponsitory(directory);
        if(git != null){
            return git.getRepository().getBranch();
        }
        return null;
    }

    private Ref findRefByBranch(String branch, boolean remoteFlg){
        //分支合并
        Ref ref = null;
        try {
            if (remoteFlg) {
                ref = repository.getRefDatabase().findRef(REF_REMOTES + branch);
            }else {
                ref = repository.getRefDatabase().findRef(branch);
            }
        } catch (IOException ignored) {}
        return ref;
    }

    @Override
    public String[] getBranches(String directory) throws IOException, GitAPIException {
        Git git = getByReponsitory(directory);
        if(git == null) return null;

        //拉取远程分支
        git.fetch().call();

        final List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        
        String[] branches = new String[refs.size()];

        int i = 0;
        for (Ref tmpRef : refs){
            String branch = tmpRef.getName().replace(REF_REMOTES, "");
            if (!branch.equals("HEAD")) {
                branches[i] = branch;
                i++;
            }
        }
        return branches;
    }

    private Git git;
    private Repository repository;

    public List<DiffFileEntity> diffBranch(String reponsitoryDirectory, String fromBranch, String targetBranch) throws IOException, GitAPIException {
        git = getByReponsitory(reponsitoryDirectory);
        repository = git.getRepository();
        git.pull().call();

        ObjectReader reader = repository.newObjectReader();

        ObjectId oldObject = git.getRepository().resolve(REF_REMOTES + targetBranch +"^{tree}");
        ObjectId newObject = git.getRepository().resolve(REF_REMOTES + fromBranch+"^{tree}");
        CanonicalTreeParser oldIter = new CanonicalTreeParser();
        oldIter.reset(reader, oldObject);
        CanonicalTreeParser newIter = new CanonicalTreeParser();
        newIter.reset(reader, newObject);

        List<DiffEntry> diffs = git.diff().setNewTree(newIter).setOldTree(oldIter).call();

        List<DiffFileEntity> listDiffFileEntity = new ArrayList<>();
        for (DiffEntry diffEntry : diffs) {
            DiffFileEntity diffFileEntity = new DiffFileEntity();
            DiffEntry.ChangeType changeType = diffEntry.getChangeType();
            diffFileEntity.setChangeType(changeType);
            if (changeType == DiffEntry.ChangeType.ADD){
                diffFileEntity.setFileName(diffEntry.getNewPath());
            }else{
                diffFileEntity.setFileName(diffEntry.getOldPath());
            }

            List<DiffContentLine> listDiffContentList = new ArrayList<>();
            if (changeType != DiffEntry.ChangeType.DELETE) {
                int oldLine = 1;
                int newLine = 1;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DiffFormatter df = new DiffFormatter(out);
                df.setRepository(repository);
                df.format(diffEntry);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (
                            !line.contains("diff --git ") &&
                            !line.contains("index ") &&
                            !line.contains("--- ") &&
                            !line.contains("+++ ") &&
                            !line.contains("new file mode")
                    ) {
                        if (line.contains("@@ ") && line.contains(" @@")) {
                            line = line.substring(3, line.length() - 3);
                            String[] str_arr = line.split(",");
                            for (String subStr : str_arr) {
                                if (subStr.contains("-")) {
                                    oldLine = Integer.parseInt(subStr.replace("-", ""));
                                }
                                if (subStr.contains("+")) {
                                    newLine = Integer.parseInt(subStr.split("\\+")[1]);
                                }
                            }
                        } else {
                            DiffContentLine diffContentLine = new DiffContentLine();
                            String operate = line.substring(0, 1);
                            diffContentLine.setOperate(operate);
                            if (operate.equals("+")) {
                                diffContentLine.setOldLineNum(0);
                            } else {
                                diffContentLine.setOldLineNum(oldLine);
                                oldLine++;
                            }
                            if (operate.equals("-")) {
                                diffContentLine.setNewLineNum(0);
                            } else {
                                diffContentLine.setNewLineNum(newLine);
                                newLine++;
                            }
                            String content = line.substring(1);
                            diffContentLine.setContent(content);
                            listDiffContentList.add(diffContentLine);
                        }
                    }
                }
            }
            diffFileEntity.setDiffContentLine(listDiffContentList);
            listDiffFileEntity.add(diffFileEntity);
        }
        return listDiffFileEntity;
    }

    @Override
    public String checkoutBranch(long servsId, String reponsitory, String branch, DeployLogger deployLogger) throws GitAPIException {
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(servsId);
        Git git;
        //获取git对象
        try {
            git = getByReponsitory(reponsitory);
        } catch (IOException e) {
            ExceptionDetail exceptionDetail = new ExceptionDetail(e);
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                consoleObject.setExceptionDetail(exceptionDetail);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            return null;
        }
        Repository repository = git.getRepository();

        //切到主分支并拉取最新代码
        try {
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("up");
                consoleObject.setTitle("git clean -fd");
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            CleanCommand cleanCommand = git.clean();
            cleanCommand.setCleanDirectories(true);
            cleanCommand.setForce(true);
            cleanCommand.call();
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("down");
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}

            if(!repository.getBranch().equals(branch)){
                //判断本地是否有该分支
                Ref ref = this.findRefByBranch(branch, false);
                if (ref == null){
                    //拉取远程分支
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("up");
                        consoleObject.setTitle("git fetch");
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                    git.fetch().call();
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("down");
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                    //切换分支
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("up");
                        consoleObject.setTitle("git checkout " + branch);
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                    git.checkout().setCreateBranch(true).setName(branch).call();
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("down");
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                }else {
                    //切换分支
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("up");
                        consoleObject.setTitle("git checkout " + branch);
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                    git.checkout().setCreateBranch(false).setName(branch).call();
                    try {
                        ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                        consoleObject.setStage(1);
                        consoleObject.setStatus("down");
                        deployLogger.log(consoleObject);
                    } catch (CloneNotSupportedException ignored) {}
                }
            }
        } catch (IOException e) {
            ExceptionDetail exceptionDetail = new ExceptionDetail(e);
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                consoleObject.setExceptionDetail(exceptionDetail);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            return null;
        }
        //更新本地代码到最新
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("up");
            consoleObject.setTitle("git pull origin " + branch);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        git.pull().call();
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("down");
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        Iterable<RevCommit> iterable=git.log().call();
        RevCommit commit = iterable.iterator().next();
        String commitID = commit.getName().substring(0,7);

        return commitID;
    }

    @Override
    public boolean mergeBranch(long servsId, String reponsitory, String fromBranch, String toBranch, DeployLogger deployLogger) throws GitAPIException {
        if (this.checkoutBranch(servsId, reponsitory, toBranch, deployLogger) == null) return false;
        ConsoleObject consoleObjectInit = deployLogger.getConsoleObject(servsId);
        Git git;
        //获取git对象
        try {
            git = getByReponsitory(reponsitory);
        } catch (IOException e) {
            ExceptionDetail exceptionDetail = new ExceptionDetail(e);
            try {
                ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
                consoleObject.setStage(1);
                consoleObject.setStatus("down");
                consoleObject.setLevel(ConsoleObject.LEVEL_ERROR);
                consoleObject.setExceptionDetail(exceptionDetail);
                deployLogger.log(consoleObject);
            } catch (CloneNotSupportedException ignored) {}
            return false;
        }
        //分支合并
        Ref ref = this.findRefByBranch(fromBranch, true);
        if(ref == null){
            Exception e = new Exception("分支 "+fromBranch+" 不存在");
            ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                {
                    put("fromBranch", REF_REMOTES + fromBranch);
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
            } catch (CloneNotSupportedException ignored) {}
            return false;
        }

        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("up");
            consoleObject.setTitle("git merge " + fromBranch);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        MergeCommand mgCmd = git.merge();
        mgCmd.include(ref);
        MergeResult res = mgCmd.setMessage("合并分支 "+fromBranch+" 到分支 "+toBranch).call();
        if (res.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
            Exception e = new Exception("合并分支出现冲突");
            ExceptionDetail exceptionDetail = new ExceptionDetail(e, new HashMap<String, String>(){
                {
                    put("fromBranch", REF_REMOTES + fromBranch);
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
            } catch (CloneNotSupportedException ignored) {}
            return false;
        }
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("down");
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("up");
            consoleObject.setTitle("git push origin " + toBranch);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        git.push().call();
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("down");
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}

        deployLogger.setMergedBranch(consoleObjectInit, fromBranch);

        //上线后删除原来分支
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("up");
            consoleObject.setTitle("git push origin --delete " + fromBranch);
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        git.branchDelete().setBranchNames("refs/heads/" + fromBranch).call();
        RefSpec refSpec = new RefSpec()
                .setSource(null)
                .setDestination("refs/heads/" + fromBranch);
        git.push().setRefSpecs(refSpec).setRemote("origin").call();
        try {
            ConsoleObject consoleObject = (ConsoleObject) consoleObjectInit.clone();
            consoleObject.setStage(1);
            consoleObject.setStatus("down");
            deployLogger.log(consoleObject);
        } catch (CloneNotSupportedException ignored) {}
        return true;
    }
}
