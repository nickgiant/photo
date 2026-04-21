package com.photo.act.photo_act.views.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.github.prominence.openweathermap.api.exception.NoDataFoundException;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import com.github.prominence.openweathermap.api.model.weather.Weather;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.PhotoRatingService;
import com.photo.act.photo_act.services.WeatherImageService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.AlbumsView.subPathThumbs;
import static com.photo.act.photo_act.views.HomeView.subPathLarge;
import static com.photo.act.photo_act.views.MainLayout.*;


public class GenericView {


    public static String DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
    private String sessionid;
    private long sessionCreation;
    private String sessionDateTime;
    private String sysUserName;
    private boolean isMobile;
    private String timeZoneId;
    private String locale;
    private String localeName;
    private String publicIp;
    private String strPath;
    private String hostname;
    private String hostAddress;
    private String canonicalHostname;

    private String strUrlRequestToBeLogged;

    private RecordService recordService;
    private WeatherService weatherService;
    private PhotoRatingService photoRatingService;

    private static final Logger logger = LoggerFactory.getLogger(GenericView.class);
    private String dirChar = FileSystems.getDefault().getSeparator();

    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestination = "SELECT distinct city_name, prefecture, country " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " ORDER BY city_name ASC ";

    private UtilsDate utilsDate;
    private String strOS;
    private String strBrowser;

    private String sqlReadGallery;
    private String[] arrColumnsGallery;

    private List<Record> recProps;

    private VerticalLayout layoutMeta;
    private String strMemberId;
    private Dialog dlgCarousel;
    private Div divCarousel;
    private Scroller scroller;
    //private VerticalLayout layoutMap;


    public GenericView(RecordService recordService) {
        this.recordService = recordService;

        utilsDate = new UtilsDate();

        getUserClientInfo();

        String sqlReadAppConfig = "SELECT app, host, propName, propValue FROM dbinfo WHERE host like '" + hostname + "' ";
        String[] arrCols = {"propName", "propValue"};
        recProps = recordService.findAll(sqlReadAppConfig, arrCols);
    }

    public void setPhotoRatingService(PhotoRatingService photoRatingService) {
        this.photoRatingService = photoRatingService;
    }

    public String checkIfAuthUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        } else {
            return null;
        }
    }

    public String checkIfAuthMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();


            String[] arrColumnNames = {"userId", "username", "avatar_path", "name", "surname"};
            String sqlMember = "SELECT " +
                    "   usr.userId,  usr.username,  " +
                    "  usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
                    //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
                    " FROM dbuser usr " +
                    " WHERE username = '" + currentUserName + "' ";

            List<Record> lstRecords = getRecordsFromDb(sqlMember, arrColumnNames);
            Record rec = lstRecords.get(0);

            strMemberId = rec.getColumnData("userId");


            return strMemberId;
        } else {
            return null;
        }
    }

    public int getAuthMemberMonthCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int intMonths = 0;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();


            String[] arrColumnNames = {"userId", "username", "member_for_months", "avatar_path", "name", "surname"};
            String sqlMember = "SELECT " +
                    "   usr.userId,  usr.username " +
                    " , TIMESTAMPDIFF(MONTH, date_joined, NOW()) AS member_for_months " +
                    " , usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
                    //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
                    " FROM dbuser usr " +
                    " WHERE username = '" + currentUserName + "' ";

            List<Record> lstRecords = getRecordsFromDb(sqlMember, arrColumnNames);
            Record rec = lstRecords.get(0);

            try {

                intMonths = Integer.parseInt(rec.getColumnData("member_for_months"));
            } catch (Exception e) {
                logger.error("Month count error: " + e.getMessage());
            }


            return intMonths;
        } else {
            return 0;
        }
    }

    public String getAuthAvatarPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();


            String[] arrColumnNames = {"userId", "username", "avatar_path", "name", "surname"};
            String sqlMember = "SELECT " +
                    "   usr.userId,  usr.username,  " +
                    "  usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
                    //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
                    " FROM dbuser usr " +
                    " WHERE username = '" + currentUserName + "' ";

            List<Record> lstRecords = getRecordsFromDb(sqlMember, arrColumnNames);
            Record rec = lstRecords.get(0);

            String strAvatarPath = rec.getColumnData("avatar_path");

            return strAvatarPath;
        } else {
            return null;
        }
    }

    public VerticalLayout getAuthUserPanel(String strUsername) {

        String[] arrColumnNames = {"userId", "username", "avatar_path", "name", "surname"};
        String sqlMember = "SELECT " +
                "   usr.userId,  usr.username,  " +
                "  usr.avatar_path, name, surname, short_bio, url_insta, url_fb, url_flickr, url_yt, email, resident, resident_country " +
                //     "--  , pa.inc, pm.title, pm.id, pm.name_new, pm.title, pm.subtitle, pm.space_size, pm.location_by_user\\n\" +\n" +
                " FROM dbuser usr " +
                " WHERE username = '" + strUsername + "' ";


        List<Record> lstRecords = getRecordsFromDb(sqlMember, arrColumnNames);
        Record rec = lstRecords.get(0);

        String strName = rec.getColumnData("name");
        String strSurname = rec.getColumnData("surname");
//      String strUsername = rec.getColumnData("username");
        String strMemberFor = rec.getColumnData("member_for");
        String strAvatarPath = rec.getColumnData("avatar_path");

        Image imgAvatar = getAvatarThumbImage(strAvatarPath, strUsername, "180px", "180px");

        H3 objName = new H3(strName + " " + strSurname);
        H4 objMember = new H4(strUsername);

        VerticalLayout layoutMember = new VerticalLayout();
        layoutMember.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.LARGE,
                LumoUtility.Margin.LARGE,
                LumoUtility.Gap.MEDIUM
        );
        layoutMember.add(imgAvatar, objName, objMember);

        return layoutMember;
    }

    public String getAppProps(String prop) {

        for (int r = 0; r < recProps.size(); r++) {
            String strProp = recProps.get(r).getColumnData("propName");
            String strValue = recProps.get(r).getColumnData("propValue");
            if (prop.equalsIgnoreCase(strProp)) {
                return strValue;
            } else {
                return null;
            }
        }
        return null;
    }


/*    public String[] getLocationCoordinates(String destination, String country) {
        WeatherService weatherService = new WeatherService("metric");
        String[] locations = weatherService.lookUpLocation(destination, "", country);
        return locations;
    }*/

