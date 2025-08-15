package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.photo.act.photo_act.utils.UtilsDate;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.UUID;

import static com.photo.act.photo_act.views.GalleryView.*;
import static com.photo.act.photo_act.views.MainLayout.*;
import static com.photo.act.photo_act.views.MeView.subPathLarge;
import static com.photo.act.photo_act.views.MeView.subPathSmall;

public class UploadImageCard extends VerticalLayout {


    private GenericView genericView;
    private File file;
    private String originalFileName;
    private String mimeType;
    private RecordService recordService;


    private static final Logger logger = LoggerFactory.getLogger(UploadImageCard.class);
    private UtilsDate utilsDate;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private int intUserId;
    private String strUserName;
    private String publicIp;
    private String hostname;
    private String sessionId;
    private String sessionDateTime;
    private WeatherService weatherService;

    private EmailSendService emailSendService;

    private String strMailboxSend = "info@photoact.net";

    @Autowired
    public UploadImageCard(RecordService recordService, EmailSendService emailSendService, int intUserId, String strUserName, long sessionCreation, String publicIp, String hostname) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        this.intUserId = intUserId;
        this.strUserName = strUserName;

        this.publicIp = publicIp;
        this.hostname = hostname;

        this.sessionId = Long.toString(sessionCreation);

        weatherService = new WeatherService("metric");
        genericView = new GenericView(recordService, 1);
        utilsDate = new UtilsDate();

        this.sessionDateTime = utilsDate.calcDateTimeFromLong(sessionCreation, "UTC");
    }

    //    https://cookbook.vaadin.com/upload-image-to-file
    public VerticalLayout getUploadImageCard() {

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.BorderColor.CONTRAST_5);

//        MemoryBuffer buffer = new MemoryBuffer();
//        Upload upload = new Upload(buffer);

