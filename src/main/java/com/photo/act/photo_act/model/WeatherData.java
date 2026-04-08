package com.photo.act.photo_act.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class WeatherData {
    
    // Current weather data
    public static class CurrentWeather {
        private double temperature;
        private double feelsLike;
        private int humidity;
        private double windSpeed;
        private int pressure;
        private String description;
        private String weatherType;
        private int cloudiness;
        private double visibility;
        private ZonedDateTime dateTime;
        
        public CurrentWeather() {}
        
        public CurrentWeather(double temperature, double feelsLike, int humidity, 
                            double windSpeed, int pressure, String description, 
                            String weatherType, int cloudiness, double visibility,
                            ZonedDateTime dateTime) {
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.pressure = pressure;
            this.description = description;
            this.weatherType = weatherType;
            this.cloudiness = cloudiness;
            this.visibility = visibility;
            this.dateTime = dateTime;
        }

        // Getters and Setters
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public double getFeelsLike() { return feelsLike; }
        public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
        
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public int getPressure() { return pressure; }
        public void setPressure(int pressure) { this.pressure = pressure; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getWeatherType() { return weatherType; }
        public void setWeatherType(String weatherType) { this.weatherType = weatherType; }
        
        public int getCloudiness() { return cloudiness; }
        public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }
        
        public double getVisibility() { return visibility; }
        public void setVisibility(double visibility) { this.visibility = visibility; }
        
        public ZonedDateTime getDateTime() { return dateTime; }
        public void setDateTime(ZonedDateTime dateTime) { this.dateTime = dateTime; }
    }
    
    // Daily forecast data
    public static class DailyForecast {
        private LocalDate date;
        private double minTemp;
        private double maxTemp;
        private String description;
        private String weatherType;
        private int humidity;
        private double windSpeed;
        private int cloudiness;
        private LocalDateTime sunrise;
        private LocalDateTime sunset;
        
        public DailyForecast() {}
        
        public DailyForecast(LocalDate date, double minTemp, double maxTemp, 
                           String description, String weatherType, int humidity,
                           double windSpeed, int cloudiness, LocalDateTime sunrise,
                           LocalDateTime sunset) {
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.description = description;
            this.weatherType = weatherType;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.cloudiness = cloudiness;
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }
        
        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getWeatherType() { return weatherType; }
        public void setWeatherType(String weatherType) { this.weatherType = weatherType; }
        
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public int getCloudiness() { return cloudiness; }
        public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }
        
        public LocalDateTime getSunrise() { return sunrise; }
        public void setSunrise(LocalDateTime sunrise) { this.sunrise = sunrise; }
        
        public LocalDateTime getSunset() { return sunset; }
        public void setSunset(LocalDateTime sunset) { this.sunset = sunset; }
    }
    
    // Hourly forecast data
    public static class HourlyForecast {
        private LocalDateTime dateTime;
        private double temperature;
        private double feelsLike;
        private String description;
        private String weatherType;
        private int humidity;
        private double windSpeed;
        private int cloudiness;
        private double precipitationProbability;
        
        public HourlyForecast() {}
        
        public HourlyForecast(LocalDateTime dateTime, double temperature, double feelsLike,
                            String description, String weatherType, int humidity,
                            double windSpeed, int cloudiness, double precipitationProbability) {
            this.dateTime = dateTime;
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.description = description;
            this.weatherType = weatherType;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.cloudiness = cloudiness;
            this.precipitationProbability = precipitationProbability;
        }

        // Getters and Setters
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
        
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public double getFeelsLike() { return feelsLike; }
        public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getWeatherType() { return weatherType; }
        public void setWeatherType(String weatherType) { this.weatherType = weatherType; }
        
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public int getCloudiness() { return cloudiness; }
        public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }
        
        public double getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(double precipitationProbability) { 
            this.precipitationProbability = precipitationProbability; 
        }
    }
    
    // Location data
    public static class LocationData {
        private String city;
        private String country;
        private double latitude;
        private double longitude;
        private int elevation;
        private String timezone;
        
        public LocationData() {}
        
        public LocationData(String city, String country, double latitude, 
                          double longitude, int elevation, String timezone) {
            this.city = city;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.timezone = timezone;
        }

        // Getters and Setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public int getElevation() { return elevation; }
        public void setElevation(int elevation) { this.elevation = elevation; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }
    
    // Sunrise/Sunset data
    public static class SunData {
        private LocalDateTime sunrise;
        private LocalDateTime sunset;
        private LocalDateTime solarNoon;
        private String dayLength;
        private LocalDateTime civilTwilightBegin;
        private LocalDateTime civilTwilightEnd;
        
        public SunData() {}

        // Getters and Setters
        public LocalDateTime getSunrise() { return sunrise; }
        public void setSunrise(LocalDateTime sunrise) { this.sunrise = sunrise; }
        
        public LocalDateTime getSunset() { return sunset; }
        public void setSunset(LocalDateTime sunset) { this.sunset = sunset; }
        
        public LocalDateTime getSolarNoon() { return solarNoon; }
        public void setSolarNoon(LocalDateTime solarNoon) { this.solarNoon = solarNoon; }
        
        public String getDayLength() { return dayLength; }
        public void setDayLength(String dayLength) { this.dayLength = dayLength; }
        
        public LocalDateTime getCivilTwilightBegin() { return civilTwilightBegin; }
        public void setCivilTwilightBegin(LocalDateTime civilTwilightBegin) { 
            this.civilTwilightBegin = civilTwilightBegin; 
        }
        
        public LocalDateTime getCivilTwilightEnd() { return civilTwilightEnd; }
        public void setCivilTwilightEnd(LocalDateTime civilTwilightEnd) { 
            this.civilTwilightEnd = civilTwilightEnd; 
        }
    }
}
