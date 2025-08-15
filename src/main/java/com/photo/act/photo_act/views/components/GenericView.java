package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.WeatherImageService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.photo.act.photo_act.views.AlbumsView.subPathThumbs;
import static com.photo.act.photo_act.views.MainLayout.*;
import static com.photo.act.photo_act.views.MeView.subPathLarge;

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

    private static final Logger logger = LoggerFactory.getLogger(GenericView.class);
    private String dirChar = FileSystems.getDefault().getSeparator();

    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestination = "SELECT distinct city_name, prefecture, country " +
            " FROM  photo_meta pm LEFT JOIN destination d ON pm.destination_Id = d.id " +
            " ORDER BY city_name ASC ";

    private int userId;
    private UtilsDate utilsDate;
    private String strOS;
    private String strBrowser;

    private String sqlReadGallery;
    private String[] arrColumnsGallery;

    private List<Record> recProps;

    private VerticalLayout layoutPhotoInfo;
    private Div divCarousel;

    public GenericView(RecordService recordService, int userId) {
        this.recordService = recordService;
        this.userId = userId;
        utilsDate = new UtilsDate();


        getUserClientInfo();

        String sqlReadAppConfig = "SELECT app, host, propName, propValue FROM dbinfo WHERE host like '" + hostname + "' ";
        String[] arrCols = {"propName", "propValue"};
        recProps = recordService.findAll(sqlReadAppConfig, arrCols);
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
//        String strUsername = rec.getColumnData("username");
        String strMemberFor = rec.getColumnData("member_for");
        String strAvatarPath = rec.getColumnData("avatar_path");

        Image imgAvatar = getAvatarImage(strAvatarPath, strUsername, "180px", "180px");

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
        if (locations != null) {
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
        } else {
            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.setPadding(false);
            return layout;
        }

    }

    public Image getAvatarImage(String strAvatarPath, String altDescr, String width, String height) {

        String strAvatarFullPath = getAppProps(PROP_PHOTOS) + dirChar + SUB_PATH_AVATARS + dirChar + strAvatarPath;
        Path path = Paths.get(strAvatarFullPath);
        File file = path.toFile();

        final StreamResource imageResource = new StreamResource("streamResource", () -> {
            try {
                logger.info("GenericView strAvatarFullPath:" + file.getAbsolutePath());
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                logErrorInDb(e, "GenericView StreamResource FileNotFoundException", hostname, userId, "", publicIp, sessionCreation, file.getAbsolutePath());
                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                logger.error("GenericView FileNotFoundException  " + e.getMessage() + "  " + file.getAbsolutePath());
            }
            return null;
        });

        Image image = new Image();
        image.setWidth(width);
        image.setHeight(height);
        image.addClassNames(LumoUtility.BorderRadius.MEDIUM);
        image.setAlt(altDescr);
        image.setSrc(imageResource);

        return image;
    }

    public VerticalLayout loadCarouselWithThumbnails(String sqlRead, String[] arrColumnNames, String strSelection) {

        sqlReadGallery = sqlRead;
        arrColumnsGallery = arrColumnNames;

        layoutPhotoInfo = new VerticalLayout();
        divCarousel = new Div();

        VerticalLayout layoutAll = new VerticalLayout();
        layoutAll.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.LARGE);

        ArrayList<Image> lstImageThumbs = fetchPhotoThumbs(sqlReadGallery, arrColumnsGallery);


        HorizontalLayout layoutCarouselAndInfo = new HorizontalLayout();
        layoutCarouselAndInfo.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);


        divCarousel.addClassNames(LumoUtility.Overflow.SCROLL,
                LumoUtility.Width.FULL, LumoUtility.Height.AUTO,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.LARGE);

        Image imageLarge = fetchPhotosLarge(sqlReadGallery, arrColumnsGallery).get(0);
        divCarousel.add(imageLarge);

