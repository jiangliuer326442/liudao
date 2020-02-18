package com.onlinetool.userprofile.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.library.util.Call;
import com.onlinetool.userprofile.client.model.object.ConsoleObject;
import com.onlinetool.userprofile.client.model.object.ResultObject;
import com.onlinetool.userprofile.client.service.ReponsitoryService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
/**
 * @author ：jiangliuer
 * @date ：Created in 2019/11/2 下午2:51
 * @description： 客户端工具集
 * @modified By：
 * @version: $
 */
public class UtilController {
    private Call call;
    private ReponsitoryService reponsitoryService;
    private ObjectMapper objectMapper;
    private SimpMessagingTemplate simpMessageSendingOperations;

    @Autowired
    public UtilController(Call call, ReponsitoryService reponsitoryService, ObjectMapper objectMapper, SimpMessagingTemplate simpMessageSendingOperations) {
        this.call = call;
        this.reponsitoryService = reponsitoryService;
        this.objectMapper = objectMapper;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    @PostMapping(value = "/ckversion")
    public ResultObject ckUpdate(){
        return call.invoke("global", "checkVersion", new Object[]{}, ResultObject.class);
    }

    @PostMapping(value = "/ckgit")
    public ResultObject ckGitDirectory(String directory){
        String[] branches = null;
        try {
            branches = this.reponsitoryService.getBranches(directory);
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return new ResultObject(ResultCodeConstant.PARADER, "git目录无效");
        }
        if(branches == null){
            return new ResultObject(ResultCodeConstant.PARADER, "git目录无效");
        }
        return new ResultObject<String[]>(ResultCodeConstant.SUCCESS, "获取分之列表成功", branches);
    }
}
