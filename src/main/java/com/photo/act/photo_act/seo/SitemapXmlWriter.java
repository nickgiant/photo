package com.photo.act.photo_act.seo;

import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renders {@link SitemapUrlEntry} rows as Sitemaps Protocol 0.9 XML.
 * Pure formatting — no querying, no I/O. See {@link SitemapQueryService} for data gathering.
 *
 * https://www.sitemaps.org/protocol.html
 */
@Component
public class SitemapXmlWriter {

    /** W3C datetime format required by the Sitemaps 0.9 spec */
    private static final DateTimeFormatter W3C_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    public String write(List<SitemapUrlEntry> entries) {
        StringBuilder xml = new StringBuilder(2048);

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n\n");

        for (SitemapUrlEntry entry : entries) {
            appendUrl(xml, entry);
        }

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void appendUrl(StringBuilder xml, SitemapUrlEntry entry) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escXml(entry.loc())).append("</loc>\n");
        xml.append("    <lastmod>").append(W3C_DATE.format(entry.lastmod())).append("</lastmod>\n");
        xml.append("    <changefreq>").append(entry.changefreq()).append("</changefreq>\n");
        xml.append("    <priority>").append(entry.priority()).append("</priority>\n");
        xml.append("  </url>\n\n");
    }

    /** Escapes the five XML special characters. Must be applied to every dynamic value inserted into XML. */
    private String escXml(String value) {
        if (value == null) return "";
        return value
                .replace("&",  "&amp;")   // must be first
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}
