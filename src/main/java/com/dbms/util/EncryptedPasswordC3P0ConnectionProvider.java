/*
 * =============================================================================
 * 
 *   Copyright (c) 2007-2010, The JASYPT team (http://www.jasypt.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package com.dbms.util;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate4.connectionprovider.ParameterNaming;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p>
 * Extension of {@link C3P0ConnectionProvider} that allows the user
 * to write the datasource configuration parameters in an encrypted manner in the 
 * <tt>hibernate.cfg.xml</tt> or <tt>hibernate.properties</tt> file
 * </p>
 * <p>
 * The encryptable parameters are:
 *  <ul>
 *    <li><tt>connection.driver_class</tt></li>
 *    <li><tt>connection.url</tt></li>
 *    <li><tt>connection.username</tt></li>
 *    <li><tt>connection.password</tt></li>
 *  </ul>
 * </p>
 * <p>
 * The name of the password encryptor (decryptor, in fact) will be set in
 * property <tt>hibernate.connection.encryptor_registered_name</tt>. 
 * Its value must be the name of a {@link PBEStringEncryptor} object 
 * previously registered within {@link HibernatePBEEncryptorRegistry}.
 * </p>
 * <p>
 * An example <tt>hibernate.cfg.xml</tt> file:
 * </p>
 * <p>
 * <pre>
 *  &lt;hibernate-configuration>
 *
 *    &lt;session-factory>
 *
 *      <!-- Database connection settings -->
 *      &lt;property name="<b>connection.provider_class</b>">org.jasypt.hibernate.connectionprovider.EncryptedPasswordC3P0ConnectionProvider&lt;/property>
 *      &lt;property name="<b>connection.encryptor_registered_name</b>">stringEncryptor&lt;/property>
 *      &lt;property name="connection.driver_class">org.postgresql.Driver&lt;/property>
 *      &lt;property name="connection.url">jdbc:postgresql://localhost/mydatabase&lt;/property>
 *      &lt;property name="connection.username">myuser&lt;/property>
 *      &lt;property name="connection.password">ENC(T6DAe34NasW==)&lt;/property>
 *      &lt;property name="c3p0.min_size">5&lt;/property>
 *      &lt;property name="c3p0.max_size">20&lt;/property>
 *      &lt;property name="c3p0.timeout">1800&lt;/property>
 *      &lt;property name="c3p0.max_statements">50&lt;/property>
 *      ...
 *      
 *    &lt;/session-factory>
 *    
 *    ...
 *    
 *  &lt;/hibernate-configuration>
 * </pre>
 * </p>
 * 
 * @since 1.9.0
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 */
public final class EncryptedPasswordC3P0ConnectionProvider 
        extends C3P0ConnectionProvider {
    
    private static final long serialVersionUID = 5273353009914873806L;

	private static final Logger LOG = LoggerFactory.getLogger(EncryptorListener.class);

	private static final String hibernateEncryptor = "hibernateEncryptor";
	private static final String TOKEN_KEY = "TOKEN_KEY";
	private static final String ALGORITHM = "PBEWITHMD5ANDDES";

	private static HibernatePBEEncryptorRegistry registry = null;
	
	
    public EncryptedPasswordC3P0ConnectionProvider() {
        super();
        init();
    }

    public void init() {   	
    	if (registry == null) {
	    	String myKey = System.getenv("TOKEN_KEY");
	    	LOG.info("key: [" + myKey + "]");
	
			EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
			config.setPasswordEnvName(TOKEN_KEY);
			  
			StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
			strongEncryptor.setAlgorithm(ALGORITHM);
			strongEncryptor.setConfig(config);
			  
			registry =  HibernatePBEEncryptorRegistry.getInstance();
			registry.registerPBEStringEncryptor(hibernateEncryptor, strongEncryptor);
	
			LOG.info("'" + hibernateEncryptor + "' has been registered.");
    	}
    }
    
	public void configure(Map props) {
		final String encryptorRegisteredName =
				(String)props.get(ParameterNaming.ENCRYPTOR_REGISTERED_NAME);

		final HibernatePBEEncryptorRegistry encryptorRegistry =
				HibernatePBEEncryptorRegistry.getInstance();
		final PBEStringEncryptor encryptor =
				encryptorRegistry.getPBEStringEncryptor(encryptorRegisteredName);

		if (encryptor == null) {
			throw new EncryptionInitializationException(
					"No string encryptor registered for hibernate " +
							"with name \"" + encryptorRegisteredName + "\"");
		}

		// Get the original values, which may be encrypted
		final String driver = (String)props.get(AvailableSettings.DRIVER);
		final String url = (String)props.get(AvailableSettings.URL);
		final String user = (String)props.get(AvailableSettings.USER);
		final String password = (String)props.get(AvailableSettings.PASS);

		// Perform decryption operations as needed and store the new values
		if (PropertyValueEncryptionUtils.isEncryptedValue(driver)) {
			props.put(
					AvailableSettings.DRIVER,
					PropertyValueEncryptionUtils.decrypt(driver, encryptor));
		}
		if (PropertyValueEncryptionUtils.isEncryptedValue(url)) {
			props.put(
					AvailableSettings.URL,
					PropertyValueEncryptionUtils.decrypt(url, encryptor));
		}
		if (PropertyValueEncryptionUtils.isEncryptedValue(user)) {
			props.put(
					AvailableSettings.USER,
					PropertyValueEncryptionUtils.decrypt(user, encryptor));
		}
		if (PropertyValueEncryptionUtils.isEncryptedValue(password)) {
			props.put(
					AvailableSettings.PASS,
					PropertyValueEncryptionUtils.decrypt(password, encryptor));
		}

		// Let Hibernate do the rest
		super.configure(props);
	}
}
