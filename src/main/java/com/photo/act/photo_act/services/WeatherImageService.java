package com.photo.act.photo_act.services;

public class WeatherImageService {

        /**
         * Returns the image URL based on the weather data.
         *
         * @param weatherData an array of weather data
         * @return the image URL
         */
        public String weatherImage(String[] weatherData) {
            String weatherType = weatherData[6];
            Integer comparedTime = Integer.valueOf(weatherData[8]);
            Integer sunrise = Integer.valueOf(weatherData[9]);
            Integer sunset = Integer.valueOf(weatherData[10]);;
            return imageReturn(comparedTime, sunrise, sunset, weatherType);
        }

        /**
         * Handles the selection of an image based on the weather data and time.
         *
         * @param weatherData the array of weather data containing the time, temperature, humidity, and weather type
         * @param sunrise the time of sunrise
         * @param sunset the time of sunset
         * @return the selected image based on the compared time, sunrise time, sunset time, and weather type
         */
        public String hourlyImageHandler(String[] weatherData, String sunrise, String sunset) {
            String weatherType = weatherData[3];
            Integer comparedTime = Integer.valueOf(weatherData[0]);
            Integer sunriseValue = Integer.valueOf(sunrise);
            Integer sunsetValue = Integer.valueOf(sunset);
            return imageReturn(comparedTime, sunriseValue, sunsetValue, weatherType);

        }

        /**
         * Handles the forecast image for a given weather type.
         *
         * @param weatherType the type of weather for which the image is being handled
         * @return the forecast image for the specified weather type
         */
        public String forecastImageHandler(String weatherType) {
            return imageReturn(12, 5, 18, weatherType);
        }

        /**
         * Returns the image path based on the compared time, sunrise, sunset, and weather type.
         *
         * @param comparedTime The time to compare with sunrise and sunset.
         * @param sunrise The time of sunrise.
         * @param sunset The time of sunset.
         * @param weatherType The type of weather.
         * @return The image path based on the compared time, sunrise, sunset, and weather type.
         */
        public String imageReturn(int comparedTime, int sunrise, int sunset, String weatherType) {
            if (comparedTime <= sunset && comparedTime >= sunrise) {
                switch (weatherType) {
                    case "Thunderstorm":
                        return "/icons/day-cloudy-thunder.png";

                    case "Rain":
                        return "/icons/day-cloudy-rain.png";

                    case "Clear":
                        return "/icons/day-clear.png";

                    case "Snow":
                        return "/icons/day-cloudy-snowy.png";

                    case "Clouds":
                        return "/icons/day-cloudy.png";
                }
                return "/icons/day-clear.png";
            } else {
                switch (weatherType) {
                    case "Thunderstorm":
                        return "/icons/night-cloudy-thunder.png";

                    case "Rain":
                        return "/icons/night-cloudy-rain.png";

                    case "Clear":
                        return "/icons/night-clear.png";

                    case "Snow":
                        return "/icons/night-cloudy-snowy.png";

                    case "Clouds":
                        return "/icons/night-cloudy.png";
                }
                return "/icons/night-clear.png";
            }
        }




}
