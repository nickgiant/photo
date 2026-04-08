package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.model.WeatherData;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.TimezoneUtils;
import com.photo.act.photo_act.utils.WeatherIcons;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LocalWeatherForecast - A comprehensive weather forecast component for Vaadin
 *
 * Features:
 * - Current weather display
 * - 5-day forecast with daily buttons
 * - Hourly forecast dialog for each day
 * - Sunrise/sunset information
 * - Location data (city, country, coordinates, elevation)
 * - SVG weather icons
 * - Optional background image based on weather
 */
public class LocalWeatherForecast extends VerticalLayout {

    private final WeatherService weatherService;

    // Location fields
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String timezone;

    // UI Components
    private final VerticalLayout mainContainer;
    private final Div currentWeatherSection;
    private final Div locationInfoSection;
    private final HorizontalLayout forecastButtonsLayout;
    private final Checkbox backgroundImageToggle;

    // Data cache
    private final Map<LocalDate, List<WeatherData.HourlyForecast>> hourlyForecastCache = new HashMap<>();
    private List<WeatherData.DailyForecast> dailyForecasts;
    private WeatherData.SunData currentSunData;

    // Settings
    private boolean useBackgroundImage = false;

    /**
     * Constructor for city and country based weather
     */
    public LocalWeatherForecast(WeatherService weatherService, String city, String country) {
        this.weatherService = weatherService;
        this.city = city;
        this.country = country;

        this.mainContainer = new VerticalLayout();
        this.currentWeatherSection = new Div();
        this.locationInfoSection = new Div();
        this.forecastButtonsLayout = new HorizontalLayout();
        this.backgroundImageToggle = new Checkbox("Use background image");

        initializeComponent();
    }

    /**
     * Constructor for GPS coordinates based weather
     */
    public LocalWeatherForecast(WeatherService weatherService, double latitude, double longitude) {
        this.weatherService = weatherService;
        this.latitude = latitude;
        this.longitude = longitude;

        this.mainContainer = new VerticalLayout();
        this.currentWeatherSection = new Div();
        this.locationInfoSection = new Div();
        this.forecastButtonsLayout = new HorizontalLayout();
        this.backgroundImageToggle = new Checkbox("Use background image");

        initializeComponent();
    }

    private void initializeComponent() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Setup background toggle
        backgroundImageToggle.setVisible(false);
        backgroundImageToggle.setValue(useBackgroundImage);
        backgroundImageToggle.addValueChangeListener(e -> {
            useBackgroundImage = e.getValue();
            updateBackground();
        });

