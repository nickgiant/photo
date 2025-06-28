package com.photo.act.photo_act.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.NetUtils;
import com.photo.act.photo_act.utils.UtilsDate;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.photo.act.photo_act.views.GalleryView.DIR_PHOTOS_SERVER;
import static com.photo.act.photo_act.views.MainLayout.*;


@RouteAlias(value = "travel/:section?", layout = MainLayout.class)
@RouteAlias(value = "travel/:section/:member?", layout = MainLayout.class)
@Route(value = "travel/:section/:subsection/:member?", layout = MainLayout.class)

@AnonymousAllowed
public class TravelView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasComponents, HasDynamicTitle, HasStyle, BeforeEnterObserver {

    private VerticalLayout panelContainer;
    private static final Logger logger = LoggerFactory.getLogger(TravelView.class);

    private String timeZoneId = "";
    private String ref = "";
    private String publicIp = "";
    private String sessionid;

    private RecordService recordService;
    private boolean isMobile = false;
    private String parentSection = "travel";
    private String section = "explore";
    private String subsection = "";
    private String pageTitle = "Travelling";

    private String dirChar = FileSystems.getDefault().getSeparator();

    public static String subPathThumbs = "photo-thumbs";
    public static String subPathMedium = "photo-medium";
    public static String subPathUpload = "photo-upload";
    public static String subPathShow = "photo-show";

    public static String             DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";


    private int userId = 1;
    private String username = "user_travel_v";

    private String hostname = "";
    private HorizontalLayout layoutPageTop;
    private String locales = "";

    private UtilsDate utilsDate;
    private String sessionDateTime;
    private String strUrlRequestToBeLogged;
    private long sessionCreation;

    private GenericView genericView;
    private String strOS;
    private String strBrowser;
//    private CreationsViewCard creationsViewCard;


    private final String DESTINATIONS_INFO = "info";
    private final String DESTINATIONS_EXPLORE = "explore";
    private final String DESTINATIONS_MY_TRAVELS = "my-travels";
    private final String DESTINATIONS_LISTS = "lists";
    private final String DESTINATIONS_MY_LISTS = "my-lists";
    private String locale;
    private String localeName;
    private String sysUserName;
    private String strPath;
    private String hostAddress;
    private String canonicalHostname;


    public TravelView(RecordService recordService) {

        this.recordService = recordService;
        layoutPageTop = new HorizontalLayout();
        layoutPageTop.setWidthFull();

        utilsDate = new UtilsDate();
        genericView = new GenericView(recordService);

//        creationsViewCard = new CreationsViewCard();


        constructUI();
    }