/*        Carousel carousel = new Carousel();
        carousel.setSlides(slides);
        //carousel.setHideNavigation(true);
        carousel.setAutoProgress(false);
        carousel.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.LARGE);*/

        Div titleImg = new Div((lstImageThumbs.size()) + " photos");


        layoutPhotoInfo.addClassNames(LumoUtility.Overflow.HIDDEN,
                LumoUtility.Height.FULL, LumoUtility.Width.FULL,  //must be comment
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        layoutPhotoInfo.setMaxWidth("260px");


        Scroller scroller = new Scroller();
        scroller.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);
        scroller.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);


        Select<String> cmbAlbum = new Select<>();
        cmbAlbum.setLabel("Album");


        Select<String> cmbLocation = new Select<>();
        cmbLocation.setLabel("Location");
        List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadDestination, arrDestinationNames);
        ArrayList<String> lstDestinations = new ArrayList<>();
        for (int r = 0; r < lstDestinationRecs.size(); r++) {
            String strDestination = lstDestinationRecs.get(r).getColumnData("city_name");
            if (strDestination == null || strDestination.trim().isEmpty() || strDestination.trim().equalsIgnoreCase("null")) {
            } else {
                lstDestinations.add(strDestination);
                if (r == 0) {
                    if (cmbLocation.getValue() == null || cmbLocation.getValue().equalsIgnoreCase("null") || cmbLocation.getValue().isEmpty()) {
                        cmbLocation.setValue(strDestination);
                    }
                }
            }

        }

        HorizontalLayout layoutThumbs = showThumbs(sqlReadGallery, arrColumnsGallery, cmbLocation);
        scroller.setContent(layoutThumbs);

        cmbLocation.setItems(lstDestinations);
        cmbLocation.addValueChangeListener(event -> {
            HorizontalLayout layoutLocationThumbs = selectLocation(event.getValue(), event.getSource());
            scroller.setContent(layoutLocationThumbs);
        });
        logger.info("strSelection:" + strSelection);
        cmbLocation.setValue(strSelection);

        layoutPhotoInfo.add(cmbLocation);


        layoutCarouselAndInfo.add(divCarousel, layoutPhotoInfo);
        layoutAll.add(layoutCarouselAndInfo, scroller);

        return layoutAll;
    }

    private ArrayList<Image> fetchPhotoThumbs(String sqlRead, String[] arrColumnNames) {

        ArrayList<Image> lstImage = new ArrayList<>();
        List<com.photo.act.photo_act.db.Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record record = lstRecords.get(r);
            lstImage.add(getImageThumb(record));
        }
        return lstImage;
    }


    private ArrayList<Image> fetchPhotosLarge(String sqlRead, String[] arrColumnNames) {

        ArrayList<Image> lstImage = new ArrayList<>();
        List<com.photo.act.photo_act.db.Record> lstRecords = getRecordsFromDb(sqlRead, arrColumnNames);
        for (int r = 0; r < lstRecords.size(); r++) {

            Record record = lstRecords.get(r);
            lstImage.add(getImageLarge(record));
        }
        return lstImage;
    }

    private VerticalLayout getPhotoMetaInfoOnCarousel(Record record) {

        VerticalLayout layoutPhotoInfo = new VerticalLayout();
        layoutPhotoInfo.addClassNames(LumoUtility.Overflow.SCROLL,
                LumoUtility.Width.FULL, LumoUtility.Height.FULL,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START);

        String strMetaCameraModel = record.getColumnData("meta_camera_model");
        String strMetaLensModel = record.getColumnData("meta_lens_model");

        String strMetaFocalLengthFF = record.getColumnData("meta_focal_length_ff");
        String strMetaFocalLength = record.getColumnData("meta_focal_length");
        String strMetaIso = record.getColumnData("meta_iso");
        String strMetaAperture = record.getColumnData("meta_aperture");
        String strMetaShutterSpeed = record.getColumnData("meta_shutter_speed");


        VerticalLayout layoutPhotoCameraMeta = new VerticalLayout();
        layoutPhotoCameraMeta.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.NONE
        );
        Div divMetaCameraTitle = new Div("Camera");
        divMetaCameraTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaCamera = new Div(strMetaCameraModel);
        Div divMetaLensTitle = new Div("Lens");
        divMetaLensTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaLens = new Div(strMetaLensModel);
        layoutPhotoCameraMeta.add(divMetaCameraTitle, divMetaCamera, divMetaLensTitle, divMetaLens);

        VerticalLayout layoutPhotoMeta = new VerticalLayout();
        layoutPhotoMeta.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.AlignItems.START, LumoUtility.JustifyContent.START,
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.XSMALL,
                LumoUtility.BorderRadius.NONE
        );
        Div divFocalTitle = new Div("Focal Length");
        divFocalTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLength = new Div(strMetaFocalLength + " mm");
        if (strMetaFocalLength.equalsIgnoreCase("null")) {
            divFocalTitle.setVisible(false);
            divMetaFocalLength.setVisible(false);
        }

        Div divFocalFFTitle = new Div("Focal Length (Full Frame)");
        divFocalFFTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaFocalLengthFF = new Div(strMetaFocalLengthFF + " mm");
        if (strMetaFocalLength.equalsIgnoreCase(strMetaFocalLengthFF) || strMetaFocalLengthFF.equalsIgnoreCase("null")) {
            divFocalFFTitle.setVisible(false);
            divMetaFocalLengthFF.setVisible(false);
        }

        Div divApertureTitle = new Div("Aperture");
        divApertureTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaAperture = new Div(strMetaAperture);
        if (strMetaAperture.equalsIgnoreCase("null")) {
            divApertureTitle.setVisible(false);
            divMetaAperture.setVisible(false);
        }

        Div divSSTitle = new Div("Shutter Speed");
        divSSTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaSS = new Div(strMetaShutterSpeed + " sec");
        if (strMetaShutterSpeed.equalsIgnoreCase("null")) {
            divSSTitle.setVisible(false);
            divMetaSS.setVisible(false);
        }

        Div divIsoTitle = new Div("ISO");
        divIsoTitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.Padding.Vertical.NONE, LumoUtility.FontSize.XSMALL);
        Div divMetaIso = new Div(strMetaIso);

        layoutPhotoMeta.add(divFocalTitle, divMetaFocalLength, divFocalFFTitle, divMetaFocalLengthFF, divApertureTitle, divMetaAperture,
                divSSTitle, divMetaSS, divIsoTitle, divMetaIso);

        layoutPhotoInfo.add(layoutPhotoCameraMeta, layoutPhotoMeta);
        return layoutPhotoInfo;
    }

    private HorizontalLayout selectLocation(String locationName, Select<String> cmbLocation) {
//        List<String> lstPhotoFilenames = getImagesFilenames(sqlRead, arrColumnNames);
        String sqlReadWithLocation = sqlReadGallery;
        if(!locationName.isEmpty()) {
            sqlReadWithLocation = sqlReadGallery + " AND d.city_name LIKE '" + locationName + "' ";
        }
        HorizontalLayout layoutThumbs = showThumbs(sqlReadWithLocation, arrColumnsGallery, cmbLocation);
        Image imageLarge = fetchPhotosLarge(sqlReadWithLocation, arrColumnsGallery).get(0);
        divCarousel.removeAll();
        divCarousel.add(imageLarge);

        return layoutThumbs;
    }

    private VerticalLayout selectPhotoThumb(List<com.photo.act.photo_act.db.Record> lstImageFiles, String sqlReadWithLocation, int intImage, Select<String> cmbLocation) {

        layoutPhotoInfo.removeAll();
        divCarousel.removeAll();


        Record record = lstImageFiles.get(intImage);

        Image imageLarge = fetchPhotosLarge(sqlReadWithLocation, arrColumnsGallery).get(intImage);
        imageLarge.addClassName("image-to-show");
        divCarousel.add(imageLarge);



        VerticalLayout layoutMeta = getPhotoMetaInfoOnCarousel(record);
        layoutMeta.addClassName("image-meta-to-show");
        layoutPhotoInfo.add(layoutMeta);
        imageLarge.getStyle().setOpacity("1");
        layoutMeta.getStyle().setOpacity("1");
        layoutPhotoInfo.add(cmbLocation);
        return layoutPhotoInfo;
    }

    private HorizontalLayout showThumbs(String sqlRead, String[] arrColumnNames, Select<String> cmbLocation) {

        ArrayList<Image> lstImageThumbs = fetchPhotoThumbs(sqlRead, arrColumnNames);

        List<com.photo.act.photo_act.db.Record> lstImageFiles = getRecordsFromDb(sqlRead, arrColumnNames);

        HorizontalLayout layoutThumbs = new HorizontalLayout();
        layoutThumbs.addClassNames(//LumoUtility.Overflow.HIDDEN,
                //LumoUtility.Width.FULL, //LumoUtility.Height.FULL,
                LumoUtility.Display.INLINE_FLEX,
                LumoUtility.Margin.NONE, LumoUtility.Padding.XSMALL,
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        layoutThumbs.setHeight("105px");

        for (int t = 0; t < lstImageThumbs.size(); t++) {
//            Div btnThumb = new Div();
            Div divBtnPhoto = new Div();
            Image imageThumb = lstImageThumbs.get(t);
            imageThumb.setWidth("auto");
            imageThumb.setHeight("80px");
            divBtnPhoto.add(imageThumb);
            divBtnPhoto.addClassNames(//LumoUtility.Overflow.HIDDEN,
//                    LumoUtility.Width.AUTO, LumoUtility.Height.FULL,
                    LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.BorderRadius.MEDIUM);
//
//            divBtnPhoto.setWidth("auto");
//            divBtnPhoto.setHeight("80px");
            divBtnPhoto.addClassName("btn-thumb-photo");

//            btnThumb.add(divBtnPhoto);
            final int tFinal = t;
            divBtnPhoto.addClickListener(click -> {
                selectPhotoThumb(lstImageFiles, sqlRead, tFinal, cmbLocation);
            });
            layoutThumbs.add(divBtnPhoto);
        }
        return layoutThumbs;
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

        String strCityName = record.getColumnData("city_name");
        String strUploader = record.getColumnData("uploader");

        if (strTitle == null || strTitle.isEmpty()) {
            strTitle = "image";
        }

        String strImagePath = strPathIn + dirChar + strFileName;
        logger.info(" strImagePath " + strImagePath);
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

    public void logErrorInDb(Exception e, String function, String hostname, int userId, String strUsername, String publicIp, long sessionCreation, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, strUsername, publicIp, Long.toString(sessionCreation), info);
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


        Span divPhotoActMoto = new Span("[ Network and Act around Photography ]");
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