        // Setup main container
        mainContainer.setSizeFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.addClassNames(LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        mainContainer.addClassName("weather-layout");
//        mainContainer.getStyle()
//                .set("background", "rgba(255, 255, 255, 0.9)")
//                .set("border-radius", "12px")
//                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");

        add(mainContainer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadWeatherData();
    }

    private void loadWeatherData() {
        try {
            mainContainer.removeAll();

            // Add settings toggle at the top
            HorizontalLayout settingsLayout = new HorizontalLayout(backgroundImageToggle);
            settingsLayout.setWidthFull();
            settingsLayout.setJustifyContentMode(JustifyContentMode.END);
            mainContainer.add(settingsLayout);

            // Load location data
            loadLocationInfo();

            // Load current weather
            loadCurrentWeather();

            // Load forecast
//            loadForecast();

            // Update background
            updateBackground();

        } catch (Exception e) {
            showError("Failed to load weather data: " + e.getMessage());
        }
    }

    private void loadLocationInfo() {
        WeatherData.LocationData locationData;

        if (city != null && country != null) {
            locationData = weatherService.getLocationData(city, country);
            this.latitude = locationData.getLatitude();
            this.longitude = locationData.getLongitude();
            this.timezone = locationData.getTimezone();
        } else {
            // For GPS coordinates, we need to get location info differently
            WeatherData.CurrentWeather currentWeather =
                    weatherService.getCurrentWeather(latitude, longitude);
            locationData = new WeatherData.LocationData();
            locationData.setLatitude(latitude);
            locationData.setLongitude(longitude);
            locationData.setCity("Location");
            locationData.setCountry("");

            // Get timezone from coordinates
            try {
                this.timezone = TimezoneUtils
                        .getTimezoneFromCoordinatesCached(latitude, longitude).getId();
            } catch (Exception e) {
                this.timezone = null; // Will use system default as fallback
            }

            // Try to get elevation
            try {
                // Use open-meteo for elevation
                int elevation = getElevationFromCoordinates(latitude, longitude);
                locationData.setElevation(elevation);
            } catch (Exception e) {
                locationData.setElevation(0);
            }
        }

        displayLocationInfo(locationData);
    }

    private void displayLocationInfo(WeatherData.LocationData locationData) {
        locationInfoSection.removeAll();
        locationInfoSection.addClassNames(LumoUtility.Width.FULL, LumoUtility.TextAlignment.CENTER,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        locationInfoSection.addClassName("weather-location-info");
//                .set("padding", "15px")
//                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
//                .set("border-radius", "8px")
//                .set("color", "white")
//                .set("margin-bottom", "20px");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.addClassNames(LumoUtility.Width.FULL, LumoUtility.TextAlignment.CENTER,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        H2 locationTitle = new H2(locationData.getCity() +
                (locationData.getCountry().isEmpty() ? "" : ", " + locationData.getCountry()));
//        locationTitle.getStyle().set("margin", "0").set("color", "white");

        HorizontalLayout coordsLayout = new HorizontalLayout();
        coordsLayout.setSpacing(true);

        Span coordsSpan = new Span(String.format(" %.4f°, %.4f°",
                locationData.getLatitude(),
                locationData.getLongitude()));
        coordsSpan.getStyle().set("font-size", "14px");

        if (locationData.getElevation() > 0) {
            Span elevationSpan = new Span(String.format("⛰ %d m", locationData.getElevation()));
            elevationSpan.getStyle().set("font-size", "14px").set("margin-left", "15px");
            coordsLayout.add(coordsSpan, elevationSpan);
        } else {
            coordsLayout.add(coordsSpan);
        }

        infoLayout.add(locationTitle, coordsLayout);
        locationInfoSection.add(infoLayout);
        mainContainer.add(locationInfoSection);
    }

    private void loadCurrentWeather() {
        WeatherData.CurrentWeather currentWeather;

        if (city != null && country != null) {
            currentWeather = weatherService.getCurrentWeather(city, country);
        } else {
            currentWeather = weatherService.getCurrentWeather(latitude, longitude);
        }

        displayCurrentWeather(currentWeather);
    }

    private void displayCurrentWeather(WeatherData.CurrentWeather weather) {
        currentWeatherSection.removeAll();
        currentWeatherSection.addClassName("weather-current");

        // Add sunrise/sunset info
        addSunriseSunsetInfo(LocalDate.now());

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Determine if it's night time
        boolean isNight = false;
        if (currentSunData != null) {
            isNight = WeatherIcons.isNightTime(weather.getDateTime(),
                    currentSunData.getSunrise(),
                    currentSunData.getSunset());
        }

        // Left side: Temperature and icon
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setPadding(false);
        leftLayout.setSpacing(false);
        leftLayout.setAlignItems(Alignment.CENTER);

        Div iconDiv = new Div();
        iconDiv.getElement().appendChild(
                WeatherIcons.getWeatherIcon(weather.getWeatherType(), 120, isNight)
        );

        Span tempSpan = new Span(String.format("%.1f°C", weather.getTemperature()));
        tempSpan.getStyle()
                .set("font-size", "38px")
                .set("font-weight", "bold")
                .set("color", "#7f8c8d");
//                .set("color", "#2c3e50");

        Span descSpan = new Span(weather.getDescription());
        descSpan.getStyle()
                .set("font-size", "18px")
                .set("color", "#7f8c8d")
                .set("text-transform", "capitalize");

        leftLayout.add(iconDiv, tempSpan, descSpan);

        // Right side: Additional info
        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setPadding(false);
        rightLayout.setSpacing(true);

        rightLayout.add(createInfoRow("🌡️ Feels like", String.format("%.1f°C", weather.getFeelsLike())));
        rightLayout.add(createInfoRow("💧 Humidity", weather.getHumidity() + "%"));
        rightLayout.add(createInfoRow("💨 Wind Speed", String.format("%.1f m/s", weather.getWindSpeed())));
        rightLayout.add(createInfoRow("🎈 Pressure", weather.getPressure() + " hPa"));
        rightLayout.add(createInfoRow("☁️ Cloudiness", weather.getCloudiness() + "%"));
        if(weather.getVisibility()>0) {
            rightLayout.add(createInfoRow("👁️ Visibility", String.format("%.1f km", weather.getVisibility())));
        }

        mainLayout.add(leftLayout, rightLayout);
        currentWeatherSection.add(mainLayout);



        mainContainer.add(currentWeatherSection);
    }

    private void addSunriseSunsetInfo( LocalDate localDate) {
        try {
            currentSunData = weatherService.getSunData(latitude, longitude, localDate, timezone);

            HorizontalLayout sunLayout = new HorizontalLayout();
            sunLayout.setWidthFull();
            sunLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            sunLayout.setSpacing(true);
            sunLayout.getStyle()
                    .set("margin-top", "10px")
                    .set("margin-bottom", "10px")
                    .set("padding", "10px")
                    .set("background", "rgba(255, 255, 255, 0.5)")
                    .set("border-radius", "6px");

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            Span sunriseSpan = new Span(" Sunrise: ");
            sunriseSpan.getStyle().set("font-weight", "500");

            Span sunsetSpan = new Span(" Sunset: ");
            sunsetSpan.getStyle().set("font-weight", "500");

            Span dayLengthSpan = new Span(" Day length: ");
            dayLengthSpan.getStyle().set("font-weight", "500");

            sunLayout.add(sunriseSpan,  createInfoValue(currentSunData.getSunrise().format(timeFormat)),
                    sunsetSpan, createInfoValue(currentSunData.getSunset().format(timeFormat)),
                    dayLengthSpan, createInfoValue(formatDayLength(currentSunData.getDayLength())));
            currentWeatherSection.add(sunLayout);
        } catch (Exception e) {
            // Silently fail if sun data is not available
            currentSunData = null;
        }
    }

    private String formatDayLength(String dayLength) {
        // dayLength comes in seconds, format as HH:mm:ss
        try {
            long seconds = Long.parseLong(dayLength);
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return String.format("%02d:%02d", hours, minutes);
        } catch (Exception e) {
            return dayLength;
        }
    }

    private Span createInfoValue(String value){
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#2c3e50")
                .set("font-weight", "600");
        return valueSpan;
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "500")
                .set("color", "#34495e")
                .set("min-width", "120px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#2c3e50")
                .set("font-weight", "600");

        layout.add(labelSpan, valueSpan);
        return layout;
    }

