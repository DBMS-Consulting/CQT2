package com.dbms.mail;

import java.util.Properties;
import java.util.List;
import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.*;
import javax.mail.internet.*;


public class EmailEntity { 

        private String smtp_host;
        private String smtp_port;
        private String userId;
        private String encryptedPassword;
        private List<String> recipients;
        private String subject;
        private String textMessage;

        public EmailEntity(String smtp_host, String smtp_port, String userId, String encryptedPassword, 
                       List<String> recipients, String subject, String textMessage) {
                
                this.smtp_host = smtp_host;
                this.smtp_port = smtp_port;
                this.userId = userId;
                this.encryptedPassword = encryptedPassword;
                this.recipients = recipients;
                this.subject = subject;
                this.textMessage = textMessage;

        }

	public void sendEmail () throws MessagingException {
	
		System.out.println("Preparing the email to send");
		Properties properties = new Properties();
		
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.ssl.trust", this.smtp_host);
		properties.put("mail.smtp.host", this.smtp_host);
		properties.put("mail.smtp.port", this.smtp_port);
		
		String password = encryptPassword;
		
		Session session = Session.getInstance(properties, new Authenticator() { 
			@Override
			protected PasswordAuthentication getPasswordAuthentication() { 
				return new PasswordAuthentication(this.userId, password);
			}
		});
		
		InternetAddress[] recipientAddress = new InternetAddress[recipients.size()];
		int counter = 0;
		for (String recipient : recipients) {
			recipientAddress[counter] = new InternetAddress(recipient.trim());
			counter++;
		}
		
		Message message = prepareMessage(session, recipientAddress);
		
		Transport.send(message);
		System.out.println("Email sent success..");
	}
	
	private static Message prepareMessage(Session session, InternetAddress[] recipientAddress) { 
		
		try { 
		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.userId));
			message.setRecipients(Message.RecipientType.TO, recipientAddress);
			message.setSubject(this.subject);
			message.setText(this.textMessage);
			return message;
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			
		}
		return null;
	}
}
	