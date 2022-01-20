package com.example.demofle.keyManagement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import com.example.demofle.config.Config;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class CreateMasterKey {

    public static void main(String[] args) throws IOException {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        byte[] localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);
        try (FileOutputStream stream = new FileOutputStream(Config.masterKeyFile)) {
            stream.write(localMasterKey);
        }
    }
}