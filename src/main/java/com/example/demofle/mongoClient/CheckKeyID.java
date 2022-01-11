package com.example.demofle.mongoClient;

import java.io.IOException;
import java.util.Base64;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class CheckKeyID {
    public static void main(String[] args) throws IOException {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        
        String base64KeyId = "87j9MH/FR6e9x2PIXkBiaQ=="; // use the base64 data key id returned by createKey() in the prior step
        System.out.println("decodeDataKey = " + Base64.getDecoder().decode(base64KeyId));        
        System.out.println("encodeDataKey = " + Base64.getEncoder().encodeToString(Base64.getDecoder().decode(base64KeyId)));        
        
    }
}