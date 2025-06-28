package com.photo.act.photo_act.utils;

import com.photo.act.photo_act.views.LearningsView;
import com.vaadin.flow.server.VaadinRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.photo.act.photo_act.views.MainLayout.HOSTNAME_LAPTOP;
import static com.photo.act.photo_act.views.MainLayout.HOSTNAME_LAPTOP_LENOVO_WIN;

public class NetUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    public NetUtils() {


    }
    public String getClientPublicIp(String hostname) {
        if((!hostname.equalsIgnoreCase(HOSTNAME_LAPTOP))) // && (!hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_WIN))) {
        {   String publicIp;
            publicIp = VaadinRequest.getCurrent().getHeader("X-Real-IP");
//            VaadinRequest.getCurrent().getRemoteAddr()
//            if(publicIp.equalsIgnoreCase("185.162.238.159" )) {
//                String urlString = "https://checkip.amazonaws.com/";
//
//                try {
//                    URL url = new URL(urlString);
//                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//                    publicIp = br.readLine();
//                } catch (IOException MalformedURLException) {
//                    logger.error("error getClientPublicIp from " + urlString);
//                }
//                return publicIp;
//            }
            return publicIp;
        }
        else {
            return "mike-SATELLITE-PRO-C50-H-11G  -  my-pc";
        }
    }
}