/*
    public VerticalLayout getWeatherApiCurrent(String destination, String country) {

        WeatherImageService weatherImage = new WeatherImageService();
        WeatherService weatherService = new WeatherService("metric");

        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );

        Anchor apiLink = new Anchor();
        apiLink.getStyle().setColor("#8b94a0");
        apiLink.setClassName("lazy-api-link");
        apiLink.setHref(weatherService.getUrlReference());
        apiLink.setTarget("_blank");
        apiLink.setText("Weather data by: " + weatherService.getTitleReference());

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);

        layout.add(layoutWeather, apiLink);

        try {
            Weather weatherCurrent = weatherService.getApiCurrent(destination, country); //getCurrentWeatherDataMetric(locations);


//        layoutWeather.addClassName("lazy-card-overview-min-space");
            //layoutWeather.addClassName("lazy-card-overview-border-solid");

//        WeatherService weatherService = new WeatherService("metric");

//        String[] locations = weatherService.lookUpLocation(destination, "", country);
//        if (locations != null) {
//            for (int i = 0; i < locations.length; i++) {
//                logger.info("locations  " + locations[i]);
//            }
//            String[] currentWeatherData = weatherService.getCurrentWeatherDataMetric(locations);
            //String[][] dailyForecast =weatherService.getDailyForecastMetric(locations);

//        layoutWeather.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        layoutWeather.getStyle().setJustifyContent(Style.JustifyContent.SPACE_AROUND);

            VerticalLayout layoutLeft = new VerticalLayout();
            layoutLeft.setMargin(false);
            layoutLeft.setSpacing(false);
            layoutLeft.setPadding(false);


            Image imageWeather = new Image();
            imageWeather.getStyle().setOpacity("62%");

//            StreamResource iconWeather = new StreamResource(currentWeatherData[5],
//                    () -> getClass().getResourceAsStream(weatherImage.weatherImage(currentWeatherData)));
            String imgPath = weatherImage.getImageApiWeather(weatherCurrent);
            imageWeather.setSrc(DownloadHandler.forClassResource(getClass(), imgPath));

            imageWeather.setWidth("80px");
            imageWeather.setHeight("auto");
            imageWeather.setAlt(weatherCurrent.getWeatherState().getDescription());

            VerticalLayout layoutRight = new VerticalLayout();
            layoutRight.setMinWidth("200px");
            layoutRight.setMargin(false);
            layoutRight.setSpacing(false);
            layoutRight.setPadding(false);

            layoutLeft.add(imageWeather);
            layoutLeft.setSizeFull();
            Div hTemp = new Div(weatherCurrent.getTemperature().getValue() + " " + weatherCurrent.getTemperature().getUnit());
            hTemp.addClassName("lazy-card-overview-font-big");
            hTemp.getStyle().setFontSize("26px");
            hTemp.getStyle().setFontWeight(Style.FontWeight.BOLDER);
            layoutLeft.add(hTemp);

            Div hConditionName = new Div(weatherCurrent.getWeatherState().getWeatherConditionEnum().getName());
            hConditionName.addClassName("lazy-card-overview-font-big");
            hConditionName.getStyle().setFontSize("16px");
            hConditionName.getStyle().setFontWeight(Style.FontWeight.BOLD);
//        Div hConditionDescr = new Div(weatherCurrent.getWeatherState().getWeatherConditionEnum().getDescription());
//        hConditionDescr.addClassName("lazy-card-overview-font-big");
//        hConditionDescr.getStyle().setFontSize("14px");

            layoutLeft.add(hConditionName);

//        Div divTime = new Div(currentWeatherData[14]);
//        divTime.getStyle().setFontWeight(Style.FontWeight.BOLDER);

//        Div divSunRise = new Div(currentWeatherData[12]);
//        divSunRise.getStyle().setFontWeight(Style.FontWeight.BOLD);
//
//        Div divSunset = new Div(currentWeatherData[13]);
//        divSunset.getStyle().setFontWeight(Style.FontWeight.BOLD);

//        String strIconSize = "35px";

//        Image imageSunrise = new Image();
//        StreamResource iconSunrise = new StreamResource("Sunrise",
//                () -> getClass().getResourceAsStream("/icons/sunrise.png"));
//        imageSunrise.setSrc(iconSunrise);
//        imageSunrise.setAlt("Sunrise");
////        imageSunrise.setClassName("lazy-card-travel-weather-icons");
//        imageSunrise.getStyle().setWidth(strIconSize);
//        imageSunrise.getStyle().setHeight(strIconSize);
//
//        Image imageSet = new Image();
//        StreamResource iconSunset = new StreamResource("Sunset",
//                () -> getClass().getResourceAsStream("/icons/sunset.png"));
//        imageSet.setSrc(iconSunset);
//        imageSet.setAlt("Sunset");
////        imageSet.setClassName("lazy-card-travel-weather-icons");
//        imageSet.getStyle().setWidth(strIconSize);
//        imageSet.getStyle().setHeight(strIconSize);
//

//        Div divTime = new Div(weatherCurrent.getCalculationTime().getDayOfWeek() + "");
//        divTime.getStyle().setFontSize("11px");

//        Div divToday = new Div("Today");
//        layoutRight.add(new HorizontalLayout(divTime));
//        layoutRight.add(new HorizontalLayout(new Div("Sunrise: "), divSunRise));
//        layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));
            // layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));


            Div divL = new Div(weatherCurrent.getTemperature().getMinTemperature() + "" + weatherCurrent.getTemperature().getUnit());
            divL.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divH = new Div(weatherCurrent.getTemperature().getMaxTemperature() + "" + weatherCurrent.getTemperature().getUnit());
            divH.getStyle().setFontWeight(Style.FontWeight.BOLDER);


            Div divFeelsLike = new Div(weatherCurrent.getTemperature().getFeelsLike() + " " + weatherCurrent.getTemperature().getUnit());
            divFeelsLike.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divHumidity = new Div(weatherCurrent.getHumidity().getValue() + " " + weatherCurrent.getHumidity().getUnit());
            divHumidity.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divWindSpeed = new Div(weatherCurrent.getWind().getSpeed() + "m/s");
            divWindSpeed.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divWindDegrees = new Div(weatherCurrent.getWind().getDegrees() + "d");
            divWindDegrees.getStyle().setFontWeight(Style.FontWeight.BOLDER);
//        Div divWindUnit = new Div("m/s");

            Div divClouds = new Div(weatherCurrent.getClouds().getValue() + " " + weatherCurrent.getClouds().getUnit());
            divClouds.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divRain = new Div();
            String strRain = "";


//                Div divVisibility = new Div(weatherForecast.get);
//                divVisibility.getStyle().setFontWeight(Style.FontWeight.BOLDER);


//            layoutRight.add(new HorizontalLayout(new Div("L: "), divL, new Div("H: "), divH));

            layoutRight.add(new HorizontalLayout(new Div("Feels like: "), divFeelsLike));
            layoutRight.add(new HorizontalLayout(new Div("L:"), divL, new Div("H:"), divH));
            layoutRight.add(new HorizontalLayout(new Div("Clouds: "), divClouds));

            if (weatherCurrent.getRain() != null) {
                divRain.setText(weatherCurrent.getRain().getOneHourLevel() + " " + weatherCurrent.getRain().getUnit() + " (in 1h)");
                divRain.getStyle().setFontWeight(Style.FontWeight.BOLDER);
                layoutRight.add(new HorizontalLayout(new Div("Rain: "), divRain));
            }

            layoutRight.add(new HorizontalLayout(new Div("Humidity: "), divHumidity));
            layoutRight.add(new HorizontalLayout(new Div("Wind: "), divWindSpeed, new Div("  "), divWindDegrees));

            layoutWeather.add(layoutLeft, layoutRight);

        } catch (NoDataFoundException e) {
            layout.removeAll();
            layout.setVisible(false);
        }

        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);

        return layout;
    }
*/

