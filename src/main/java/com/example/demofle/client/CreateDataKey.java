package com.example.demofle.client;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.BsonBinary;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class CreateDataKey {

    public static void main( String[] args ) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        
        try {
            String connectionString = "mongodb://c:c@13.214.135.136:27077";
            String keyVaultNamespace = "encryption.__keyVault";
            
            String path = "master-key.txt";
            byte[] localMasterKey= new byte[96];
            try (FileInputStream fis = new FileInputStream(path)) {
                fis.readNBytes(localMasterKey, 0, 96);
            }
            System.out.println("localMasterKey = " + localMasterKey.toString());

            Map<String, Object> keyMap = new HashMap<String, Object>();
            keyMap.put("key", localMasterKey);

            Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>();
            kmsProviders.put("local", keyMap);

            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .build())
        .keyVaultNamespace(keyVaultNamespace)
        .kmsProviders(kmsProviders)
        .build();

            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
            
            BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
            System.out.println("DataKeyId [UUID]: " + dataKeyId.asUuid());

            String base64DataKeyId = Base64.getEncoder().encodeToString(dataKeyId.getData());
            System.out.println("DataKeyId [base64]: " + base64DataKeyId);
            
        } catch (Exception e){

        }
    }

}