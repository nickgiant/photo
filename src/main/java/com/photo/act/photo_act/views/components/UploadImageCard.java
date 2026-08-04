package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailSendService;
import com.photo.act.photo_act.services.PhotoProcessingService;
import com.photo.act.photo_act.services.WeatherService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.photo.act.photo_act.utils.SlugUtil;
import com.photo.act.photo_act.utils.UtilsDate;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
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
import java.util.List;
import java.util.UUID;

import static com.photo.act.photo_act.views.GalleryView.DIR_PHOTOS_SERVER;
import static com.photo.act.photo_act.views.HomeView.*;
import static com.photo.act.photo_act.views.MainLayout.*;


public class UploadImageCard extends VerticalLayout {


    private GenericView genericView;
    private File file;
    private String originalFileName;
    private String mimeType;
    private RecordService recordService;

    private String[] arrGenreNames = {"id", "title"};
    private String sqlReadGenre = "SELECT id,  title " +
            " FROM  photo_genres " +
            " ORDER BY title ASC ";

    private String[] arrDestinationNames = {"id", "city_name", "prefecture", "country"};
    private String sqlReadDestination = "SELECT distinct city_name, id, prefecture, country " +
            " FROM  destination d " +
            " ORDER BY country ASC, city_name ASC ";


    private String[] arrSubjectNames = {"id", "subject_name", "subject_description", "subject_type"};
    private String sqlReadSubject = "SELECT distinct subject_name, id,  subject_description, subject_type " +
            " FROM  subject s " +
            " ORDER BY subject_name ASC ";

    private static final Logger logger = LoggerFactory.getLogger(UploadImageCard.class);
    private UtilsDate utilsDate;

    private String dirChar = FileSystems.getDefault().getSeparator();

    private int intUserId;
    private String strUserName;
    private String publicIp;
    private String hostname;
    private String sessionId;
    private String sessionDateTime;
//    private WeatherService weatherService;

    private EmailSendService emailSendService;
    private PhotoProcessingService photoProcessingService;
    private boolean isTypeProfile = false;

    private String strMailboxSend = "info@photoact.net";

    @Autowired
    public UploadImageCard(RecordService recordService, EmailSendService emailSendService,
                           PhotoProcessingService photoProcessingService,
                           int intUserId, String strUserName, long sessionCreation, String publicIp, String hostname) {
        this.recordService = recordService;
        this.emailSendService = emailSendService;
        this.photoProcessingService = photoProcessingService;
        this.intUserId = intUserId;
        this.strUserName = strUserName;

        this.publicIp = publicIp;
        this.hostname = hostname;

        this.sessionId = Long.toString(sessionCreation);

//        weatherService = new WeatherService("metric");
        genericView = new GenericView(recordService);
        utilsDate = new UtilsDate();

        this.sessionDateTime = utilsDate.calcDateTimeFromLong(sessionCreation, "UTC");
    }

    //    https://cookbook.vaadin.com/upload-image-to-file
    public VerticalLayout getUploadImageCard() {

        DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);

