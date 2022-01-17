FLE 操作說明


1.	了解基礎 MongoDB 讀寫說明

準備好 mongodb
	參數準備：
		connectionString, dbName, collName

MongoClient :
  BasicConnectAndQuery
  BasicConnectAndWrite
  BasicConnectAndDelete

MongoTemplate :
  BasicConnectAndQuery
  BasicConnectAndWrite

2.	建立 Key and Key Space

CreateMasterKey
  產生檔案master-key.txt
CreateDataKey
  建立datakey資料於keyVaultNamespace
  紀錄 base64KeyId
CheckDataKey
  確認 base64KeyId 存在於

3.	加密資料自動讀寫
  記得參數準備
		connectionString, dbName, collName, keyVaultNamespace, base64DataKeyId

MongoClient :
	WriteEncryptionAuto
  QueryEncryptAuto

MongoTemplate :
	WriteEncryptionAuto
  QueryEncryptAuto


4.	加密資料手動讀寫
記得參數準備
		connectionString, dbName, collName, keyVaultNamespace, base64DataKeyId

MongoClient :
  WriteEncryptionExplicit
  QueryEncryptExplicit

MongoTemplate :
	WriteEncryptionExplicit
	QueryEncryptExplicit


5.	
