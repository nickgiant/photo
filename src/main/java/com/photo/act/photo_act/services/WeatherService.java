package com.photo.act.photo_act.services;

//  https://github.com/khanhpham2134/WeatherApp/blob/main/WeatherApp/src/main/java/fi/tuni/prog3/weatherapp/WeatherData.java

import com.google.gson.*;
import com.photo.act.photo_act.views.ImageGalleryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class WeatherService {


    private final String API_KEY;
    private String UNIT;
    private boolean ERROR_LOCATION;
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    /**
         * Constructor that initializes the API key and the unit for temperature
         * @param unit
         */
        public WeatherService(String unit){
            API_KEY = "6a730a06d6e11820be20f350a79720a5";
            UNIT = unit;
            ERROR_LOCATION = false;
        }

   // private final iAPI weatherAPIMetric = new WeatherData("metric");
   // private final iAPI weatherAPIImperial = new WeatherData("imperial");

    /**
     * Checks if the input data for the city is valid and retrieves the city location using the weather API.
     *
     * @param cityData an array of strings containing the city data
     * @return true if the input data is valid, false otherwise
     */
    public boolean ifInputValid(String[] cityData) {
        if (cityData.length == 1) {
            lookUpLocation(cityData[0], "", "");

        } else if (cityData.length == 2) {
            lookUpLocation(cityData[0], "", cityData[1]);

        } else if (cityData.length == 3) {
            lookUpLocation(cityData[0], cityData[1], cityData[2]);
        } else { // invalid amount of parameters
            return true;
        }
        return get_error_flag();
    }

    /**
     * Retrieves the city information based on the provided city data.
     *
     * @param cityData an array of strings containing the city data. The array should contain the city name, and optionally the state and country.
     * @return an array of strings representing the city location information.
     */
    public String[] getCityInformation(String[] cityData) {
        String[] cityLocation = {};
        if (cityData.length == 1) {
            cityLocation = lookUpLocation(cityData[0], "", "");
        } else if (cityData.length == 2) {
            cityLocation = lookUpLocation(cityData[0], "", cityData[1]);
        } else {
            cityLocation = lookUpLocation(cityData[0], cityData[1], cityData[2]);
        }

        return cityLocation;
    }

    /**
     * Retrieves the current weather data in metric units for a given city.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return an array of weather data in metric units
     */
    public String[] getCurrentWeatherDataMetric(String[] cityData) {
        String[] weatherData = {};
        weatherData = getCurrentWeather(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));

        return weatherData;
    }

    /**
     * Retrieves the daily forecast data in metric units for a given city.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return a 2D array of weather data containing the forecast for each day
     */
    public String[][] getDailyForecastMetric(String[] cityData) {
        String[][] weatherData = {};
        weatherData = getForecast(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));

        return weatherData;
    }

    /**
     * Retrieves the hourly forecast data for a given city in metric units.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return a 2D array of strings representing the hourly weather forecast data
     */
    public String[][] getHourlyForecastMetric(String[] cityData) {
        Object weatherDataObject;
        weatherDataObject = getHourlyForecast(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));
        String[][] weatherData = (String[][]) weatherDataObject;

        return weatherData;
    }

    /**
     * Retrieves the current weather data in imperial units for a given city.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return an array of weather data in imperial units
     */
    public String[] getCurrentWeatherDataImperial(String[] cityData) {
        String[] weatherData = {};
        weatherData = getCurrentWeather(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));

        return weatherData;
    }

    /**
     * Retrieves the daily weather forecast in imperial units for a given city.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return a 2D array containing the weather data for each day
     */
    public String[][] getDailyForecastImperial(String[] cityData) {
        String[][] weatherData = {};
        weatherData = getForecast(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));

        return weatherData;
    }

    /**
     * Retrieves the hourly forecast data in imperial units for a given city.
     *
     * @param cityData an array containing the latitude and longitude of the city
     * @return a two-dimensional array of strings representing the weather data
     */
    public String[][] getHourlyForecastImperial(String[] cityData) {
        Object weatherDataObject;
        weatherDataObject = getHourlyForecast(Double.valueOf(cityData[0]), Double.valueOf(cityData[1]));
        String[][] weatherData = (String[][]) weatherDataObject;

        return weatherData;
    }


        public boolean get_error_flag(){
            return ERROR_LOCATION;
        }

        /**
         * Retrieves hourly weather forecast data for the specified latitude and longitude coordinates.
         *
         * @param lat The latitude of the location for which to retrieve the forecast data.
         * @param lon The longitude of the location for which to retrieve the forecast data.
         * @return A 2D String array containing hourly forecast data for the next 24 hours. Each row represents a forecast hour,
         *         and each column contains specific information in the following order: timestamp, temperature, wind speed
         *         ,weather description, and humidity.
         */

        public String[][] getHourlyForecast(double lat, double lon) {
            // Initialize the 2D array to store hourly forecast data
            // Each row represents an hour, and each column represents different attributes
            String[][] hourlyForecast = new String[24][5]; // 24 timestamps, 5 attributes

            try {
                // Construct the URL for OpenWeatherMap API request
                String urlString = "https://pro.openweathermap.org/data/2.5/forecast/hourly?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&cnt=24" + "&units=" + UNIT;
                URL url = new URL(urlString);
                // Establish connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read response from the API
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray hourlyData = jsonResponse.getAsJsonArray("list");

                // Retrieve timezone offset from JSON response
                int timezoneOffset = jsonResponse.get("city").getAsJsonObject().get("timezone").getAsInt();
                ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(timezoneOffset);
                ZoneId zoneId = ZoneId.ofOffset("UTC", zoneOffset);

                // Process each hourly data point
                int index = 0; // Index to keep track of the hour
                for (JsonElement element : hourlyData) {
                    JsonObject dataPoint = element.getAsJsonObject();

                    // Extract timestamp and convert to local time
                    long timestampSeconds = dataPoint.get("dt").getAsLong(); // Timestamp in seconds
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(timestampSeconds), zoneId);
                    String localDateTimeString = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH"));

                    // Extract temperature, weather, humidity, and wind speed
                    double temp = dataPoint.getAsJsonObject("main").get("temp").getAsDouble();
                    long temp_rounded = Math.round(temp);
                    String unitSuffix = "metric".equals(UNIT) ? "°C" : "°F";
                    String temperature = String.valueOf(temp_rounded) + unitSuffix;
                    String weatherMain = dataPoint.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
                    String humidity = dataPoint.getAsJsonObject("main").get("humidity").getAsString();
                    double windSpeed = dataPoint.getAsJsonObject("wind").get("speed").getAsDouble();
                    String windSpeedFormatted = String.valueOf(windSpeed) + (UNIT.equals("metric") ? "m/s" : "mph");

                    // Store data in array
                    hourlyForecast[index][0] = localDateTimeString; // Time
                    hourlyForecast[index][1] = temperature; // Temperature
                    hourlyForecast[index][2] = windSpeedFormatted; // Wind speed
                    hourlyForecast[index][3] = weatherMain; // Weather description
                    hourlyForecast[index][4] = humidity + "%"; // Humidity

                    index++; // Move to the next hour
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Return the hourly forecast data
            return hourlyForecast;
        }

        /**
         * Return the coordinate, the state (if it is a US location), and the country code
         * of the wanted location.
         *
         * @param loc_name: name of the location
         * @param state_name: name of the state if it's an US location
         * @param country_code: country code
         * @return a String array that contains latitude, longitude, state or empty string,
         * and country code.
         */

        // https://openweathermap.org/api/geocoding-api

        public String[] lookUpLocation(String loc_name, String state_name, String country_code) {
            final String Open_weather_geocoding = "http://api.openweathermap.org/geo/1.0/direct";
            ERROR_LOCATION = false;

            logger.info(" loc_name: "+loc_name+" state_name: "+state_name+" country_code: "+country_code);
            // Start building the query
            StringBuilder query = new StringBuilder("q=" + loc_name);

            // Add country_code if it's not null or empty (no need for state_name in many cases)
            if (country_code != null && !country_code.isEmpty()) {
                query.append(",").append(country_code);
            }

            // Append the API key
            query.append("&appid=").append(API_KEY);

            // URL
            String address = Open_weather_geocoding + "?" + query.toString().replace(" ", "%20");


            // Utilizing scanner
            Scanner scanner = null;
            try{
                URL url = new URL(address); // URL created

                scanner = new Scanner(url.openStream(), "UTF-8"); // Attach scanner to a stream opened from URL

                // Read coordinate data as a JSON string, use StringBuilder
                StringBuilder json_string = new StringBuilder();
                while (scanner.hasNextLine()){
                    json_string.append(scanner.nextLine());
                }

                // Parse a JSON array of location
                JsonArray json_array = JsonParser.parseString(json_string.toString()).getAsJsonArray();
                JsonElement json_element = json_array.get(0);
                String latitude = json_element.getAsJsonObject().get("lat").getAsString();
                String longitude = json_element.getAsJsonObject().get("lon").getAsString();
                String Country = json_element.getAsJsonObject().get("country").getAsString();
                String state = ""; // Default value is an empty string
                JsonElement stateElement = json_element.getAsJsonObject().get("state");
                if (stateElement != null) {
                    state = stateElement.getAsString(); // Update state if it's present in the JSON
                }
                return new String[] {latitude, longitude,state,Country};
            }

            catch(IOException e){
                e.printStackTrace();
                ERROR_LOCATION = true;
                System.out.println("Catch block IOException is executed");
                return null;
            }
            catch (JsonSyntaxException | NoSuchElementException | IndexOutOfBoundsException e) {
                ERROR_LOCATION = true;
                System.out.println("Error API query due to mismatched loc_name and country_code");
                return null;
            }

            finally{
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        /**
         * Retrieves current weather information based on latitude and longitude coordinates.
         *
         * @param lat    The latitude coordinate of the location.
         * @param lon    The longitude coordinate of the location.

         * @return       An array of string containing the current temperature,
         * feels like, min and max temperature (in metric, Celsius), humidity, description,
         * sky, and wind speed (in metric, m/s), current local time, local sunrise and
         * local sunset time IN THIS ORDER.
         *
         */

        public String[] getCurrentWeather(double lat, double lon) {
            // API endpoint for OpenWeatherMap
            final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather";

            // Constructing the query string
            String query = "lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=" + UNIT;

            // Constructing the URL address
            String address = OPEN_WEATHER_MAP_API + "?" + query;

            // Creating URL object
            URL url = null;
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            try {
                // Establishing connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Reading response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                // Parsing JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                // Retrieve timezone offset from JSON response
                int timezoneOffset = jsonResponse.get("timezone").getAsInt();
                ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(timezoneOffset);
                ZoneId zoneId = ZoneId.ofOffset("UTC", zoneOffset);

                //System.out.println(" zoneId:  "+zoneId.getId()+" "+zoneId.toString());

                // Extracting relevant weather information
                long dateTime = jsonResponse.get("dt").getAsLong();
                LocalDateTime localDateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(dateTime), zoneId);
                String localDateTimeString = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH"));

                String fulllocalDateTimeString = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));


                JsonObject sys = jsonResponse.getAsJsonObject("sys");
                long sunRise = sys.get("sunrise").getAsLong();
                LocalDateTime localsunRise = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(sunRise), zoneId);
                String local_sun_rise = localsunRise.format(java.time.format.DateTimeFormatter.ofPattern("HH"));
                String full_local_sun_rise = localsunRise.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))+"am";

                long sunSet = sys.get("sunset").getAsLong();
                LocalDateTime localsunSet = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(sunSet), zoneId);
                String local_sun_set = localsunSet.format(java.time.format.DateTimeFormatter.ofPattern("HH"));
                String full_local_sun_set = localsunSet.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))+"pm";



                JsonObject wind = jsonResponse.getAsJsonObject("wind");
                String windSpeed = wind.get("speed").getAsString() + (UNIT.equals("metric") ? "m/s" : "mph");

                JsonObject main = jsonResponse.getAsJsonObject("main");
                double temperature = main.get("temp").getAsDouble();
                double feelsLike = main.get("feels_like").getAsDouble();
                double minTemp = main.get("temp_min").getAsDouble();
                double maxTemp = main.get("temp_max").getAsDouble();

                JsonElement visibilityObj = jsonResponse.get("visibility");//.getAsJsonObject("visibility");
                String visibility = visibilityObj.getAsString(); //main.get("visibility").getAsString();

                // Rounding the temperature values to the nearest integer
                String temperatureString = Math.round(temperature) + (UNIT.equals("metric") ? "°C" : "°F");
                String feelsLikeString = Math.round(feelsLike) + (UNIT.equals("metric") ? "°C" : "°F");
                String minTempString = Math.round(minTemp) + (UNIT.equals("metric") ? "°C" : "°F");
                String maxTempString = Math.round(maxTemp) + (UNIT.equals("metric") ? "°C" : "°F");

                String humidity = main.get("humidity").getAsString() + "%";

                JsonObject weather = jsonResponse.getAsJsonArray("weather").get(0).getAsJsonObject();
                String description = weather.get("description").getAsString();
                String mainSky = weather.get("main").getAsString();

                JsonObject cloudsObj = jsonResponse.getAsJsonObject("clouds");
                String clouds = cloudsObj.get("all").getAsString()+"%";

                JsonObject rainObj = jsonResponse.getAsJsonObject("rain");
                String rain = "";
                if(rainObj == null || rainObj.isJsonNull())
                {

                }
                else {
                    rain =  rainObj.get("1h").getAsString()+"mm";
                }


                // Constructing the array with weather information
                String[] weatherInfo = {temperatureString, feelsLikeString, minTempString, maxTempString,
                        humidity, description, mainSky, windSpeed, localDateTimeString, local_sun_rise, local_sun_set,
                        fulllocalDateTimeString, full_local_sun_rise, full_local_sun_set, zoneId.getId(),clouds, rain, visibility};

                return weatherInfo;
            } catch (IOException e) {
                e.printStackTrace();
                // In case of error, return null
                return null;
            }
        }

        /**
         * Retrieves today + next 3 days weather forecast for the same location coordinates
         * with the getCurrentWeather function.
         *
         * @param lat The latitude of the location.
         * @param lon The longitude of the location.
         *
         * @return A String array of string arrays in which each child array represent
         * the weather of a day (starting from today). Each child String array contains the date,
         * main temperature, min, max (in Celsius) and sky.
         */

        public String[][] getForecast(double lat, double lon) {
            // Build the URL for the API call
            String url_String = "http://api.openweathermap.org/data/2.5/forecast/daily"
                    + "?lat=" + lat
                    + "&lon=" + lon
                    + "&cnt=4" // 4 days including today
                    + "&appid=" + API_KEY + "&units=" + UNIT;

            try {
                // Create a URL object
                URL url = new URL(url_String);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method
                connection.setRequestMethod("GET");

                // Get the response code
                int responseCode = connection.getResponseCode();

                // Check if the response code indicates success
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse the JSON response
                    JsonObject jsonObject = new Gson().fromJson(response.toString(), JsonObject.class);

                    // Extract the forecast data for the upcoming 4 days including today
                    JsonArray forecastList = jsonObject.getAsJsonArray("list");

                    String[][] weather_forecast = new String[4][5]; // 4 days, each with 5 pieces of information

                    for (int i = 0; i < forecastList.size(); i++) {
                        JsonObject day = forecastList.get(i).getAsJsonObject();

                        long dateTime = day.get("dt").getAsLong();
                        String date = new java.text.SimpleDateFormat("dd/MM").format(new java.util.Date(dateTime * 1000));

                        double tempDouble = day.getAsJsonObject("temp").get("day").getAsDouble();
                        long tempRounded = Math.round(tempDouble);

                        double minTempDouble = day.getAsJsonObject("temp").get("min").getAsDouble();
                        long minTempRounded = Math.round(minTempDouble);

                        double maxTempDouble = day.getAsJsonObject("temp").get("max").getAsDouble();
                        long maxTempRounded = Math.round(maxTempDouble);

                        String sky = day.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

                        String unitSuffix = "metric".equals(UNIT) ? "°C" : "°F";

                        weather_forecast[i][0] = date;
                        weather_forecast[i][1] = String.valueOf(tempRounded) + unitSuffix;
                        weather_forecast[i][2] = String.valueOf(minTempRounded)+ unitSuffix;
                        weather_forecast[i][3] = String.valueOf(maxTempRounded)+ unitSuffix;
                        weather_forecast[i][4] = sky;
                    }

                    return weather_forecast;
                } else {
                    System.out.println("Error: " + responseCode);
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getUrlReference()
        {
            return "https://openweathermap.org";
        }

    public String getTitleReference()
    {
        return "openweathermap.org";
    }


}