/*
    public VerticalLayout getWeatherApiForecast(String destination, String country, int countOfTimeStamps) {

        WeatherService weatherServiceForecast = new WeatherService("metric");

        List<WeatherForecast> weatherForecasts = weatherServiceForecast.getApiForecast(destination, country, countOfTimeStamps); //getCurrentWeatherDataMetric(locations);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);

        for (int f = 0; f < weatherForecasts.size(); f++) {

            HorizontalLayout layoutWeather = new HorizontalLayout();
            layoutWeather.getStyle().setColor("#8b94a0");
            layoutWeather.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
            );

            logger.info(f + " -> " + weatherForecasts.get(f).getForecastTime().getDayOfWeek() + " - " + weatherForecasts.get(f).getForecastTime().getMonth() + "  //  " + weatherForecasts.get(f).getForecastTime().getHour() + ":" + weatherForecasts.get(f).getForecastTime().getMinute());

            WeatherForecast weatherForecast = weatherForecasts.get(f);

            VerticalLayout layoutLeft = new VerticalLayout();
            layoutLeft.setMargin(false);
            layoutLeft.setSpacing(false);
            layoutLeft.setPadding(false);

            WeatherImageService weatherImage = new WeatherImageService();

            Image imageWeather = new Image();
            imageWeather.getStyle().setOpacity("62%");

            String imgPath = weatherImage.getImageApiForecast(weatherForecast);
            imageWeather.setSrc(DownloadHandler.forClassResource(getClass(), imgPath));

            imageWeather.setMaxWidth("80px");
            imageWeather.setAlt(weatherForecast.getWeatherState().getWeatherConditionEnum().getName());

            VerticalLayout layoutRight = new VerticalLayout();
            layoutRight.setMinWidth("180px");
            layoutRight.setMargin(false);
            layoutRight.setSpacing(false);
            layoutRight.setPadding(false);

            layoutLeft.add(imageWeather);
            layoutLeft.setSizeFull();
            Div hTemp = new Div(weatherForecast.getTemperature().getValue() + " " + weatherForecast.getTemperature().getUnit());
            hTemp.addClassName("lazy-card-overview-font-big");
            hTemp.getStyle().setFontSize("26px");
            hTemp.getStyle().setFontWeight(Style.FontWeight.BOLDER);
            layoutLeft.add(hTemp);

            Div hConditionName = new Div(weatherForecast.getWeatherState().getWeatherConditionEnum().getName());
            hConditionName.addClassName("lazy-card-overview-font-big");
            hConditionName.getStyle().setFontSize("16px");
            hConditionName.getStyle().setFontWeight(Style.FontWeight.BOLD);
//            Div hConditionDescr = new Div(weatherForecast.getWeatherState().getWeatherConditionEnum().getDescription());
//            hConditionDescr.addClassName("lazy-card-overview-font-big");
//            hConditionDescr.getStyle().setFontSize("14px");

            layoutLeft.add(hConditionName); //, hConditionDescr);

            Div divTime = new Div(weatherForecast.getForecastTime().getDayOfWeek() + " / " + weatherForecast.getForecastTime().getHour());
            divTime.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            String strIconSize = "35px";

//                Image imageSunrise = new Image();
//                StreamResource iconSunrise = new StreamResource("Sunrise",
//                        () -> getClass().getResourceAsStream("/icons/sunrise.png"));
//                imageSunrise.setSrc(iconSunrise);
//                imageSunrise.setAlt("Sunrise");
////        imageSunrise.setClassName("lazy-card-travel-weather-icons");
//                imageSunrise.getStyle().setWidth(strIconSize);
//                imageSunrise.getStyle().setHeight(strIconSize);
//
//                Image imageSet = new Image();
//                StreamResource iconSunset = new StreamResource("Sunset",
//                        () -> getClass().getResourceAsStream("/icons/sunset.png"));
//                imageSet.setSrc(iconSunset);
//                imageSet.setAlt("Sunset");
////        imageSet.setClassName("lazy-card-travel-weather-icons");
//                imageSet.getStyle().setWidth(strIconSize);
//                imageSet.getStyle().setHeight(strIconSize);


            Div divToday = new Div(weatherForecasts.get(f).getDayTime().toString());
            divToday.getStyle().setFontSize("11px");
            Div divNow = new Div(weatherForecasts.get(f).getForecastTime().getDayOfWeek() + " Hour: " + weatherForecasts.get(f).getForecastTime().getHour());
            divNow.getStyle().setFontSize("11px");
            layoutRight.add(new HorizontalLayout(divToday, divNow));
//                layoutRight.add(new HorizontalLayout(new Div("Sunrise: "), divSunRise));
//                layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));
            // layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));


//            Div divL = new Div(weatherForecast.getTemperature().getMinTemperature() + "" + weatherForecast.getTemperature().getUnit());
//            divL.getStyle().setFontWeight(Style.FontWeight.BOLDER);
//
//            Div divH = new Div(weatherForecast.getTemperature().getMaxTemperature() + "" + weatherForecast.getTemperature().getUnit());
//            divH.getStyle().setFontWeight(Style.FontWeight.BOLDER);


            Div divFeelsLike = new Div(weatherForecast.getTemperature().getFeelsLike() + weatherForecast.getTemperature().getUnit());
            divFeelsLike.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divHumidity = new Div(weatherForecast.getHumidity().getValue() + " " + weatherForecast.getHumidity().getUnit());
            divHumidity.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divWindSpeed = new Div(weatherForecast.getWind().getSpeed() + "m/s");
            divWindSpeed.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divWindDegrees = new Div(weatherForecast.getWind().getDegrees() + "d");
            divWindDegrees.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divClouds = new Div(weatherForecast.getClouds().getValue() + " " + weatherForecast.getClouds().getUnit());
            divClouds.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divRain = new Div();
            String strRain = "";


//                Div divVisibility = new Div(weatherForecast.get);
//                divVisibility.getStyle().setFontWeight(Style.FontWeight.BOLDER);


//            layoutRight.add(new HorizontalLayout(new Div("L: "), divL, new Div("H: "), divH));

            layoutRight.add(new HorizontalLayout(new Div("Feels like: "), divFeelsLike));
            layoutRight.add(new HorizontalLayout(new Div("Clouds: "), divClouds));

            if (weatherForecast.getRain() != null) {
                divRain.setText(weatherForecast.getRain().getThreeHourLevel() + " " + weatherForecast.getRain().getUnit() + " (in 3h)");
                divRain.getStyle().setFontWeight(Style.FontWeight.BOLDER);
                layoutRight.add(new HorizontalLayout(new Div("Rain: "), divRain));
            }

            layoutRight.add(new HorizontalLayout(new Div("Humidity: "), divHumidity));
            layoutRight.add(new HorizontalLayout(new Div("Wind: "), divWindSpeed, new Div("  "), divWindDegrees));

            layoutWeather.add(layoutLeft, layoutRight);

            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);

            layout.add(layoutWeather);
        }

        Anchor apiLink = new Anchor();
        apiLink.getStyle().setColor("#8b94a0");
        apiLink.setClassName("lazy-api-link");
        apiLink.setHref(weatherServiceForecast.getUrlReference());
        apiLink.setTarget("_blank");
        apiLink.setText("Weather data by: " + weatherServiceForecast.getTitleReference());

        layout.add(apiLink);
        return layout;
    }
*/


    public Image getAvatarThumbImage(String strAvatarPath, String altDescr, String width, String height) {

        String strAvatarFullPath = getAppProps(PROP_PHOTOS) + dirChar + SUB_PATH_AVATARS_THUMBS + dirChar + strAvatarPath;
        Path path = Paths.get(strAvatarFullPath);
        File file = path.toFile();

        Image image = new Image();
        image.setWidth(width);
        image.setHeight(height);
        image.addClassNames(LumoUtility.BorderRadius.FULL);
        image.setAlt(altDescr);
        image.setSrc(DownloadHandler.forFile(file));

        return image;
    }

    public Dialog showCarouselDialog(int isType, String sqlRead, String sqlReadOrderBy, String[] arrColumnNames, String strSelection, String filterColumn,
                                     String sqlReadSelection, String[] arrColumnNamesSelection, String strPhotoId, String strAlbumUsername, boolean isOnlyRating,
                                     Runnable onRatingSaved) {

        dlgCarousel = new Dialog();
        dlgCarousel.setDraggable(true);
        dlgCarousel.setResizable(true);
        dlgCarousel.setHeightFull();
        dlgCarousel.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.NONE);
        dlgCarousel.setCloseOnOutsideClick(true);
        dlgCarousel.setCloseOnEsc(true);

        /*HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.BETWEEN);
        Div divTitle = new Div("-");

        Button btnClose = new Button();
        btnClose.setIcon(VaadinIcon.CLOSE.create());
        btnClose.addClickListener(event -> {
            dlgCarousel.close();
        });

        layoutTitle.add(divTitle, btnClose);

        dlgCarousel.add(layoutTitle);
         */
        dlgCarousel.add(loadCarouselWithThumbnails(isType, sqlRead, sqlReadOrderBy, arrColumnNames, strSelection, filterColumn,
                sqlReadSelection, arrColumnNamesSelection, strPhotoId, strAlbumUsername, isOnlyRating, onRatingSaved));
        return dlgCarousel;
    }

    public VerticalLayout loadCarouselWithThumbnails(int isType, String sqlRead, String sqlReadOrderBy, String[] arrColumnNames, String strSelection,
                                                     String filterColumn, String sqlReadSelection, String[] arrColumnNamesSelection, String strPhotoId,
                                                     String strAlbumUsername, boolean isOnlyRating, Runnable onRatingSaved) {
        String sqlReadPhotos = "";

        if (strSelection.isEmpty()) {
            if (isType == 2 || isType == 3) {
                sqlReadPhotos = sqlRead;
            } else {
                sqlReadPhotos = sqlRead + " " + sqlReadOrderBy;
            }
        } else {
            if (isType == 2 || isType == 3) {

                sqlReadPhotos = sqlRead + " AND " + filterColumn + " LIKE '" + strSelection + "' ";
            } else if (isType == 1) {

                sqlReadPhotos = sqlRead + " AND " + filterColumn + " LIKE '" + strSelection + "' ";
            }
        }

        arrColumnsGallery = arrColumnNames;

        VerticalLayout layoutPhotoInfo = new VerticalLayout();

        divCarousel = new Div();
        divCarousel.addClassNames(LumoUtility.Overflow.SCROLL,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL, // <--
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.LARGE);

        VerticalLayout layoutAll = new VerticalLayout();
        layoutAll.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.NONE);

        scroller = new Scroller();

        List<Record> lstImageFiles = getRecordsFromDb(sqlRead, arrColumnNames);
        Record record = null;
        for (int r = 0; r < lstImageFiles.size(); r++) {
            String strPhotoIdDb = lstImageFiles.get(r).getColumnData("id");
            if (strPhotoIdDb.equalsIgnoreCase(strPhotoId)) {
                record = lstImageFiles.get(r);
            }
        }

        String strMetaOrientation = record.getColumnData("meta_orientation");
        Image imageLarge = fetchPhotosLarge(sqlReadPhotos, arrColumnsGallery, strPhotoId);
        imageLarge.addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.FULL);

        if (strMetaOrientation.equalsIgnoreCase("8")) {
//            imageLarge.getStyle().set("rotate", "-90deg");
        } else if (strMetaOrientation.equalsIgnoreCase("6")) {
//            imageLarge.getStyle().set("rotate", "90deg");
        }
        imageLarge.getStyle().set("object-fit", "contain");
        imageLarge.addClassName("image-to-show");


        layoutMeta = new VerticalLayout();
        layoutMeta.addClassNames(
                LumoUtility.Height.FULL, LumoUtility.Width.FULL,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL
        );

        final Record finalRec = record;
        HorizontalLayout layoutTabSelect = new HorizontalLayout();
        layoutTabSelect.addClassName("thin-tab-select");
        RadioButtonGroup<String> btnGroupSelect = new RadioButtonGroup<>();

            btnGroupSelect.setItems("Meta Data", "Rate");

        final String nameNew = record.getColumnData("name_new");

        btnGroupSelect.addValueChangeListener(event -> {
            if (event.getSource().getValue().contains("Meta")) {
                layoutMeta.removeAll();
                layoutMeta.add(fetchPhotoCreatorOnCarousel(finalRec, false));
                layoutMeta.add(fetchPhotoMetaInfoOnCarousel(finalRec));
            } else {
                layoutMeta.removeAll();
                layoutMeta.add(fetchPhotoCreatorOnCarousel(finalRec, false));
                layoutMeta.add(loadPanelRate(strPhotoId, nameNew, onRatingSaved));

            }
        });
        layoutTabSelect.add(btnGroupSelect);

        if(isOnlyRating){
            btnGroupSelect.setValue("Rate");
        }else {
            btnGroupSelect.setValue("Meta Data");
        }

        imageLarge.getStyle().setOpacity("1");
        divCarousel.add(imageLarge);

        layoutPhotoInfo.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Height.FULL,// LumoUtility.Width.FULL,  //must be comment, look bellow -> setWidth
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.START);
        layoutPhotoInfo.setWidth("420px");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClassNames(LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);

        Button btnClose = new Button();
        btnClose.setIcon(VaadinIcon.CLOSE.create());
        btnClose.addClickListener(event -> {
            dlgCarousel.close();
        });

        horizontalLayout.add(layoutTabSelect, btnClose);
        layoutPhotoInfo.add(horizontalLayout, layoutMeta);

        HorizontalLayout layoutThumbsFull = new HorizontalLayout();
        layoutThumbsFull.addClassName("layout-thumbs");
        layoutThumbsFull.setVisible(false);

        if (!isOnlyRating) {

            List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadSelection, arrColumnNamesSelection);
            ArrayList<String> lstDestinations = new ArrayList<>();
            for (int r = 0; r < lstDestinationRecs.size(); r++) {
                String strDestination = "";
                if (isType == 1) {
                    strDestination = lstDestinationRecs.get(r).getColumnData("title");
                } else if (isType == 2) {
                    strDestination = lstDestinationRecs.get(r).getColumnData("city_name");
                } else if (isType == 3) {
                    strDestination = lstDestinationRecs.get(r).getColumnData("subject_name");
                }
                if (strDestination.trim().isEmpty() || strDestination.trim().equalsIgnoreCase("null")) {
                } else {
                    lstDestinations.add(strDestination);
                }
            }

            HorizontalLayout layoutAlbumController = new HorizontalLayout();
            layoutAlbumController.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE, LumoUtility.Padding.NONE
            );
            Select<String> cmbAlbum = new Select<>();
            cmbAlbum.setWidthFull();

            Button btnLeft = new Button();
            btnLeft.setIcon(FontAwesome.Solid.ARROW_LEFT_LONG.create());
            Button btnRight = new Button();
            btnRight.setIcon(FontAwesome.Solid.ARROW_RIGHT_LONG.create());
            layoutAlbumController.add(cmbAlbum);

            cmbAlbum.setItems(lstDestinations);

            if (cmbAlbum.getValue() == null || cmbAlbum.getValue().equalsIgnoreCase("null") || cmbAlbum.getValue().isEmpty()) {
                cmbAlbum.setValue(strSelection);
            }

            cmbAlbum.addValueChangeListener(event -> {
                HorizontalLayout layoutLocationThumbs = selectLocation(event.getValue(), sqlRead, sqlReadOrderBy, filterColumn);
                scroller.setContent(layoutLocationThumbs);
            });

            VerticalLayout layoutTitle = new VerticalLayout();
            layoutTitle.addClassNames(LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL,
                    LumoUtility.Gap.XSMALL,
                    LumoUtility.BorderRadius.MEDIUM
            );
            Div divTitle = new Div();
            divTitle.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);
            if (isType == 1) {
                divTitle.setText("Albums of " + strAlbumUsername);
            } else if (isType == 2) {
                divTitle.setText("Location");
            } else if (isType == 3) {
                divTitle.setText("Subject");
            }
            layoutTitle.add(divTitle, cmbAlbum);


            layoutPhotoInfo.add(layoutTitle);

            scroller.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);

            HorizontalLayout layoutThumbs = fetchThumbs(sqlReadPhotos, arrColumnsGallery);
           scroller.setContent(layoutThumbs);


            Button btnThumbsLeft = new Button();
            btnThumbsLeft.addClassNames(LumoUtility.Height.FULL);
            btnThumbsLeft.setIcon(FontAwesome.Solid.ARROW_LEFT.create());

            Button btnThumbsRight = new Button();
            btnThumbsRight.addClassNames(LumoUtility.Height.FULL);
            btnThumbsRight.setIcon(FontAwesome.Solid.ARROW_RIGHT.create());

            layoutThumbsFull.add(scroller);
            layoutThumbsFull.setVisible(true);
        }

        HorizontalLayout layoutCarouselAndInfo = new HorizontalLayout();
        layoutCarouselAndInfo.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE
        );

        layoutCarouselAndInfo.add(divCarousel, layoutPhotoInfo);

        List<Record> lstRecord = getRecordsFromDb(sqlReadPhotos, arrColumnsGallery);
        Record recDestination = lstRecord.get(0);
        String strCityName = recDestination.getColumnData("album_destination_name_map");
        String strCountry = recDestination.getColumnData("album_destination_country_map");

        //      Div divLocation = new Div(strCityName + " " + strCountry);