    private void loadForecast() {
        if (city != null && country != null) {
            dailyForecasts = weatherService.getFiveDayForecast(city, country);
        } else {
            dailyForecasts = weatherService.getFiveDayForecast(latitude, longitude);
        }

        displayForecastButtons();
    }

    private void displayForecastButtons() {
        forecastButtonsLayout.removeAll();
        forecastButtonsLayout.addClassName("weather-day-buttons");
        forecastButtonsLayout.setWidthFull();
        forecastButtonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        forecastButtonsLayout.setSpacing(true);
        forecastButtonsLayout.getStyle()
//                .set("padding", "50px 0")
                .set("flex-wrap", "wrap");

        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd");

        for (WeatherData.DailyForecast forecast : dailyForecasts) {
            Button dayButton = createForecastButton(forecast, dayFormat, dateFormat);
            forecastButtonsLayout.add(dayButton);
        }

        mainContainer.add(forecastButtonsLayout);
    }

    private Button createForecastButton(WeatherData.DailyForecast forecast,
                                        DateTimeFormatter dayFormat,
                                        DateTimeFormatter dateFormat) {
        VerticalLayout buttonContent = new VerticalLayout();
        buttonContent.setPadding(false);
        buttonContent.setSpacing(false);
        buttonContent.setAlignItems(Alignment.CENTER);

        Span daySpan = new Span(forecast.getDate().format(dayFormat));
        daySpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "14px");

