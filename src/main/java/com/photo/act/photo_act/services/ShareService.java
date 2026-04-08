package com.photo.act.photo_act.services;


import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
public class ShareService {

    public String facebook(String url) {
        return "https://www.facebook.com/sharer/sharer.php?u=" + encode(url);
    }

    public String linkedIn(String url) {
        return "https://www.linkedin.com/sharing/share-offsite/?url=" + encode(url);
    }

    public String pinterest(String url, String mediaUrl, String description) {
        return "https://pinterest.com/pin/create/button/"
                + "?url=" + encode(url)
                + "&media=" + encode(mediaUrl)
                + "&description=" + encode(description);
    }

    public String instagram(String url) {
        // Instagram does not support direct web share links.
        // Redirect to Instagram profile or open main page.
        return "https://www.instagram.com/";
    }

    public String email(String subject, String body) {
        return "mailto:?subject=" + encode(subject)
                + "&body=" + encode(body);
    }

    private String encode(String value) {
        return UriUtils.encode(value, StandardCharsets.UTF_8);
    }
}
