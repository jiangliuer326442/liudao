package com.onlinetool.userprofile.client.controller;

import com.onlinetool.userprofile.client.library.constant.ResultCodeConstant;
import com.onlinetool.userprofile.client.model.object.ResultObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：jiangliuer
 * @date ：Created in 2019/9/22 下午10:38
 * @description：
 * @modified By：
 * @version: $
 */
public class BaseController {
    ResultObject<Map<String, String>> _bindCheck(BindingResult bindingResult){
        ResultObject<Map<String, String>> resultObject = new ResultObject<>();

        Map<String, String> retDat = new HashMap<>();

        if (bindingResult.hasErrors()) {
            for (FieldError it : bindingResult.getFieldErrors()) {
                retDat.put(it.getField(), it.getDefaultMessage());
            }
            resultObject.setCode(ResultCodeConstant.PARADER);
            resultObject.setData(retDat);
            return resultObject;
        }
        return resultObject;
    }
}
