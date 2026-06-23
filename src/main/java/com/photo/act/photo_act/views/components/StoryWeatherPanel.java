package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.model.WeatherData;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.TimezoneUtils;
import com.photo.act.photo_act.utils.WeatherIcons;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
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

public class StoryWeatherPanel extends Div {

    private final WeatherService weatherService;
    private final double lat;
    private final double lon;
    private final String locationArea;

    private String timezone;
    private WeatherData.SunData currentSunData;
    private final Map<LocalDate, List<WeatherData.HourlyForecast>> hourlyCache = new HashMap<>();

    private final HorizontalLayout tabBar = new HorizontalLayout();
    private final Div contentArea = new Div();
    private Button activeTabBtn = null;

    public StoryWeatherPanel(WeatherService weatherService, double lat, double lon, String locationArea) {
        this.weatherService = weatherService;
        this.lat = lat;
        this.lon = lon;
        this.locationArea = locationArea;

        addClassName("story-weather-panel");
        setWidthFull();

        tabBar.addClassName("story-weather-tabs");
        tabBar.setWidthFull();
        tabBar.setSpacing(false);
        tabBar.setPadding(false);

        contentArea.addClassName("story-weather-content");
        contentArea.setWidthFull();

        add(tabBar, contentArea);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadPanel();
    }

