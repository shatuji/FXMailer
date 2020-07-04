package com.houarizegai.fxmailer.engine;



import com.houarizegai.fxmailer.util.Tools;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.print.attribute.standard.PDLOverrideSupported;
import java.util.Properties;


public class EmailEngine {

    private Session session;
    private Message message;

    private MimeMultipart multipart;
    private BodyPart messageBodyPart;

    public EmailEngine() {
        // This mail has 2 part, the BODY and the embedded image
        multipart = new MimeMultipart("related");
    }

    public EmailEngine setAuth(String email, String password) {
        Properties props = Tools.getProperties(email);
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

        // Create a default MimeMessage object.
        message = new MimeMessage(session);

        // Set From: header field of the header.
        try {
            message.setFrom(new InternetAddress(email));
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public EmailEngine setSubject(String subject) {
        // Set Subject: header field
        try {
            message.setSubject(subject);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public EmailEngine setContent(String htmlContent) {
        // first part (the html)
        messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setContent(htmlContent, "text/html");
            // add it
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    /***
     * send images for head
     * @param path
     * @return
     */
    public EmailEngine setHeaderImage(String path) {
        // second part (the image)
        messageBodyPart = new MimeBodyPart();
        DataSource fds = new FileDataSource(path);

        try {
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<headerImage>");

            // add image to the multipart
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public EmailEngine setAttachFile(String path){
        // second part (the image)
        messageBodyPart = new MimeBodyPart();
        DataSource fds = new FileDataSource(path);

        try {
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setFileName("附件");

            // add image to the multipart
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean send(String recipient) {
        try {
            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

            // put everything together
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            return true; // successful send it
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
