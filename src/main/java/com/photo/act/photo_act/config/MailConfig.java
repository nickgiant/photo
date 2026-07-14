package com.photo.act.photo_act.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * JavaMailSender using SMTPS implicit TLS on port 465.
 *
 * Stalwart's cert is for mail.photoact.net but the app connects via
 * the Docker service name "stalwart-mail" — hostname mismatch causes
 * SSLHandshakeException. A trust-all SSLSocketFactory is installed
 * via the correct JavaMail property for implicit TLS (port 465).
 *
 * Safe: traffic never leaves Docker mail-network (192.168.90.0/24).
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        // Build trust-all SSLSocketFactory first
        SSLSocketFactory trustAllFactory;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }}, new java.security.SecureRandom());
            trustAllFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build trust-all SSLContext", e);
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();

        // Use smtps protocol — implicit TLS from the first byte (port 465)
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.auth",         "true");
        props.put("mail.smtps.host",         host);
        props.put("mail.smtps.port",         String.valueOf(port));

        // Timeouts
        props.put("mail.smtps.connectiontimeout", "10000");
        props.put("mail.smtps.timeout",           "30000");
        props.put("mail.smtps.writetimeout",      "30000");

        // The correct property for implicit TLS socket factory
        props.put("mail.smtps.socketFactory",               trustAllFactory);
        props.put("mail.smtps.socketFactory.fallback",      "false");
        props.put("mail.smtps.socketFactory.port",          String.valueOf(port));

        // Disable hostname verification — cert is for mail.photoact.net
        // but we connect via "stalwart-mail" (internal Docker DNS)
        props.put("mail.smtps.ssl.checkserveridentity",     "false");
        props.put("mail.smtps.ssl.trust",                   "*");

        return sender;
    }
}