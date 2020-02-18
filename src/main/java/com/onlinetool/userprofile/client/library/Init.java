package com.onlinetool.userprofile.client.library;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class Init implements ApplicationRunner {

    @Value("${custom.path.logs}")
    private String logPath;

    @Value("${custom.path.tars}")
    private String tarPath;

    @Value("${custom.path.reponsitory}")
    private String reponsitoryPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File fileTar = new File(tarPath);
        File fileLog = new File(logPath);
        File fileReponsitory = new File(reponsitoryPath);
        if (!fileTar.exists() && !fileTar.mkdirs()) {
            System.out.println("failed to mkdir: " + fileTar.getAbsolutePath());
            System.exit(-1);
        } else if (!fileLog.exists() && !fileLog.mkdirs()) {
            System.out.println("failed to mkdir: " + fileLog.getAbsolutePath());
            System.exit(-1);
        } else if (!fileReponsitory.exists() && !fileReponsitory.mkdirs()) {
            System.out.println("failed to mkdir: " + fileReponsitory.getAbsolutePath());
            System.exit(-1);
        }
    }
}
