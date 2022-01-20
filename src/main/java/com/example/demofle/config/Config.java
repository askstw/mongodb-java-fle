package com.example.demofle.config;

public class Config {

    //mongodb url
    public static final String connectionString = "mongodb://c:c@13.214.135.136:27077";

    //target nameSpace
    public static final String nameSpace = "test.customer";

    //target database
    public static final String dbName = "test";

    //target collection
    public static final String collName = "customer";

    //master key file and path
    public static final String masterKeyFile = "master-key.txt";

    //data key store in db namespace
    public static final String keyVaultNamespace = "encryption.__keyVault";

    //data key store in db 
    public static final String keyVaultDb = "encryption";

    //data key store in collection
    public static final String keyVaultCollection = "__keyVault";

    //data key present in base64
    //should be modify based on your data key
    public static final String base64DataKeyId = "87j9MH/FR6e9x2PIXkBiaQ==";

}
