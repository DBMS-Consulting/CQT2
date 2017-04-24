package com.dbms.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.filters.AuthenticationFilter;



@WebListener
public class EncryptorListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(EncryptorListener.class);

	private static final String hibernateEncryptor = "hibernateEncryptor";
	private static final String TOKEN_KEY = "TOKEN_KEY";
	private static final String ALGORITHM = "PBEWITHMD5ANDDES";
	
	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
    	String myKey = System.getenv("TOKEN_KEY");
    	LOG.info("key: [" + myKey + "]");

		EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
		config.setPasswordEnvName(TOKEN_KEY);
		  
		StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
		strongEncryptor.setAlgorithm(ALGORITHM);
		strongEncryptor.setConfig(config);
		  
		HibernatePBEEncryptorRegistry registry =  HibernatePBEEncryptorRegistry.getInstance();
		registry.registerPBEStringEncryptor(hibernateEncryptor, strongEncryptor);

		LOG.info("'" + hibernateEncryptor + "' has been registered.");
	}


}