//        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
//        Upload upload = new Upload(multiFileBuffer);

        Upload upload = new Upload(this::receiveUpload);
        upload.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.SMALL, LumoUtility.BorderColor.CONTRAST_5);

        upload.setMaxFiles(3);

        int maxFileSizeInBytes = 18 * 1024 * 1024; // 18MB
        upload.setMaxFileSize(maxFileSizeInBytes);

        Div output = new Div(new Text("(no image file uploaded yet)"));
        output.getStyle().setAlignItems(Style.AlignItems.CENTER);
        output.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        layout.add(upload, output);

        upload.setAcceptedFileTypes("image/jpeg", "image/png");//, "image/gif");


        upload.addSucceededListener(event -> {
            output.removeAll();

            Image image = new Image();
//            output.add(new Text("Uploaded: "+originalFileName+" to "+ file.getAbsolutePath()+ " Type: "+mimeType+" Size: "+file.length()));

            StreamResource streamResource = new StreamResource(this.originalFileName, this::loadFile);
            image.setSrc(streamResource);

            image.setMaxWidth("68%");
            image.setHeight("auto");
            image.getStyle().setAlignItems(Style.AlignItems.CENTER);
            image.getStyle().setJustifyContent(Style.JustifyContent.CENTER);

            VerticalLayout layoutImageInfo = new VerticalLayout();
            layoutImageInfo.setWidthFull();
            layoutImageInfo.setSpacing(true);
            layoutImageInfo.setMargin(false);
            layoutImageInfo.setPadding(false);

            ArrayList<String> lstPhotoMetaData = new ArrayList<>();

            String strPathUpload = DIR_PHOTOS_SERVER + dirChar + subPathUpload;

            UUID uuid = UUID.randomUUID();
            String strUUID = uuid.toString();
            String strNewFileName = intUserId + "_" + strUserName + "_" + strUUID + ".jpg";

            Button btnSave = new Button("Upload Photo");

            InputStream isUpload = loadFile(file.getAbsolutePath());
            OutputStream outUpload = null;
            String outputUploadFileName = strPathUpload + dirChar + strNewFileName;
            File outputUploadFile = new File(outputUploadFileName);

            try {

                outUpload = new FileOutputStream(outputUploadFile);
                StreamUtils.copy(isUpload, outUpload);
                logger.info(" upload Photo Success to: " + this.originalFileName + " ---> " + strNewFileName + " size: " + getFileSizeAsString(outputUploadFile));
            } catch (Exception e) {

                String errorMessage = "Upload failed: " + e.getMessage();

                Notification notification = Notification.show(
                        errorMessage,
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "getUploadImageCard to upload  " + errorMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + errorMessage);
                logErrorInDb(e, "getUploadImageCard to upload", this.file.getAbsolutePath(), intUserId, strUserName);

                logger.error(" upload " + e.getMessage());
//                throw new RuntimeException(e);
            }

            ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
            File imgFile = new File(outputUploadFileName);
            logger.info("for photo " + outputUploadFileName + " get meta info");
            StringBuilder strImageMetaInfo = new StringBuilder();
            try {
                //  logger.info(" A for photo "+outputUploadFileName+" get meta info to html "+imgFile.getAbsolutePath());
                strImageMetaInfo.append(imageUtilsMeta.getMetadataInfo(imgFile));
                //   logger.info(" B for photo "+outputUploadFileName+" get meta info to list "+imgFile.getAbsolutePath());
                lstPhotoMetaData = imageUtilsMeta.getListImageInfo();
                if (lstPhotoMetaData != null && lstPhotoMetaData.size() > 0) {
                    Html imageInfo = new Html(strImageMetaInfo.toString());
                    layoutImageInfo.add(imageInfo);
                    btnSave.setVisible(true);
                } else {

                    btnSave.setVisible(true);

//                    Notification notification = Notification.show(
//                            "Photo contains no metadata.",
//                            5000,
//                            Notification.Position.MIDDLE
//                    );
//                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                }
            } catch (Exception e) {

                btnSave.setVisible(false);

                String errorMessage = "Upload failed: " + e.getMessage();
                Notification notification = Notification.show(
                        errorMessage,
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "getUploadImageCard  strImage Meta Info  " + errorMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + errorMessage);

                logErrorInDb(e, "getUploadImageCard  strImage Meta Info " + e.getMessage(), this.file.getAbsolutePath(), intUserId, strUserName);

                logger.error(" strImage Meta Info " + e.getMessage());
//                throw new RuntimeException(e);
            }


            VerticalLayout photoToAddLayout = new VerticalLayout();
            photoToAddLayout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
//                    LumoUtility.Background.CONTRAST_5,
                    LumoUtility.Padding.XLARGE,
                    LumoUtility.BorderRadius.LARGE,
                    LumoUtility.Background.CONTRAST_5);


            upload.clearFileList();
            photoToAddLayout.add(image);
            photoToAddLayout.add(btnSave);
//            photoToAddLayout.add(layoutLocation, btnSave);
            output.add(photoToAddLayout);




            ArrayList<String> finalLstPhotoMetaData = lstPhotoMetaData;

            btnSave.addClickListener(clickevent -> {
                if (confirmedUploadPhoto(strNewFileName, finalLstPhotoMetaData, publicIp, hostname, strImageMetaInfo)) {
                    photoToAddLayout.removeAll();
                    upload.clearFileList();
                    output.remove(photoToAddLayout);


                    double dblSize = Double.parseDouble(event.getContentLength() + "");
                    String strFilesize = getFileSizeMB(dblSize) + "";

                    if (strFilesize.length() > 7) {
                        strFilesize = strFilesize.substring(0, 5) + " MB";  //String.format("%.2f", dblSize);
                    }

                    String message = "Photo Uploaded ! (" + strFilesize + ")";
                    Notification notification = Notification.show(message, 6000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    logger.info("to sent mail for hostname:"+hostname);
                    if(hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO_WIN) || hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO) || hostname.equalsIgnoreCase(HOSTNAME_LAPTOP))
                    {}else {
//                        emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "Photo Uploaded", "From IP: " + publicIp + " username: " + strUserName + " (" + strFilesize + ")");
                        logger.info("mail sent");
                    }

                }
            });


//            photoToAddLayout.add(image, layoutImageInfo);

//            HorizontalLayout layoutLocationSearch = new HorizontalLayout();
//            layoutLocationSearch.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER, //LumoUtility.Width.FULL,
//                    LumoUtility.Padding.LARGE,
//                    LumoUtility.Background.TINT_10,
//                    LumoUtility.BorderRadius.LARGE);
//            Div divLocation = new Div("Location:");
//            TextField txtLocation = new TextField();
//
//
//            VerticalLayout locationResultLayout = new VerticalLayout();
//            locationResultLayout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER, //LumoUtility.Width.FULL,
//                    LumoUtility.Margin.LARGE,
//                    LumoUtility.Background.TINT_10,
//                    LumoUtility.BorderRadius.LARGE);
//
//            Button btnSearchLocation = new Button("Find");
//            btnSearchLocation.addThemeVariants(ButtonVariant.LUMO_SMALL);
//            btnSearchLocation.addClickListener(clickevent -> {
//                locationResultLayout.removeAll();
////                locationResultLayout.add(showLocationLayout(txtLocation.getValue()));
//
//            });
//
//            layoutLocationSearch.removeAll();
//            layoutLocationSearch.add(divLocation, txtLocation, btnSearchLocation);
//
//            VerticalLayout layoutLocation = new VerticalLayout();
//            layoutLocation.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER, //LumoUtility.Width.FULL,
//                    LumoUtility.Margin.NONE, LumoUtility.Padding.NONE, LumoUtility.Gap.MEDIUM,
//                    LumoUtility.Background.CONTRAST_5,
//                    LumoUtility.BorderRadius.LARGE);
//
//
//            layoutLocation.add(layoutLocationSearch,locationResultLayout);



        });

        upload.addFailedListener(event -> {
//            Notification.show("Upload failed: addFailedListener: " + event.getReason()+ " getContentLength: "+event.getContentLength());
            String errorMessage = "Upload failed: " + event.getReason();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);


            String strMessage = "Upload failed: " + event.getReason();
            output.removeAll();
            output.add(new Text(strMessage));

            emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", strMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + strMessage);
            logErrorInDb(null, "addFailedListener " + event.getReason(), this.file.getAbsolutePath(), intUserId, strUserName);


        });

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", errorMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + errorMessage);
            logErrorInDb(null, "addFileRejectedListener " + event.getErrorMessage(), this.file.getAbsolutePath(), intUserId, strUserName);
        });

        return layout;
    }


    private VerticalLayout showLocationLayout(String value) {
        String[] location = null;
        location = weatherService.lookUpLocation(value, "", "");

        Div divSelectedLocation = new Div();
        if (location != null && location.length != 0) {
            divSelectedLocation.setText("Area: " + location[2] + " Country: " + location[3]);
        }

        VerticalLayout layoutWeather = new VerticalLayout();
        layoutWeather.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);

        Button btnSelectLocation = new Button("Show Weather");
        String[] finalLocation = location;
        btnSelectLocation.addClickListener(clickevent -> {

            StringBuilder locationInfo = new StringBuilder();
            locationInfo.append(value);
            for (String s : finalLocation) {
                locationInfo.append(", ").append(s);
            }

            Notification notification = Notification.show(locationInfo.toString(), 4000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            String[] currentWeatherData = weatherService.getCurrentWeather(Double.parseDouble(finalLocation[0]), Double.parseDouble(finalLocation[1]));

            Div divTime = new Div(currentWeatherData[14]);
            divTime.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divSunRise = new Div(currentWeatherData[12]);
            divSunRise.getStyle().setFontWeight(Style.FontWeight.BOLD);

            Div divSunset = new Div(currentWeatherData[13]);
            divSunset.getStyle().setFontWeight(Style.FontWeight.BOLD);

            Div divFeelsLike = new Div(currentWeatherData[1]);
            divFeelsLike.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divHumidity = new Div(currentWeatherData[4]);
            divHumidity.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divWindSpeed = new Div(currentWeatherData[7]);
            divWindSpeed.getStyle().setFontWeight(Style.FontWeight.BOLDER);

            Div divClouds = new Div(currentWeatherData[15]);
            divClouds.getStyle().setFontWeight(Style.FontWeight.BOLDER);


            layoutWeather.removeAll();

            layoutWeather.setMinWidth("180px");
            layoutWeather.setMargin(false);
            layoutWeather.setSpacing(false);
            layoutWeather.setPadding(false);
            layoutWeather.add(new HorizontalLayout(new Div("Sunrise: "), divSunRise));
            layoutWeather.add(new HorizontalLayout(new Div("Sunset: "), divSunset));
            layoutWeather.add(new HorizontalLayout(new Div("Feels like: "), divFeelsLike));
            layoutWeather.add(new HorizontalLayout(new Div("Clouds: "), divClouds));
            layoutWeather.add(new HorizontalLayout(new Div("Humidity: "), divHumidity));
            layoutWeather.add(new HorizontalLayout(new Div("Wind speed: "), divWindSpeed));

        });

        VerticalLayout layout = new VerticalLayout(divSelectedLocation, btnSelectLocation, layoutWeather);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("padding", "var(--lumo-space-m) var(--lumo-space-m) var(--lumo-space-xs)");

        return layout;
    }


    public InputStream loadFile(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logErrorInDb(e, "loadFile  fileName: " + fileName, this.file.getAbsolutePath(), intUserId, strUserName);
            logger.error("Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * Load a file from local filesystem.
     */
    public InputStream loadFile() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logErrorInDb(e, "loadFile", this.file.getAbsolutePath(), intUserId, strUserName);
            logger.error("Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * Receive an uploaded file to a file.
     */
//  https://cookbook.vaadin.com/upload-image-to-file
    public OutputStream receiveUpload(String originalFileName, String MIMEType) {
        this.originalFileName = originalFileName;
        this.mimeType = MIMEType;
        try {
            // Create a temporary file for example, you can provide your file here.
            this.file = File.createTempFile("prefix-", "-suffix");
            file.deleteOnExit();
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            logErrorInDb(e, "receiveUpload", this.file.getAbsolutePath(), intUserId, strUserName);
            logger.error("Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        } catch (IOException e) {
            logErrorInDb(e, "receiveUpload", this.file.getAbsolutePath(), intUserId, strUserName);
            logger.error("Failed to create InputStream for: '" + this.file.getAbsolutePath() + "'", e);
        }

        return null;
    }

    private void logErrorInDb(Exception e, String function, String info, int intUserId, String strUsername) {

//        Notification.show(" logErrorInDb  .  " + function + "  .  " + info);
        recordService.logErrorInDb(e, "", function, intUserId, strUsername, "", "", info);
    }

    //                lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME)); // date time
//                lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE)); // camera make
//                lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL)); // camera model
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE)); // lens make
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL)); // camera model
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH)); // focal length
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT)); // focal length in ff
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO)); // iso
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE)); // shutter speed
//                lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE)); // aperture

    private boolean confirmedUploadPhoto(String strNewFileName, ArrayList<String> lstPhotoMetaData, String publicIp, String hostname,
                                         StringBuilder strImageMetaInfo) {


        String strPathUpload = DIR_PHOTOS_SERVER + dirChar + subPathUpload;
        String outputUploadFileName = strPathUpload + dirChar + strNewFileName;
        File fileUploaded = new File(outputUploadFileName);

        String strPathShow = DIR_PHOTOS_SERVER + dirChar + subPathShow;
        String outputShowFileName = strPathShow + dirChar + strNewFileName;
        File directoryShow = new File(strPathShow);
        File fileShow = new File(outputShowFileName);


        try {

            FileUtils.copyFileToDirectory(fileUploaded, directoryShow);


            int intSize = 140;
            String strSubPath = subPathThumbs;
            String strPathThumbs = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputThumbsFileName = strPathThumbs + dirChar + strNewFileName;
            File fileThumbs = new File(outputThumbsFileName);
            BufferedImage bImageT = ImageIO.read(fileShow);
            BufferedImage bufferedThumb = Scalr.resize(bImageT, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
            ImageIO.write(bufferedThumb, "jpg", fileThumbs);

            intSize = 660;
            strSubPath = subPathSmall;
            String strPathSmall = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputSmallFileName = strPathSmall + dirChar + strNewFileName;
            File fileSmall = new File(outputSmallFileName);
            BufferedImage bImageS = ImageIO.read(fileShow);
            BufferedImage bufferedSmall = Scalr.resize(bImageS, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
            ImageIO.write(bufferedSmall, "jpg", fileSmall);


            intSize = 1040;
            strSubPath = subPathMedium;
            String strPathMedium = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputMediumFileName = strPathMedium + dirChar + strNewFileName;
            File fileMedium = new File(outputMediumFileName);
            BufferedImage bImageM = ImageIO.read(fileShow);
            BufferedImage bufferedMedium = Scalr.resize(bImageM, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
            ImageIO.write(bufferedMedium, "jpg", fileMedium);

            intSize = 1990;
            strSubPath = subPathLarge;
            String strPathLarge = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputLargeFileName = strPathLarge + dirChar + strNewFileName;
            File fileLarge = new File(outputLargeFileName);
            BufferedImage bImageL = ImageIO.read(fileShow);
            BufferedImage bufferedLarge = Scalr.resize(bImageL, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
            ImageIO.write(bufferedLarge, "jpg", fileLarge);

            logger.info("path to compress from: " + fileShow.getAbsolutePath());
            logger.info("path to compress   to:   " + strPathLarge);


//
//            } catch (IOException e) {
//
//                String errorMessage = "reCompress failed: " + e.getMessage();
//
//                Notification notification = Notification.show(
//                        errorMessage,
//                        5000,
//                        Notification.Position.MIDDLE
//                );
//                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//            }


            logger.info(" before insert:  0 " + lstPhotoMetaData.get(0) + " 1 " + lstPhotoMetaData.get(1) + " 2 " + lstPhotoMetaData.get(2) + " 3 " + lstPhotoMetaData.get(3)
                    + " 4 " + lstPhotoMetaData.get(4) + " 5 " + lstPhotoMetaData.get(5) + " 6 " + lstPhotoMetaData.get(6) + " 7 " + lstPhotoMetaData.get(7)
                    + " 8 " + lstPhotoMetaData.get(8) + " 9 " + lstPhotoMetaData.get(9) + " 10MeteringMode " + lstPhotoMetaData.get(10)
                    + " 11Length " + lstPhotoMetaData.get(11) + " 12Width " + lstPhotoMetaData.get(12)+ "  .........");

            try {
                Double.parseDouble(lstPhotoMetaData.get(5));
            } catch (NumberFormatException e) {
                lstPhotoMetaData.set(5, "0");
            }

            try {
                Double.parseDouble(lstPhotoMetaData.get(6));
            } catch (NumberFormatException e) {
                lstPhotoMetaData.set(6, "0");
            }

            String strPhotoShutterSpeed = lstPhotoMetaData.get(8);
            double dblPhotoShutterSpeed = 0;
            if (!strPhotoShutterSpeed.trim().equalsIgnoreCase("null") && strPhotoShutterSpeed.trim().length() > 0) {
                String strSS = "";
                try {

                    if ((strPhotoShutterSpeed.indexOf("(") == -1) && (strPhotoShutterSpeed.indexOf(")") == -1)) {
                        strSS = strPhotoShutterSpeed; // integer
                    } else {
                        strSS = strPhotoShutterSpeed.substring(strPhotoShutterSpeed.indexOf("(") + 1, strPhotoShutterSpeed.indexOf(")"));
                    }

                    dblPhotoShutterSpeed = Double.parseDouble(strSS);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //      logErrorInDb(e, "insertPhotoToDb", e.getMessage(),intUserId,strUserName);
                }

            }

            double dblPhotoAperture = 0;
            String strPhotoAperture = lstPhotoMetaData.get(9);
            if (!strPhotoAperture.trim().equalsIgnoreCase("null") && strPhotoAperture.trim().length() > 0) {
                String strAperture = "";
                try {
                    if ((strPhotoAperture.indexOf("(") == -1) && (strPhotoAperture.indexOf(")") == -1)) {
                        strAperture = strPhotoAperture; // integer
                    } else {
                        strAperture = strPhotoAperture.substring(strPhotoAperture.indexOf("(") + 1, strPhotoAperture.indexOf(")"));
                    }
                    dblPhotoAperture = Double.parseDouble(strAperture);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

            int intIso = 0;
            String strIso = lstPhotoMetaData.get(7);
            if (strIso != null && !strIso.isEmpty() && !strIso.trim().equalsIgnoreCase(("null"))) {
                intIso = Integer.parseInt(strIso);
            }


            String strMeteringMode = lstPhotoMetaData.get(10);
            if (strMeteringMode!=null && !strMeteringMode.isEmpty() && !strMeteringMode.trim().equalsIgnoreCase("null")) {
                    //double dblF = Double.parseDouble(strAperture);
            }else{
                strMeteringMode = "";
            }

            int intLength = 0;
            String strILength = lstPhotoMetaData.get(11);
            if (strILength!= null && !strILength.isEmpty() && !strILength.trim().equalsIgnoreCase("null")) {
                try{
                    intLength = Integer.parseInt(strILength);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

            int intWidth = 0;
            String strIWidth = lstPhotoMetaData.get(12);
            if (strIWidth!= null && !strIWidth.isEmpty() && !strIWidth.trim().equalsIgnoreCase("null")) {
                try{
                    intWidth = Integer.parseInt(strIWidth);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }


            String strOrientation = lstPhotoMetaData.get(13);


            if (insertPhotoToDb(publicIp, sessionDateTime, strNewFileName, hostname, fileShow.length(), fileLarge.length(),
                    fileMedium.length(), fileSmall.length(), fileThumbs.length(), strImageMetaInfo.toString(), lstPhotoMetaData.get(0), lstPhotoMetaData.get(1),
                    lstPhotoMetaData.get(2), lstPhotoMetaData.get(3), lstPhotoMetaData.get(4), Double.parseDouble(lstPhotoMetaData.get(5)),
                    Double.parseDouble(lstPhotoMetaData.get(6)), intIso,
                    dblPhotoShutterSpeed, dblPhotoAperture, strMeteringMode,intLength, intWidth ,strOrientation)) {


                return true;
            } else {
                String errorMessage = "Upload failed!";

                Notification notification = Notification.show(
                        errorMessage,
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }


        } catch (IOException e) {

            String errorMessage = "Upload failed: " + e.getMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            logErrorInDb(e, "getUploadImageCard upload failed. dir: ", this.file.getAbsolutePath(), intUserId, strUserName);
            logger.error(" upload failed. dir: " + this.file.getAbsolutePath() + "  " + e.getMessage());
            return false;
        }

        return false;

    }

    private boolean insertPhotoToDb(String publicIp, String sessionDateTime, String strNewFileName, String hostname, long photoSpaceSize, long photoSpaceSizeLarge,
                                    long photoSpaceSizeMedium, long photoSpaceSizeSmall, long photoSpaceSizeThumb,
                                    String strImageMetaInfo, String strPhotoDateTime, String strPhotoCameraMake,
                                    String strPhotoCameraModel, String strPhotoLensMake, String strPhotoLensModel, double dblPhotoFocalLength, double dblPhotoFocalLengthFF,
                                    int intPhotoISO,
                                    double dblPhotoShutterSpeed, double dblPhotoAperture, String strMeteringMode, int imageLength, int imageWidth, String strOrientation)
                                     {

        // String publicIpAddress = VaadinSession.getCurrent().getBrowser().getAddress();
        String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
        int versionOfBrowserMajor = VaadinSession.getCurrent().getBrowser().getBrowserMajorVersion();
        int versionOfBrowserMinor = VaadinSession.getCurrent().getBrowser().getBrowserMinorVersion();
        int intUiId = VaadinSession.getCurrent().getNextUIid();

        int[] availWidth = calcTotalAvailableWidth();

        String strOS = "";

        if (VaadinSession.getCurrent().getBrowser().isAndroid()) {
            strOS = "Android";
        } else if (VaadinSession.getCurrent().getBrowser().isIPhone()) {
            strOS = "IPhone";
        } else if (VaadinSession.getCurrent().getBrowser().isWindows()) {
            strOS = "Windows";
        } else if (VaadinSession.getCurrent().getBrowser().isLinux()) {
            strOS = "Linux";
        } else if (VaadinSession.getCurrent().getBrowser().isMacOSX()) {
            strOS = "Mac OS X";
        } else {
            strOS = "Unknown";
        }


        ArrayList<Object[]> listInsertValues = new ArrayList<>();
        String[] imageInfo = {strImageMetaInfo};
        listInsertValues.add(imageInfo);

        int arrLength = listInsertValues.get(0).length;
        String[] arrType = new String[arrLength];
        for (int i = 0; i < arrLength; i++) {
            arrType[i] = "java.lang.String";
        }

        ArrayList<String[]> listInsertTypes = new ArrayList<>();

        for (int i = 0; i < listInsertValues.size(); i++) {
            listInsertTypes.add(arrType);
        }

        if (strPhotoLensMake.isEmpty()) {
            strPhotoLensMake = " null ";
        }


        String insertSQL = "INSERT INTO photo_meta SET id = 0,  date_fromapp = now(), uploaderId = " + intUserId + ", uploader = '" + strUserName + "', name_new = '" + strNewFileName + "', hostname = '" + hostname + "', " +
                " space_size = '" + photoSpaceSize + "', " +
                " space_size_large = '" + photoSpaceSizeLarge + "', " +
                " space_size_medium = '" + photoSpaceSizeMedium + "', " +
                " space_size_small = '" + photoSpaceSizeSmall + "', " +
                " space_size_thumb = '" + photoSpaceSizeThumb + "', " +
                " meta_all = ? , " +
//                " meta_date = DATE_FORMAT("+strPhotoDateTime+", '%Y:%m:%d %h:%i:%s')";
                " meta_date = DATE_FORMAT(" + strPhotoDateTime + ", '%Y:%m:%d %H:%i:%s')," +
                " meta_camera_make = " + strPhotoCameraMake + ", " +
                " meta_camera_model = " + strPhotoCameraModel + ", " +
                " meta_lens_make = " + strPhotoLensMake + ", " +
                " meta_lens_model = " + strPhotoLensModel + ", " +
                " meta_focal_length = '" + dblPhotoFocalLength + "', " +
                " meta_focal_length_ff = '" + dblPhotoFocalLengthFF + "', " +
                " meta_iso = '" + intPhotoISO + "' " +
                " , meta_shutter_speed = '" + dblPhotoShutterSpeed + "' " +
                " , meta_aperture = '" + dblPhotoAperture + "' "
                +" , meta_metering_mode = '" + strMeteringMode + "' "
                +" , meta_i_length = '" + imageLength + "' "
                +" , meta_i_width = '" + imageWidth + "' "
                +" , meta_orientation = '" + strOrientation + "' "
                ;


        logger.info("  insert SQL:   " + insertSQL);

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.setGlobalInfo(hostname, intUserId, strUserName, publicIp, sessionId);
        if (recordService.massRecordInsert(lstQueryInsert, listInsertValues, listInsertTypes) == 1) {
            return true;
        } else {
            logErrorInDb(null, "UploadImageCard insertPhotoToDb.", insertSQL, intUserId, strUserName);
            return false;
        }

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


    private String getFileSizeAsString(File file) {

        return String.format("%.2f", getFileSizeAsDouble(file));

    }

    private double getFileSizeAsDouble(File file) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }

    private double getFileSizeMB(double fileSize) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }

    private String getClientPublicpublicIp() {
        String urlString = "http://checkpublicIp.amazonaws.com/";
        String publicpublicIp = "";
        try {
            URL url = new URL(urlString);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            publicpublicIp = br.readLine();
        } catch (IOException MalformedURLException) {
            logger.error("error getClientPublicpublicIp from " + urlString);
        }
        return publicpublicIp;
    }

    /*
    temporary called only by GalleryView
     */
//    public VerticalLayout getLocationSelectionLayout() {
//        VerticalLayout verticalLayout = new VerticalLayout();
//
//        HorizontalLayout layoutLocationSearch = new HorizontalLayout();
//        layoutLocationSearch.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER, LumoUtility.Width.FULL,
//                LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE);
//        Div divLocation = new Div("Find:");
//        TextField txtLocation = new TextField();
//
//
//        VerticalLayout locationResultLayout = new VerticalLayout();
//        locationResultLayout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER, LumoUtility.Width.FULL,
//                LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE);
//
//
//
//        Button btnSearchLocation = new Button("Find");
//        btnSearchLocation.addThemeVariants(ButtonVariant.LUMO_SMALL);
//        btnSearchLocation.addClickListener(clickevent -> {
//            locationResultLayout.add(showLocationLayout(txtLocation.getValue()));
//
//        });
//
//        layoutLocationSearch.removeAll();
//        layoutLocationSearch.add(divLocation, txtLocation, btnSearchLocation);
//        verticalLayout.removeAll();
//        verticalLayout.add(layoutLocationSearch,locationResultLayout);
//        return verticalLayout;
//    }
}
