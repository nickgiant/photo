package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.services.WeatherImageService;
import com.photo.act.photo_act.services.WeatherService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.photo.act.photo_act.views.MainLayout.APP_NAME;

public class GenericView {

    private static final Logger logger = LoggerFactory.getLogger(GenericView.class);

    public GenericView() {

    }

    public VerticalLayout getWeatherCurrent(String destination, String country) {
        HorizontalLayout layoutWeather = new HorizontalLayout();
        layoutWeather.getStyle().setColor("#8b94a0");
        layoutWeather.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER
        );
//        layoutWeather.addClassName("lazy-card-overview-min-space");
        //layoutWeather.addClassName("lazy-card-overview-border-solid");

        WeatherService weatherService = new WeatherService("metric");

        String[] locations = weatherService.lookUpLocation(destination, "", country);

        for (int i = 0; i < locations.length; i++) {
            logger.info("locations  " + locations[i]);
        }
        String[] currentWeatherData = weatherService.getCurrentWeatherDataMetric(locations);
        //String[][] dailyForecast =weatherService.getDailyForecastMetric(locations);

//        layoutWeather.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        layoutWeather.getStyle().setJustifyContent(Style.JustifyContent.SPACE_AROUND);

        VerticalLayout layoutLeft = new VerticalLayout();
        layoutLeft.setMargin(false);
        layoutLeft.setSpacing(false);
        layoutLeft.setPadding(false);

        WeatherImageService weatherImage = new WeatherImageService();

        Image imageWeather = new Image();
        imageWeather.getStyle().setOpacity("62%");

        StreamResource iconWeather = new StreamResource(currentWeatherData[5],
                () -> getClass().getResourceAsStream(weatherImage.weatherImage(currentWeatherData)));

        imageWeather.setSrc(iconWeather);
        imageWeather.setMaxWidth("80px");
        imageWeather.setAlt(currentWeatherData[5]);

        VerticalLayout layoutRight = new VerticalLayout();
        layoutRight.setMinWidth("180px");
        layoutRight.setMargin(false);
        layoutRight.setSpacing(false);
        layoutRight.setPadding(false);

        layoutLeft.add(imageWeather);
        layoutLeft.setSizeFull();
        Div hTemp = new Div(currentWeatherData[0]);
        hTemp.addClassName("lazy-card-overview-font-big");
        hTemp.getStyle().setFontSize("26px");
        hTemp.getStyle().setFontWeight(Style.FontWeight.BOLDER);
        layoutLeft.add(hTemp);

        Div hCondition = new Div(currentWeatherData[5]);
        hCondition.addClassName("lazy-card-overview-font-big");
        hCondition.getStyle().setFontSize("16px");
        hTemp.getStyle().setFontWeight(Style.FontWeight.BOLD);
        layoutLeft.add(hCondition);


        Div divTime = new Div(currentWeatherData[14]);
        divTime.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divSunRise = new Div(currentWeatherData[12]);
        divSunRise.getStyle().setFontWeight(Style.FontWeight.BOLD);

        Div divSunset = new Div(currentWeatherData[13]);
        divSunset.getStyle().setFontWeight(Style.FontWeight.BOLD);

        String strIconSize = "35px";

        Image imageSunrise = new Image();
        StreamResource iconSunrise = new StreamResource("Sunrise",
                () -> getClass().getResourceAsStream("/icons/sunrise.png"));
        imageSunrise.setSrc(iconSunrise);
        imageSunrise.setAlt("Sunrise");
//        imageSunrise.setClassName("lazy-card-travel-weather-icons");
        imageSunrise.getStyle().setWidth(strIconSize);
        imageSunrise.getStyle().setHeight(strIconSize);

        Image imageSet = new Image();
        StreamResource iconSunset = new StreamResource("Sunset",
                () -> getClass().getResourceAsStream("/icons/sunset.png"));
        imageSet.setSrc(iconSunset);
        imageSet.setAlt("Sunset");
//        imageSet.setClassName("lazy-card-travel-weather-icons");
        imageSet.getStyle().setWidth(strIconSize);
        imageSet.getStyle().setHeight(strIconSize);


