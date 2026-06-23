package com.photo.act.photo_act.services;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.Coordinate;
import com.github.prominence.openweathermap.api.model.forecast.Forecast;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import com.github.prominence.openweathermap.api.model.weather.Weather;

import com.photo.act.photo_act.model.WeatherData;
import com.photo.act.photo_act.utils.TimezoneUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public WeatherService() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Get current weather by city and country
     */
    public WeatherData.CurrentWeather getCurrentWeather(String city, String country) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Weather weather = client.currentWeather()
                    .single()
                    .byCityName(city + "," + country)
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();

            return mapToCurrentWeather(weather);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch current weather: " + e.getMessage(), e);
        }
    }

    /**
     * Get current weather by coordinates
     */
    public WeatherData.CurrentWeather getCurrentWeather(double latitude, double longitude) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Weather weather = client.currentWeather()
                    .single()
                    .byCoordinate(Coordinate.of(latitude, longitude))
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();

            return mapToCurrentWeather(weather);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch current weather: " + e.getMessage(), e);
        }
    }

    /**
     * Get 5-day forecast with 3-hour intervals by city and country
     */
    public List<WeatherData.DailyForecast> getFiveDayForecast(String city, String country) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Forecast forecast = client.forecast5Day3HourStep()
                    .byCityName(city + "," + country)
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .count(40) // 5 days * 8 forecasts per day
                    .retrieve()
                    .asJava();

            return processForecastToDaily(forecast);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch forecast: " + e.getMessage(), e);
        }
    }

    /**
     * Get 5-day forecast with 3-hour intervals by coordinates
     */
    public List<WeatherData.DailyForecast> getFiveDayForecast(double latitude, double longitude) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Forecast forecast = client.forecast5Day3HourStep()
                    .byCoordinate(Coordinate.of(latitude, longitude))
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .count(40)
                    .retrieve()
                    .asJava();

            return processForecastToDaily(forecast);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch forecast: " + e.getMessage(), e);
        }
    }

    /**
     * Get hourly forecast for a specific date
     */
    public List<WeatherData.HourlyForecast> getHourlyForecast(String city, String country, LocalDate date) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Forecast forecast = client.forecast5Day3HourStep()
                    .byCityName(city + "," + country)
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .count(40)
                    .retrieve()
                    .asJava();

            return processForecastToHourly(forecast, date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch hourly forecast: " + e.getMessage(), e);
        }
    }

    /**
     * Get hourly forecast for a specific date by coordinates
     */
    public List<WeatherData.HourlyForecast> getHourlyForecast(double latitude, double longitude, LocalDate date) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Forecast forecast = client.forecast5Day3HourStep()
                    .byCoordinate(Coordinate.of(latitude, longitude))
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .count(40)
                    .retrieve()
                    .asJava();

            return processForecastToHourly(forecast, date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch hourly forecast: " + e.getMessage(), e);
        }
    }

    /**
     * Get location data by city and country
     */
    public WeatherData.LocationData getLocationData(String city, String country) {
        try {
            OpenWeatherMapClient client = new OpenWeatherMapClient(apiKey);

            Weather weather = client.currentWeather()
                    .single()
                    .byCityName(city + "," + country)
                    .retrieve()
                    .asJava();

            WeatherData.LocationData locationData = new WeatherData.LocationData();
            locationData.setCity(city);
            locationData.setCountry(country);
            double lat = weather.getLocation().getCoordinate().getLatitude();
            double lon = weather.getLocation().getCoordinate().getLongitude();
            locationData.setLatitude(lat);
            locationData.setLongitude(lon);

            // Get timezone from coordinates
            ZoneId zoneId = TimezoneUtils.getTimezoneFromCoordinatesCached(lat, lon);
            locationData.setTimezone(zoneId.getId());

            // Try to get elevation from elevation API (open-meteo is free)
            try {
                int elevation = getElevation(lat, lon);
                locationData.setElevation(elevation);
            } catch (Exception e) {
                locationData.setElevation(0);
            }

            return locationData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch location data: " + e.getMessage(), e);
        }
    }

    /**
     * Get sunrise and sunset data from sunrise-sunset.org API
     *
     * @param latitude  Location latitude
     * @param longitude Location longitude
     * @param date      Date for which to get sun data
     * @param timezone  Timezone of the location (e.g., "Europe/London")
     */
    public WeatherData.SunData getSunData(double latitude, double longitude, LocalDate date, String timezone) {
        try {
            String url = String.format(
                    "https://api.sunrise-sunset.org/json?lat=%f&lng=%f&date=%s&formatted=0",
                    latitude, longitude, date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            WeatherData.SunData sunData = new WeatherData.SunData();

            if (response != null && "OK".equals(response.get("status"))) {
                Map<String, String> results = (Map<String, String>) response.get("results");

                ZoneId zoneId = timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();


                sunData.setSunrise(parseUtcDateTime((String) results.get("sunrise"), zoneId));
                sunData.setSunset(parseUtcDateTime((String) results.get("sunset"), zoneId));
                sunData.setSolarNoon(parseUtcDateTime((String) results.get("solar_noon"), zoneId));

                Object dayLength = results.get("day_length");
                sunData.setDayLength(dayLength != null ? String.valueOf(dayLength) : "0");

                sunData.setCivilTwilightBegin(parseUtcDateTime((String) results.get("civil_twilight_begin"), zoneId));
                sunData.setCivilTwilightEnd(parseUtcDateTime((String) results.get("civil_twilight_end"), zoneId));
            }

            return sunData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch sun data: " + e.getMessage(), e);
        }
    }

    /**
     * Overloaded version for backward compatibility - uses system default timezone
     */
    public WeatherData.SunData getSunData(double latitude, double longitude, LocalDate date) {
        return getSunData(latitude, longitude, date, null);
    }

    /**
     * Get elevation from open-meteo API (free)
     */
    private int getElevation(double latitude, double longitude) {
        try {
            String url = String.format(
                    "https://api.open-meteo.com/v1/elevation?latitude=%f&longitude=%f",
                    latitude, longitude
            );

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("elevation")) {
                List<Double> elevations = (List<Double>) response.get("elevation");
                return elevations.get(0).intValue();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Helper methods

    private WeatherData.CurrentWeather mapToCurrentWeather(Weather weather) {
        // Get timezone from coordinates using free API
        ZoneId zoneId = TimezoneUtils.getTimezoneFromCoordinatesCached(
                weather.getLocation().getCoordinate().getLatitude(),
                weather.getLocation().getCoordinate().getLongitude()
        );

        return new WeatherData.CurrentWeather(
                weather.getTemperature().getValue(),
                weather.getTemperature().getFeelsLike(),
                weather.getHumidity().getValue(),
                weather.getWind().getSpeed(),
                Integer.parseInt(String.valueOf(Math.round(weather.getAtmosphericPressure().getSeaLevelValue()))),
                weather.getWeatherState().getDescription(),
                weather.getWeatherState().getName(),
                weather.getClouds().getValue(),
                0,
//                weather.getVisibility().getValue() / 1000.0, // Convert to km
                weather.getCalculationTime().atZone(zoneId)
        );
    }

    private List<WeatherData.DailyForecast> processForecastToDaily(Forecast forecast) {
        try {
            // Get timezone from coordinates using free API
            ZoneId zoneId = TimezoneUtils.getTimezoneFromCoordinatesCached(
                    forecast.getLocation().getCoordinate().getLatitude(),
                    forecast.getLocation().getCoordinate().getLongitude()
            );

            Map<LocalDate, List<WeatherForecast>> groupedByDay = forecast.getWeatherForecasts()
                    .stream()
                    .collect(Collectors.groupingBy(wf -> {
                        try {
                            return wf.getForecastTime().atZone(zoneId).toLocalDate();
                        } catch (Exception e) {
                            System.err.println("Error converting forecast time: " + e.getMessage());
                            // Fallback to system default timezone
                            return LocalDateTime.ofInstant(wf.getForecastTime().atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()).toLocalDate();
                        }
                    }));

            return groupedByDay.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        LocalDate date = entry.getKey();
                        List<WeatherForecast> dayForecasts = entry.getValue();

                        double minTemp = dayForecasts.stream()
                                .mapToDouble(wf -> wf.getTemperature().getValue())
                                .min().orElse(0.0);
                        double maxTemp = dayForecasts.stream()
                                .mapToDouble(wf -> wf.getTemperature().getValue())
                                .max().orElse(0.0);

                        // Use the most common weather description
                        String description = dayForecasts.get(dayForecasts.size() / 2)
                                .getWeatherState().getDescription();
                        String weatherType = dayForecasts.get(dayForecasts.size() / 2)
                                .getWeatherState().getName();

                        int avgHumidity = (int) dayForecasts.stream()
                                .mapToInt(wf -> wf.getHumidity().getValue())
                                .average().orElse(0);

                        double avgWindSpeed = dayForecasts.stream()
                                .mapToDouble(wf -> wf.getWind().getSpeed())
                                .average().orElse(0);

                        int avgCloudiness = (int) dayForecasts.stream()
                                .mapToInt(wf -> wf.getClouds().getValue())
                                .average().orElse(0);

                        // Try to get sunrise/sunset for this date
                        LocalDateTime sunrise = null;
                        LocalDateTime sunset = null;
                        try {
                            // Get coordinates from the forecast
                            double lat = forecast.getLocation().getCoordinate().getLatitude();
                            double lon = forecast.getLocation().getCoordinate().getLongitude();
                            // Use the timezone ID string from our lookup
                            WeatherData.SunData sunData = getSunData(lat, lon, date, zoneId.getId());
                            sunrise = sunData.getSunrise();
                            sunset = sunData.getSunset();
                        } catch (Exception e) {
                            // If sun data fails, leave as null
                            System.err.println("Error getting sun data for " + date + ": " + e.getMessage());
                        }

                        return new WeatherData.DailyForecast(
                                date, minTemp, maxTemp, description, weatherType,
                                avgHumidity, avgWindSpeed, avgCloudiness, sunrise, sunset
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in processForecastToDaily: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<WeatherData.HourlyForecast> processForecastToHourly(Forecast forecast, LocalDate targetDate) {
        // Get timezone from coordinates using free API
        try {
        ZoneId zoneId = TimezoneUtils.getTimezoneFromCoordinatesCached(
                forecast.getLocation().getCoordinate().getLatitude(),
                forecast.getLocation().getCoordinate().getLongitude()
        );

        return forecast.getWeatherForecasts().stream()
                .filter(wf -> {
                    try {
                        LocalDate date = wf.getForecastTime().atZone(zoneId).toLocalDate();
                        return date.equals(targetDate);
                    } catch (Exception e) {
                        // Fallback to system default timezone
                        try {
                            LocalDate date = wf.getForecastTime().atZone(ZoneId.systemDefault()).toLocalDate();
                            return date.equals(targetDate);
                        } catch (Exception e2) {
                            return false; // Skip this forecast item
                        }
                    }
                })
                .map(wf -> {
                    try {
                        return new WeatherData.HourlyForecast(
                                wf.getForecastTime().atZone(zoneId).toLocalDateTime(),
                                wf.getTemperature().getValue(),
                                wf.getTemperature().getFeelsLike(),
                                wf.getWeatherState().getDescription(),
                                wf.getWeatherState().getName(),
                                wf.getHumidity().getValue(),
                                wf.getWind().getSpeed(),
                                wf.getClouds().getValue(),
                                wf.getRain() != null ? wf.getRain().getThreeHourLevel() : 0.0
                        );
                    } catch (Exception e) {
                        System.err.println("Error creating hourly forecast: " + e.getMessage());
                        // Fallback to system default timezone
                        return new WeatherData.HourlyForecast(
                                wf.getForecastTime().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                                wf.getTemperature().getValue(),
                                wf.getTemperature().getFeelsLike(),
                                wf.getWeatherState().getDescription(),
                                wf.getWeatherState().getName(),
                                wf.getHumidity().getValue(),
                                wf.getWind().getSpeed(),
                                wf.getClouds().getValue(),
                                wf.getRain() != null ? wf.getRain().getThreeHourLevel() : 0.0
                        );
                    }
                })
                .collect(Collectors.toList());


    } catch (Exception e) {
        System.err.println("Error in processForecastToHourly: " + e.getMessage());
        e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Search locations by name using OpenStreetMap Nominatim API.
     * Returns list of [displayName, lat, lon] arrays.
     */
    public List<String[]> searchMapLocationsByName(String query) {
        List<String[]> results = new ArrayList<>();
        try {
            String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + encoded
                    + "&format=json&limit=5&addressdetails=0&accept-language=en";

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(
                    webClient.get()
                            .uri(url)
                            .header("User-Agent", "PhotoActApp/1.0")
                            .header("Accept-Language", "en")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block()
            );
            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                String displayName = node.path("display_name").asText("");
                String lat = node.path("lat").asText("");
                String lon = node.path("lon").asText("");
                if (!lat.isEmpty() && !lon.isEmpty() && !displayName.isEmpty()) {
                    results.add(new String[]{displayName, lat, lon});
                }
            }
        } catch (Exception e) {
            // return empty list so caller can show "not found" message
        }
        return results;
    }

    /**
     * Search locations by name using OpenWeatherMap Geocoding API.
     * Returns list of [displayName, lat, lon] arrays.
     */
    public List<String[]> searchLocationsByName(String query) {
        List<String[]> results = new ArrayList<>();
        try {
            String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String url = "http://api.openweathermap.org/geo/1.0/direct?q=" + encoded + "&limit=5&appid=" + apiKey;

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block()
            );
            for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                // Prefer English name from local_names, fall back to default name
                String enName = node.path("local_names").path("en").asText("");
                String name = enName.isEmpty() ? node.path("name").asText("") : enName;
                String country = node.path("country").asText("");
                String state = node.path("state").asText("");
                String lat = node.path("lat").asText("");
                String lon = node.path("lon").asText("");
                if (!lat.isEmpty() && !lon.isEmpty()) {
                    StringBuilder displayName = new StringBuilder(name);
                    if (!state.isEmpty()) displayName.append(", ").append(state);
                    if (!country.isEmpty()) displayName.append(", ").append(country);
                    results.add(new String[]{displayName.toString(), lat, lon});
                }
            }
        } catch (Exception e) {
            // log but return empty list so caller can show "no weather data" message
        }
        return results;
    }

    private LocalDateTime parseUtcDateTime(String isoDateTime, ZoneId targetZone) {
        return ZonedDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(targetZone)
                .toLocalDateTime();
    }
}