    @Override
    public void beforeEnter(@OptionalParameter BeforeEnterEvent event) {
        section = event.getRouteParameters().get("section").orElse("explore");
        username = event.getRouteParameters().get("member").orElse("members");
        subsection = event.getRouteParameters().get("subsection").orElse("all");

        getUserClientInfo();

        if (section != null) {
            if (section.equalsIgnoreCase(DESTINATIONS_EXPLORE)) {
                pageTitle = "Travelling/Explore";
            } else if (section.equalsIgnoreCase(DESTINATIONS_LISTS)) {
                pageTitle = "Travelling/Lists";
            } else if (section.equalsIgnoreCase(DESTINATIONS_MY_LISTS)) {
                pageTitle = "Travelling/My Lists";
            } else if (section.equalsIgnoreCase(DESTINATIONS_MY_TRAVELS)) {
                pageTitle = "Travelling/My Travels";
            } else if (section.equalsIgnoreCase(DESTINATIONS_INFO)) {
                pageTitle = "Travelling/Information ";
            }
        } else {

        }


        loadData(section);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String o) {
        section = o;//beforeEvent.getRouteParameters().get("section").orElse("pictures");
        /*if (section != null) {
            if (section.equalsIgnoreCase("pictures")) {
                pageTitle = "Photography/Pictures";
            }
        }
         */
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    private void constructUI() {

        //this.setClassName("creations-view");
        //addClassNames("creations-view");
        //addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        sessionid = VaadinSession.getCurrent().getSession().getId();


        String[] itemsOrder = {"Last posted", "Most selected to view", "Most liked", "Most saved"};
        List<String> listOrderBy = new ArrayList<String>();
        listOrderBy = Arrays.stream(itemsOrder).toList();

        //    Div topicDscription = new Div(System.getProperty("user.dir")+"      Μπορείτε να φιλτράρετε και ταξινομήσετε για να βρείτε πιο γρήγορα αυτό που σας ενδιαφέρει." +
        //           "Σε περίπτωση που έχετε λογαριασμό μπορείτε και να προσθέσετε δικό σας περιεχόμενο.");

        //logger.info(System.getProperty("user.dir"));
        //logger.info(new File("").getAbsolutePath());
        // Div divPath1 = new Div(System.getProperty("user.dir"));
        // Div divPath2 = new Div(new File("").getAbsolutePath());
        //   topicDscription.setClassName("lazy-topic-desc");

        if (VaadinSession.getCurrent().getBrowser().isAndroid() || VaadinSession.getCurrent().getBrowser().isIPhone()) {
            isMobile = true;
            //topicDscription.setVisible(false);
        }
        panelContainer = new VerticalLayout();
        panelContainer.setWidthFull();
        // panelContainer.addClassNames("creations-view-container");


        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostname = inetAddress.getHostName();
        hostAddress = inetAddress.getHostAddress();
        canonicalHostname = inetAddress.getCanonicalHostName();

        if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP)) {
                     DIR_PHOTOS_SERVER = "/home/mike/Pictures/lazy-photos";
        }else if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO)){
            DIR_PHOTOS_SERVER = "/home/linux-pc/Pictures/lazy-photos";
        } else if(hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO_WIN)){
            DIR_PHOTOS_SERVER =  "C:\\Users\\nickg\\Pictures\\lazy-photos";
        } else if (hostname.equalsIgnoreCase("piot")) {
                        DIR_PHOTOS_SERVER = "/home/pi/lazy-photos";
        }else if (hostname.equalsIgnoreCase(HOSTNAME_SERVER_HOSTINGER)){
            DIR_PHOTOS_SERVER = "/home/mikel/lazy-photos";
        } else {
            DIR_PHOTOS_SERVER = "/home/sammy/lazy-photos";

        }


        String[] itemsLocationCat = {"Any location", "At location I am currently", "Nearby locations", "On Line only", "Favourite locations", "Locations I have lived or visited", "Locations I am going to travel in 1 year ahead"};


        add(layoutPageTop, panelContainer);

    }

    private VerticalLayout loadTopPanelManage() {

        VerticalLayout layoutManage = new VerticalLayout();
        layoutManage.setWidthFull();
        layoutManage.setClassName("lazy-top-panel-manage");

        HorizontalLayout layoutControls = new HorizontalLayout();
        layoutControls.setWidthFull();
        layoutControls.setClassName("lazy-top-panel-manage");

//
//        MultiSelectListBox<String> listBoxArea = new MultiSelectListBox<>();
//        listBoxArea.setItems("Europe", "Asia", "Africa");
//        listBoxArea.select("Europe");


//        ListBox<String> listBoxFilters = new ListBox<>();
//        listBoxFilters.setAriaLabel("Filters:");
//        listBoxFilters.setItems("Great","Medium","Small");
//        listBoxFilters.setValue("Medium");

        CheckboxGroup<String> checkboxGroupFilters = new CheckboxGroup<>();
        checkboxGroupFilters.setLabel("Destinations:");
        checkboxGroupFilters.setItems("Europe", "Asia", "Customer", "Status", "Order ID", "Product name", "Africa");
        checkboxGroupFilters.select("Europe", "Asia");
        // checkboxGroupFilters.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);


//        ListBox<String> listBoxOrder = new ListBox<>();
//        listBoxOrder.setItems("Alphabetical: A - Z", "Alphabetical: Z - A", "Time Posted: Newer");
//        listBoxOrder.setValue("Alphabetical: A - Z");

        Select<String> selectOrder = new Select<>();
        selectOrder.setLabel("Sort by:");
        selectOrder.setItems("Most recent first", "Rating: high to low",
                "Rating: low to high", "Price: high to low",
                "Price: low to high");
        selectOrder.addComponents("Most recent first", new Hr());
        selectOrder.addComponents("Rating: low to high", new Hr());
        selectOrder.setValue("Most recent first");


        RadioButtonGroup<String> radioGroupView = new RadioButtonGroup<>();
        //radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupView.setLabel("View");
        radioGroupView.setItems("Great", "Medium", "Small");


        layoutControls.add(checkboxGroupFilters, selectOrder, radioGroupView);


        // -------

        HorizontalLayout layoutControlsSpots = new HorizontalLayout();
        layoutControlsSpots.setWidthFull();
        layoutControlsSpots.setClassName("lazy-top-panel-manage");

        CheckboxGroup<String> checkboxGroupFiltersSpots = new CheckboxGroup<>();
        checkboxGroupFiltersSpots.setLabel("Spot Types:");
        checkboxGroupFiltersSpots.setItems("Customer", "Status", "Order ID", "Product name");
        checkboxGroupFiltersSpots.select("Status", "Order ID");
        // checkboxGroupFilters.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);


        Select<String> selectOrderSpots = new Select<>();
        selectOrderSpots.setLabel("Sort by:");
        selectOrderSpots.setItems("Most recent first", "Rating: high to low",
                "Rating: low to high", "Price: high to low",
                "Price: low to high");
        selectOrderSpots.addComponents("Most recent first", new Hr());
        selectOrderSpots.addComponents("Rating: low to high", new Hr());
        selectOrderSpots.setValue("Most recent first");


        RadioButtonGroup<String> radioGroupViewSpots = new RadioButtonGroup<>();
        //radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupViewSpots.setLabel("View");
        radioGroupViewSpots.setItems("Great", "Medium", "Small");


        layoutControlsSpots.add(checkboxGroupFiltersSpots, selectOrderSpots, radioGroupViewSpots);


        // -------

        HorizontalLayout layoutControlsPhotos = new HorizontalLayout();
        layoutControlsPhotos.setWidthFull();
        layoutControlsPhotos.setClassName("lazy-top-panel-manage");

        CheckboxGroup<String> checkboxGroupFiltersPhotos = new CheckboxGroup<>();
        checkboxGroupFiltersPhotos.setLabel("Photo Categories:");
        checkboxGroupFiltersPhotos.setItems("Order ID", "Customer", "Status", "Product name");
        checkboxGroupFiltersPhotos.select("Status", "Order ID");
        // checkboxGroupFilters.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);


        Select<String> selectOrderPhotos = new Select<>();
        selectOrderPhotos.setLabel("Sort by:");
        selectOrderPhotos.setItems("Most recent first", "Rating: high to low",
                "Rating: low to high", "Price: high to low",
                "Price: low to high");
        selectOrderPhotos.addComponents("Most recent first", new Hr());
        selectOrderPhotos.addComponents("Rating: low to high", new Hr());
        selectOrderPhotos.setValue("Most recent first");


        RadioButtonGroup<String> radioGroupViewPhotos = new RadioButtonGroup<>();
        //radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupViewPhotos.setLabel("View");
        radioGroupViewPhotos.setItems("Great", "Medium", "Small");


        layoutControlsPhotos.add(checkboxGroupFiltersPhotos, selectOrderPhotos, radioGroupViewPhotos);

/////------------


        layoutManage.add(layoutControls, layoutControlsSpots, layoutControlsPhotos);

//        MultiSelectListBox<Person> listBox = new MultiSelectListBox<>();
//        listBox.setItems(items);
//        listBox.setRenderer(new ComponentRenderer<>(person -> {
//            HorizontalLayout row = new HorizontalLayout();
//            row.setAlignItems(FlexComponent.Alignment.CENTER);
//
//            Avatar avatar = new Avatar();
//            avatar.setName(person.getFullName());
//            avatar.setImage(person.getPictureUrl());
//
//            Span name = new Span(person.getFullName());
//            Span profession = new Span(person.getProfession());
//            profession.getStyle()
//                    .set("color", "var(--lumo-secondary-text-color)")
//                    .set("font-size", "var(--lumo-font-size-s)");
//
//            VerticalLayout column = new VerticalLayout(name, profession);
//            column.setPadding(false);
//            column.setSpacing(false);
//
//            row.add(avatar, column);
//            row.getStyle().set("line-height", "var(--lumo-line-height-m)");
//            return row;
//        }));
//        listBox.select(items.get(0), items.get(2));


        return layoutManage;
    }


    private void loadData(String section) {

//  --        VerticalLayout layoutTopPanelManage = loadTopPanelManage();
// --        layoutPageTop.add(layoutTopPanelManage);

        panelContainer.removeAll();


        logger.info("loadData  section  " + section);
        //VerticalLayout uploadPhotoPanel = new NewPostPanel(recordService).getNewPostPanel( userId,username,parentSection,section, isMobile);


        H6 sectionTitle = new H6("Travelling info about destinations");
        sectionTitle.getStyle().setColor("#8b94a0");
        sectionTitle.setWidthFull();
        sectionTitle.getStyle().setAlignItems(Style.AlignItems.CENTER);
        sectionTitle.getStyle().setTextAlign(Style.TextAlign.CENTER);


        String dirChar = FileSystems.getDefault().getSeparator();
        //logger.info(System.getProperty("user.dir"));
        //logger.info(new File("").getAbsolutePath());

        // GoogleMap gmaps = new GoogleMap("AIzaSyAWAeM4_KMQd2h_fq6lJ-Q54Q4f3eTnm6o", null, null);
        // gmaps.setMapType(GoogleMap.MapType.SATELLITE);
        //gmaps.setWidthFull();
        //gmaps.setHeightFull();
        //gmaps.setCenter(new LatLon(-31.636036, -60.7055271));


//        gmaps.setSizeFull();
//        gmaps.setCenter(new LatLon(40.640, 22.944));
//        gmaps.addMarker("Center", new LatLon(40.640, 22.944), true, "");
//        GoogleMapPolygon gmp = gmaps.addPolygon(Arrays.asList(new GoogleMapPoint(gmaps.getCenter()),
//                new GoogleMapPoint(gmaps.getCenter().getLat(),gmaps.getCenter().getLon()+1),
//                new GoogleMapPoint(gmaps.getCenter().getLat()+1,gmaps.getCenter().getLon())));

        panelContainer.add(sectionTitle);
        //       panelContainer.add(gmaps);


        final LComponentManagementRegistry reg = new LDefaultComponentManagementRegistry(this);

        // Create and add the MapContainer (which contains the map) to the UI
        final MapContainer mapContainer = new MapContainer(reg);
        mapContainer.setSizeFull();
        panelContainer.add(mapContainer);

        final LMap map = mapContainer.getlMap();

        // Add a (default) TileLayer so that we can see something on the map
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(reg));

        // Set what part of the world should be shown
        map.setView(new LLatLng(reg, 40.640, 22.944), 17);

        // Create a new marker
        new LMarker(reg, new LLatLng(reg, 40.640, 22.944))
                // Bind a popup which is displayed when clicking the marker
                .bindPopup("My Soft")
                // Add it to the map
                .addTo(map);


        String[] arrDestination = {"city_name", "prefecture", "country", "spot_name", "spot_type", "photo_festival_ed_Id"};
        String readSqlDestination = "";

        if (section.equalsIgnoreCase(DESTINATIONS_EXPLORE)) {
            readSqlDestination = "SELECT d.city_name, d.prefecture, d.country, ds.spot_name, ds.spot_type, ds.photo_festival_ed_Id FROM destination d LEFT JOIN destination_spots ds ON d.id = ds.destination_id ORDER BY d.city_name, ds.spot_type, ds.spot_name";
            //  List<Record> rec = this.getRecordsFromDb(readSqlDestination);
            loadDestinationSpots("viewtype", readSqlDestination, arrDestination);
            //panelContainer.add(getDestinationExplore(rec));
        }

    }

    private void loadDestinationSpots(String viewType, String readSqlDestination, String[] arrDestination) {

        List<Record> lstRecords = getRecordsFromDb(readSqlDestination, arrDestination);

        HashSet<String> hashsetDestination = new HashSet<>();
        List<Div> listDivSpot = new ArrayList<>();

        Map<HashSet<String>, List<Div>> hashList = new HashMap<>();

        for (int r = 0; r < lstRecords.size(); r++) {

            Record rec = lstRecords.get(r);
            if (viewType.equalsIgnoreCase("viewtype")) {

                String city = rec.getColumnData("city_name");
                String country = rec.getColumnData("country");
                String destination = city + ", " + country;

                hashsetDestination.add(destination);

                hashList.put(hashsetDestination, listDivSpot);

                VerticalLayout layoutPostLine = new VerticalLayout();
                layoutPostLine.setSpacing(true);
                layoutPostLine.setPadding(true);
                layoutPostLine.setMargin(true);
                layoutPostLine.setClassName("lazy-home-section");
                layoutPostLine.setWidthFull();

                HorizontalLayout layoutWithMap = new HorizontalLayout();
                layoutWithMap.setWidthFull();

                VerticalLayout layoutSpots = new VerticalLayout();
                layoutSpots.setClassName("lazy-card-overview");
                layoutSpots.addClassName("lazy-card-overview-border-solid");

                String destinationBefore = "";
                HorizontalLayout layoutSpot = getSpotLayout(rec);


                if (r > 0) {
                    Record recBefore = lstRecords.get(r - 1);
                    String cityBefore = recBefore.getColumnData("city_name");
                    String countryBefore = recBefore.getColumnData("country");
                    destinationBefore = cityBefore + ", " + countryBefore;
                }

//                if(hashList.containsKey(hashsetDestination.contains(destination))) {
//                    hashList.get(destination).add(divSpot);
//                    logger.info("destinations   --  "+r+"   "+destination+"   "+hashList.get(destination).size());
//                }


                if (destinationBefore.equalsIgnoreCase(destination)) {
//                    listDivSpot.add(layoutSpot);

                    layoutSpots.add(layoutSpot);
                    logger.info("destinations:  " + r + " " + destination + " == " + destinationBefore + " " + listDivSpot.size());
                } else if (!destination.equalsIgnoreCase(destinationBefore)) {


//                    listDivSpot.add(divSpot);
                    //logger.info("     hashset  "+destination+" "+destinationBefore+"  "+listDivSpot.size());


                    //for(int s=0;s<listDivSpot.size();s++)
                    //{
                    layoutSpots.add(layoutSpot);
                    //}

                    logger.info("destinations:  " + r + " " + destination + " != " + destinationBefore + " " + listDivSpot.size());

                    GenericView genericView = new GenericView(recordService);

                    VerticalLayout layoutWeather = genericView.getWeatherCurrent(city, country);

                    layoutPostLine.add(getDestinationTitle(rec));
                    layoutPostLine.add(layoutWeather);
                    layoutWithMap.add(layoutSpots, getDestinationMap(destination, country));
                    layoutPostLine.add(layoutWithMap);
//                    layoutPostLine.add(getDestinationPhotos(city,6));
                    panelContainer.add(layoutPostLine);

                }

            }
        }
    }

    private HorizontalLayout getDestinationTitle(Record record) {

        HorizontalLayout layoutPostTitle = new HorizontalLayout();
        layoutPostTitle.setWidthFull();
        layoutPostTitle.setPadding(true);
        layoutPostTitle.setSpacing(true);
        layoutPostTitle.setMargin(true);
        layoutPostTitle.setClassName("lazy-home-section-title");

        String city = record.getColumnData("city_name");
        String country = record.getColumnData("country");
        H4 titleDestination = new H4(city);
        titleDestination.setWidthFull();
        titleDestination.getStyle().setTextAlign(Style.TextAlign.CENTER);
        titleDestination.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        titleDestination.getStyle().setColor("#344e41");
        titleDestination.addClassName("lazy-card-overview-font-big");


        //linkTimeRelated.setIcon(FontAwesome.Solid.CALENDAR.create());
        // linkTime.setClassName("lazy-keywords-related");


        layoutPostTitle.add(titleDestination);
        return layoutPostTitle;

    }

    private HorizontalLayout getSpotLayout(Record record) {

        HorizontalLayout layoutSpot = new HorizontalLayout();
        layoutSpot.setWidthFull();
        // layoutSpot.setClassName("lazy-card-overview-gradient");
        layoutSpot.addClassName("lazy-card-overview-align-left");
        layoutSpot.addClassName("lazy-card-overview-border-solid");

        String city = record.getColumnData("city_name");
        String country = record.getColumnData("country");
        String spot_type = record.getColumnData("spot_type");
        String name = record.getColumnData("name");

        Div divSpot = new Div(name); // +" ("+entity_type+")");
        divSpot.setWidthFull();
        divSpot.getStyle().setColor("#8e7138");
        divSpot.addClassName("lazy-card-overview-font-important");


        //divSpot.addComponentAtIndex(0, LineAwesomeIcon.MAP_PIN_SOLID.create());

        StreamResource iconLike = new StreamResource("thumbs-up-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/thumbs-up-line-icon.svg"));
        SvgIcon svgLike = new SvgIcon(iconLike);
        Button btnLike = new Button(svgLike);
        btnLike.setTooltipText("Like It");
        btnLike.setClassName("lazy-topic-actions");


        Button btnSaveInCalendar = new Button(FontAwesome.Solid.CALENDAR_CHECK.create());
        btnSaveInCalendar.setTooltipText("Save in Calendar");
        btnSaveInCalendar.setClassName("lazy-topic-actions");

        StreamResource iconAction = new StreamResource("testimonial-icon.svg",
                () -> getClass().getResourceAsStream("/icons/testimonial-icon.svg"));
        SvgIcon svgAction = new SvgIcon(iconAction);
        Button btnMoreAction = new Button(svgAction);
        btnMoreAction.setTooltipText("More Actions");
        btnMoreAction.setClassName("lazy-topic-actions");

        Button btnComment = new Button(VaadinIcon.COMMENT.create());
        btnComment.setTooltipText("Comment on it");
        btnComment.setClassName("lazy-topic-actions");

        StreamResource iconShare = new StreamResource("share-line-icon.svg",
                () -> getClass().getResourceAsStream("/icons/share-line-icon.svg"));
        SvgIcon svgShare = new SvgIcon(iconShare);
        Button btnShare = new Button(svgShare);
        btnShare.setTooltipText("Share it");
        btnShare.setClassName("lazy-topic-actions");

        layoutSpot.add(LineAwesomeIcon.MAP_PIN_SOLID.create(), divSpot, btnLike, btnShare);

        return layoutSpot;

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
        mapsFrame.setHeight("400px");
        mapsFrame.getStyle().setBorder("0px");
        mapsFrame.getStyle().setBorderRadius("6px");


        return mapsFrame;
    }