//        layoutMap = new VerticalLayout();
//
//        layoutMap.addClassName("image-to-show");
//        layoutMap.add(fetchMapOnCarousel(strCityName, strCountry));
//        layoutMap.getStyle().setOpacity("1");

        if (strSelection == null || strSelection.isEmpty() || strSelection.equalsIgnoreCase("null")) {
//            layoutCarouselAndInfo.setHeight("600px");
        } else {
//            layoutCarouselAndInfo.setHeight("600px");
        }

        VerticalLayout layoutPhotosView = new VerticalLayout();
        layoutPhotosView.addClassNames(
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);
        layoutPhotosView.add(layoutCarouselAndInfo, layoutThumbsFull);


        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            int intHeight = details.getWindowInnerHeight();
            logger.warn("intHeight: " + intHeight);
        });

        layoutAll.add(layoutPhotosView);

        return layoutAll;
    }

    private ArrayList<Image> fetchPhotoThumbs(String sqlRead, String[] arrColumnNames) {

        ArrayList<Image> lstImage = new ArrayList<>();
        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record record = lstRecords.get(r);
            lstImage.add(getImageThumb(record));
        }
        return lstImage;
    }

    private Image fetchPhotosLarge(String sqlRead, String[] arrColumnNames, String strPhotoId) {

        Image lstImage = new Image();
        List<Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {
            Record record = lstRecords.get(r);
            String strPhotoIdDb = lstRecords.get(r).getColumnData("id");
            if (strPhotoId == null) {
                lstImage = getImageLarge(record);
            } else if (strPhotoIdDb.equalsIgnoreCase(strPhotoId)) {
                lstImage = getImageLarge(record);
            }
        }
        return lstImage;
    }

    private VerticalLayout fetchPhotoMetaInfoOnCarousel(Record record) {

        VerticalLayout layoutPhotoInfo = new VerticalLayout();
        layoutPhotoInfo.addClassNames(LumoUtility.Overflow.SCROLL,
                LumoUtility.Width.FULL, //LumoUtility.Height.FULL,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START);
        layoutPhotoInfo.addClassName("member-profile-design");
        layoutPhotoInfo.addClassName("image-to-show");
        layoutPhotoInfo.getStyle().setOpacity("1");

        String strMetaCameraModel = record.getColumnData("meta_camera_model");
        String strMetaLensModel = record.getColumnData("meta_lens_model");

        String strMetaFocalLengthFF = record.getColumnData("meta_focal_length_ff");
        String strMetaFocalLength = record.getColumnData("meta_focal_length");
        String strMetaIso = record.getColumnData("meta_iso");
        String strMetaAperture = record.getColumnData("meta_aperture");
        String strMetaShutterSpeed = record.getColumnData("meta_shutter_speed");
        String strMetaDate = record.getColumnData("meta_date");
        String strMetaPhotoDate = record.getColumnData("photo_date");
        String strMetaPhotoTime = record.getColumnData("photo_time");


        VerticalLayout layoutPhotoCameraMeta = new VerticalLayout();
        layoutPhotoCameraMeta.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.NONE
        );
        Div divMetaCameraTitle = new Div("Camera");
        divMetaCameraTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaCamera = new Div(strMetaCameraModel);
        divMetaCamera.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        Div divMetaLensTitle = new Div("Lens");
        divMetaLensTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaLens = new Div(strMetaLensModel);
        divMetaLens.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        layoutPhotoCameraMeta.add(divMetaCameraTitle, divMetaCamera, divMetaLensTitle, divMetaLens);

        VerticalLayout layoutPhotoMeta = new VerticalLayout();
        layoutPhotoMeta.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.NONE
        );
        Div divFocalTitle = new Div("Focal Length");
        divFocalTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLength = new Div(strMetaFocalLength + " mm");
        divMetaFocalLength.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        if (strMetaFocalLength.equalsIgnoreCase("null")) {
            divFocalTitle.setVisible(false);
            divMetaFocalLength.setVisible(false);
        }

        Div divFocalFFTitle = new Div("Focal Length (Full Frame)");
        divFocalFFTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLengthFF = new Div(strMetaFocalLengthFF + " mm");
        divMetaFocalLengthFF.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        if (strMetaFocalLength.equalsIgnoreCase(strMetaFocalLengthFF) || strMetaFocalLengthFF.equalsIgnoreCase("null")) {
            divFocalFFTitle.setVisible(false);
            divMetaFocalLengthFF.setVisible(false);
        }

        Span divApertureTitle = new Span("   Aperture ");
        divApertureTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Span divMetaAperture = new Span(strMetaAperture);
        divMetaAperture.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL, LumoUtility.Gap.MEDIUM);
        divMetaAperture.add(divApertureTitle);
        if (strMetaAperture.equalsIgnoreCase("null")) {
            divApertureTitle.setVisible(false);
            divMetaAperture.setVisible(false);
        }

        Span divSSTitle = new Span("  Shutter Speed ");
        divSSTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Span divMetaSS = new Span(strMetaShutterSpeed + " sec");
        divMetaSS.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL, LumoUtility.Gap.MEDIUM);
        divMetaSS.add(divSSTitle);
        if (strMetaShutterSpeed.equalsIgnoreCase("null")) {
            divSSTitle.setVisible(false);
            divMetaSS.setVisible(false);
        }

        Span divIsoTitle = new Span("  ISO ");
        divIsoTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Span divMetaIso = new Span(strMetaIso);
        divMetaIso.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL, LumoUtility.Gap.MEDIUM);
        divMetaIso.add(divIsoTitle);

        Div divDateTimeTitle = new Div("Date/Time");
        divDateTimeTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaPhotoDate = new Div(strMetaPhotoDate + "  " + strMetaPhotoTime);
        divMetaPhotoDate.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);


        layoutPhotoMeta.add(divFocalTitle, divMetaFocalLength, divFocalFFTitle, divMetaFocalLengthFF, divMetaAperture,
                divMetaSS, divMetaIso, divDateTimeTitle, divMetaPhotoDate);

        layoutPhotoInfo.add(layoutPhotoCameraMeta, layoutPhotoMeta);

        return layoutPhotoInfo;
    }

    private VerticalLayout fetchPhotoCreatorOnCarousel(Record record, boolean showMinimum) {

        VerticalLayout layoutCreatorInfo = new VerticalLayout();
        layoutCreatorInfo.addClassNames(
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START);
        layoutCreatorInfo.addClassNames("member-profile-design");
        layoutCreatorInfo.addClassName("info-to-show");
        layoutCreatorInfo.setMaxHeight("160px");
//        layoutCreatorInfo.getStyle().setOpacity("1");

        String strCreatorId = record.getColumnData("uploaderId");
        String strUsername = record.getColumnData("username");
        String strName = record.getColumnData("name");
        String strSurname = record.getColumnData("surname");
        String strShortBio = record.getColumnData("short_bio");
        String strMemberSince = record.getColumnData("member_since");
        String strAvatarPath = record.getColumnData("avatar_path");
        String strResident = record.getColumnData("resident");
        String strResidentCountry = record.getColumnData("resident_country");

        String strCountPhotos = record.getColumnData("count_photos");
        String strCountAlbums = record.getColumnData("count_albums");

        Div divImgAvatar = new Div();
        divImgAvatar.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);

        String strAvatarSize = "50px";
        Image imageAvatar = getAvatarThumbImage(strAvatarPath, strUsername, strAvatarSize, strAvatarSize);
        divImgAvatar.add(imageAvatar);


        HorizontalLayout horizontalLayout = new HorizontalLayout();

        layoutCreatorInfo.getStyle().setOpacity("1");


        H4 objMember = new H4(strUsername);
        objMember.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.NORMAL, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        H4 objName = new H4(strName + " " + strSurname);
        objName.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        Div divMemberSince = new Div("Member since "+strMemberSince);
        divMemberSince.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.EXTRALIGHT, LumoUtility.FontSize.XSMALL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL);



        Icon iconPhoto = VaadinIcon.PICTURE.create();
        Icon iconAlbum = FontAwesome.Solid.PHOTO_FILM.create();
        Span spPhotos = new Span(" Photos");
        spPhotos.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divPhotos = new Span(strCountPhotos);
        divPhotos.add(spPhotos);
        divPhotos.addClassNames(LumoUtility.TextColor.SECONDARY);
        Span spAlbums = new Span(" Albums");
        spAlbums.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        Span divAlbums = new Span(strCountAlbums);
        divAlbums.addClassNames(LumoUtility.TextColor.SECONDARY);
        divAlbums.add(spAlbums);

        HorizontalLayout layoutCounts = new HorizontalLayout();
        layoutCounts.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                LumoUtility.Padding.SMALL, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Background.CONTRAST_5,
                LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);
        layoutCounts.add(iconPhoto, divPhotos, iconAlbum, divAlbums);

        VerticalLayout layoutMemberCard = new VerticalLayout();
