package com.dbms.util;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmqCryptoHandler {
	private static final Logger LOG = LoggerFactory.getLogger(CmqCryptoHandler.class);
	
	private static final String TOKEN_KEY = "TOKEN_KEY";
	private static final String ALGORITHM = "PBEWITHMD5ANDDES";
	private StandardPBEStringEncryptor encryptor;
	
	public String decrypt(String cipherText, String defaultValue) {
		if(!StringUtils.isBlank(cipherText)) {
			if(PropertyValueEncryptionUtils.isEncryptedValue(cipherText)) {
				try{
					EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
					
					//EnvironmentStringPBEConfig reads int he value from the env variable TOKEN_KEY
					config.setPasswordEnvName(TOKEN_KEY);
					  
					this.encryptor = new StandardPBEStringEncryptor();
					this.encryptor.setAlgorithm(ALGORITHM);
					this.encryptor.setConfig(config);
					cipherText = cipherText.substring(cipherText.indexOf("ENC(") + 4, cipherText.length() - 1);
					return this.encryptor.decrypt(cipherText);
				} catch (Exception e) {
					LOG.error("An error occured while decrypting cipher text {}", cipherText, e);
					return defaultValue;
				}
			} else {
				return cipherText; 
			}
		} else {
			return defaultValue; 
		}
	}
	
	public String encrypt(String plainText) {
		if(!StringUtils.isBlank(plainText)) {
			try{
				EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
				
				//EnvironmentStringPBEConfig reads int he value from the env variable TOKEN_KEY
				config.setPasswordEnvName(TOKEN_KEY);
				  
				this.encryptor = new StandardPBEStringEncryptor();
				this.encryptor.setAlgorithm(ALGORITHM);
				this.encryptor.setConfig(config);
				return this.encryptor.encrypt(plainText);
			} catch (Exception e) {
				LOG.error("An error occured while encrypting plain text {}", plainText, e);
				return e.getMessage();
			}
		} else {
			return null; 
		}
	}
	
	public static void main(String[] args) {
		CmqCryptoHandler cryptoHandler = new CmqCryptoHandler();
		//String cipherText = cryptoHandler.encrypt("cn=cqt_admpx,ou=People,dc=pxed,dc=pfizer,dc=com");
		//System.out.println("cipher text:" + cipherText);
		System.out.println(cryptoHandler.decrypt("ENC(sJEddYRJT3Cz19zqHkL64Jl9XRUsSNXeNWR5kMivEv7cim66hj6HySp380Kd2rykJ4WoRQKkKWk=)" ,"gg"));
		
	}
}