//    private HorizontalLayout getDestinationPhotos(String destination, int count) {
//        HorizontalLayout layoutPhotos = new HorizontalLayout();
//        layoutPhotos.setPadding(false);
//        layoutPhotos.setMargin(false);
//        layoutPhotos.setSpacing(false);
//        //layoutPhotos.setWidthFull();
//        PhotoFlickrService photoFlickr = new PhotoFlickrService();
//        ArrayList<Photo> listPhotos = photoFlickr.findPhotos(destination, count);
//        for (int p = 0; p < listPhotos.size(); p++) {
//
//
//            Photo photo = listPhotos.get(p);
//
//            String thumbUrl = photo.getSmallUrl(); //.getThumbnailUrl();
//            String title = photo.getTitle();
//
//
//            User user = photo.getOwner();
//            user.getId();
//            user.getRealName();
//            user.getProfileurl();
//            user.getPhotosCount();
//            user.getPhotosurl();
////
//
////
//            Image image = new Image(thumbUrl, destination);
//            image.setHeight("195px");
//            image.setWidth("auto");
//
//            VerticalLayout photoLayout = new VerticalLayout();
//            photoLayout.setPadding(false);
//            photoLayout.setMargin(false);
//            photoLayout.setSpacing(false);
//
//            HorizontalLayout layoutUser = new HorizontalLayout();
//            layoutUser.setSpacing(false);
//            layoutUser.setMargin(false);
//            layoutUser.setPadding(false);
//            layoutUser.setAlignItems(FlexComponent.Alignment.CENTER);
//            layoutUser.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
//
//            String userId = user.getId();
//            String userName = user.getRealName(); //photoFlickr.getUserName(userId); //user.getUsername();
////            photoFlickr.getUserName(userId)
//
//            logger.info("  " + userName + "  " + userId + "  ");
//
//            String userUrl = "https://www.flickr.com/photos/" + userId;
//            Anchor linkUserInNewTab = new Anchor(userUrl, "");
//            linkUserInNewTab.getElement().setAttribute("target", "_blank");
//            linkUserInNewTab.addComponentAtIndex(0, VaadinIcon.USER.create());
//            linkUserInNewTab.setClassName("lazy-result-line-button");
//
//            //Div divUser = new Div();
//            // divUser.add(VaadinIcon.USER_CARD.create());
//            //divUser.setText("flickr user: ");
//            //divUser.setClassName("lazy-result-line-button");
//
//            layoutUser.add(linkUserInNewTab);
//            photoLayout.add(layoutUser, image);
//            layoutPhotos.add(photoLayout);
//
//
////
////                photoUrls.add(photoList.get(i).getThumbnailUrl());//.getSmall320Url());
////             //   layoutPhotos.add(photoList.get(i).getThumbnailUrl());
//
//

    /// /            Image image = new Image(listPhotosLayout.get(p),destination);
    /// /            image.setHeight("180px");
    /// /            image.setWidth("auto");
