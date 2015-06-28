package org.em.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {
	private String from_;
	private String to_;

	private String smtp_server;
	private String user_;
	private String password_;
	private int port_;
	private String content_;
	private String subject_;

	public SendMail(String from_, String smtp_server, String user_,
			String password_, int port_) {
		super();
		this.from_ = from_;
		this.smtp_server = smtp_server;
		this.user_ = user_;
		this.password_ = password_;
		this.port_ = port_;
	}

	public String getContent_() {
		return content_;
	}

	public void setContent_(String content_) {
		this.content_ = content_;
	}

	public String getSubject_() {
		return subject_;
	}

	public void setSubject_(String subject_) {
		this.subject_ = subject_;
	}
	
	

	public String getTo_() {
		return to_;
	}

	public void setTo_(String to_) {
		this.to_ = to_;
	}

	public int send() {
		Properties props = System.getProperties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", this.smtp_server);
		props.put("mail.smtp.port", this.port_);
		props.setProperty("mail.smtp.auth", "true");
		// 控制连接和socket timeout 时间
		props.put("mail.smtp.connectiontimeout",10180);
		props.put("mail.smtp.timeout", 10600);
		props.setProperty("mail.smtp.ssl.enable", "true");

		props.setProperty("mail.mime.encodefilename", "true");

		// 认证的用户和密码， 不同于登录站点的用户名和密码，需要登录SendCloud进行发信域名获取
		final String userName = this.user_;
		final String password = this.password_;
		try {

			Session mailSession = Session.getInstance(props,
					new Authenticator() {
						// 用户验证
						//@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(userName,
									password);
						}
					});

			// 设置调试，打印smtp信息
			// session.setDebug(true);

			Transport transport = mailSession.getTransport();
			MimeMessage message = new MimeMessage(mailSession);
			Multipart multipart = new MimeMultipart("alternative");

			// 纯文本形式的邮件正文
			// BodyPart part1 = new MimeBodyPart();
			// part1.setText("欢迎使用SendCloud", "text/plain;charset=UTF-8");
			// part1.setHeader("Content-Type", "text/plain;charset=UTF-8");
			// part1.setHeader("Content-Transfer-Encoding", "base64");

			// html形式的邮件正文
			BodyPart part2 = new MimeBodyPart();
			part2.setHeader("Content-Type", "text/html;charset=UTF-8");
			part2.setHeader("Content-Transfer-Encoding", "quoted-printable"); // 或者使用base64
			part2.setContent(this.content_, "text/html;charset=UTF-8");

			// multipart.addBodyPart(part1);
			multipart.addBodyPart(part2);

			message.setContent(multipart);

			message.setFrom(new InternetAddress(this.from_, this.from_, "UTF-8"));
			message.setSubject(this.subject_, "UTF-8");
			message.addRecipient(javax.mail.Message.RecipientType.TO,
					new InternetAddress(this.to_));

			transport.connect();
			transport.sendMessage(message,
					message.getRecipients(javax.mail.Message.RecipientType.TO));
			transport.close();
			return 0;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();

		} catch (MessagingException e) {
			e.printStackTrace();

		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		

		return -1;
	}

}
