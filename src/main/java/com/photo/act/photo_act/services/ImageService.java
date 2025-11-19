package com.photo.act.photo_act.services;

// https://medium.com/@kouomeukevin/how-to-upload-and-download-image-into-sql-database-with-spring-boot-c849ec5daec6

import com.photo.act.photo_act.db.Record;
import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.utils.ImageUtilsMeta;
import com.photo.act.photo_act.views.components.GenericView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.photo.act.photo_act.views.GalleryView.DIR_PHOTOS_SERVER;
import static com.photo.act.photo_act.views.HomeView.subPathUpload;
import static com.photo.act.photo_act.views.MainLayout.PROP_PHOTOS;


@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private RecordService recordService;
    private String dirChar = FileSystems.getDefault().getSeparator();

    // https://medium.com/@dulanjayasandaruwan1998/uploading-images-in-a-spring-boot-project-a-step-by-step-guide-8a55248ea520
    public String uploadImage(MultipartFile file, String uploadDir) {

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            logger.error("Only JPEG or PNG images are allowed");
            throw new IllegalArgumentException("Only JPEG or PNG images are allowed");
        }

        // Save the file to the directory
        String filePath = saveImage(file, uploadDir);
        return "Image uploaded successfully: " + filePath;
    }

    private String saveImage(MultipartFile file, String uploadDir) {
        Path uploadPath = Paths.get(uploadDir);
        try {

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "Error uploading image " + e.getMessage();
        }
    }

    public String copyImage(String fileName, InputStream inputStream, String uploadDir) {
        Path uploadPath = Paths.get(uploadDir);
        try {

//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "Error uploading image " + e.getMessage();
        }
    }

    public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        Path uploadPath = Path.of(uploadDirectory);
        Path filePath = uploadPath.resolve(uniqueFileName);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public byte[] getImage(String imageDirectory, String imageName) throws IOException {
        Path imagePath = Path.of(imageDirectory, imageName);

        if (Files.exists(imagePath)) {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return imageBytes;
        } else {
            return null; // Handle missing images
        }
    }

    public boolean updatePhotoMeta(RecordService recordService, int intUserId) {

        GenericView genericView = new GenericView(recordService);

        ArrayList<String> lstQueryUpdate = new ArrayList<String>();

        String sqlReadPhotos = "SELECT name_new FROM photo_meta WHERE uploaderId = " + intUserId + " ORDER BY id ASC ";
        String[] arrColumns = {"name_new"};
        List<Record> lstPhotoFilenames = recordService.findAll(sqlReadPhotos, arrColumns);

//        Notification notificationStart = Notification.show(
//                lstPhotoFilenames.size() + " Photos for User:" + intUserId,
//                5000,
//                Notification.Position.MIDDLE
//        );
//        notificationStart.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        int intRecsUpdated = 0;
        for (int l = 0; l < lstPhotoFilenames.size(); l++) {
            String strNewFileName = lstPhotoFilenames.get(l).getColumnData("name_new");


            ArrayList<String> lstPhotoMetaData = new ArrayList<>();

            DIR_PHOTOS_SERVER = genericView.getAppProps(PROP_PHOTOS);
            String strPathUpload = DIR_PHOTOS_SERVER + dirChar + subPathUpload;
            String outputUploadFileName = strPathUpload + dirChar + strNewFileName;

            ImageUtilsMeta imageUtilsMeta = new ImageUtilsMeta();
            File imgFile = new File(outputUploadFileName);
            logger.info("for photo " + outputUploadFileName + " get meta info");


//            Notification notificationA = Notification.show(
//                    l+".  A  Photos  "+intUserId,
//                    5000,
//                    Notification.Position.MIDDLE
//            );
//            notificationA.addThemeVariants(NotificationVariant.LUMO_PRIMARY);


            StringBuilder strImageMetaInfo = new StringBuilder();
            String[] arrPhotoGpsMeta = new String[3];
            try {
                //  logger.info(" A for photo "+outputUploadFileName+" get meta info to html "+imgFile.getAbsolutePath());
                strImageMetaInfo.append(imageUtilsMeta.getMetadataInfo(imgFile));
                //   logger.info(" B for photo "+outputUploadFileName+" get meta info to list "+imgFile.getAbsolutePath());
                lstPhotoMetaData = imageUtilsMeta.getListImageInfo();
                arrPhotoGpsMeta = imageUtilsMeta.getPhotoGPSMeta(outputUploadFileName);
            } catch (Exception e) {
                String errorMessage = "Scan failed: " + e.getMessage();
                Notification notification = Notification.show(
                        errorMessage,
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            if (lstPhotoMetaData != null) {

                String strPhotoDateTime = lstPhotoMetaData.get(0);
                String strPhotoCameraMake = lstPhotoMetaData.get(1);
                String strPhotoCameraModel = lstPhotoMetaData.get(2);
                String strPhotoLensMake = lstPhotoMetaData.get(3);
                String strPhotoLensModel = lstPhotoMetaData.get(4);
                String strFocalLength = lstPhotoMetaData.get(5);
                double dblPhotoFocalLength = 0;
                String strFl;
                try {
                    if (strFocalLength.indexOf("(") == -1 && strFocalLength.indexOf(")") == -1) {
                        strFl = strFocalLength;
                    } else {
                        strFl = strFocalLength.substring(strFocalLength.indexOf("(") + 1, strFocalLength.indexOf(")"));
                    }
                    dblPhotoFocalLength = Double.parseDouble(strFl);
                } catch (Exception e) {
                    logger.error(e.getMessage());


                    String errorMessage = "strFocalLength: " + strFocalLength + "  " + e.getMessage();
                    Notification notification = Notification.show(
                            errorMessage,
                            5000,
                            Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                // double dblPhotoFocalLengthFF = Double.parseDouble(lstPhotoMetaData.get(6));
                String strFocalLengthFF = lstPhotoMetaData.get(6);
                double dblPhotoFocalLengthFF = 0;
                if (strFocalLengthFF != null && !strFocalLengthFF.equalsIgnoreCase("null")) {
                    String strFlFF;
                    try {
                        if (strFocalLengthFF.indexOf("(") == -1 && strFocalLengthFF.indexOf(")") == -1) {
                            strFlFF = strFocalLengthFF;
                        } else {
                            strFlFF = strFocalLengthFF.substring(strFocalLengthFF.indexOf("(") + 1, strFocalLengthFF.indexOf(")"));
                        }
                        dblPhotoFocalLengthFF = Double.parseDouble(strFlFF);
                    } catch (Exception e) {
                        logger.error(e.getMessage());

                    /*
                        String errorMessage = "strFocalLengthFF: " + strFocalLengthFF + "  " + e.getMessage();
                        Notification notification = Notification.show(
                                errorMessage,
                                5000,
                                Notification.Position.MIDDLE
                        );
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                     */
                    }
                }


                int intPhotoISO = Integer.parseInt(lstPhotoMetaData.get(7));

                String strPhotoShutterSpeed = lstPhotoMetaData.get(8);

                double dblPhotoShutterSpeed = 0;
                if (!strPhotoShutterSpeed.equalsIgnoreCase("null")) {
                    String strSS = "";
                    try {
                        if (strPhotoShutterSpeed.indexOf("(") == -1 && strPhotoShutterSpeed.indexOf(")") == -1) {
                            strSS = strPhotoShutterSpeed; // is integer
                        } else {
                            strSS = strPhotoShutterSpeed.substring(strPhotoShutterSpeed.indexOf("(") + 1, strPhotoShutterSpeed.indexOf(")"));
                        }
                        dblPhotoShutterSpeed = Double.parseDouble(strSS);
                    } catch (Exception e) {
                        logger.error(e.getMessage());


                        String errorMessage = "strPhotoShutterSpeed: " + strPhotoShutterSpeed + "  " + e.getMessage();
                        Notification notification = Notification.show(
                                errorMessage,
                                5000,
                                Notification.Position.MIDDLE
                        );
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }

                } else {

                    String errorMessage = "strPhotoShutterSpeed: " + strPhotoShutterSpeed;
                    Notification notification = Notification.show(
                            errorMessage,
                            5000,
                            Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }


                double dblPhotoAperture = 0;
                String strPhotoAperture = lstPhotoMetaData.get(9);
                if (!strPhotoAperture.equalsIgnoreCase("null")) {
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


                        String errorMessage = "strPhotoAperture: " + strPhotoAperture + "  " + e.getMessage();
                        Notification notification = Notification.show(
                                errorMessage,
                                5000,
                                Notification.Position.MIDDLE
                        );
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                } else {
                    String errorMessage = "strPhotoAperture: " + strPhotoAperture;
                    Notification notification = Notification.show(
                            errorMessage,
                            5000,
                            Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                String strMeteringMode = lstPhotoMetaData.get(10);
                if (strMeteringMode == null || strMeteringMode.isEmpty() || strMeteringMode.equalsIgnoreCase("null")) {
                    strMeteringMode = "";
                }

                int intImageLength = 0;
                String strImageLength = lstPhotoMetaData.get(11);
                if (strImageLength == null || strImageLength.isEmpty() || strImageLength.equalsIgnoreCase("null")) {
                    intImageLength = 0;
                } else {
                    intImageLength = Integer.parseInt(strImageLength);
                }

                int intImageWidth = 0;
                String strImageWidth = lstPhotoMetaData.get(12);
                if (strImageWidth == null || strImageWidth.isEmpty() || strImageWidth.equalsIgnoreCase("null")) {
                    intImageWidth = 0;
                } else {
                    intImageWidth = Integer.parseInt(strImageWidth);
                }

                String strOrientation = lstPhotoMetaData.get(13);

                if (strPhotoLensMake.isEmpty()) {
                    strPhotoLensMake = "''";
                }

                String strLat = arrPhotoGpsMeta[0] != null ? " '" + Double.parseDouble(arrPhotoGpsMeta[0]) + "' " : " NULL ";
                String strLon = arrPhotoGpsMeta[1] != null ? " '" + Double.parseDouble(arrPhotoGpsMeta[1]) + "' " : " NULL ";


                String updateSQL = "UPDATE photo_meta SET " +
//                    " space_size = '" + photoSpaceSize + "', " +
//                    " space_size_medium = '" + photoSpaceSizeMedium + "', " +
//                    " space_size_thumb = '" + photoSpaceSizeThumb + "', " +
//                    " meta_all = ? , " +
                        " meta_date = DATE_FORMAT(" + strPhotoDateTime + ", '%Y:%m:%d %H:%i:%s')," +
                        " meta_camera_make = " + strPhotoCameraMake + ", " +
                        " meta_camera_model = " + strPhotoCameraModel + ", " +
                        " meta_lens_make = " + strPhotoLensMake + ", " +
                        " meta_lens_model = " + strPhotoLensModel + ", " +
                        " meta_focal_length = '" + dblPhotoFocalLength + "', " +
                        " meta_focal_length_ff = '" + dblPhotoFocalLengthFF + "', " +
                        " meta_iso = '" + intPhotoISO + "' " +
                        " , meta_shutter_speed = '" + dblPhotoShutterSpeed + "' " +
                        " , meta_aperture = '" + dblPhotoAperture + "' " +
                        " , meta_metering_mode = '" + strMeteringMode + "' " +
                        " , meta_i_length = '" + intImageLength + "' " +
                        " , meta_i_width = '" + intImageWidth + "' " +
                        " , meta_orientation = '" + strOrientation + "' "
                        + " , location_lat = " + strLat
                        + " , location_lon = " + strLon

                        + " WHERE name_new LIKE '" + strNewFileName + "' AND uploaderId = " + intUserId + " ORDER BY id ASC ";

                logger.info("  updateSQL SQL:   " + updateSQL);


                lstQueryUpdate.add(updateSQL);


                intRecsUpdated = intRecsUpdated + recordService.insertOneRecordWithQuery(updateSQL, null, null);


//        ArrayList<Object[]> listInsertValues = new ArrayList<>();
//        String[] imageInfo = {strImageMetaInfo.toString()};
//        listInsertValues.add(imageInfo);
//
//        int arrLength = listInsertValues.get(0).length;
//        String[] arrType = new String[arrLength];
//        for (int i = 0; i < arrLength; i++) {
//            arrType[i] = "java.lang.String";
//        }
//
//        ArrayList<String[]> listInsertTypes = new ArrayList<>();
//
//        for (int i = 0; i < listInsertValues.size(); i++) {
//            listInsertTypes.add(arrType);
//        }
//
//            Notification notificationLast = Notification.show(
//                    "  Last " + lstQueryUpdate.size() + "  =  " + lstPhotoFilenames.size(),
//                    5000,
//                    Notification.Position.MIDDLE
//            );
//            notificationLast.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            }

        }
        if (intRecsUpdated > 0) {
            Notification notificationC = Notification.show(
                    "Data of " + intRecsUpdated + " Photos Updated !",
                    8000,
                    Notification.Position.TOP_CENTER
            );
            notificationC.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } else {
            Notification notificationC = Notification.show(
                    intRecsUpdated + "Photo data Update Failed !",
                    8000,
                    Notification.Position.TOP_CENTER
            );
            notificationC.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

//
//

        // recordService.setGlobalInfo(hostname, intUserId, strUserName, publicIp, sessionId);
//            if (recordService.massRecordInsert(lstQueryUpdate, null, null) >0) {
//               return true;
//            } else {
        //  logErrorInDb(null, "UploadImageCard insertPhotoToDb.", insertSQL, intUserId, strUserName);
//                return false;
//            }

    }


}
//@Service
//public class ImageService {
//
//    private final StorageService storageService;
//
//    public ImageService(StorageService storageService) {
//        this.storageService = storageService;
//    }
//
//    public Path saveImage(MultipartFile file, String filename) throws Exception {
//        // TODO: handle upload in a separate directory + convert image to jpeg or webp +
//        // crop images, ...
//        if (!Arrays.asList(ALLOWED_CONTENT_TYPE).contains(file.getContentType()))
//            throw new Exception("Unsupported image content type");
//        if(!Objects.equals(file.getContentType(), JPG_CONTENT_TYPE)) {
/// /            ConvertImageToJPG(file.getInputStream());
//        }
//
//        return storageService.saveFile(file, filename);
//    }
//}


//@Service
//@RequiredArgsConstructor
//public class ImageService {
//
//   // private final ImageRepository imageRepository;
//
//    public String uploadImage(MultipartFile imageFile) throws IOException {
//        var imageToSave = Image.builder()
//                .name(imageFile.getOriginalFilename())
//                .type(imageFile.getContentType())
//                .imageData(ImageUtils.compressImage(imageFile.getBytes()))
//                .build();
//        imageRepository.save(imageToSave);
//        return "file uploaded successfully : " + imageFile.getOriginalFilename();
//    }
//
//    public byte[] downloadImage(String imageName) {
//        Optional<Image> dbImage = imageRepository.findByName(imageName);
//
//        return dbImage.map(image -> {
//            try {
//                return ImageUtils.decompressImage(image.getImageData());
//            } catch (DataFormatException | IOException exception) {
//                throw new ContextedRuntimeException("Error downloading an image", exception)
//                        .addContextValue("Image ID",  image.getId())
//                        .addContextValue("Image name", imageName);
//            }
//        }).orElse(null);
//    }
//}