        this.addClassName("image-gallery-view");
        this.addClassName("background");

        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.BorderColor.CONTRAST_5);


        VerticalLayout photoToAddLayout;

        List<File> outputFiles = new ArrayList<>();


        // UploadHandler uploadHandler = UploadHandler.toTempFile((uploadMetadata, file) -> outputFiles.add(file));

        //getElement().setAttribute("target", uploadHandler);

        // FileFactory fileFactory = (metadata) -> outputFiles.stream().findAny().get();// new File(strPathUpload, strNewFileName);
        // FileUploadHandler fileHandler = UploadHandler.toFile(successHandler, fileFactory);
        Div output = new Div();
        output.addClassName("gallery-upload");
        output.getStyle().setAlignItems(Style.AlignItems.CENTER);
        output.getStyle().setJustifyContent(Style.JustifyContent.CENTER);

        UploadHandler uploadHandler = UploadHandler.toTempFile((uploadMetadata, file) -> {
                    outputFiles.add(file);

                    logger.info("File saved to: " + outputFiles.size() + " - " + file.getAbsolutePath());
                    output.add(getPhotoForUploadPanel(file));
                })
                .whenStart(() -> {
                    // logger.info("File saved to: " + outputFiles.size() + " - " + file.getAbsolutePath());
                })
                .onProgress((transferredBytes, totalBytes) -> {
                    double percentage = (double) transferredBytes / totalBytes * 100;
//                    if (percentage == 50) {
                    //  logger.info("Upload progress:" + percentage);
                    //                   }
                })
                .whenComplete((success) -> {
                    if (success) {
                        logger.info("Upload completed successfully");
                    } else {
                        logger.info("Upload failed");
                    }
                });

        Upload upload = new Upload(uploadHandler);

        // Upload upload = new Upload(this::receiveUpload);
        upload.setMinHeight("160px");
        upload.addClassNames(LumoUtility.Width.FULL, LumoUtility.Height.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.SMALL, LumoUtility.BorderColor.CONTRAST_5);

        int intMemberMonthsCount = genericView.getAuthMemberMonthCount();
        Div divMaxFileNSize = new Div();
        divMaxFileNSize.addClassNames(LumoUtility.TextColor.DISABLED);
        if (intMemberMonthsCount >= 8) {
            divMaxFileNSize.setText("Max Photo size 9MB, Max Photos 7 per time");
            upload.setMaxFiles(7);
            int maxFileSizeInBytes = 9 * 1024 * 1024;
            upload.setMaxFileSize(maxFileSizeInBytes);
        } else {
            divMaxFileNSize.setText("Max Photo size 9MB, Max Photos 5 per time");
            upload.setMaxFiles(5);
            int maxFileSizeInBytes = 9 * 1024 * 1024;
            upload.setMaxFileSize(maxFileSizeInBytes);
        }

        layout.add(divMaxFileNSize, upload, output);

        upload.setAcceptedFileTypes("image/jpeg"); //, "image/png");//, "image/gif");


//        upload.addSucceededListener(event -> {

/*            Image image = new Image();

            VerticalLayout layoutImageInfo = new VerticalLayout();
            layoutImageInfo.setWidthFull();

            String strPathUpload = DIR_PHOTOS_SERVER + dirChar + subPathUpload;

            UUID uuid = UUID.randomUUID();
            String strUUID = uuid.toString();
            String strNewFileName = intUserId + "_" + strUserName + "_" + strUUID + ".jpg";

            InputStream isUpload = loadFile(file.getAbsolutePath());
            OutputStream outUpload = null;
            String outputUploadFileName = strPathUpload + dirChar + strNewFileName;
            File outputUploadFile = new File(outputUploadFileName);

//          output.add(new Text("Uploaded: "+originalFileName+" to "+ file.getAbsolutePath()+ " Type: "+mimeType+" Size: "+file.length()));

            image.setMaxWidth("68%");
            image.setMaxHeight("1000px");
            image.setHeight("auto");
            image.getStyle().setAlignItems(Style.AlignItems.CENTER);
            image.getStyle().setJustifyContent(Style.JustifyContent.CENTER);

            try {
                outUpload = new FileOutputStream(outputUploadFile);
                StreamUtils.copy(isUpload, outUpload);
                logger.info(" upload Photo Success to: " + this.originalFileName + " ---> " + strNewFileName + " size: " + getFileSizeAsString(outputUploadFile));
                final StreamResource imageResource = new StreamResource("streamResource", () -> {
                    try {
//                        ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
//                        imageUtilsMeta.printPhotoMetadataValue(file);

                        return new FileInputStream(outputUploadFile);
                    } catch (final FileNotFoundException e) {
                        // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
                        logger.error("FileNotFoundException  " + e.getMessage());
                    }
                    return null;
                });

                image.setSrc(imageResource);
                image.setAlt(file.getName());
                photoToAddLayout.add(image);
                photoToAddLayout.add(layoutSelections);
                photoToAddLayout.add(btnSave);*/

/*            } catch (Exception e) {

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
            }*/


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


        /*upload.addFailedListener(event -> {
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
        });*/

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            // emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", errorMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + errorMessage);
            logErrorInDb(null, "addFileRejectedListener " + event.getErrorMessage(), this.file.getAbsolutePath(), intUserId, strUserName);
        });

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

