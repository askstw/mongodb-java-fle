package com.example.demofle.keyManagement;

import java.io.IOException;
import java.util.Base64;

import com.example.demofle.config.Config;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class CheckKeyID {
    public static void main(String[] args) throws IOException {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        
        System.out.println("decodeDataKey = " + Base64.getDecoder().decode(Config.base64DataKeyId));        
        System.out.println("encodeDataKey = " + Base64.getEncoder().encodeToString(Base64.getDecoder().decode(Config.base64DataKeyId)));        
        
    }
}