//            layoutMemberCard.getStyle().setMaxWidth("300px");
//            layoutMemberCard.getStyle().set("border", "lightgrey 1px solid");
        layoutMemberCard.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        layoutMemberCard.setMaxWidth("60px");
        layoutMemberCard.add(divImgAvatar);

        Div divResidentCaption = new Div("Resident");
        Div divResident = new Div(strResident);
        divResident.addClassNames(LumoUtility.FontWeight.BOLD);

        VerticalLayout layoutAdditional = new VerticalLayout();
        layoutAdditional.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL);
        layoutAdditional.add(objMember, objName, divMemberSince); //, divBioTitle, divBio);//, divResidentCaption, divResident);

        horizontalLayout.add(layoutMemberCard, layoutAdditional);

        if(showMinimum){
            layoutCreatorInfo.add(horizontalLayout);
        }else {
            layoutCreatorInfo.add(horizontalLayout, layoutCounts);
        }

        return layoutCreatorInfo;
    }

    private VerticalLayout loadPanelRate(String strPhotoId, String nameNew, Runnable onRatingSaved) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        verticalLayout.addClassName("rating-content");
        verticalLayout.addClassName("info-to-show");
        verticalLayout.getStyle().setOpacity("1");

        // ── Average rating summary ─────────────────────────────────────────────
        int photoIdInt = 0;
        try { photoIdInt = Integer.parseInt(strPhotoId); } catch (NumberFormatException ignored) {}

        double avgRating = 0.0;
        long ratingCount = 0;
        if (photoRatingService != null) {
            avgRating = photoRatingService.getAverageRating(photoIdInt);
            ratingCount = photoRatingService.getRatingCount(photoIdInt);
        }

        SvgIcon svgRate = new SvgIcon(DownloadHandler.forClassResource(GalleryImageViewCard.class, "/icons/star-empty-icon.svg"));
        HorizontalLayout layoutSummary = new HorizontalLayout();
        layoutSummary.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Gap.SMALL, LumoUtility.Padding.XSMALL);
        Span spanAvg = new Span(ratingCount > 0
                ? String.format(" %.1f  (%d ratings)", avgRating, ratingCount)
                : "No ratings yet");
        spanAvg.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        layoutSummary.add(svgRate,spanAvg);

        // ── Auth check ─────────────────────────────────────────────────────────
        String authUserId = checkIfAuthMemberId();
        if (authUserId == null) {
            Span loginMsg = new Span("Please log in to rate this photo.");
            loginMsg.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.Background.ERROR_10,
                    LumoUtility.FontSize.SMALL, LumoUtility.TextColor.TERTIARY,
                    LumoUtility.Padding.SMALL);
            verticalLayout.add(layoutSummary, loginMsg);
            return verticalLayout;
        }