//    private StreamResource getPhoto(File file) {
//        final StreamResource imageResource = new StreamResource("streamResource", () -> {
//            try {

    /// /                        ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
    /// /                        imageUtilsMeta.printPhotoMetadataValue(file);
//
//                return new FileInputStream(file);
//            } catch (final FileNotFoundException e) {
//                // logErrorInDb(e,hostname,"CreationsViewCard StreamResource",userId,strUserName,file.getAbsolutePath());
//                logger.error("FileNotFoundException  " + e.getMessage());
//            }
//            return null;
//        });
//
//        return imageResource;
//    }
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

    private boolean confirmedUploadPhoto(String orgFileName, String strNewFileName, ArrayList<String> lstPhotoMetaData, String[] arrPhotoGpsMeta, String publicIp, String hostname,
                                         StringBuilder strImageMetaInfo,
                                         String strSubTitle, String strGenre,  String strDestination, String strSubject, String strPersonalNotes, boolean isTypeProfile
    ) {

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
//            BufferedImage bImageT = ImageIO.read(fileShow);
//            BufferedImage bufferedThumb = Scalr.resize(bImageT, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
//            ImageIO.write(bufferedThumb, "jpg", fileThumbs);

//            String outputThumbsDir = strPathThumbs + dirChar;
//            File fileThumbsB = new File(outputThumbsDir);
            Thumbnails.of(fileShow)
                    .size(intSize, intSize)
                    .useExifOrientation(true)
//                    .rotate(90)
//                    .watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("watermark.png")), 0.5f)
                    .outputQuality(0.6)
                    .toFile(fileThumbs);


            intSize = 660;
            strSubPath = subPathSmall;
            String strPathSmall = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputSmallFileName = strPathSmall + dirChar + strNewFileName;
            File fileSmall = new File(outputSmallFileName);
//            BufferedImage bImageS = ImageIO.read(fileShow);
//            BufferedImage bufferedSmall = Scalr.resize(bImageS, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
//            ImageIO.write(bufferedSmall, "jpg", fileSmall);

//            String outputSmallDir = strPathSmall + dirChar;
//            File fileThumbsC = new File(outputSmallDir);
            Thumbnails.of(fileShow)
                    .size(intSize, intSize)
                    .useExifOrientation(true)
//                    .rotate(90)
//                    .watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("watermark.png")), 0.5f)
                    .outputQuality(0.7)
                    .toFile(fileSmall);

            intSize = 1040;
            strSubPath = subPathMedium;
            String strPathMedium = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputMediumFileName = strPathMedium + dirChar + strNewFileName;
            File fileMedium = new File(outputMediumFileName);
//            BufferedImage bImageM = ImageIO.read(fileShow);
//            BufferedImage bufferedMedium = Scalr.resize(bImageM, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
//            ImageIO.write(bufferedMedium, "jpg", fileMedium);

//            String outputMediumDir = strPathMedium + dirChar;
//            File fileThumbsD = new File(outputMediumDir);
            Thumbnails.of(fileShow)
                    .size(intSize, intSize)
                    .useExifOrientation(true)
//                    .rotate(90)
//                    .watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("watermark.png")), 0.5f)
                    .outputQuality(0.7)
                    .toFile(fileMedium);

            intSize = 1990;
            strSubPath = subPathLarge;
            String strPathLarge = DIR_PHOTOS_SERVER + dirChar + strSubPath;
            String outputLargeFileName = strPathLarge + dirChar + strNewFileName;
            File fileLarge = new File(outputLargeFileName);
//            BufferedImage bImageL = ImageIO.read(fileShow);
//            BufferedImage bufferedLarge = Scalr.resize(bImageL, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, intSize, Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
//            ImageIO.write(bufferedLarge, "jpg", fileLarge);

//            String outputLargeDir = strPathLarge + dirChar;
//            File fileThumbsE = new File(outputLargeDir);
            Thumbnails.of(fileShow)
                    .size(intSize, intSize)
                    .useExifOrientation(true)
//                    .rotate(90)
//                    .watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("watermark.png")), 0.5f)
                    .outputQuality(0.8)
                    .toFile(fileLarge);

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

            if (lstPhotoMetaData==null || lstPhotoMetaData.isEmpty()) {

                if (insertPhotoToDb(publicIp, sessionDateTime, orgFileName, strNewFileName, hostname, fileShow.length(), fileLarge.length(),
                        fileMedium.length(), fileSmall.length(), fileThumbs.length(), strImageMetaInfo.toString(), "", "''",
                        "''", "''", "''", 0,
                        0, 0,
                        0, 0, "''", 0, 0, "''", arrPhotoGpsMeta,
                        strSubTitle, strGenre, strDestination, strSubject, strPersonalNotes, isTypeProfile
                )) {
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

            }else {
                logger.info(" before insert:  0 " + lstPhotoMetaData.get(0) + " 1 " + lstPhotoMetaData.get(1) + " 2 " + lstPhotoMetaData.get(2) + " 3 " + lstPhotoMetaData.get(3)
                        + " 4 " + lstPhotoMetaData.get(4) + " 5 " + lstPhotoMetaData.get(5) + " 6 " + lstPhotoMetaData.get(6) + " 7 " + lstPhotoMetaData.get(7)
                        + " 8 " + lstPhotoMetaData.get(8) + " 9 " + lstPhotoMetaData.get(9) + " 10MeteringMode " + lstPhotoMetaData.get(10)
                        + " 11Length " + lstPhotoMetaData.get(11) + " 12Width " + lstPhotoMetaData.get(12) + "  .........");

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
                if (strMeteringMode != null && !strMeteringMode.isEmpty() && !strMeteringMode.trim().equalsIgnoreCase("null")) {
                    //double dblF = Double.parseDouble(strAperture);
                } else {
                    strMeteringMode = "";
                }

                int intLength = 0;
                String strILength = lstPhotoMetaData.get(11);
                if (strILength != null && !strILength.isEmpty() && !strILength.trim().equalsIgnoreCase("null")) {
                    try {
                        intLength = Integer.parseInt(strILength);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

                int intWidth = 0;
                String strIWidth = lstPhotoMetaData.get(12);
                if (strIWidth != null && !strIWidth.isEmpty() && !strIWidth.trim().equalsIgnoreCase("null")) {
                    try {
                        intWidth = Integer.parseInt(strIWidth);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

                String strOrientation = lstPhotoMetaData.get(13);

                if (insertPhotoToDb(publicIp, sessionDateTime, orgFileName, strNewFileName, hostname, fileShow.length(), fileLarge.length(),
                        fileMedium.length(), fileSmall.length(), fileThumbs.length(), strImageMetaInfo.toString(), lstPhotoMetaData.get(0), lstPhotoMetaData.get(1),
                        lstPhotoMetaData.get(2), lstPhotoMetaData.get(3), lstPhotoMetaData.get(4), Double.parseDouble(lstPhotoMetaData.get(5)),
                        Double.parseDouble(lstPhotoMetaData.get(6)), intIso,
                        dblPhotoShutterSpeed, dblPhotoAperture, strMeteringMode, intLength, intWidth, strOrientation, arrPhotoGpsMeta,
                        strSubTitle, strGenre, strDestination, strSubject, strPersonalNotes, isTypeProfile
                )) {
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

    private VerticalLayout getPhotoForUploadPanel(File uploadedFile) {


        Image image = new Image();
        image.setMaxWidth("68%");
        image.setMaxHeight("1000px");
        image.setHeight("auto");
        image.getStyle().setAlignItems(Style.AlignItems.CENTER);
        image.getStyle().setJustifyContent(Style.JustifyContent.CENTER);



        UUID uuid = UUID.randomUUID();
        String strUUID = uuid.toString();
        String strNewFileName = intUserId + "_" + strUserName + "_" + strUUID + ".jpg";
        String strPathUpload = DIR_PHOTOS_SERVER + dirChar + subPathUpload;
        String outputUploadFileName = strPathUpload + dirChar + strNewFileName;
        File outputUploadFile = new File(outputUploadFileName);

        VerticalLayout photoToAddLayout = new VerticalLayout();
        photoToAddLayout.addClassNames(LumoUtility.Width.FULL, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
//                    LumoUtility.Background.CONTRAST_5,
                LumoUtility.Padding.XLARGE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Background.CONTRAST_5);


        image.setSrc(DownloadHandler.forFile(uploadedFile));
        image.setAlt(uploadedFile.getName());
        photoToAddLayout.add(image);

        VerticalLayout layoutSelections = new VerticalLayout();
        layoutSelections.addClassNames(

                );

        TextArea txtSubtitle = new TextArea("Short Description", "What differentiates this photo from the rest?");
        txtSubtitle.setMinWidth("300px");
        txtSubtitle.setMinRows(5);
        txtSubtitle.setMaxLength(120);

        Select<String> cmbGenre = new Select<>();
        cmbGenre.setLabel("Genre");
        cmbGenre.setHelperText("Select the Genre which describes best the photo.");


        Select<String> cmbDestination = new Select<>();
        cmbDestination.setLabel("Location");
        cmbDestination.setHelperText("Avoid to select, when there are identifiable humans.");
        Select<String> cmbSubject = new Select<>();
        cmbSubject.setLabel("Main Subject");
        cmbSubject.setHelperText("Select a subject when is the main object and location can be anywhere.");

        List<Record> lstDestinationRecs = getRecordsFromDb(sqlReadDestination, arrDestinationNames);
        ArrayList<String> lstDestinations = new ArrayList<>();
        ArrayList<String> lstDestinationsId = new ArrayList<>();
        for (int r = 0; r < lstDestinationRecs.size(); r++) {
            String strDestination = "";

            strDestination = lstDestinationRecs.get(r).getColumnData("city_name") + " (" + lstDestinationRecs.get(r).getColumnData("country") + ")";
            // strDestination = lstDestinationRecs.get(r).getColumnData("city_name");
            lstDestinations.add(strDestination);
            lstDestinationsId.add(lstDestinationRecs.get(r).getColumnData("Id"));
        }
        cmbDestination.setItems(lstDestinations);

        List<Record> lstGenreRecs = getRecordsFromDb(sqlReadGenre, arrGenreNames);
        ArrayList<String> lstGenres = new ArrayList<>();
        ArrayList<String> lstGenreId = new ArrayList<>();
        for (int r = 0; r < lstGenreRecs.size(); r++) {
            String strGenre = "";

            strGenre = lstGenreRecs.get(r).getColumnData("title");
            lstGenres.add(strGenre);
            lstGenreId.add(lstGenreRecs.get(r).getColumnData("id"));
        }
        cmbGenre.setItems(lstGenres);

        List<Record> lstSubjectRecs = getRecordsFromDb(sqlReadSubject, arrSubjectNames);
        ArrayList<String> lstSubjects = new ArrayList<>();
        ArrayList<String> lstSubjectsId = new ArrayList<>();
        for (int r = 0; r < lstSubjectRecs.size(); r++) {
            String strSubject = "";

            strSubject = lstSubjectRecs.get(r).getColumnData("subject_name");
            lstSubjects.add(strSubject);
            lstSubjectsId.add(lstSubjectRecs.get(r).getColumnData("Id"));
        }
        cmbSubject.setItems(lstSubjects);

        TextArea txtPersonalNotes = new TextArea("Notes","Notes only visible to you");
        txtPersonalNotes.setMinWidth("300px");
        txtPersonalNotes.setMinRows(3);
        txtPersonalNotes.setMaxLength(120);

        HorizontalLayout horLayoutB = new HorizontalLayout();
        horLayoutB.add( cmbGenre, cmbDestination, cmbSubject);

        Checkbox chkIsTypeProfile = new Checkbox("Is this photo for your Profile");
        chkIsTypeProfile.addValueChangeListener(event->{
            isTypeProfile = event.getValue();
            if(isTypeProfile){
                horLayoutB.setVisible(false);
                txtSubtitle.setVisible(false);
            }else {
                horLayoutB.setVisible(true);
                txtSubtitle.setVisible(true);
            }
        });

        HorizontalLayout horLayoutA = new HorizontalLayout();
        horLayoutA.add(txtPersonalNotes,chkIsTypeProfile);

        layoutSelections.add(horLayoutA,horLayoutB,txtSubtitle);

        Button btnSave = new Button("Upload Photo");
        btnSave.addClickListener(clickevent -> {

            //InputStream isUpload = outputFiles.get(ff).getAbsolutePath(); //loadFile(file.getAbsolutePath());
            OutputStream outUpload = null;

            try {
                FileInputStream fileInputStream = new FileInputStream(uploadedFile);
                outUpload = new FileOutputStream(outputUploadFile);
                StreamUtils.copy(fileInputStream, outUpload);
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
            }

            ArrayList<String> lstPhotoMetaData = new ArrayList<>();
            ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
            String[] arrPhotoGpsMeta = new String[3];

            File imgFile = new File(outputUploadFileName);
            logger.info("for photo " + outputUploadFileName + " get meta info");
            StringBuilder strImageMetaInfo = new StringBuilder();
            try {
                //  logger.info(" A for photo "+outputUploadFileName+" get meta info to html "+imgFile.getAbsolutePath());
                strImageMetaInfo.append(imageUtilsMeta.getMetadataInfo(imgFile));
                //   logger.info(" B for photo "+outputUploadFileName+" get meta info to list "+imgFile.getAbsolutePath());
                lstPhotoMetaData = imageUtilsMeta.getListImageInfo();
                arrPhotoGpsMeta = imageUtilsMeta.getPhotoGPSMeta(outputUploadFileName);

                //  if (lstPhotoMetaData != null && lstPhotoMetaData.size() > 0) {
                //  Html imageInfo = new Html(strImageMetaInfo.toString());
                // layoutImageInfo.add(imageInfo);
//                btnSave.setVisible(true);
                //   } else {

//                btnSave.setVisible(true);

//                    Notification notification = Notification.show(
//                            "Photo contains no metadata.",
//                            5000,
//                            Notification.Position.MIDDLE
//                    );
//                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                //  }
            } catch (Exception e) {

                String errorMessage = "Upload failed: " + e.getMessage();
                Notification notificationErr = Notification.show(
                        errorMessage,
                        5000,
                        Notification.Position.MIDDLE
                );
                notificationErr.addThemeVariants(NotificationVariant.LUMO_ERROR);

                emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "getUploadImageCard  strImage Meta Info  " + errorMessage + "  -  " + publicIp, " " + publicIp + " " + hostname + "  -  " + errorMessage);

                logErrorInDb(e, "getUploadImageCard  strImage Meta Info " + e.getMessage(), this.file.getAbsolutePath(), intUserId, strUserName);

                logger.error(" strImage Meta Info " + e.getMessage());
            }

            String strOrgFileName = this.originalFileName;
            // strOrgFileName = strOrgFileName.replaceAll("([\\^\\$\\*\\?\\(\\)\\|\\{\\}\\[\\]\\\\])", "");

            String strGenreId = "";
            String strGenre = cmbGenre.getValue();
            for (int i = 0; i < lstGenres.size(); i++) {
                if (lstGenres.get(i).equalsIgnoreCase(strGenre)) {
                    strGenreId = lstGenreId.get(i);
                }
            }

            String strDestinationId = "";
            String strDestination = cmbDestination.getValue();
            for (int i = 0; i < lstDestinations.size(); i++) {
                if (lstDestinations.get(i).equalsIgnoreCase(strDestination)) {
                    strDestinationId = lstDestinationsId.get(i);
                }
            }

            String strSubjectId = "";
            String strSubject = cmbSubject.getValue();
            for (int i = 0; i < lstSubjects.size(); i++) {
                if (lstSubjects.get(i).equalsIgnoreCase(strSubject)) {
                    strSubjectId = lstSubjectsId.get(i);
                }
            }


            if (confirmedUploadPhoto(strOrgFileName, strNewFileName, lstPhotoMetaData, arrPhotoGpsMeta, publicIp, hostname, strImageMetaInfo,
                    txtSubtitle.getValue().trim(), strGenreId, strDestinationId, strSubjectId, txtPersonalNotes.getValue().trim(), isTypeProfile)) {

                // Trigger CDN variant generation asynchronously — does not block the UI
                // Reads from photo-show (full-quality copy) and writes OG/Pinterest/medium/thumb to CDN
                String showFilePath = DIR_PHOTOS_SERVER + dirChar + subPathShow + dirChar + strNewFileName;
                if (photoProcessingService != null) {
                    photoProcessingService.processAsync(showFilePath, strNewFileName);
                }

                String messageUp = "Upload Finished!";
                Notification notificationUp = Notification.show(messageUp, 6000, Notification.Position.MIDDLE);
                notificationUp.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                logger.info("to sent mail for hostname:" + hostname);
                if (hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO_WIN) || hostname.equalsIgnoreCase(HOSTNAME_LAPTOP_LENOVO) || hostname.equalsIgnoreCase(HOSTNAME_LAPTOP)) {
                } else {
//                        emailSendService.sendSimpleMail(strMailboxSend, "nickgiant@yahoo.com", "Photo Uploaded", "From IP: " + publicIp + " username: " + strUserName + " (" + strFilesize + ")");
                    logger.info("mail sent");
                }

                photoToAddLayout.removeAll();
                //                         upload.clearFileList();
            }
        });


        photoToAddLayout.add(layoutSelections);
        photoToAddLayout.add(btnSave);
        return photoToAddLayout;

    }

    public List<Record> getRecordsFromDb(String sql, String[] arrColumnNames) {
        logger.info(" photo  getRecordsFromDb:   " + sql);
        return recordService.findAll(sql, arrColumnNames);
    }

    private boolean insertPhotoToDb(String publicIp, String sessionDateTime, String orgFileName, String strNewFileName, String hostname, long photoSpaceSize, long photoSpaceSizeLarge,
                                    long photoSpaceSizeMedium, long photoSpaceSizeSmall, long photoSpaceSizeThumb,
                                    String strImageMetaInfo, String strPhotoDateTime, String strPhotoCameraMake,
                                    String strPhotoCameraModel, String strPhotoLensMake, String strPhotoLensModel, double dblPhotoFocalLength, double dblPhotoFocalLengthFF,
                                    int intPhotoISO,
                                    double dblPhotoShutterSpeed, double dblPhotoAperture, String strMeteringMode, int imageLength, int imageWidth, String strOrientation,
                                    String[] arrPhotoGpsMeta,
                                    String strSubtitle, String strGenre, String strDestination, String strSubject, String strPersonalNotes, boolean isTypeProfile) {

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

        String strLat = arrPhotoGpsMeta[0] != null ? " '" + Double.parseDouble(arrPhotoGpsMeta[0]) + "' " : " NULL ";
        String strLon = arrPhotoGpsMeta[1] != null ? " '" + Double.parseDouble(arrPhotoGpsMeta[1]) + "' " : " NULL ";

        String strVisibleTo = " visible_to = 'ALL', ";
        if(isTypeProfile) {
            strVisibleTo = " visible_to = 'Profile', ";
        }

        if (strPhotoDateTime.isEmpty()){
            strPhotoDateTime = "'2000/01/01 00:00:00'";
        }

        String insertSQL = "INSERT INTO photo_meta SET id = 0,  date_fromapp = now(), uploaderId = " + intUserId + ", uploader = '" + strUserName + "', name_org = '" + orgFileName + "', name_new = '" + strNewFileName + "', hostname = '" + hostname + "', " +
                " space_size = '" + photoSpaceSize + "', " +
                " space_size_large = '" + photoSpaceSizeLarge + "', " +
                " space_size_medium = '" + photoSpaceSizeMedium + "', " +
                " space_size_small = '" + photoSpaceSizeSmall + "', " +
                " space_size_thumb = '" + photoSpaceSizeThumb + "', " +
                strVisibleTo +
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
                + " , meta_metering_mode = '" + strMeteringMode + "' "
                + " , meta_i_height = '" + imageLength + "' "
                + " , meta_i_length = '" + imageLength + "' "
                + " , meta_i_width = '" + imageWidth + "' "
                + " , meta_orientation = '" + strOrientation + "' "
                + " , location_lat = " + strLat
                + " , location_lon = " + strLon;


        logger.info("  insert SQL:   " + insertSQL);

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.setGlobalInfo(hostname, intUserId, strUserName, publicIp, sessionId);
        if (recordService.massRecordInsert(lstQueryInsert, listInsertValues, listInsertTypes) == 1) {

            if (!strGenre.isEmpty()) {
                String strUpdateGenre = "UPDATE photo_meta SET " +
                        " genre_id = '" + strGenre + "' " +
                        " WHERE name_new = '" + strNewFileName + "'";
                recordService.insertOneRecordWithQuery(strUpdateGenre, null, null);
            }

            if (!strDestination.isEmpty()) {
                String strUpdateDest = "UPDATE photo_meta SET " +
                        " destination_id = '" + strDestination + "' " +
                        " WHERE name_new = '" + strNewFileName + "'";
                recordService.insertOneRecordWithQuery(strUpdateDest, null, null);
            }

            // Slug tracks the assigned destination (plus the description, when set). When
            // neither is set, fall back to a fixed "010"-prefixed id so the photo still gets
            // a stable slug.
            boolean hasDescription = strSubtitle != null && !strSubtitle.isBlank();
            if (!strDestination.isEmpty() || !hasDescription) {
                String[] arrPhotoId = {"id"};
                List<Record> lstPhotoId = recordService.findAll(
                        "SELECT id FROM photo_meta WHERE name_new = '" + strNewFileName + "'", arrPhotoId);

                if (!lstPhotoId.isEmpty()) {
                    String strPhotoId = lstPhotoId.get(0).getColumnData("id");
                    String strPhotoSlug = null;

                    if (!strDestination.isEmpty()) {
                        String[] arrDestName = {"city_name", "country"};
                        List<Record> lstDestName = recordService.findAll(
                                "SELECT city_name, country FROM destination WHERE id = ?",
                                arrDestName, new Object[]{Integer.parseInt(strDestination)}, new String[]{"java.lang.Integer"});

                        if (!lstDestName.isEmpty()) {
                            String strDestDisplay = lstDestName.get(0).getColumnData("city_name") + " (" + lstDestName.get(0).getColumnData("country") + ")";
                            String strSlugBase = hasDescription ? strDestDisplay + " " + strSubtitle : strDestDisplay;
                            strPhotoSlug = SlugUtil.toSlug(strSlugBase) + "-" + strPhotoId;
                        }
                    } else if (!hasDescription) {
                        // Hyphenated — PhotoLightboxView resolves a photo route by extracting the
                        // trailing digit run, which needs a non-numeric marker before the id.
                        strPhotoSlug = "010-" + strPhotoId;
                    }

                    if (strPhotoSlug != null) {
                        recordService.insertOneRecordWithQuery(
                                "UPDATE photo_meta SET slug = ? WHERE id = ?",
                                new Object[]{strPhotoSlug, Integer.parseInt(strPhotoId)},
                                new String[]{"java.lang.String", "java.lang.Integer"});
                    }
                }
            }
            if (!strSubject.isEmpty()) {
                String strUpdateSubj = "UPDATE photo_meta SET " +
                        " subject_id = '" + strSubject + "' " +
                        " WHERE name_new = '" + strNewFileName + "'";
                recordService.insertOneRecordWithQuery(strUpdateSubj, null, null);
            }


            Object[] fieldValue = {strSubtitle, strPersonalNotes};
            String[] fieldType = {"java.lang.String", "java.lang.String"};

            String strUpdateSubj = "UPDATE photo_meta SET " +
                    " subtitle = ? , notes = ? " +
                    " WHERE name_new = '" + strNewFileName + "'";
            int ret = recordService.insertOneRecordWithQuery(strUpdateSubj, fieldValue, fieldType);


            Object[] fieldValueCount = {strUserName, intUserId};
            String[] fieldTypeCount = {"java.lang.String", "java.lang.Integer"};

            String strUpdateCount = "UPDATE dbuser_extra AS d " +
                    " JOIN ( " +
                    "    SELECT uploaderId, COUNT(*) AS photo_count " +
                    "    FROM photo_meta " +
                    "    WHERE visible_to = 'ALL' " +
                    "    GROUP BY uploaderId " +
                    " ) AS p ON d.user_id = p.uploaderId " +
                    " SET d.username = ? , d.count_photos = p.photo_count " +
                    " WHERE d.user_id = ? ";
            int retCount = recordService.insertOneRecordWithQuery(strUpdateCount, fieldValueCount, fieldTypeCount);


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