        Span dateSpan = new Span(forecast.getDate().format(dateFormat));
        dateSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#7f8c8d");

        // Determine if icon should be day or night (use noon as reference for daily forecast)
        LocalDateTime noonTime = forecast.getDate().atTime(12, 0);
        boolean isNight = false;
        if (forecast.getSunrise() != null && forecast.getSunset() != null) {
            isNight = WeatherIcons.isNightTime(noonTime, forecast.getSunrise(), forecast.getSunset());
        }

        Div iconDiv = new Div();
        iconDiv.getElement().appendChild(
                WeatherIcons.getWeatherIcon(forecast.getWeatherType(), 50, isNight)
        );

        Span tempSpan = new Span(String.format("%.0f° / %.0f°",
                forecast.getMaxTemp(),
                forecast.getMinTemp()));
        tempSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600");

        buttonContent.add(daySpan, dateSpan, iconDiv, tempSpan);

        Button button = new Button(buttonContent);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getStyle()
                .set("border", "2px solid #e0e6ed")
                .set("border-radius", "12px")
                .set("padding", "15px")
                .set("background", "white")
                .set("cursor", "pointer")
                .set("transition", "all 0.3s ease")
                .set("min-width", "120px");

        // Hover effect using tooltip
        String tooltipText = String.format("%s: %s, %.0f°-%.0f°C",
                forecast.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                forecast.getDescription(),
                forecast.getMinTemp(),
                forecast.getMaxTemp());
        button.setTooltipText(tooltipText);

        // Click to show hourly forecast
        button.addClickListener(e -> showHourlyForecastDialog(forecast.getDate()));

        // Load hover data in background
        button.getElement().addEventListener("mouseenter", event -> {
            if (!hourlyForecastCache.containsKey(forecast.getDate())) {
                loadHourlyForecastInBackground(forecast.getDate());
            }
        });