/*        String[] str1 = {"1 Snapshot", "Casual capture with minimal intent or craft."};
        String[] str2 = {"2 Adequate", "Technically acceptable image lacking strong visual intent."};
        String[] str3 = {"3 Competent", "Clear subject, balanced exposure, developing compositional awareness."};
        String[] str4 = {"4 Polished", "Intentional composition supported by effective light control."};
        String[] str5 = {"5 Compelling", "Engaging mood with confident artistic decision making."};
        String[] str6 = {"6 Exceptional", "Distinct vision executed with precision and emotional depth."};
        String[] str7 = {"7 World Class", "Iconic imagery demonstrating mastery, originality, and lasting impact."};*/

        String[] str1 = {"1 Snapshot", "Unplanned capture with minimal intent, weak composition, and technical flaws."};
        String[] str2 = {"2 Basic", "Technically acceptable image but lacks clear subject and visual direction."};
        String[] str3 = {"3 Competent", "Clear subject, balanced exposure, showing emerging compositional awareness and control."};
        String[] str4 = {"4 Polished", "Intentional composition, effective lighting, strong clarity, and cohesive visual storytelling."};
        String[] str5 = {"5 Compelling", "Powerful imagery with distinct vision, emotional impact, and confident artistic execution."};

        String[][] allRatings = { str5, str4, str3, str2, str1};

        RadioButtonGroup<String[]> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.addClassNames(LumoUtility.Width.FULL);
        radioButtonGroup.setRenderer(new ComponentRenderer<>(strings -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            H4 title = new H4(strings[0]);
            Span description = new Span(strings[1]);
            VerticalLayout column = new VerticalLayout(title, description);
            column.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL, LumoUtility.Gap.XSMALL);
            row.add(column);
            row.setWidthFull();
            return row;
        }));
        radioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioButtonGroup.addClassName("rating-options");
        radioButtonGroup.setItems( str5, str4, str3, str2, str1);

        // Pre-select existing user rating if any
        if (photoRatingService != null) {
            try {
                int userIdInt = Integer.parseInt(authUserId);
                int existing = photoRatingService.getUserRating(photoIdInt, userIdInt);
                if (existing > 0 && existing <= allRatings.length) {
                    radioButtonGroup.setValue(allRatings[existing - 1]);
                }
            } catch (NumberFormatException ignored) {}
        }

        // ── Status label ──────────────────────────────────────────────────────
        final Span spanStatus = new Span();
        spanStatus.addClassNames(LumoUtility.FontSize.SMALL);
        spanStatus.setVisible(false);

        // ── Submit button ─────────────────────────────────────────────────────
        final int finalPhotoId = photoIdInt;
        final String finalAuthUserId = authUserId;
        final String finalIp = (publicIp != null && !publicIp.isBlank()) ? publicIp : "unknown";
        final String finalNameNew = (nameNew != null) ? nameNew : "";

        Button btnRate = new Button("Submit Rating");
        btnRate.addClassName("btn-rate");
        btnRate.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
        btnRate.addClickListener(event -> {
            String[] selected = radioButtonGroup.getValue();
            if (selected == null) {
                spanStatus.setText("Please select a rating first.");
                spanStatus.getStyle().set("color", "var(--lumo-error-color)");
                spanStatus.setVisible(true);
                return;
            }
            int ratingValue = Character.getNumericValue(selected[0].charAt(0));
            if (photoRatingService != null) {
                try {
                    int userIdInt = Integer.parseInt(finalAuthUserId);
                    String sessionId = VaadinSession.getCurrent().getSession().getId();
                    long sessionCreationMs = VaadinSession.getCurrent().getSession().getCreationTime();
                    LocalDateTime sessionDateTime = new UtilsDate().calcDateTimeFromLongInLDT(sessionCreationMs, "UTC");
                    photoRatingService.saveOrUpdateRating(finalPhotoId, userIdInt, ratingValue, finalNameNew, finalIp,
                            sessionId, sessionDateTime);
                    // Close dialog and notify card to refresh its stats row
                    dlgCarousel.close();
                    if (onRatingSaved != null) onRatingSaved.run();
                } catch (Exception e) {
                    logger.error("Error saving rating: " + e.getMessage());
                    spanStatus.setText("Could not save rating. Please try again.");
                    spanStatus.getStyle().set("color", "var(--lumo-error-color)");
                    spanStatus.setVisible(true);
                }
            }
        });

        verticalLayout.add(layoutSummary, radioButtonGroup, btnRate, spanStatus);
        return verticalLayout;
    }

    private VerticalLayout fetchMapOnCarousel(String strCity, String strCountry) {

        VerticalLayout layoutInnerMap = new VerticalLayout();

        if (strCity == null || strCity.isEmpty() || strCity.equalsIgnoreCase("null")) {

            //    layoutMap.setHeight("0px");
        } else {


            layoutInnerMap.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Padding.SMALL, LumoUtility.Margin.NONE, LumoUtility.Gap.XSMALL);

            IFrame frameMap = getDestinationMap(strCity, strCountry);
            frameMap.setWidth("90%");
            frameMap.setMaxWidth("1080px");
            frameMap.setHeight("600px");
            frameMap.addClassName("image-meta-to-show");
            frameMap.getStyle().setOpacity("1");


            layoutInnerMap.add(frameMap);
        }
        return layoutInnerMap;
    }

    private HorizontalLayout selectLocation(String locationName, String sqlRead, String sqlReadOrderBy, String filterColumn) {
//        List<String> lstPhotoFilenames = getImagesFilenames(sqlRead, arrColumnNames);
        String sqlReadWithLocation = sqlRead;
        if (!locationName.isEmpty()) {
            sqlReadWithLocation = sqlRead + " AND " + filterColumn + " LIKE '" + locationName + "' " + sqlReadOrderBy;
        }


//        HorizontalLayout layoutThumbs = fetchThumbs(sqlReadWithLocation, arrColumnsGallery);
//        layoutThumbs.addClassNames(LumoUtility.Width.FULL);
//        scroller.setContent(layoutThumbs);

        HorizontalLayout layoutThumbs = fetchThumbs(sqlReadWithLocation, arrColumnsGallery);

        Image imageLarge = fetchPhotosLarge(sqlReadWithLocation, arrColumnsGallery, null);
        imageLarge.addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.FULL);
        imageLarge.getStyle().set("object-fit", "contain");

        imageLarge.addClassName("image-to-show");
        imageLarge.getStyle().setOpacity("1");
        divCarousel.add(imageLarge);

        Component imgPrevious = divCarousel.getComponentAt(0);
        imgPrevious.addClassName("image-to-hide");
        imgPrevious.getStyle().setOpacity("0");
        divCarousel.remove(imgPrevious);


        List<Record> lstRecord = getRecordsFromDb(sqlReadWithLocation, arrColumnsGallery);
        if (!lstRecord.isEmpty()) {
            Record recDestination = lstRecord.get(0);
            String strCityName = recDestination.getColumnData("album_destination_name_map");
            String strCountry = recDestination.getColumnData("album_destination_country_map");
        }

        return layoutThumbs;
    }

    private void selectThumb(List<Record> lstImageFiles, String sqlReadWithLocation, String strPhotoId) {


        Record record = null;
        for (int r = 0; r < lstImageFiles.size(); r++) {

            String strPhotoIdDb = lstImageFiles.get(r).getColumnData("id");
            if (strPhotoIdDb.equalsIgnoreCase(strPhotoId)) {
                record = lstImageFiles.get(r);
            }
        }


        Image imageLarge = fetchPhotosLarge(sqlReadWithLocation, arrColumnsGallery, strPhotoId);
        imageLarge.addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.FULL);
        imageLarge.getStyle().set("object-fit", "contain");


        imageLarge.addClassName("image-to-show");
        imageLarge.getStyle().setOpacity("1");
        divCarousel.add(imageLarge);

        Component imgPrevious = divCarousel.getComponentAt(0);
        imgPrevious.addClassName("image-to-hide");
        imgPrevious.getStyle().setOpacity("0");
        divCarousel.remove(imgPrevious);


        layoutMeta.removeAll();
        layoutMeta.add(fetchPhotoCreatorOnCarousel(record,false));
        layoutMeta.add(fetchPhotoMetaInfoOnCarousel(record));


    }

    private HorizontalLayout fetchThumbs(String sqlRead, String[] arrColumnNames) {

        ArrayList<Image> lstImageThumbs = fetchPhotoThumbs(sqlRead, arrColumnNames);
        List<Record> lstImageFiles = getRecordsFromDb(sqlRead, arrColumnNames);


        HorizontalLayout layoutThumbs = new HorizontalLayout();
        layoutThumbs.addClassNames(
                //  don't   LumoUtility.Width.FULL, //LumoUtility.Height.FULL,
                LumoUtility.Display.INLINE_FLEX,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);



        for (int t = 0; t < lstImageThumbs.size(); t++) {
            Div divBtnPhoto = new Div();
            divBtnPhoto.addClassName("btn-thumb-photo");

            Image imageThumb = lstImageThumbs.get(t);
            imageThumb.addClassNames(
                    LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                    LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                    LumoUtility.BorderRadius.SMALL
            );

            divBtnPhoto.add(imageThumb);

            final int tFinal = t;
            divBtnPhoto.addClickListener(click -> {
                selectThumb(lstImageFiles, sqlRead, lstImageFiles.get(tFinal).getColumnData("id"));
            });
            layoutThumbs.add(divBtnPhoto);
        }

        return layoutThumbs;
    }

    private IFrame getDestinationMap(String city, String country) {

        String strHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<title>Add a marker using a place name</title>\n" +
                "<meta name=\"viewport\" content=\"initial-scale=1,maximum-scale=1,user-scalable=no\">\n" +
                "<link href=\"https://api.mapbox.com/mapbox-gl-js/v3.7.0/mapbox-gl.css\" rel=\"stylesheet\">\n" +
                "<script src=\"https://api.mapbox.com/mapbox-gl-js/v3.7.0/mapbox-gl.js\"></script>\n" +
                "<style>\n" +
                "body { margin: 0; padding: 0; }\n" +
                "#map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"map\"></div>\n" +
                "\n" +
                "<script src=\"https://unpkg.com/@mapbox/mapbox-sdk/umd/mapbox-sdk.min.js\"></script>\n" +
                "\n" +
                "<script>\n" +
                "\tmapboxgl.accessToken = 'pk.eyJ1Ijoibmlja2dpY2siLCJhIjoiY20xcm9nMTZ5MGJsNDJzczM1aWk0Mm1zdCJ9.qSV85DCU8ewpGjTA3uajpg';\n" +
                "    const mapboxClient = mapboxSdk({ accessToken: mapboxgl.accessToken });\n" +
                "    mapboxClient.geocoding\n" +
                "        .forwardGeocode({\n" +
                "            query: '" + city + ", " + country + "',\n" +
                "            autocomplete: false,\n" +
                "            limit: 1\n" +
                "        })\n" +
                "        .send()\n" +
                "        .then((response) => {\n" +
                "            if (\n" +
                "                !response ||\n" +
                "                !response.body ||\n" +
                "                !response.body.features ||\n" +
                "                !response.body.features.length\n" +
                "            ) {\n" +
                "                console.error('Invalid response:');\n" +
                "                console.error(response);\n" +
                "                return;\n" +
                "            }\n" +
                "            const feature = response.body.features[0];\n" +
                "\n" +
                "            const map = new mapboxgl.Map({\n" +
                "                container: 'map',\n" +
                "                // Choose from Mapbox's core styles, or make your own style with Mapbox Studio\n" +
                "                style: 'mapbox://styles/mapbox/streets-v12',\n" +
                "                center: feature.center,\n" +
                "                zoom: 12\n" +
                "            });\n" +
                "\n" +
                "    // Add the control to the map.\n" +
                "    map.addControl(\n" +
                "        new MapboxGeocoder({\n" +
                "            accessToken: mapboxgl.accessToken,\n" +
                "            language: 'en-GB',\n" +
                "            mapboxgl: mapboxgl\n" +
                "        })\n" +
                "    );\n" +
                "\n" +
                "            // Create a marker and add it to the map.\n" +
                "            new mapboxgl.Marker().setLngLat(feature.center).addTo(map);\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "    map.addControl(new mapboxgl.FullscreenControl());\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        //String mapSrc = "https://api.mapbox.com/search/geocode/v6/forward?q=budapest&proximity=ip&access_token=pk.eyJ1Ijoibmlja2dpY2siLCJhIjoiY20xcm9nMTZ5MGJsNDJzczM1aWk0Mm1zdCJ9.qSV85DCU8ewpGjTA3uajpg";

        //String strMaps =
//"<iframe width='100%' height='400px' src=\""+mapSrc+"\" title=\"Navigation\" style=\"border:none;\"></iframe>";

        IFrame mapsFrame = new IFrame();
        mapsFrame.setSrcdoc(strHtml);
        mapsFrame.setWidthFull();
        mapsFrame.getStyle().setBorder("0px");
        mapsFrame.getStyle().setBorderRadius("6px");


        return mapsFrame;
    }

    public boolean checkIfMemberValueExists(String strField, String strValue) {


        String sqlCheckEmail = "SELECT " + strField + " FROM dbuser WHERE " + strField + " = ? ";
        String[] arrColEmail = {strField};
        Object[] objEmail = {strValue};
        String[] arrTypeEmail = {"java.lang.String"};

        List<Record> lstEmail = recordService.findAll(sqlCheckEmail, arrColEmail, objEmail, arrTypeEmail);

        logger.info(!lstEmail.isEmpty() + "  " + strValue);
        return !lstEmail.isEmpty();

    }

    public String checkIfCodeExistsOrAppliedForMember(String strValue, String userId) {


        String sqlCheckEmail = "SELECT avail_code, until_date, CURRENT_TIMESTAMP() FROM avail_code WHERE avail_code = ? AND until_date >= CURRENT_TIMESTAMP();";
        String[] arrColEmail = {"avail_code"};
        Object[] objEmail = {strValue};
        String[] arrTypeEmail = {"java.lang.String"};

        List<Record> lstEmail = recordService.findAll(sqlCheckEmail, arrColEmail, objEmail, arrTypeEmail);

        String strApplied = "Code is OK!";

        // 0 is a user not created
        if (userId.equalsIgnoreCase("0")) {
            if (lstEmail.size() == 1) {
                logger.info("OKKKKKKKK  " + strApplied);
                return strApplied;
            } else {
                logger.info("code not found");
                return "Code not found!";
            }
        } else {


            logger.info(!lstEmail.isEmpty() + "  " + strValue);

            String sqlCheckUserCode = "SELECT code, date_entered FROM dbuser_code WHERE code = ? and user_Id";
            String[] arrColUserCode = {"code"};
            Object[] objUserCode = {strValue};
            String[] arrUserCodeType = {"java.lang.String"};

            List<Record> lstUserCode = recordService.findAll(sqlCheckUserCode, arrColUserCode, objUserCode, arrUserCodeType);

            if (lstUserCode.isEmpty()) {
                return "Code can be Applied!";
            } else {
                return "Has been applied in the past. You cannot use it again!";
            }
        }
    }

    public void logVisitorToDb(String section, String logText) {


//        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
//            // This is your own method that you may do something with the url.
//            // Note that this method runs asynchronously
//
//            strUrlRequestToBeLogged = currentUrl.toExternalForm();
//
//        });

//        int[] availWidth = calcTotalAvailableWidth();


        sysUserName = System.getProperty("user.name");


        // String ipAddress = VaadinSession.getCurrent().getBrowser().getAddress();
        String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
        int versionOfBrowserMajor = VaadinSession.getCurrent().getBrowser().getBrowserMajorVersion();
        int versionOfBrowserMinor = VaadinSession.getCurrent().getBrowser().getBrowserMinorVersion();
        int intUiId = VaadinSession.getCurrent().getNextUIid();


//        int[] availWidth = calcTotalAvailableWidth();

        if (strUrlRequestToBeLogged == null || strUrlRequestToBeLogged.isEmpty() || strUrlRequestToBeLogged.equalsIgnoreCase("null")) {
            strUrlRequestToBeLogged = "NULL";
        } else {
            strUrlRequestToBeLogged = strUrlRequestToBeLogged.replace("'", "");
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

//        if (strPath == null || strPath.isEmpty()) {
//            strPath = "NULL";
//        } else {
//            strPath = strPath.replace("\\", "-");
//            strPath = strPath.replace("'", "");
//            strPath = "'" + strPath + "'";
//        }


        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "',  parentSection = 'photo',  sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = '" + logText + "', ref = " + strUrlRequestToBeLogged + ", "
                + " locale = '" + locale + "', localeName ='" + localeName + "' ";

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.massRecordInsert(lstQueryInsert, null, null);
    }

    public Image getImageLarge(Record record) {
        String strPathShow = getAppProps("dir-photos") + dirChar + subPathLarge;
        return getImageFromDb(record, strPathShow);
    }

    public Image getImageThumb(Record record) {
        String strPathThumbs = getAppProps("dir-photos") + dirChar + subPathThumbs;
        return getImageFromDb(record, strPathThumbs);
    }

    public Image getImageFromDb(Record record, String strPathIn) {
        strPath = strPathIn;

        String strFileName = record.getColumnData("name_new");
        String strTitle = record.getColumnData("title");
        String strSubTitle = record.getColumnData("subtitle");
        String strPhotoType = record.getColumnData("photo_type");
        String strMetaOrientation = record.getColumnData("meta_orientation");

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        if (strTitle == null || strTitle.isEmpty()) {
            strTitle = "image";
        }

        String strImagePath = strPathIn + dirChar + strFileName;
        //   logger.info(" strImagePath " + strImagePath);
//        Image image1 = new Image("https://images.unsplash.com/photo-1536048810607-3dc7f86981cb?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80", "img2");
        //GalleryImageViewCard imageGalleryViewCard = new GalleryImageViewCard(record,strImagePath,isMobile,userId, strUsername, sessionCreation,hostname,publicIp, isEditable, linkUploader, lstRouterLinks, recordService);
        Image image = new Image();
        image.addClassNames(
                LumoUtility.Width.FULL, LumoUtility.Height.FULL
        );

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                Path path = Paths.get(strImagePath);
                File file = path.toFile();
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
//                logErrorInDb(e, "GalleryImageViewCard StreamResource FileNotFoundException", hostname, userId, strUserName, publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error(e.getMessage());
            }
            return null;
        });
        image.setSrc(imageResource);



        if (strMetaOrientation.equalsIgnoreCase("8")) {
//            image.getStyle().set("rotate", "-90deg");
        }

        return image;
    }

    public List<String> getImagesFilenames(String sqlRead, String[] arrColumnNames) {

        ArrayList<String> lstImageFilename = new ArrayList<>();
        List<com.photo.act.photo_act.db.Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record record = lstRecords.get(r);
            String strFileName = record.getColumnData("name_new");
            String strTitle = record.getColumnData("title");
            String strSubTitle = record.getColumnData("subtitle");
            String strPhotoType = record.getColumnData("photo_type");

            String strCityName = record.getColumnData("city_name");
            String strUploader = record.getColumnData("uploader");
            lstImageFilename.add(strFileName);

        }

        return lstImageFilename;
    }

    public void getUserClientInfo() {

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostname = inetAddress.getHostName();
        hostAddress = inetAddress.getHostAddress();
        canonicalHostname = inetAddress.getCanonicalHostName();

        sessionid = VaadinSession.getCurrent().getSession().getId();
        sessionCreation = VaadinSession.getCurrent().getSession().getCreationTime();
        isMobile = VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone() || VaadinSession.getCurrent().getBrowser().isWindowsPhone();

        if (VaadinSession.getCurrent().getBrowser().isAndroid()) {
            strOS = "Android";
        } else if (VaadinSession.getCurrent().getBrowser().isIPhone()) {
            strOS = "iPhone";
        } else if (VaadinSession.getCurrent().getBrowser().isWindows()) {
            strOS = "Windows";
        } else if (VaadinSession.getCurrent().getBrowser().isLinux()) {
            strOS = "Linux";
        } else if (VaadinSession.getCurrent().getBrowser().isMacOSX()) {
            strOS = "Mac OS X";
        } else if (VaadinSession.getCurrent().getBrowser().isChromeOS()) {
            strOS = "ChromeOS";
        } else if (VaadinSession.getCurrent().getBrowser().isWindowsPhone()) {
            strOS = "WindowsPhone";
        } else {
            strOS = "Unknown";
        }

        if (VaadinSession.getCurrent().getBrowser().isChrome()) {
            strBrowser = "Chrome";
        } else if (VaadinSession.getCurrent().getBrowser().isFirefox()) {
            strBrowser = "Firefox";
        } else if (VaadinSession.getCurrent().getBrowser().isEdge()) {
            strBrowser = "Edge";
        } else if (VaadinSession.getCurrent().getBrowser().isSafari()) {
            strBrowser = "Safari";
        } else if (VaadinSession.getCurrent().getBrowser().isOpera()) {
            strBrowser = "Opera";
        } else if (VaadinSession.getCurrent().getBrowser().isIE()) {
            strBrowser = "IE";
        } else {
            strBrowser = "not known";
        }


        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            if (extendedClientDetails == null) {
                logger.info("Image gallery - error timeZoneId: Cannot retrieve client details:" + extendedClientDetails);
                return;
            }
            timeZoneId = extendedClientDetails.getTimeZoneId();
        });

        sessionDateTime = utilsDate.calcDateTimeFromLong(sessionCreation, "UTC");
        Locale loc = VaadinService.getCurrentRequest().getLocales().nextElement();
        locale = loc.getLanguage() + "." + loc.getCountry();
        localeName = loc.getDisplayName();

        NetUtils netUtils = new NetUtils();
        publicIp = netUtils.getClientPublicIp(hostname);

        final String[] urlHost = {"", "", "", "", "", "", "", ""};

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            strUrlRequestToBeLogged = currentUrl.toExternalForm();

            urlHost[0] = currentUrl.getHost();
            urlHost[1] = currentUrl.getProtocol();
            urlHost[2] = currentUrl.getRef();
            urlHost[3] = currentUrl.getUserInfo();
            urlHost[4] = currentUrl.toExternalForm();
            urlHost[5] = currentUrl.getPort() + "";
            urlHost[6] = currentUrl.getAuthority();
            urlHost[7] = currentUrl.getQuery();

            logger.info("  url:" + urlHost[0] + "  url:" + urlHost[1] + "  url:" + urlHost[2] + "  url:" + urlHost[3] + "  url:" + urlHost[4]
                    + "  url:" + urlHost[5] + "  url:" + urlHost[6] + "  url:" + urlHost[7]);
        });

    }

    public void logErrorInDb(Exception e, String function, String hostname, String strMemberId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, Integer.parseInt(strMemberId), strUsername, publicIp, Long.toString(sessionCreation), info);
    }

    public List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {
        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    public Footer loadFooter(boolean isMobile) {

        Footer footer = new Footer();
        footer.addClassNames(LumoUtility.Width.FULL);

//        Div logoLayout = new Div();
//        logoLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
//                LumoUtility.Width.FULL,
//                LumoUtility.Gap.XSMALL,
//                LumoUtility.Margin.NONE,
//                LumoUtility.Padding.MEDIUM
//        );

        H1 appName = new H1(APP_NAME);
        //appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD, TextColor.TERTIARY);
//        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.TERTIARY,
//                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
//        appName.getStyle().set("font-family", "Times-New-Roman, serif");
//        appName.getStyle().set("font-stretch", "semi-expanded");
//        appName.getStyle().setColor("#514c3f");
//        appName.getStyle().setColor("#eaeae8");//"#f9943b");//""#bd3450");

        Span cameraLogo = new Span();
        cameraLogo.add(VaadinIcon.CAMERA.create());
        // divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
//        divLogo.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.TERTIARY,
//                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
//        divLogo.getStyle().setColor("#514c3f");

        //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
        //divLogo.getStyle().setColor("#d64f00");


        Span divPhotoActMoto = new Span("[ Through Photography, We Connect and Act ]");
//        divPhotoActMoto.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD,
//                LumoUtility.Padding.NONE, LumoUtility.Margin.MEDIUM);

//        HorizontalLayout layoutLine = new HorizontalLayout();
//        if(isMobile) {
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.XSMALL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }else{
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }
//
//        layoutLine.add(divTitle);


//        VerticalLayout layoutFooter = new VerticalLayout();
//        layoutFooter.setMinHeight("250px");
//        layoutFooter.getStyle().setBackgroundColor("#8d8d8d"); //"#78868f");
//        layoutFooter.getStyle().setColor("#eaeae8");
////        layoutFooter.addClassName("bottom-radius-shadow");
//
//        if (isMobile) {
//            layoutFooter.addClassNames(
//                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
//                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
//                    LumoUtility.Margin.NONE,
//                    LumoUtility.Padding.NONE,
//                    LumoUtility.Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
////                    Background.CONTRAST_5,
//                    LumoUtility.BorderRadius.NONE);
//        } else {
//            layoutFooter.addClassNames(
//                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
//                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
//                    LumoUtility.Margin.NONE,
//                    LumoUtility.Padding.MEDIUM,
//                    LumoUtility.Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
////                    Background.CONTRAST_5,
//                    LumoUtility.BorderRadius.NONE);
//        }
//        layoutFooter.addClassName("footer");
        Div divLineBottom = new Div();
        divLineBottom.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.MEDIUM,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                LumoUtility.Background.PRIMARY,
                LumoUtility.BorderRadius.NONE);

        footer.add(cameraLogo, appName, divPhotoActMoto, divLineBottom);
        return footer;
    }


    public MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
                                   String label, String ariaLabel) {
        return createIconItem(menu, iconName, label, ariaLabel, false);
    }

    public MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
                                   String label, String ariaLabel, boolean isChild) {
        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }


}