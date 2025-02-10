package com.photo.act.photo_act.views.components;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtils;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static com.photo.act.photo_act.views.ImageGalleryView.*;

public class UploadImageCard extends VerticalLayout {


    private File file;
    private String originalFileName;
    private String mimeType;
    private RecordService recordService;

    private MultiFileBuffer multiFileBuffer = new MultiFileBuffer();

    //MultiFileMemoryBuffer multiFileMemoryBuffer = new MultiFileMemoryBuffer();

    private static final Logger logger = LoggerFactory.getLogger(UploadImageCard.class);
    private String dirChar = FileSystems.getDefault().getSeparator();

    private int userId;
    private String strUserName;
    private String sessionId;

    //    https://cookbook.vaadin.com/upload-image-to-file
    public VerticalLayout getUploadImageCard(int userId, String strUserName, long sessionCreation, String hostname, RecordService recordService) {
        this.recordService = recordService;
        this.userId = userId;
        this.strUserName = strUserName;
        VerticalLayout layout = new VerticalLayout();
        this.sessionId = Long.toString(sessionCreation);
//        MemoryBuffer buffer = new MemoryBuffer();
//        Upload upload = new Upload(buffer);

//        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
//        Upload upload = new Upload(multiFileBuffer);

        Upload upload = new Upload(this::receiveUpload);
        upload.setMaxFiles(3);

        int maxFileSizeInBytes = 12 * 1024 * 1024; // 12MB
        upload.setMaxFileSize(maxFileSizeInBytes);


        Div output = new Div(new Text("(no image file uploaded yet) (max size:12MB)"));
        output.getStyle().setAlignItems(Style.AlignItems.CENTER);
        output.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        layout.add(upload, output);

        upload.setAcceptedFileTypes("image/jpeg", "image/png");//, "image/gif");

        upload.addSucceededListener(event -> {
            output.removeAll();

            Image image = new Image();
            output.add(new Text("Uploaded: "+originalFileName+" to "+ file.getAbsolutePath()+ " Type: "+mimeType+" Size: "+file.length()));

//            // Determine which file was uploaded
//            String fileName = event.getFileName();
//            // Read the data for that specific file.
//            InputStream inputStream = multiFileMemoryBuffer.getInputStream(fileName);
//            // Get other information about the file.
//            String mimeType = event.getMIMEType();
//            long contentLength = event.getContentLength();
//            image.setSrc(inputStream.toString());

            /*
            ImageService imageService = new ImageService();
           String  strPathUploads =DIR_PHOTOS_SERVER + dirChar + subPathUploads;
         String strResult = imageService.copyImage(fileName, inputStream, strPathUploads);
           // imageService.uploadImage()
            logger.info(strResult);
*/
//            FileInputStream fileInputStream = null;
//            try {
//                fileInputStream = new FileInputStream(uploadFileName);
//            } catch (FileNotFoundException e) {
//                logger.error(" uploadFileName:"+uploadFileName+" "+e.getMessage());
//                throw new RuntimeException(e);
//            }
//
//            //StreamResource streamResource = new StreamResource(uploadFileName,loadFile());
//            logger.info("to Image.  path: "+absolutePath+" filename: "+uploadFileName);
//
//            StreamResource streamResource = new StreamResource(uploadFileName,this::loadFile);
//
//          //  Image img = new Image(new StreamResource(this.originalFileName,this::loadFile),"Uploaded image");
//
//            String strImgPath = absolutePath + dirChar + uploadFileName;
//            image.setSrc(absolutePath);

            StreamResource streamResource = new StreamResource(this.originalFileName,this::loadFile);
            image.setSrc(streamResource);
            image.setWidth("68%");
            image.setHeight("auto");
            image.getStyle().setAlignItems(Style.AlignItems.CENTER);
            image.getStyle().setJustifyContent(Style.JustifyContent.CENTER);

            VerticalLayout layoutImageInfo = new VerticalLayout();
            layoutImageInfo.setWidthFull();
            layoutImageInfo.setSpacing(true);
            layoutImageInfo.setMargin(false);
            layoutImageInfo.setPadding(false);

            ArrayList<String> lstPhotoMetaData = new ArrayList<>();

            String strPathUpload = DIR_PHOTOS_SERVER + dirChar+subPathUpload;

            UUID uuid= UUID.randomUUID();
            String strUUID = uuid.toString();
            String strNewFileName = userId+"_"+strUserName+"_"+strUUID+".jpg";

            InputStream isUpload = loadFile(file.getAbsolutePath());
            OutputStream outUpload = null;
            String outputUploadFileName = strPathUpload+dirChar+strNewFileName;
            File outputUploadFile = new File(outputUploadFileName);
            try {
                outUpload = new FileOutputStream(outputUploadFile);
                StreamUtils.copy(isUpload, outUpload);
                logger.info(" uploadPhoto upload: "+strNewFileName+" size: "+getFileSizeAsString(outputUploadFile));
            } catch (IOException e) {
                logErrorInDb(e,"getUploadImageCard copy to upload",this.file.getAbsolutePath(),userId,strUserName);
                logger.error(" copy to upload "+e.getMessage());
                throw new RuntimeException(e);
            }

            ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
            File imgFile = new File(outputUploadFileName);
            StringBuilder strImageMetaInfo = new StringBuilder();
            try {
                strImageMetaInfo.append(imageUtilsMeta.getMetadataInfo(imgFile));
                lstPhotoMetaData = imageUtilsMeta.getListImageInfo();
                Html imageInfo = new Html(strImageMetaInfo.toString());
                layoutImageInfo.add(imageInfo);
                imageUtilsMeta.printPhotoMetadataValue(imgFile);

            } catch (IOException e) {
                logErrorInDb(e,"getUploadImageCard  strImageMetaInfo "+e.getMessage(),this.file.getAbsolutePath(),userId,strUserName);
                logger.error(" strImageMetaInfo "+e.getMessage());
                throw new RuntimeException(e);
            }

            Button btnSave = new Button("Save");
            VerticalLayout photoToAddLayout = new VerticalLayout(image, layoutImageInfo,btnSave);
            photoToAddLayout.setWidthFull();
            photoToAddLayout.setSpacing(true);
            photoToAddLayout.setMargin(false);
            photoToAddLayout.setPadding(false);
            photoToAddLayout.getStyle().setAlignItems(Style.AlignItems.CENTER);
            photoToAddLayout.getStyle().setJustifyContent(Style.JustifyContent.CENTER);

            ArrayList<String> finalLstPhotoMetaData = lstPhotoMetaData;
            btnSave.addClickListener(clickevent -> {
                InputStream is2 = loadFile(file.getAbsolutePath());
                String strPathShow = DIR_PHOTOS_SERVER + dirChar+subPathShow;
                String outputShowFileName = strPathShow+dirChar+strNewFileName;
                logger.info(" to: outputUploadFileName: "+ outputUploadFileName);

                String strPathThumbs = DIR_PHOTOS_SERVER + dirChar+subPathThumbs;
                String outputThumbsFileName = strPathThumbs+dirChar+strNewFileName;
                OutputStream outShow = null;

                File outputShowFile = new File(outputShowFileName);
                try{
                    outShow = new FileOutputStream(outputShowFile);
                    StreamUtils.copy(is2, outShow);
                    logger.info(" uploadPhoto show A: "+strNewFileName+" size: "+getFileSizeAsString(outputShowFile));
                    ImageUtils.convertImageToJPG(outputShowFile.toPath());
                    logger.info(" uploadPhoto show B: "+strNewFileName+" size: "+getFileSizeAsString(outputShowFile));
                } catch (IOException e) {
                    logErrorInDb(e,"getUploadImageCard copy to show",this.file.getAbsolutePath(),userId,strUserName);
                    logger.error(" copy to show "+e.getMessage());
                    throw new RuntimeException(e);
                }

                BufferedImage bImage;
                try {
                    bImage = ImageIO.read(outputShowFile);
                    // BufferedImage bufferedThumb= Scalr.resize(bImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, 800,Scalr.OP_ANTIALIAS);   //  Imgscalr    https://www.baeldung.com/java-resize-image
                    File outputFileThumb = new File(outputThumbsFileName);
                    ImageIO.write(bImage, "jpg", outputFileThumb);
                    logger.info(" uploadPhoto thumbs: "+strNewFileName+" size: "+getFileSizeAsString(outputFileThumb));
                } catch (IOException e) {
                    logErrorInDb(e,"getUploadImageCard   try to write thumbs: "+e.getMessage(),this.file.getAbsolutePath(),userId,strUserName);
                    logger.error(" try to write thumbs:  "+e.getMessage());
                    throw new RuntimeException(e);
                }
                logger.info(" written:  "+outputThumbsFileName);

                String ip = getClientPublicIp();

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

                logger.info(" "+finalLstPhotoMetaData.get(0)+" "+finalLstPhotoMetaData.get(1)+" "+finalLstPhotoMetaData.get(2)+" "+finalLstPhotoMetaData.get(3) + " "
                + finalLstPhotoMetaData.get(4)+" "+finalLstPhotoMetaData.get(5)+" "+finalLstPhotoMetaData.get(6)+" "+finalLstPhotoMetaData.get(7)+" "
                        + finalLstPhotoMetaData.get(8)+" "+finalLstPhotoMetaData.get(9));

                insertImageToDb(ip,sessionCreation,strNewFileName,hostname, strImageMetaInfo.toString(), finalLstPhotoMetaData.get(0), finalLstPhotoMetaData.get(1),
                        finalLstPhotoMetaData.get(2), finalLstPhotoMetaData.get(3), finalLstPhotoMetaData.get(4), (int) Integer.parseInt(finalLstPhotoMetaData.get(5)),
                        (int) Integer.parseInt(finalLstPhotoMetaData.get(6)), (int) Integer.parseInt(finalLstPhotoMetaData.get(7)),
                        (Double) Double.parseDouble(finalLstPhotoMetaData.get(8)), (Double) Double.parseDouble(finalLstPhotoMetaData.get(9)));
            });

            output.add(photoToAddLayout);
        });

        upload.addFailedListener(event -> {
            Notification.show("Upload failed: addFailedListener: " + event.getReason()+ " getContentLength: "+event.getContentLength());
            output.removeAll();
            output.add(new Text("Upload failed: " + event.getReason()));
            logErrorInDb(null,"addFailedListener "+event.getReason(),this.file.getAbsolutePath(),userId,strUserName);

        });

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            logErrorInDb(null,"addFileRejectedListener "+event.getErrorMessage(),this.file.getAbsolutePath(),userId,strUserName);
        });

        return layout;
    }



    public InputStream loadFile(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logErrorInDb(e,"loadFile  fileName: "+fileName,this.file.getAbsolutePath(),userId,strUserName);
            logger.error( "Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        }
        return null;
    }

    /** Load a file from local filesystem.
     *
     */
    public InputStream loadFile() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logErrorInDb(e,"loadFile",this.file.getAbsolutePath(),userId,strUserName);
            logger.error( "Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        }
        return null;
    }
    /** Receive a uploaded file to a file.
     */
//    https://cookbook.vaadin.com/upload-image-to-file
    public OutputStream receiveUpload(String originalFileName, String MIMEType) {
        this.originalFileName = originalFileName;
        this.mimeType = MIMEType;
        try {
            // Create a temporary file for example, you can provide your file here.
            this.file = File.createTempFile("prefix-", "-suffix");
            file.deleteOnExit();
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            logErrorInDb(e,"receiveUpload",this.file.getAbsolutePath(),userId,strUserName);
            logger.error("Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        } catch (IOException e) {
            logErrorInDb(e,"receiveUpload",this.file.getAbsolutePath(),userId,strUserName);
            logger.error( "Failed to create InputStream for: '" + this.file.getAbsolutePath() + "'", e);
        }

        return null;
    }

    private void logErrorInDb(Exception e, String function, String info, int userId, String strUsername) {

        Notification.show(" logErrorInDb  .  "+function+"  .  "+info);
       // recordService.logErrorInDb(e,"",function,userId,strUsername,"","",info);
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


    private void insertImageToDb(String ip, long sessionCreation, String strNewFileName, String hostname, String strImageMetaInfo, String strPhotoDateTime, String strPhotoCameraMake,
                                 String strPhotoCameraModel, String strPhotoLensMake, String strPhotoLensModel, int intPhotoFocalLength, int intPhotoFocalLengthFF, int intPhotoISO,
                                 double dblPhotoShutterSpeed, double dblPhotoAperture) {

//        section = section.replaceAll("'", " ");
//        section = section.replaceAll("\"", " ");

        //search = search.replaceAll("'"," ");
        //search = search.replaceAll("\""," ");

        // String ipAddress = VaadinSession.getCurrent().getBrowser().getAddress();
        String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
        int versionOfBrowserMajor = VaadinSession.getCurrent().getBrowser().getBrowserMajorVersion();
        int versionOfBrowserMinor = VaadinSession.getCurrent().getBrowser().getBrowserMinorVersion();
        int intUiId = VaadinSession.getCurrent().getNextUIid();

        int[] availWidth = calcTotalAvailableWidth();

        String strOS = "";

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
        } else {
            strOS = "Unknown";
        }

        String sessionDateTime = calcDateTimeFromLong(sessionCreation);

        ArrayList<Object[]> listInsertValues = new ArrayList<>();
        String[] imageInfo = { strImageMetaInfo };
        listInsertValues.add(imageInfo);

        int arrLength = listInsertValues.get(0).length;
        String[] arrType = new String[arrLength];
        for (int i = 0; i < arrLength; i++)
        {
            arrType[i]= "java.lang.String";
        }

        ArrayList<String[]> listInsertTypes = new ArrayList<>();

        for (int i = 0; i < listInsertValues.size(); i++) {
            listInsertTypes.add(arrType);
        }

        String insertSQL = "INSERT INTO photo_meta SET id = 0,  date_fromapp = now(), uploaderId = " + userId + ", uploader = '" + strUserName + "', name_new = '"+strNewFileName+"', hostname = '"+hostname+"', "+
                " meta_all = ? , " +
                " meta_date = DATE_FORMAT("+strPhotoDateTime+", '%Y:%m:%d %h:%i:%s')," +
                " meta_camera_make = '"+strPhotoCameraMake+"', "+
                " meta_camera_model = '"+strPhotoCameraModel+"', "+
                " meta_lens_make = '"+strPhotoCameraMake+"', "+
                " meta_lens_model = '"+strPhotoCameraModel+"', "+
                " meta_focal_length = '"+intPhotoFocalLength+"', "+
                " meta_focal_length_ff = '"+intPhotoFocalLengthFF+"', "+
                " meta_iso = '"+intPhotoISO+"', "+
                " meta_shutter_speed = '"+dblPhotoShutterSpeed+"', "+
                " meta_aperture = '"+dblPhotoAperture+"' ";

        logger.info("  insert SQL:   "+insertSQL);

        ArrayList<String> lstQueryInsert = new ArrayList<String>();
        lstQueryInsert.add(insertSQL);

        recordService.setGlobalInfo(hostname, userId, strUserName,  ip,  sessionId);
        recordService.massRecordInsert(lstQueryInsert, listInsertValues, listInsertTypes);
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

    private String calcDateTimeFromLong(Long datetime) {

        Instant instant = Instant.ofEpochMilli(datetime);
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return localDateTime.format(formatter);
    }

    private String getFileSizeAsString(File file){

        return String.format("%.2f", getFileSizeAsDouble(file));

    }

    private double getFileSizeAsDouble(File file) {

        double filesizeMB = (double) file.length() / (1024 * 1024);// + " mb";
        return filesizeMB;
    }

    private String getClientPublicIp() {
        String urlString = "http://checkip.amazonaws.com/";
        String publicIp = "";
        try {
            URL url = new URL(urlString);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            publicIp = br.readLine();
        } catch (IOException MalformedURLException) {
            logger.error("error getClientPublicIp from " + urlString);
        }
        return publicIp;
    }

}