        return button;
    }

    private void loadHourlyForecastInBackground(LocalDate date) {
        try {
            List<WeatherData.HourlyForecast> hourlyData;
            if (city != null && country != null) {
                hourlyData = weatherService.getHourlyForecast(city, country, date);
            } else {
                hourlyData = weatherService.getHourlyForecast(latitude, longitude, date);
            }
            hourlyForecastCache.put(date, hourlyData);
        } catch (Exception e) {
            // Silently fail, will try again on click
        }
    }

    private void showHourlyForecastDialog(LocalDate date) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("80vh");

        // Header
        H3 header = new H3("Hourly Forecast - " +
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                ", " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        header.getStyle().set("margin", "0");
        dialog.setHeaderTitle(header.getText());

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());
        dialog.getHeader().add(closeButton);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        try{
        currentSunData = weatherService.getSunData(latitude, longitude, date, timezone);

        // Add sunrise/sunset info at the top if available
        if (currentSunData != null && currentSunData.getSunrise() != null && currentSunData.getSunset() != null) {

            HorizontalLayout sunInfoLayout = new HorizontalLayout();
            sunInfoLayout.setWidthFull();
            sunInfoLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            sunInfoLayout.getStyle()
//                    .set("background", "linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)")
                    .set("border-radius", "8px")
                    .set("padding", "10px")
                    .set("margin-bottom", "10px");




/*            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            Span sunriseSpan = new Span(" Sunrise: " + currentSunData.getSunrise().format(timeFormat));
            sunriseSpan.getStyle().set("font-weight", "500");

            Span sunsetSpan = new Span(" Sunset: " + currentSunData.getSunset().format(timeFormat));
            sunsetSpan.getStyle().set("font-weight", "500");

            Span dayLengthSpan = new Span(" Day length: " + formatDayLength(currentSunData.getDayLength()));
            dayLengthSpan.getStyle().set("font-weight", "500");*/

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            Span sunriseSpan = new Span(" Sunrise: ");
            sunriseSpan.getStyle().set("font-weight", "500");

            Span sunsetSpan = new Span(" Sunset: ");
            sunsetSpan.getStyle().set("font-weight", "500");

            Span dayLengthSpan = new Span(" Day length: ");
            dayLengthSpan.getStyle().set("font-weight", "500");



            sunInfoLayout.add(sunriseSpan,  createInfoValue(currentSunData.getSunrise().format(timeFormat)),
                    sunsetSpan, createInfoValue(currentSunData.getSunset().format(timeFormat)),
                    dayLengthSpan, createInfoValue(formatDayLength(currentSunData.getDayLength())));
            content.add(sunInfoLayout);
        }
        } catch (Exception e) {
                // Silently fail if sun data is not available
                currentSunData = null;
        }



        try {
            List<WeatherData.HourlyForecast> hourlyData = hourlyForecastCache.get(date);
            if (hourlyData == null) {
                if (city != null && country != null) {
                    hourlyData = weatherService.getHourlyForecast(city, country, date);
                } else {
                    hourlyData = weatherService.getHourlyForecast(latitude, longitude, date);
                }
                hourlyForecastCache.put(date, hourlyData);
            }

            if (hourlyData.isEmpty()) {
                content.add(new Span("No hourly data available for this date."));
            } else {
                for (WeatherData.HourlyForecast hourly : hourlyData) {
                    content.add(createHourlyForecastCard(hourly));
                }
            }
        } catch (Exception e) {
            content.add(new Span("Error loading hourly forecast: " + e.getMessage()));
            e.printStackTrace();
        }

        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createHourlyForecastCard(WeatherData.HourlyForecast hourly) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setAlignItems(Alignment.CENTER);
        card.setPadding(true);
        card.getStyle()
                .set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)")
                .set("border-radius", "8px")
                .set("margin", "5px 0");

        // Time
        Span timeSpan = new Span(hourly.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeSpan.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "600")
                .set("min-width", "60px");

        // Determine if this hour is during night - FIXED logic with DEBUG
        boolean isNight = false;

        System.out.println("DAY/NIGHT DEBUG - Hour: " + hourly.getDateTime().getHour() +
                ", currentSunData: " + (currentSunData != null ? "EXISTS" : "NULL"));

        if (currentSunData != null && currentSunData.getSunrise() != null && currentSunData.getSunset() != null) {
            LocalDateTime hourTime = hourly.getDateTime();
            LocalDateTime sunrise = currentSunData.getSunrise();
            LocalDateTime sunset = currentSunData.getSunset();

            System.out.println("DAY/NIGHT DEBUG - Sunrise: " + sunrise + ", Sunset: " + sunset + ", HourTime: " + hourTime);

            // FIXED: Compare only the TIME portion, not full date-time
            // Extract just the hour and minute for comparison
            int hourOfDay = hourTime.getHour();
            int minuteOfHour = hourTime.getMinute();
            int sunriseHour = sunrise.getHour();
            int sunriseMinute = sunrise.getMinute();
            int sunsetHour = sunset.getHour();
            int sunsetMinute = sunset.getMinute();

            // Convert to minutes since midnight for easier comparison
            int currentMinutes = hourOfDay * 60 + minuteOfHour;
            int sunriseMinutes = sunriseHour * 60 + sunriseMinute;
            int sunsetMinutes = sunsetHour * 60 + sunsetMinute;

            // Night is before sunrise OR after sunset
            isNight = currentMinutes < sunriseMinutes || currentMinutes > sunsetMinutes;

            System.out.println("DAY/NIGHT DEBUG - Current: " + hourOfDay + ":" + String.format("%02d", minuteOfHour) +
                    " (" + currentMinutes + " mins), Sunrise: " + sunriseHour + ":" + String.format("%02d", sunriseMinute) +
                    " (" + sunriseMinutes + " mins), Sunset: " + sunsetHour + ":" + String.format("%02d", sunsetMinute) +
                    " (" + sunsetMinutes + " mins), Result isNight: " + isNight);
        } else {
            // Fallback: check hour (6 AM to 6 PM is day)
            int hour = hourly.getDateTime().getHour();
            isNight = hour < 6 || hour >= 18;
            System.out.println("DAY/NIGHT DEBUG - FALLBACK MODE - Hour: " + hour +
                    ", isNight: " + isNight + " (< 6 or >= 18)");
        }

        // Debug logging
        System.out.println("HOURLY CARD DEBUG - Weather Type: '" + hourly.getWeatherType() +
                "', Description: '" + hourly.getDescription() +
                "', isNight: " + isNight +
                ", Hour: " + hourly.getDateTime().getHour());

        // Icon
        Div iconDiv = new Div();
        Element iconElement = WeatherIcons.getWeatherIcon(hourly.getWeatherType(), 40, isNight);
        System.out.println("HOURLY CARD DEBUG - Icon HTML length: " + iconElement.getOuterHTML().length());
        iconDiv.getElement().appendChild(iconElement);
        iconDiv.getStyle().set("margin", "0 15px");

        // Temperature
        Span tempSpan = new Span(String.format("%.1f°C", hourly.getTemperature()));
        tempSpan.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("min-width", "70px");

        // Description
        Span descSpan = new Span(hourly.getDescription());
        descSpan.getStyle()
                .set("flex", "1")
                .set("text-transform", "capitalize")
                .set("margin-right", "15px");

        // Additional info (RIGHT side)
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);
        infoLayout.setAlignItems(Alignment.END);

        Span humiditySpan = new Span("💧 " + hourly.getHumidity() + "%");
        humiditySpan.getStyle().set("font-size", "12px");

        Span windSpan = new Span("💨 " + String.format("%.1f m/s", hourly.getWindSpeed()));
        windSpan.getStyle().set("font-size", "12px");

        Span cloudinessSpan = new Span("☁️ " + hourly.getCloudiness() + "%");
        cloudinessSpan.getStyle().set("font-size", "12px");

        infoLayout.add(humiditySpan, windSpan, cloudinessSpan);

        card.add(timeSpan, iconDiv, tempSpan, descSpan, infoLayout);
        return card;
    }

    private void updateBackground() {
        if (useBackgroundImage && dailyForecasts != null && !dailyForecasts.isEmpty()) {
            String weatherType = dailyForecasts.get(0).getWeatherType();
            String cityName = city != null ? city : "weather";
            String imageUrl = WeatherIcons.getBackgroundImageUrl(weatherType, cityName);

            getStyle()
                    .set("background-image", "url('" + imageUrl + "')")
                    .set("background-size", "cover")
                    .set("background-position", "center")
                    .set("background-attachment", "fixed");

            mainContainer.getStyle()
                    .set("background", "rgba(255, 255, 255, 0.85)")
                    .set("backdrop-filter", "blur(10px)");
        } else {
            getStyle()
                    .remove("background-image")
                    .remove("background-size")
                    .remove("background-position")
                    .remove("background-attachment");

            mainContainer.getStyle()
                    .set("background", "rgba(255, 255, 255, 0.9)")
                    .remove("backdrop-filter");
        }
    }

    private void showError(String message) {
//        this.setVisible(false);
        mainContainer.removeAll();
        Div errorDiv = new Div();
        errorDiv.setText(message);
        errorDiv.getStyle()
//                .set("color", "#e74c3c")
                .set("padding", "20px")
                .set("font-size", "16px")
                .set("text-align", "center");
        mainContainer.add(errorDiv);
    }

    // Helper method for elevation (since it's not in the service)
    private int getElevationFromCoordinates(double lat, double lon) {
        // This would call the open-meteo API or similar
        // For now, return 0 as placeholder
        return 0;
    }

    // Getters and setters for updating location

    public void updateLocation(String city, String country) {
        this.city = city;
        this.country = country;
        this.latitude = null;
        this.longitude = null;
        this.hourlyForecastCache.clear();
        loadWeatherData();
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = null;
        this.country = null;
        this.hourlyForecastCache.clear();
        loadWeatherData();
    }
}