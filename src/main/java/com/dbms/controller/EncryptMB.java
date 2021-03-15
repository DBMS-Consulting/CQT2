package com.dbms.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import oracle.net.aso.e

import com.dbms.util.CmqCryptoHandler;

@ManagedBean
@ViewScoped
public class EncryptMB implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -2192336665712943163L;

	private CmqCryptoHandler cryptoHandler;

	private String text, textE;
	private boolean encrypt, decrypt;

	@PostConstruct
	public void init() {
		cryptoHandler = new CmqCryptoHandler();
		encrypt = true;
		decrypt = false;
	}

	public String encryptText() {
		textE = cryptoHandler.encrypt(text);
		
		encrypt = false;
		decrypt = true;
		
		return textE;
	}

	public String decryptText() {
		textE = cryptoHandler.decrypt("ENC(" + text + ")", "my_default_value");
		decrypt = false;
		encrypt = true;
		
		System.out.println("\n ******** textE : " + textE);
		
		return textE;
	}

	public CmqCryptoHandler getCryptoHandler() {
		return cryptoHandler;
	}

	public void setCryptoHandler(CmqCryptoHandler cryptoHandler) {
		this.cryptoHandler = cryptoHandler;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public boolean isDecrypt() {
		return decrypt;
	}

	public void setDecrypt(boolean decrypt) {
		this.decrypt = decrypt;
	}

	public String getTextE() {
		return textE;
	}

	public void setTextE(String textE) {
		this.textE = textE;
	}

}