    private void loadPanel() {
        try {
            timezone = TimezoneUtils.getTimezoneFromCoordinatesCached(lat, lon).getId();
        } catch (Exception e) {
            timezone = null;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);
        LocalDate day4 = today.plusDays(3);

        tabBar.removeAll();
        Button btnNow = buildTabButton("Now", null);
        Button btnToday = buildTabButton("Today", today);
        Button btnTomorrow = buildTabButton("Tomorrow", tomorrow);
        Button btnDay3 = buildTabButton(day3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH), day3);
        Button btnDay4 = buildTabButton(day4.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH), day4);

        tabBar.add(btnNow, btnToday, btnTomorrow, btnDay3, btnDay4);

        activateTab(btnNow, null);
    }

    private Button buildTabButton(String label, LocalDate date) {
        Button btn = new Button(label);
        btn.addClassName("story-weather-tab-btn");
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.addClickListener(e -> activateTab(btn, date));
        return btn;
    }

    private void activateTab(Button btn, LocalDate date) {
        if (activeTabBtn != null) {
            activeTabBtn.removeClassName("story-weather-tab-active");
        }
        btn.addClassName("story-weather-tab-active");
        activeTabBtn = btn;

        contentArea.removeAll();

        if (date == null) {
            showNow();
        } else {
            showHourly(date);
        }
    }

    private void showNow() {
        try {
            WeatherData.CurrentWeather current = weatherService.getCurrentWeather(lat, lon);
            currentSunData = weatherService.getSunData(lat, lon, LocalDate.now(), timezone);

            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(true);
            layout.setSpacing(true);
            layout.addClassName("story-weather-now");

            H3 locationTitle = new H3(locationArea != null && !locationArea.isBlank() ? locationArea : "Weather");
            locationTitle.addClassName("story-weather-location-title");
            layout.add(locationTitle);

            layout.add(buildSunRow(currentSunData));
            layout.add(buildCurrentWeatherRow(current, currentSunData));

            contentArea.add(layout);
        } catch (Exception e) {
            contentArea.add(buildErrorDiv("Could not load current weather."));
        }
    }

    private Div buildCurrentWeatherRow(WeatherData.CurrentWeather weather, WeatherData.SunData sunData) {
        Div card = new Div();
        card.addClassName("story-weather-now-card");

        boolean isNight = false;
        if (sunData != null) {
            isNight = WeatherIcons.isNightTime(weather.getDateTime(), sunData.getSunrise(), sunData.getSunset());
        }

        HorizontalLayout mainRow = new HorizontalLayout();
        mainRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
        mainRow.setWidthFull();
        mainRow.setSpacing(true);

        Div iconDiv = new Div();
        iconDiv.getElement().appendChild(WeatherIcons.getWeatherIcon(weather.getWeatherType(), 100, isNight));

        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setPadding(false);
        leftCol.setSpacing(false);
        leftCol.setAlignItems(VerticalLayout.Alignment.CENTER);

        Span tempSpan = new Span(String.format("%.1f°C", weather.getTemperature()));
        tempSpan.getStyle().set("font-size", "36px").set("font-weight", "bold").set("color", "#7f8c8d");

        Span descSpan = new Span(weather.getDescription());
        descSpan.getStyle().set("font-size", "16px").set("color", "#7f8c8d").set("text-transform", "capitalize");

        leftCol.add(iconDiv, tempSpan, descSpan);

        VerticalLayout rightCol = new VerticalLayout();
        rightCol.setPadding(false);
        rightCol.setSpacing(false);
        rightCol.addClassName(LumoUtility.Gap.XSMALL);

        rightCol.add(infoRow("🌡️ Feels like", String.format("%.1f°C", weather.getFeelsLike())));
        rightCol.add(infoRow("💧 Humidity", weather.getHumidity() + "%"));
        rightCol.add(infoRow("💨 Wind", String.format("%.1f m/s", weather.getWindSpeed())));
        rightCol.add(infoRow("🎈 Pressure", weather.getPressure() + " hPa"));
        rightCol.add(infoRow("☁️ Clouds", weather.getCloudiness() + "%"));
        if (weather.getVisibility() > 0) {
            rightCol.add(infoRow("👁️ Visibility", String.format("%.1f km", weather.getVisibility())));
        }

        mainRow.add(leftCol, rightCol);
        card.add(mainRow);
        return card;
    }

    private HorizontalLayout buildSunRow(WeatherData.SunData sunData) {
        HorizontalLayout row = new HorizontalLayout();
        row.addClassName("story-weather-sun-row");
        row.setWidthFull();
        row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        row.setSpacing(true);

        if (sunData != null && sunData.getSunrise() != null && sunData.getSunset() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            row.add(new Span("🌅 " + sunData.getSunrise().format(fmt)));
            row.add(new Span("🌇 " + sunData.getSunset().format(fmt)));
            try {
                long secs = Long.parseLong(sunData.getDayLength());
                row.add(new Span("⏱ " + String.format("%dh %02dm", secs / 3600, (secs % 3600) / 60)));
            } catch (Exception ignored) {}
        }
        return row;
    }

    private void showHourly(LocalDate date) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.addClassName("story-weather-hourly");

        try {
            if (!hourlyCache.containsKey(date)) {
                hourlyCache.put(date, weatherService.getHourlyForecast(lat, lon, date));
            }
            List<WeatherData.HourlyForecast> hourlyList = hourlyCache.get(date);

            try {
                currentSunData = weatherService.getSunData(lat, lon, date, timezone);
            } catch (Exception ignored) {
                currentSunData = null;
            }

            if (currentSunData != null && currentSunData.getSunrise() != null) {
                layout.add(buildSunRow(currentSunData));
            }

            if (hourlyList == null || hourlyList.isEmpty()) {
                layout.add(new Span("No hourly data available."));
            } else {
                for (WeatherData.HourlyForecast h : hourlyList) {
                    layout.add(buildHourlyCard(h));
                }
            }
        } catch (Exception e) {
            layout.add(buildErrorDiv("Could not load hourly forecast."));
        }

        contentArea.add(layout);
    }

    private HorizontalLayout buildHourlyCard(WeatherData.HourlyForecast hourly) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("story-weather-hourly-card");
        card.setWidthFull();
        card.setAlignItems(HorizontalLayout.Alignment.CENTER);
        card.setSpacing(true);

        Span timeSpan = new Span(hourly.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeSpan.getStyle().set("font-size", "18px").set("font-weight", "600").set("min-width", "55px");

        boolean isNight = resolveIsNight(hourly.getDateTime());

        Div iconDiv = new Div();
        Element iconEl = WeatherIcons.getWeatherIcon(hourly.getWeatherType(), 38, isNight);
        iconDiv.getElement().appendChild(iconEl);
        iconDiv.getStyle().set("flex-shrink", "0");

        Span tempSpan = new Span(String.format("%.1f°C", hourly.getTemperature()));
        tempSpan.getStyle().set("font-size", "18px").set("font-weight", "bold").set("min-width", "65px");

        Span descSpan = new Span(hourly.getDescription());
        descSpan.getStyle().set("flex", "1").set("text-transform", "capitalize");

        VerticalLayout infoCol = new VerticalLayout();
        infoCol.setPadding(false);
        infoCol.setSpacing(false);
        infoCol.setAlignItems(VerticalLayout.Alignment.END);
        infoCol.getStyle().set("white-space", "nowrap");

        infoCol.add(new Span("💧 " + hourly.getHumidity() + "%"));
        infoCol.add(new Span("💨 " + String.format("%.1f m/s", hourly.getWindSpeed())));
        infoCol.add(new Span("☁️ " + hourly.getCloudiness() + "%"));

        card.add(timeSpan, iconDiv, tempSpan, descSpan, infoCol);
        return card;
    }

    private boolean resolveIsNight(LocalDateTime time) {
        if (currentSunData != null && currentSunData.getSunrise() != null && currentSunData.getSunset() != null) {
            int cur = time.getHour() * 60 + time.getMinute();
            int rise = currentSunData.getSunrise().getHour() * 60 + currentSunData.getSunrise().getMinute();
            int set = currentSunData.getSunset().getHour() * 60 + currentSunData.getSunset().getMinute();
            return cur < rise || cur > set;
        }
        int h = time.getHour();
        return h < 6 || h >= 20;
    }

    private HorizontalLayout infoRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        Span lbl = new Span(label + ":");
        lbl.getStyle().set("font-weight", "500").set("color", "#34495e").set("min-width", "110px");
        Span val = new Span(value);
        val.getStyle().set("color", "#2c3e50").set("font-weight", "600");
        row.add(lbl, val);
        return row;
    }

    private Div buildErrorDiv(String msg) {
        Div d = new Div();
        d.setText(msg);
        d.getStyle().set("color", "var(--lumo-error-text-color)").set("padding", "var(--lumo-space-m)");
        return d;
    }
}