//            //           layoutPhotos.add(image);
//        }
//
//
//        return layoutPhotos;
//
//    }
    private void getUserClientInfo() {

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


    private List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {

        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

//    private List<Record> getRecordsFromDb(String sql, Object[] sqlParValue, String[] sqlParType) {
//        logger.info(" travel  getRecordsFromDb with params:   " + sql);
//        return recordService.findAll(sql, sqlParValue, sqlParType);
//    }

    private void logVisitorToDb() {

//        category = category.replaceAll("'", " ");
//        category = category.replaceAll("\"", " ");

        //search = search.replaceAll("'"," ");
        //search = search.replaceAll("\""," ");

        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            strUrlRequestToBeLogged = currentUrl.toExternalForm();

        });

        sysUserName = System.getProperty("user.name");


        // String ipAddress = VaadinSession.getCurrent().getBrowser().getAddress();
        String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
        int versionOfBrowserMajor = VaadinSession.getCurrent().getBrowser().getBrowserMajorVersion();
        int versionOfBrowserMinor = VaadinSession.getCurrent().getBrowser().getBrowserMinorVersion();
        int intUiId = VaadinSession.getCurrent().getNextUIid();


        int[] availWidth = calcTotalAvailableWidth();


        if (strUrlRequestToBeLogged == null || strUrlRequestToBeLogged.isEmpty() || strUrlRequestToBeLogged.equalsIgnoreCase("null")) {
            strUrlRequestToBeLogged = "NULL";
        } else {
            strUrlRequestToBeLogged = "'" + strUrlRequestToBeLogged + "'";
        }

        if (strPath == null || strPath.isEmpty()) {
            strPath = "NULL";
        } else {
            strPath = strPath.replace("\\","-");
            strPath = "'" + strPath + "'";
        }




        logger.info("photo visitor:" + publicIp + " . " + hostname + " . " + hostAddress + " . " + canonicalHostname + "  .  " + browser + " " + sessionid);

        String insertSQL = "INSERT INTO dbvisitor_log SET visitorlogId = 0,  timeOfVisit = now(), ipAddress = '" + publicIp + "', browserName = '" + browser + "', "
                + " browserVersionMajor = '" + versionOfBrowserMajor + "', browserVersionMinor = '" + versionOfBrowserMinor + "', urlParameter = NULL , timeZoneId = '" + timeZoneId + "', "
                + " appVersion = '" + APP_NAME + "-" + APP_VERSION + "', sessionId = '" + sessionid + "', sessionCreationTime = '" + sessionDateTime + "', hostname = '" + hostname + "', "
                + " hostAddress = '" + hostAddress + "', os = '" + strOS + "', browser = '" + strBrowser + "', section = '" + section + "',"
                + " item = " + strPath + ", ref = " + strUrlRequestToBeLogged + ", "
                + " locale = '" + locale + "', localeName ='" + localeName + "' ";

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.massRecordInsert(lstQueryInsert, null, null);
    }

    public int[] calcTotalAvailableWidth() {
        final int[] availWidth = {-1, -1, -1};

        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            // This is your own method that you may do something with the screen width.
            // Note that this method runs asynchronously
            availWidth[0] = details.getWindowInnerWidth();
            availWidth[1] = details.getBodyClientWidth();
            availWidth[2] = details.getScreenWidth();

            logger.info("availWidth:  window inner " + details.getWindowInnerWidth() + " body client  " + details.getBodyClientWidth() + "  screen  " + details.getScreenWidth());

        });
        return availWidth;
    }

    private MessageList setMessageListSampleData(MessageList messageList) {
        MessageListItem message1 = new MessageListItem("Nature does not hurry, yet everything gets accomplished.",
                LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC), "Matt Mambo");
        message1.setUserColorIndex(1);
        MessageListItem message2 = new MessageListItem(
                "Using your talent, hobby or profession in a way that makes you contribute with something good to this world is truly the way to go.",
                LocalDateTime.now().minusMinutes(55).toInstant(ZoneOffset.UTC), "Linsey Listy");
        message2.setUserColorIndex(2);
        messageList.setItems(message1, message2);
        return messageList;
    }

    private void logErrorInDb(Exception e, String function, String info) {
        recordService.logErrorInDb(e, hostname, function, userId, username, publicIp, sessionid, info);
    }

}