        Div divToday = new Div("Today");
        divToday.getStyle().setFontSize("11px");
        layoutRight.add(new HorizontalLayout(divToday));
        layoutRight.add(new HorizontalLayout(new Div("Sunrise: "), divSunRise));
        layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));
        // layoutRight.add(new HorizontalLayout(new Div("Sunset: "), divSunset));


        Div divL = new Div(currentWeatherData[2]);
        divL.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divH = new Div(currentWeatherData[3]);
        divH.getStyle().setFontWeight(Style.FontWeight.BOLDER);


        Div divFeelsLike = new Div(currentWeatherData[1]);
        divFeelsLike.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divHumidity = new Div(currentWeatherData[4]);
        divHumidity.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divWindSpeed = new Div(currentWeatherData[7]);
        divWindSpeed.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divClouds = new Div(currentWeatherData[15]);
        divClouds.getStyle().setFontWeight(Style.FontWeight.BOLDER);

        Div divRain = new Div();
        String rain = currentWeatherData[16];

        Div divVisibility = new Div(currentWeatherData[17]);
        divVisibility.getStyle().setFontWeight(Style.FontWeight.BOLDER);


        layoutRight.add(new HorizontalLayout(new Div("L: "), divL, new Div("H: "), divH));
        Div divNow = new Div("Now");
        divNow.getStyle().setFontSize("11px");
        layoutRight.add(new HorizontalLayout(divNow));

        layoutRight.add(new HorizontalLayout(new Div("Feels like: "), divFeelsLike));
        layoutRight.add(new HorizontalLayout(new Div("Clouds: "), divClouds));

        if (!rain.equalsIgnoreCase("")) {
            divRain.setText(currentWeatherData[16]);
            divRain.getStyle().setFontWeight(Style.FontWeight.BOLDER);
            layoutRight.add(new HorizontalLayout(new Div("Rain: "), divRain));
        }

        layoutRight.add(new HorizontalLayout(new Div("Humidity: "), divHumidity));
        layoutRight.add(new HorizontalLayout(new Div("Wind speed: "), divWindSpeed));

        layoutWeather.add(layoutLeft, layoutRight);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);

        Anchor apiLink = new Anchor();
        apiLink.getStyle().setColor("#8b94a0");
        apiLink.setClassName("lazy-api-link");
        apiLink.setHref(weatherService.getUrlReference());
        apiLink.setTarget("_blank");
        apiLink.setText("Weather data by: " + weatherService.getTitleReference());

        layout.add(layoutWeather, apiLink);

        return layout;

    }


    public VerticalLayout loadFooter(boolean isMobile) {

        Div logoLayout = new Div();
        logoLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Width.FULL,
                LumoUtility.Gap.XSMALL,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.MEDIUM
        );

        H1 appName = new H1(APP_NAME);
        //appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD, TextColor.TERTIARY);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
        appName.getStyle().set("font-family", "Times-New-Roman, serif");
        appName.getStyle().set("font-stretch", "semi-expanded");
        appName.getStyle().setColor("#514c3f");
//        appName.getStyle().setColor("#eaeae8");//"#f9943b");//""#bd3450");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        // divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
        divLogo.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
        divLogo.getStyle().setColor("#514c3f");

        //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
        //divLogo.getStyle().setColor("#d64f00");

        logoLayout.add(divLogo, appName);

        Div divPhotoActMoto = new Div("Act around Photography");
        divPhotoActMoto.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Padding.NONE, LumoUtility.Margin.MEDIUM);

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


        VerticalLayout layoutFooter = new VerticalLayout();
        layoutFooter.setMinHeight("250px");
        layoutFooter.getStyle().setBackgroundColor("#8d8d8d"); //"#78868f");
        layoutFooter.getStyle().setColor("#eaeae8");
//        layoutFooter.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutFooter.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        } else {
            layoutFooter.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.MEDIUM,
                    LumoUtility.Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        }
//        layoutFooter.addClassName("footer");
        Div divLineBottom = new Div();
        divLineBottom.addClassNames(
                LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.LARGE,
                LumoUtility.Gap.MEDIUM,
                //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                LumoUtility.Background.PRIMARY,
                LumoUtility.BorderRadius.NONE);

        layoutFooter.add(logoLayout, divPhotoActMoto, divLineBottom);
        return layoutFooter;
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
