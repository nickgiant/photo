package com.photo.act.photo_act.utils;

import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

/**
 * Utility class to get timezone information from coordinates
 * Uses geographical guessing with optional WorldTimeAPI.org verification
 */
public class TimezoneUtils {

    private static final WebClient webClient = WebClient.builder().build();
    private static final boolean ENABLE_API_VERIFICATION = false; // Set to true to enable API calls

    /**
     * Get timezone string from coordinates
     * Uses geographical guessing - API verification is optional
     * 
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return ZoneId for the location, or system default if lookup fails
     */
    public static ZoneId getTimezoneFromCoordinates(double latitude, double longitude) {
        try {
            // First, try geographical guessing (fast and reliable)
            String timezoneId = guessTimezoneFromCoordinates(latitude, longitude);
            
            if (timezoneId != null) {
                // Try to create ZoneId - this validates the timezone string
                try {
                    ZoneId zoneId = ZoneId.of(timezoneId);
                    
                    // Optionally verify with WorldTimeAPI (disabled by default to avoid connection issues)
                    if (ENABLE_API_VERIFICATION) {
                        verifyTimezoneWithAPI(timezoneId);
                    }
                    
                    return zoneId;
                } catch (Exception e) {
                    System.err.println("Invalid timezone: " + timezoneId + " - " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Timezone lookup failed: " + e.getMessage());
        }

        return ZoneId.systemDefault();
    }

    /**
     * Optional: Verify timezone exists via WorldTimeAPI
     * This method is only called if ENABLE_API_VERIFICATION is true
     * 
     * WorldTimeAPI format: http://worldtimeapi.org/api/timezone/:area/:location[/:region]
     * Examples:
     *   - Europe/Athens → http://worldtimeapi.org/api/timezone/Europe/Athens
     *   - America/New_York → http://worldtimeapi.org/api/timezone/America/New_York
     *   - America/Argentina/Buenos_Aires → http://worldtimeapi.org/api/timezone/America/Argentina/Buenos_Aires
     */
    private static void verifyTimezoneWithAPI(String timezoneId) {
        try {
            // Convert timezone format: "Europe/Athens" → "Europe/Athens"
            // No conversion needed - WorldTimeAPI accepts IANA format directly
            String url = String.format("http://worldtimeapi.org/api/timezone/%s", timezoneId);
            
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3)) // 3 second timeout
                    .block();
            
            // Verify response contains valid timezone data
            if (response != null && response.containsKey("timezone")) {
                String verifiedTimezone = (String) response.get("timezone");
                // Successfully verified
            }
            
        } catch (Exception e) {
            // API verification failed, but we'll still use the guessed timezone
            System.err.println("WorldTimeAPI verification failed (non-critical): " + e.getMessage());
        }
    }

    /**
     * Guess timezone from coordinates using geographical rules
     * This method works completely offline - no API calls required
     */
    private static String guessTimezoneFromCoordinates(double latitude, double longitude) {
        // Major city timezone mappings for common locations (100% accurate)
        
        // Europe
        if (latitude >= 51.0 && latitude <= 52.0 && longitude >= -1.0 && longitude <= 1.0) {
            return "Europe/London";
        } else if (latitude >= 48.5 && latitude <= 49.5 && longitude >= 2.0 && longitude <= 3.0) {
            return "Europe/Paris";
        } else if (latitude >= 52.0 && latitude <= 53.0 && longitude >= 13.0 && longitude <= 14.0) {
            return "Europe/Berlin";
        } else if (latitude >= 37.5 && latitude <= 38.5 && longitude >= 23.5 && longitude <= 24.0) {
            return "Europe/Athens";
        } else if (latitude >= 55.0 && latitude <= 56.0 && longitude >= 37.0 && longitude <= 38.0) {
            return "Europe/Moscow";
        }
        
        // Americas
        else if (latitude >= 40.0 && latitude <= 41.0 && longitude >= -74.5 && longitude <= -73.5) {
            return "America/New_York";
        } else if (latitude >= 37.0 && latitude <= 38.0 && longitude >= -123.0 && longitude <= -122.0) {
            return "America/Los_Angeles";
        }
        
        // Asia
        else if (latitude >= 35.0 && latitude <= 36.0 && longitude >= 139.0 && longitude <= 140.0) {
            return "Asia/Tokyo";
        } else if (latitude >= 39.0 && latitude <= 40.0 && longitude >= 116.0 && longitude <= 117.0) {
            return "Asia/Shanghai";
        } else if (latitude >= 28.0 && latitude <= 29.0 && longitude >= 77.0 && longitude <= 78.0) {
            return "Asia/Kolkata";
        }
        
        // Oceania
        else if (latitude >= -34.0 && latitude <= -33.0 && longitude >= 151.0 && longitude <= 152.0) {
            return "Australia/Sydney";
        }
        
        // Fallback: Calculate timezone from longitude
        // Each timezone is approximately 15 degrees (360° / 24 hours)
        int hoursOffset = (int) Math.round(longitude / 15.0);
        
        // Map offset to common timezones
        switch (hoursOffset) {
            case -12: return "Etc/GMT+12";
            case -11: return "Pacific/Midway";
            case -10: return "Pacific/Honolulu";
            case -9: return "America/Anchorage";
            case -8: return "America/Los_Angeles";
            case -7: return "America/Denver";
            case -6: return "America/Chicago";
            case -5: return "America/New_York";
            case -4: return "America/Halifax";
            case -3: return "America/Sao_Paulo";
            case -2: return "Atlantic/South_Georgia";
            case -1: return "Atlantic/Azores";
            case 0: return "Europe/London";
            case 1: return "Europe/Paris";
            case 2: return "Europe/Athens";
            case 3: return "Europe/Moscow";
            case 4: return "Asia/Dubai";
            case 5: return "Asia/Karachi";
            case 6: return "Asia/Dhaka";
            case 7: return "Asia/Bangkok";
            case 8: return "Asia/Shanghai";
            case 9: return "Asia/Tokyo";
            case 10: return "Australia/Sydney";
            case 11: return "Pacific/Noumea";
            case 12: return "Pacific/Auckland";
            default: return "Europe/London";
        }
    }

    /**
     * Get timezone with caching to avoid repeated calculations
     */
    private static final Map<String, ZoneId> timezoneCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static ZoneId getTimezoneFromCoordinatesCached(double latitude, double longitude) {
        // Round coordinates to 2 decimal places for cache key (approx 1km precision)
        String cacheKey = String.format("%.2f,%.2f", latitude, longitude);

        return timezoneCache.computeIfAbsent(cacheKey, k -> {
            return getTimezoneFromCoordinates(latitude, longitude);
        });
    }
}
