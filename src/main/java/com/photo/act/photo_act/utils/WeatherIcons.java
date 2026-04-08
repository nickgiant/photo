package com.photo.act.photo_act.utils;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class WeatherIcons {

    // Icon theme - can be set from outside
    private static IconTheme currentTheme = IconTheme.DEFAULT;

    /**
     * Icon theme options
     */
    public enum IconTheme {
        DEFAULT,  // Original vibrant colors
        EARTHY,   // Natural earth tones
        PASTEL    // Soft pastel colors
    }

    /**
     * Set the icon theme
     */
    public static void setTheme(IconTheme theme) {
        currentTheme = theme;
    }

    /**
     * Set the icon theme by name (for application.properties)
     */
    public static void setTheme(String themeName) {
        try {
            currentTheme = IconTheme.valueOf(themeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid icon theme: " + themeName + ". Using DEFAULT.");
            currentTheme = IconTheme.DEFAULT;
        }
    }

    /**
     * Get current theme
     */
    public static IconTheme getTheme() {
        return currentTheme;
    }

    /**
     * Get SVG icon based on weather type and time of day
     */
    public static Element getWeatherIcon(String weatherType, int size, boolean isNight) {
        String svgContent = getSvgContent(weatherType, size, isNight);
        Element div = new Element("div");
        div.setProperty("innerHTML", svgContent);
        div.getStyle()
                .set("display", "inline-block")
                .set("width", size + "px")
                .set("height", size + "px");
        return div;
    }

    /**
     * Determine if it's night time based on current time and sunrise/sunset
     * Overloaded version for ZonedDateTime (used by CurrentWeather)
     */
    public static boolean isNightTime(ZonedDateTime currentTime, LocalDateTime sunrise, LocalDateTime sunset) {
        if (currentTime == null) {
            return false;
        }

        try {
            // Convert ZonedDateTime to LocalDateTime in the same timezone for comparison
            // This preserves the "wall clock" time for comparison
            LocalDateTime localTime = currentTime.toLocalDateTime();
            return isNightTime(localTime, sunrise, sunset);
        } catch (Exception e) {
            // Fallback if conversion fails
            System.err.println("Error converting ZonedDateTime to LocalDateTime: " + e.getMessage());
            int hour = currentTime.getHour();
            return hour < 6 || hour >= 18;
        }
    }

    /**
     * Determine if it's night time based on current time and sunrise/sunset
     * Original version for LocalDateTime (used by HourlyForecast)
     */
    public static boolean isNightTime(LocalDateTime currentTime, LocalDateTime sunrise, LocalDateTime sunset) {
        if (currentTime == null) {
            // Fallback: return false
            return false;
        }

        if (sunrise == null || sunset == null) {
            // Fallback: consider 6 PM to 6 AM as night
            int hour = currentTime.getHour();
            return hour < 6 || hour >= 18;
        }

        try {
            // Compare times - all are in the same "wall clock" timezone
            return currentTime.isBefore(sunrise) || currentTime.isAfter(sunset);
        } catch (Exception e) {
            // Fallback if comparison fails
            System.err.println("Error comparing times: " + e.getMessage());
            int hour = currentTime.getHour();
            return hour < 6 || hour >= 18;
        }
    }

    private static String getSvgContent(String weatherType, int size, boolean isNight) {
        String type = weatherType.toLowerCase().trim();

        // Debug logging
        System.out.println("WeatherIcons DEBUG - Type: '" + weatherType + "' (lowercase: '" + type + "'), isNight: " + isNight);

        if (type.contains("clear") || type.contains("sunny") || type.equals("clear sky")) {
            System.out.println("WeatherIcons DEBUG - Using " + (isNight ? "MOON" : "SUN") + " icon");
            return isNight ? getMoonIcon(size, currentTheme) : getSunIcon(size, currentTheme);
        } else if (type.contains("cloud") && !type.contains("rain")) {
            System.out.println("WeatherIcons DEBUG - Using CLOUDY icon");
            return isNight ? getCloudyNightIcon(size, currentTheme) : getCloudyDayIcon(size, currentTheme);
        } else if (type.contains("rain") || type.contains("drizzle")) {
            System.out.println("WeatherIcons DEBUG - Using RAIN icon");
            return isNight ? getRainNightIcon(size, currentTheme) : getRainDayIcon(size, currentTheme);
        } else if (type.contains("thunder") || type.contains("storm")) {
            System.out.println("WeatherIcons DEBUG - Using THUNDER icon");
            return getThunderstormIcon(size, currentTheme);
        } else if (type.contains("snow")) {
            System.out.println("WeatherIcons DEBUG - Using SNOW icon");
            return isNight ? getSnowNightIcon(size, currentTheme) : getSnowDayIcon(size, currentTheme);
        } else if (type.contains("mist") || type.contains("fog") || type.contains("haze")) {
            System.out.println("WeatherIcons DEBUG - Using FOG icon");
            return getFogIcon(size, currentTheme);
        } else if (type.contains("wind")) {
            System.out.println("WeatherIcons DEBUG - Using WIND icon");
            return getWindIcon(size, currentTheme);
        } else {
            System.out.println("WeatherIcons DEBUG - Using DEFAULT (cloudy) icon for unmatched type");
            return isNight ? getCloudyNightIcon(size, currentTheme) : getCloudyDayIcon(size, currentTheme);
        }
    }

    // ============================================
    // COLOR THEMES
    // ============================================

    private static class ThemeColors {
        String sunCenter, sunMid, sunEdge, sunRay;
        String moonCenter, moonMid, moonEdge, moonCrater;
        String cloudLight, cloudDark, cloudStroke;
        String nightCloudLight, nightCloudDark, nightCloudStroke;
        String rainDrop;
        String nightRainDrop;
        String snowFlake, snowFlakeLight;
        String stormCloudLight, stormCloudDark;
        String lightningFill, lightningStroke;
        String fogBar;
        String windStroke, windDot;
        String starColor;
        String nightMoonFill;

        static ThemeColors getColors(IconTheme theme) {
            ThemeColors c = new ThemeColors();
            switch (theme) {
                case EARTHY:
                    c.sunCenter = "#F5D68A"; c.sunMid = "#D4A050"; c.sunEdge = "#B8860B"; c.sunRay = "#C49540";
                    c.moonCenter = "#E8E0D0"; c.moonMid = "#D4C9B0"; c.moonEdge = "#B8A888"; c.moonCrater = "#A89878";
                    c.cloudLight = "#CEC5B8"; c.cloudDark = "#A89E90"; c.cloudStroke = "#8E857A";
                    c.nightCloudLight = "#B8B0A2"; c.nightCloudDark = "#8E8678"; c.nightCloudStroke = "#706858";
                    c.rainDrop = "#5B7C99"; c.nightRainDrop = "#4A6B84";
                    c.snowFlake = "#C8C0B0"; c.snowFlakeLight = "#DDD5C8";
                    c.stormCloudLight = "#706860"; c.stormCloudDark = "#484038";
                    c.lightningFill = "#D4A050"; c.lightningStroke = "#B88030";
                    c.fogBar = "#A8A096"; c.windStroke = "#7A9878"; c.windDot = "#7A9878";
                    c.starColor = "#E8E0D0"; c.nightMoonFill = "#D4C9B0";
                    break;

                case PASTEL:
                    c.sunCenter = "#FFF8E1"; c.sunMid = "#FFE0B2"; c.sunEdge = "#FFCC80"; c.sunRay = "#FFD180";
                    c.moonCenter = "#F3E5F5"; c.moonMid = "#E1BEE7"; c.moonEdge = "#CE93D8"; c.moonCrater = "#BA68C8";
                    c.cloudLight = "#F5F5F5"; c.cloudDark = "#E0E0E0"; c.cloudStroke = "#BDBDBD";
                    c.nightCloudLight = "#E8EAF6"; c.nightCloudDark = "#C5CAE9"; c.nightCloudStroke = "#9FA8DA";
                    c.rainDrop = "#B3D4FC"; c.nightRainDrop = "#90B4DE";
                    c.snowFlake = "#BBDEFB"; c.snowFlakeLight = "#E3F2FD";
                    c.stormCloudLight = "#B0B0B0"; c.stormCloudDark = "#909090";
                    c.lightningFill = "#FFF9C4"; c.lightningStroke = "#FFE082";
                    c.fogBar = "#D1C4E9"; c.windStroke = "#B2EBF2"; c.windDot = "#B2EBF2";
                    c.starColor = "#F3E5F5"; c.nightMoonFill = "#E1BEE7";
                    break;

                default: // DEFAULT - modern vibrant
                    c.sunCenter = "#FFEB3B"; c.sunMid = "#FFC107"; c.sunEdge = "#FF9800"; c.sunRay = "#FFB300";
                    c.moonCenter = "#FFF9C4"; c.moonMid = "#FFF176"; c.moonEdge = "#E0C970"; c.moonCrater = "#E8D86E";
                    c.cloudLight = "#F5F5F5"; c.cloudDark = "#CFD8DC"; c.cloudStroke = "#90A4AE";
                    c.nightCloudLight = "#CFD8DC"; c.nightCloudDark = "#90A4AE"; c.nightCloudStroke = "#607D8B";
                    c.rainDrop = "#42A5F5"; c.nightRainDrop = "#5C9DC9";
                    c.snowFlake = "#90CAF9"; c.snowFlakeLight = "#BBDEFB";
                    c.stormCloudLight = "#616161"; c.stormCloudDark = "#37474F";
                    c.lightningFill = "#FFD600"; c.lightningStroke = "#FFA000";
                    c.fogBar = "#B0BEC5"; c.windStroke = "#4FC3F7"; c.windDot = "#80DEEA";
                    c.starColor = "#FFFFFF"; c.nightMoonFill = "#E0D9A8";
                    break;
            }
            return c;
        }
    }

    // ============================================
    // DAY ICONS
    // ============================================

    private static String getSunIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String gid = "sunG_" + uid;
        String fid = "sunF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <radialGradient id="%s" cx="50%%" cy="45%%" r="50%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="60%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </radialGradient>
                    <filter id="%s">
                        <feGaussianBlur stdDeviation="3.5" result="blur"/>
                        <feComposite in="SourceGraphic" in2="blur" operator="over"/>
                    </filter>
                </defs>
                <!-- Outer glow ring -->
                <circle cx="50" cy="50" r="28" fill="none" stroke="%s" stroke-width="1" opacity="0.3"/>
                <!-- Rays -->
                <g opacity="0.85">
                    <line x1="50" y1="8" x2="50" y2="18" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="50" y1="82" x2="50" y2="92" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="8" y1="50" x2="18" y2="50" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="82" y1="50" x2="92" y2="50" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="19" y1="19" x2="26" y2="26" stroke="%s" stroke-width="2.8" stroke-linecap="round"/>
                    <line x1="74" y1="74" x2="81" y2="81" stroke="%s" stroke-width="2.8" stroke-linecap="round"/>
                    <line x1="81" y1="19" x2="74" y2="26" stroke="%s" stroke-width="2.8" stroke-linecap="round"/>
                    <line x1="26" y1="74" x2="19" y2="81" stroke="%s" stroke-width="2.8" stroke-linecap="round"/>
                </g>
                <!-- Sun body -->
                <circle cx="50" cy="50" r="22" fill="url(#%s)" filter="url(#%s)"/>
                <!-- Highlight -->
                <ellipse cx="44" cy="43" rx="8" ry="6" fill="white" opacity="0.35"/>
            </svg>
            """, size, size,
                gid, c.sunCenter, c.sunMid, c.sunEdge,
                fid,
                c.sunMid,
                c.sunRay, c.sunRay, c.sunRay, c.sunRay,
                c.sunRay, c.sunRay, c.sunRay, c.sunRay,
                gid, fid);
    }

    private static String getCloudyDayIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "cdG_" + uid;
        String sgid = "cdSG_" + uid;
        String cfid = "cdF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <radialGradient id="%s" cx="50%%" cy="45%%" r="50%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </radialGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="%s" flood-opacity="0.3"/>
                    </filter>
                </defs>
                <!-- Sun peeking -->
                <circle cx="72" cy="28" r="14" fill="url(#%s)"/>
                <g opacity="0.7">
                    <line x1="72" y1="8" x2="72" y2="14" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    <line x1="92" y1="28" x2="86" y2="28" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    <line x1="86" y1="14" x2="82" y2="18" stroke="%s" stroke-width="2" stroke-linecap="round"/>
                </g>
                <!-- Cloud body -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="58" rx="32" ry="16" fill="url(#%s)"/>
                    <ellipse cx="36" cy="50" rx="18" ry="18" fill="url(#%s)"/>
                    <ellipse cx="56" cy="44" rx="22" ry="20" fill="url(#%s)"/>
                    <ellipse cx="68" cy="54" rx="16" ry="14" fill="url(#%s)"/>
                </g>
                <!-- Cloud highlight -->
                <ellipse cx="48" cy="42" rx="12" ry="6" fill="white" opacity="0.45"/>
            </svg>
            """, size, size,
                cgid, c.cloudLight, c.cloudDark,
                sgid, c.sunCenter, c.sunMid,
                cfid, c.cloudStroke,
                sgid, c.sunRay, c.sunRay, c.sunRay,
                cfid, cgid, cgid, cgid, cgid);
    }

    private static String getRainDayIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "rdG_" + uid;
        String cfid = "rdF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="1.5" stdDeviation="1.5" flood-color="%s" flood-opacity="0.3"/>
                    </filter>
                </defs>
                <!-- Dim sun behind cloud -->
                <circle cx="72" cy="18" r="9" fill="%s" opacity="0.45"/>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="42" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="35" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="30" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="38" rx="15" ry="13" fill="url(#%s)"/>
                </g>
                <!-- Rain drops - teardrop shapes -->
                <g opacity="0.8">
                    <path d="M34 58 Q34 55 35.5 58 L34 66 Q32.5 58 34 58Z" fill="%s"/>
                    <path d="M48 60 Q48 57 49.5 60 L48 70 Q46.5 60 48 60Z" fill="%s"/>
                    <path d="M62 57 Q62 54 63.5 57 L62 65 Q60.5 57 62 57Z" fill="%s"/>
                </g>
                <g opacity="0.55">
                    <path d="M40 66 Q40 63 41.5 66 L40 74 Q38.5 66 40 66Z" fill="%s"/>
                    <path d="M55 65 Q55 62 56.5 65 L55 73 Q53.5 65 55 65Z" fill="%s"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.cloudLight, c.cloudDark,
                cfid, c.cloudStroke,
                c.sunMid,
                cfid, cgid, cgid, cgid, cgid,
                c.rainDrop, c.rainDrop, c.rainDrop,
                c.rainDrop, c.rainDrop);
    }

    private static String getSnowDayIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "sdG_" + uid;
        String cfid = "sdF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="1.5" stdDeviation="1.5" flood-color="%s" flood-opacity="0.25"/>
                    </filter>
                </defs>
                <!-- Pale sun -->
                <circle cx="72" cy="18" r="9" fill="%s" opacity="0.5"/>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="40" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="33" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="28" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="36" rx="15" ry="13" fill="url(#%s)"/>
                </g>
                <!-- Snowflakes - 6-pointed with center dot -->
                <g transform="translate(35,62)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-5" x2="0" y2="5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="-2.5" x2="4.3" y2="2.5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="2.5" x2="4.3" y2="-2.5" stroke-width="1.8"/>
                    <circle cx="0" cy="0" r="1.5" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(55,72)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-4.5" x2="0" y2="4.5" stroke-width="1.6"/>
                    <line x1="-3.9" y1="-2.25" x2="3.9" y2="2.25" stroke-width="1.6"/>
                    <line x1="-3.9" y1="2.25" x2="3.9" y2="-2.25" stroke-width="1.6"/>
                    <circle cx="0" cy="0" r="1.2" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(44,75)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
                <g transform="translate(66,66)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.cloudLight, c.cloudDark,
                cfid, c.cloudStroke,
                c.sunCenter,
                cfid, cgid, cgid, cgid, cgid,
                c.snowFlake, c.snowFlake,
                c.snowFlake, c.snowFlake,
                c.snowFlakeLight, c.snowFlakeLight);
    }

    // ============================================
    // NIGHT ICONS
    // ============================================

    private static String getMoonIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String gid = "moonG_" + uid;
        String fid = "moonF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <radialGradient id="%s" cx="35%%" cy="40%%" r="60%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="50%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </radialGradient>
                    <filter id="%s">
                        <feGaussianBlur stdDeviation="3" result="blur"/>
                        <feComposite in="SourceGraphic" in2="blur" operator="over"/>
                    </filter>
                </defs>
                <!-- Stars -->
                <circle cx="18" cy="18" r="1.5" fill="%s" opacity="0.9"/>
                <circle cx="82" cy="22" r="1" fill="%s" opacity="0.6"/>
                <circle cx="78" cy="72" r="1.3" fill="%s" opacity="0.7"/>
                <circle cx="22" cy="78" r="0.8" fill="%s" opacity="0.5"/>
                <circle cx="15" cy="50" r="1" fill="%s" opacity="0.4"/>
                <circle cx="88" cy="48" r="0.8" fill="%s" opacity="0.5"/>
                <!-- Moon crescent -->
                <path d="M 58 22 A 22 22 0 1 0 58 78 A 18 18 0 1 1 58 22 Z"
                      fill="url(#%s)" filter="url(#%s)"/>
                <!-- Subtle craters -->
                <circle cx="48" cy="42" r="2.5" fill="%s" opacity="0.3"/>
                <circle cx="55" cy="58" r="3" fill="%s" opacity="0.25"/>
                <circle cx="44" cy="62" r="1.8" fill="%s" opacity="0.2"/>
            </svg>
            """, size, size,
                gid, c.moonCenter, c.moonMid, c.moonEdge,
                fid,
                c.starColor, c.starColor, c.starColor, c.starColor, c.starColor, c.starColor,
                gid, fid,
                c.moonCrater, c.moonCrater, c.moonCrater);
    }

    private static String getCloudyNightIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String mgid = "cnMG_" + uid;
        String cgid = "cnCG_" + uid;
        String cfid = "cnCF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <radialGradient id="%s" cx="35%%" cy="40%%" r="60%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </radialGradient>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s" stop-opacity="0.95"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#263238" flood-opacity="0.35"/>
                    </filter>
                </defs>
                <!-- Stars -->
                <circle cx="12" cy="14" r="1.2" fill="%s" opacity="0.8"/>
                <circle cx="88" cy="18" r="0.8" fill="%s" opacity="0.5"/>
                <circle cx="90" cy="45" r="1" fill="%s" opacity="0.4"/>
                <!-- Moon crescent (small) -->
                <path d="M 74 16 A 10 10 0 1 0 74 36 A 8.5 8.5 0 1 1 74 16 Z"
                      fill="url(#%s)" opacity="0.9"/>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="58" rx="32" ry="16" fill="url(#%s)"/>
                    <ellipse cx="36" cy="50" rx="18" ry="18" fill="url(#%s)"/>
                    <ellipse cx="56" cy="44" rx="22" ry="20" fill="url(#%s)"/>
                    <ellipse cx="68" cy="54" rx="16" ry="14" fill="url(#%s)"/>
                </g>
            </svg>
            """, size, size,
                mgid, c.moonCenter, c.moonEdge,
                cgid, c.nightCloudLight, c.nightCloudDark,
                cfid,
                c.starColor, c.starColor, c.starColor,
                mgid,
                cfid, cgid, cgid, cgid, cgid);
    }

    private static String getRainNightIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "rnG_" + uid;
        String cfid = "rnF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="1.5" stdDeviation="1.5" flood-color="#263238" flood-opacity="0.35"/>
                    </filter>
                </defs>
                <!-- Star -->
                <circle cx="14" cy="14" r="1" fill="%s" opacity="0.6"/>
                <!-- Small moon -->
                <path d="M 72 13 A 7 7 0 1 0 72 27 A 6 6 0 1 1 72 13 Z"
                      fill="%s" opacity="0.6"/>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="40" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="33" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="28" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="36" rx="15" ry="13" fill="url(#%s)"/>
                </g>
                <!-- Rain drops -->
                <g opacity="0.75">
                    <path d="M34 56 Q34 53 35.5 56 L34 64 Q32.5 56 34 56Z" fill="%s"/>
                    <path d="M48 58 Q48 55 49.5 58 L48 68 Q46.5 58 48 58Z" fill="%s"/>
                    <path d="M62 55 Q62 52 63.5 55 L62 63 Q60.5 55 62 55Z" fill="%s"/>
                </g>
                <g opacity="0.5">
                    <path d="M40 64 Q40 61 41.5 64 L40 72 Q38.5 64 40 64Z" fill="%s"/>
                    <path d="M55 63 Q55 60 56.5 63 L55 71 Q53.5 63 55 63Z" fill="%s"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.nightCloudLight, c.nightCloudDark,
                cfid,
                c.starColor,
                c.nightMoonFill,
                cfid, cgid, cgid, cgid, cgid,
                c.nightRainDrop, c.nightRainDrop, c.nightRainDrop,
                c.nightRainDrop, c.nightRainDrop);
    }

    private static String getSnowNightIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "snG_" + uid;
        String cfid = "snF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="1.5" stdDeviation="1.5" flood-color="#263238" flood-opacity="0.3"/>
                    </filter>
                </defs>
                <!-- Stars -->
                <circle cx="14" cy="16" r="1.2" fill="%s" opacity="0.8"/>
                <circle cx="82" cy="20" r="0.8" fill="%s" opacity="0.5"/>
                <!-- Moon -->
                <path d="M 70 13 A 7 7 0 1 0 70 27 A 6 6 0 1 1 70 13 Z"
                      fill="%s" opacity="0.65"/>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="40" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="33" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="28" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="36" rx="15" ry="13" fill="url(#%s)"/>
                </g>
                <!-- Snowflakes - 6-pointed with center dot -->
                <g transform="translate(35,60)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-5" x2="0" y2="5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="-2.5" x2="4.3" y2="2.5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="2.5" x2="4.3" y2="-2.5" stroke-width="1.8"/>
                    <circle cx="0" cy="0" r="1.5" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(55,70)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-4.5" x2="0" y2="4.5" stroke-width="1.6"/>
                    <line x1="-3.9" y1="-2.25" x2="3.9" y2="2.25" stroke-width="1.6"/>
                    <line x1="-3.9" y1="2.25" x2="3.9" y2="-2.25" stroke-width="1.6"/>
                    <circle cx="0" cy="0" r="1.2" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(44,73)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
                <g transform="translate(66,64)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.nightCloudLight, c.nightCloudDark,
                cfid,
                c.starColor, c.starColor,
                c.nightMoonFill,
                cfid, cgid, cgid, cgid, cgid,
                c.snowFlake, c.snowFlake,
                c.snowFlake, c.snowFlake,
                c.snowFlakeLight, c.snowFlakeLight);
    }

    // ============================================
    // DAY/NIGHT NEUTRAL ICONS
    // ============================================

    private static String getThunderstormIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "tsG_" + uid;
        String cfid = "tsF_" + uid;
        String lfid = "tsLF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#1a1a1a" flood-opacity="0.5"/>
                    </filter>
                    <filter id="%s">
                        <feGaussianBlur stdDeviation="2.5" result="blur"/>
                        <feComposite in="SourceGraphic" in2="blur" operator="over"/>
                    </filter>
                </defs>
                <!-- Dark cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="35" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="28" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="22" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="30" rx="15" ry="14" fill="url(#%s)"/>
                </g>
                <!-- Lightning bolt -->
                <path d="M 54 42 L 46 58 L 52 58 L 44 78 L 60 54 L 53 54 Z"
                      fill="%s" stroke="%s" stroke-width="0.8"
                      filter="url(#%s)"/>
                <!-- Light rain -->
                <g opacity="0.5">
                    <path d="M33 48 Q33 45 34.5 48 L33 56 Q31.5 48 33 48Z" fill="%s"/>
                    <path d="M67 46 Q67 43 68.5 46 L67 54 Q65.5 46 67 46Z" fill="%s"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.stormCloudLight, c.stormCloudDark,
                cfid,
                lfid,
                cfid, cgid, cgid, cgid, cgid,
                c.lightningFill, c.lightningStroke, lfid,
                c.rainDrop, c.rainDrop);
    }

    private static String getSnowIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String cgid = "sG_" + uid;
        String cfid = "sF_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" stop-color="%s"/>
                        <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <filter id="%s">
                        <feDropShadow dx="0" dy="1.5" stdDeviation="1.5" flood-color="%s" flood-opacity="0.25"/>
                    </filter>
                </defs>
                <!-- Cloud -->
                <g filter="url(#%s)">
                    <ellipse cx="50" cy="38" rx="32" ry="15" fill="url(#%s)"/>
                    <ellipse cx="36" cy="31" rx="18" ry="17" fill="url(#%s)"/>
                    <ellipse cx="56" cy="26" rx="22" ry="19" fill="url(#%s)"/>
                    <ellipse cx="68" cy="34" rx="15" ry="13" fill="url(#%s)"/>
                </g>
                <!-- Snowflakes - 6-pointed with center dot -->
                <g transform="translate(35,60)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-5" x2="0" y2="5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="-2.5" x2="4.3" y2="2.5" stroke-width="1.8"/>
                    <line x1="-4.3" y1="2.5" x2="4.3" y2="-2.5" stroke-width="1.8"/>
                    <circle cx="0" cy="0" r="1.5" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(55,70)" stroke="%s" stroke-linecap="round">
                    <line x1="0" y1="-4.5" x2="0" y2="4.5" stroke-width="1.6"/>
                    <line x1="-3.9" y1="-2.25" x2="3.9" y2="2.25" stroke-width="1.6"/>
                    <line x1="-3.9" y1="2.25" x2="3.9" y2="-2.25" stroke-width="1.6"/>
                    <circle cx="0" cy="0" r="1.2" fill="%s" stroke="none"/>
                </g>
                <g transform="translate(44,75)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
                <g transform="translate(66,64)" stroke="%s" stroke-linecap="round" opacity="0.7">
                    <line x1="0" y1="-3" x2="0" y2="3" stroke-width="1.3"/>
                    <line x1="-2.6" y1="-1.5" x2="2.6" y2="1.5" stroke-width="1.3"/>
                    <line x1="-2.6" y1="1.5" x2="2.6" y2="-1.5" stroke-width="1.3"/>
                </g>
            </svg>
            """, size, size,
                cgid, c.cloudLight, c.cloudDark,
                cfid, c.cloudStroke,
                cfid, cgid, cgid, cgid, cgid,
                c.snowFlake, c.snowFlake,
                c.snowFlake, c.snowFlake,
                c.snowFlakeLight, c.snowFlakeLight);
    }

    private static String getFogIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String gid = "fogG_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="100%%" y2="0%%">
                        <stop offset="0%%" stop-color="%s" stop-opacity="0.2"/>
                        <stop offset="30%%" stop-color="%s" stop-opacity="0.8"/>
                        <stop offset="70%%" stop-color="%s" stop-opacity="0.8"/>
                        <stop offset="100%%" stop-color="%s" stop-opacity="0.2"/>
                    </linearGradient>
                </defs>
                <!-- Dim sun at top -->
                <circle cx="50" cy="18" r="10" fill="%s" opacity="0.35"/>
                <!-- Fog layers -->
                <rect x="14" y="32" width="72" height="5" rx="2.5" fill="url(#%s)" opacity="0.55"/>
                <rect x="18" y="44" width="64" height="5.5" rx="2.75" fill="url(#%s)" opacity="0.7"/>
                <rect x="12" y="56" width="76" height="5.5" rx="2.75" fill="url(#%s)" opacity="0.8"/>
                <rect x="20" y="68" width="60" height="5" rx="2.5" fill="url(#%s)" opacity="0.6"/>
                <rect x="16" y="80" width="68" height="4.5" rx="2.25" fill="url(#%s)" opacity="0.4"/>
            </svg>
            """, size, size,
                gid, c.fogBar, c.fogBar, c.fogBar, c.fogBar,
                c.sunCenter,
                gid, gid, gid, gid, gid);
    }

    private static String getWindIcon(int size, IconTheme theme) {
        ThemeColors c = ThemeColors.getColors(theme);
        int uid = (int)(Math.random() * 1000000);
        String gid = "windG_" + uid;

        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 100 100">
                <defs>
                    <linearGradient id="%s" x1="0%%" y1="0%%" x2="100%%" y2="0%%">
                        <stop offset="0%%" stop-color="%s" stop-opacity="0.2"/>
                        <stop offset="50%%" stop-color="%s" stop-opacity="0.9"/>
                        <stop offset="100%%" stop-color="%s" stop-opacity="0.2"/>
                    </linearGradient>
                </defs>
                <!-- Wind lines with curls -->
                <path d="M 12 28 Q 40 22 60 28 Q 75 32 82 24 A 6 6 0 0 1 82 36 Q 72 32 60 33"
                      stroke="url(#%s)" stroke-width="3.5" fill="none" stroke-linecap="round"/>
                <path d="M 8 48 Q 30 42 55 48 Q 70 52 78 44 A 5 5 0 0 1 78 54 Q 68 50 55 52"
                      stroke="url(#%s)" stroke-width="3" fill="none" stroke-linecap="round" opacity="0.85"/>
                <path d="M 18 68 Q 45 62 65 68 Q 80 72 88 64 A 5 5 0 0 1 88 74 Q 78 70 65 72"
                      stroke="url(#%s)" stroke-width="3.5" fill="none" stroke-linecap="round"/>
                <!-- Small particles -->
                <circle cx="25" cy="38" r="1.5" fill="%s" opacity="0.4"/>
                <circle cx="45" cy="58" r="1" fill="%s" opacity="0.3"/>
                <circle cx="70" cy="78" r="1.2" fill="%s" opacity="0.35"/>
            </svg>
            """, size, size,
                gid, c.windDot, c.windStroke, c.windDot,
                gid, gid, gid,
                c.windDot, c.windDot, c.windDot);
    }

    /**
     * Get background image URL based on weather and city (for optional background feature)
     */
    public static String getBackgroundImageUrl(String weatherType, String city) {
        // Using Unsplash API for free images (requires no API key for basic usage)
        String query = weatherType.toLowerCase() + " " + city;
        return String.format("https://source.unsplash.com/1600x900/?%s,weather",
                query.replace(" ", ","));
    }
}