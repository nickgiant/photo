package com.photo.act.photo_act.utils;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ImageUtilsMeta {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtilsMeta.class);

    private ArrayList<String> lstInfo;

    public static void metadataInfo(final File file) throws ImagingException, IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        final ImageMetadata metadata = Imaging.getMetadata(file);

        // System.out.println(metadata);

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.

            System.out.println("photo file: " + file.getPath());

            // print out various interesting EXIF tags.
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE);
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL);
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_VERSION);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_MAKER_NOTE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SERIAL_NUMBER);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_RAW_FILE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SPECIFICATION);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SERIAL_NUMBER);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_METERING_MODE);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_MODE);


            System.out.println();

            // simple interface to GPS data
            final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                final TiffImageMetadata.GpsInfo gpsInfo = exifMetadata.getGpsInfo();
                if (null != gpsInfo) {
                    final String gpsDescription = gpsInfo.toString();
                    final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    System.out.println("    " + "GPS Description: " + gpsDescription);
                    System.out.println("    " + "GPS Longitude (Degrees East): " + longitude);
                    System.out.println("    " + "GPS Latitude (Degrees North): " + latitude);
                }
            }

            // more specific example of how to manually access GPS values
            final TiffField gpsLatitudeRefField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            final TiffField gpsLatitudeField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            final TiffField gpsLongitudeRefField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            final TiffField gpsLongitudeField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            if (gpsLatitudeRefField != null && gpsLatitudeField != null && gpsLongitudeRefField != null && gpsLongitudeField != null) {
                // all of these values are strings.
                final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
                final RationalNumber[] gpsLatitude = (RationalNumber[]) gpsLatitudeField.getValue();
                final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
                final RationalNumber[] gpsLongitude = (RationalNumber[]) gpsLongitudeField.getValue();

                final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
                final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
                final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

                final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
                final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
                final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

                // This will format the gps info like so:
                //
                // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
                // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

                System.out.println("    " + "GPS Latitude: " + gpsLatitudeDegrees.toDisplayString() + " degrees, " + gpsLatitudeMinutes.toDisplayString()
                        + " minutes, " + gpsLatitudeSeconds.toDisplayString() + " seconds " + gpsLatitudeRef);
                System.out.println("    " + "GPS Longitude: " + gpsLongitudeDegrees.toDisplayString() + " degrees, " + gpsLongitudeMinutes.toDisplayString()
                        + " minutes, " + gpsLongitudeSeconds.toDisplayString() + " seconds " + gpsLongitudeRef);
            }

        }
    }

    private static void printTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": " + field.getValueDescription());
        }
    }

    public void printPhotoMetadataValue(final File file) {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        final ImageMetadata metadata;
        try {
            logger.info(" ");
            logger.info(file.getAbsolutePath());
            metadata = Imaging.getMetadata(file);

            if (metadata instanceof JpegImageMetadata) {
                logger.info(" ");
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

                logger.info(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME));
                logger.info(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL));
                logger.info(" ");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    public StringBuilder getMetadataInfo(final File file) throws ImagingException, IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        final ImageMetadata metadata = Imaging.getMetadata(file);
        StringBuilder metadataInfo = new StringBuilder();

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.


            lstInfo = new ArrayList<>();

            String strTimeOrg = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            String strTimeDigi = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);

            if (strTimeOrg == null || strTimeOrg.isEmpty() || strTimeOrg.trim().equalsIgnoreCase("null")) {
                lstInfo.add(strTimeDigi); // date time
            } else {
                lstInfo.add(strTimeOrg); // date time
            }
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE)); // camera make
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL)); // camera model

            String lensMake = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE).replaceAll(", , , , ,", "").trim(); // lens make
            if (!lensMake.trim().equalsIgnoreCase("null")) {
                lstInfo.add(lensMake);
            } else {
                lstInfo.add("'Not Available'");
            }

            String lensModel = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL).replaceAll(", ", "").trim();
            if (!lensModel.trim().equalsIgnoreCase("null")) {
                if (lensModel.length() > 0) {
                    if (lensModel.startsWith("'") && lensModel.endsWith("'")) {
                        lstInfo.add(lensModel); // camera model
                    } else {
                        lstInfo.add("'" + lensModel + "'");
                    }
                }
            } else {
                lstInfo.add("'Not Available'");
            }

            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH)); // focal length
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT)); // focal length in ff
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO)); // iso

            String strShutterSpeed = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
            String strExposure = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
            if (strShutterSpeed == null || strShutterSpeed.isEmpty() || strShutterSpeed.trim().equalsIgnoreCase("null")) {
                lstInfo.add(strExposure); // shutter speed
            } else {
                lstInfo.add(strShutterSpeed); // shutter speed
            }

            String strAperture = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
            String strFNumber = getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER);
            if (strAperture == null || strAperture.isEmpty() || strAperture.trim().equalsIgnoreCase("null")) {
                lstInfo.add(strFNumber); // aperture
            } else {
                lstInfo.add(strAperture); // aperture
            }

            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_METERING_MODE));
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH));
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH));
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION));
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_MODE));
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM));




//
//            ExifTagConstants.EXIF_TAG_ISO;
//            public static final TagInfoSRational EXIF_TAG_SHUTTER_SPEED_VALUE;
//            public static final TagInfoRational EXIF_TAG_APERTURE_VALUE;
//            public static final TagInfoSRational EXIF_TAG_BRIGHTNESS_VALUE;
//            public static final TagInfoSRational EXIF_TAG_EXPOSURE_COMPENSATION;
//            public static final TagInfoRational EXIF_TAG_MAX_APERTURE_VALUE;
//            public static final TagInfoRationals EXIF_TAG_SUBJECT_DISTANCE;
//            public static final TagInfoShort EXIF_TAG_METERING_MODE;
//            public static final int METERING_MODE_VALUE_AVERAGE = 1;
//            public static final int METERING_MODE_VALUE_CENTER_WEIGHTED_AVERAGE = 2;
//            public static final int METERING_MODE_VALUE_SPOT = 3;
//            public static final int METERING_MODE_VALUE_MULTI_SPOT = 4;
//            public static final int METERING_MODE_VALUE_MULTI_SEGMENT = 5;
//            public static final int METERING_MODE_VALUE_PARTIAL = 6;
//            public static final int METERING_MODE_VALUE_OTHER = 255;
//            public static final TagInfoShort EXIF_TAG_LIGHT_SOURCE;
//            public static final int LIGHT_SOURCE_VALUE_DAYLIGHT = 1;
//            public static final int LIGHT_SOURCE_VALUE_FLUORESCENT = 2;
//            public static final int LIGHT_SOURCE_VALUE_TUNGSTEN = 3;
//            public static final int LIGHT_SOURCE_VALUE_FLASH = 4;
//            public static final int LIGHT_SOURCE_VALUE_FINE_WEATHER = 9;
//            public static final int LIGHT_SOURCE_VALUE_CLOUDY = 10;
//            public static final int LIGHT_SOURCE_VALUE_SHADE = 11;
//            public static final int LIGHT_SOURCE_VALUE_DAYLIGHT_FLUORESCENT = 12;
//            public static final int LIGHT_SOURCE_VALUE_DAY_WHITE_FLUORESCENT = 13;
//            public static final int LIGHT_SOURCE_VALUE_COOL_WHITE_FLUORESCENT = 14;
//            public static final int LIGHT_SOURCE_VALUE_WHITE_FLUORESCENT = 15;
//            public static final int LIGHT_SOURCE_VALUE_STANDARD_LIGHT_A = 17;
//            public static final int LIGHT_SOURCE_VALUE_STANDARD_LIGHT_B = 18;
//            public static final int LIGHT_SOURCE_VALUE_STANDARD_LIGHT_C = 19;
//            public static final int LIGHT_SOURCE_VALUE_D55 = 20;
//            public static final int LIGHT_SOURCE_VALUE_D65 = 21;
//            public static final int LIGHT_SOURCE_VALUE_D75 = 22;
//            public static final int LIGHT_SOURCE_VALUE_D50 = 23;
//            public static final int LIGHT_SOURCE_VALUE_ISO_STUDIO_TUNGSTEN = 24;
//            public static final int LIGHT_SOURCE_VALUE_OTHER = 255;
//            public static final TagInfoShort EXIF_TAG_FLASH;
//            public static final int FLASH_VALUE_NO_FLASH = 0;
//            public static final int FLASH_VALUE_FIRED = 1;
//            public static final int FLASH_VALUE_FIRED_RETURN_NOT_DETECTED = 5;
//            public static final int FLASH_VALUE_FIRED_RETURN_DETECTED = 7;
//            public static final int FLASH_VALUE_ON_DID_NOT_FIRE = 8;
//            public static final int FLASH_VALUE_ON = 9;
//            public static final int FLASH_VALUE_ON_RETURN_NOT_DETECTED = 13;
//            public static final int FLASH_VALUE_ON_RETURN_DETECTED = 15;
//            public static final int FLASH_VALUE_OFF = 16;
//            public static final int FLASH_VALUE_OFF_DID_NOT_FIRE_RETURN_NOT_DETECTED = 20;
//            public static final int FLASH_VALUE_AUTO_DID_NOT_FIRE = 24;
//            public static final int FLASH_VALUE_AUTO_FIRED = 25;
//            public static final int FLASH_VALUE_AUTO_FIRED_RETURN_NOT_DETECTED = 29;
//            public static final int FLASH_VALUE_AUTO_FIRED_RETURN_DETECTED = 31;
//            public static final int FLASH_VALUE_NO_FLASH_FUNCTION = 32;
//            public static final int FLASH_VALUE_OFF_NO_FLASH_FUNCTION = 48;
//            public static final int FLASH_VALUE_FIRED_RED_EYE_REDUCTION = 65;
//            public static final int FLASH_VALUE_FIRED_RED_EYE_REDUCTION_RETURN_NOT_DETECTED = 69;
//            public static final int FLASH_VALUE_FIRED_RED_EYE_REDUCTION_RETURN_DETECTED = 71;
//            public static final int FLASH_VALUE_ON_RED_EYE_REDUCTION = 73;
//            public static final int FLASH_VALUE_ON_RED_EYE_REDUCTION_RETURN_NOT_DETECTED = 77;
//            public static final int FLASH_VALUE_ON_RED_EYE_REDUCTION_RETURN_DETECTED = 79;
//            public static final int FLASH_VALUE_OFF_RED_EYE_REDUCTION = 80;
//            public static final int FLASH_VALUE_AUTO_DID_NOT_FIRE_RED_EYE_REDUCTION = 88;
//            public static final int FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION = 89;
//            public static final int FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION_RETURN_NOT_DETECTED = 93;
//            public static final int FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION_RETURN_DETECTED = 95;
//            public static final TagInfoRationals EXIF_TAG_FOCAL_LENGTH;
//            public static final TagInfoShorts EXIF_TAG_SUBJECT_AREA;
//
//
////            ExifTagConstants.ALL_EXIF_TAGS
//
//            public static final int EXPOSURE_PROGRAM_VALUE_MANUAL = 1;
//            public static final int EXPOSURE_PROGRAM_VALUE_PROGRAM_AE = 2;
//            public static final int EXPOSURE_PROGRAM_VALUE_APERTURE_PRIORITY_AE = 3;
//            public static final int EXPOSURE_PROGRAM_VALUE_SHUTTER_SPEED_PRIORITY_AE = 4;
//            public static final int EXPOSURE_PROGRAM_VALUE_CREATIVE_SLOW_SPEED = 5;
//            public static final int EXPOSURE_PROGRAM_VALUE_ACTION_HIGH_SPEED = 6;
//            public static final int EXPOSURE_PROGRAM_VALUE_PORTRAIT = 7;
//            public static final int EXPOSURE_PROGRAM_VALUE_LANDSCAPE = 8;


            logger.info(" check field format :  " + lstInfo.get(2) + "  ---  " + lstInfo.get(3) + "  ---  " + lstInfo.get(4) + "  ---  " + lstInfo.get(5) + "  ---  " +
                    lstInfo.get(lstInfo.size() - 4) + "  ---  " + lstInfo.get(lstInfo.size() - 3) + "  ---  " + lstInfo.get(lstInfo.size() - 2)
                    + "  ---  " + lstInfo.get(lstInfo.size() - 1) + "  ---  size: " + lstInfo.size());

            System.out.println("html photo file: " + file.getPath());

            metadataInfo.append("<table>");
            // print out various interesting EXIF tags.
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_VERSION));

            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_SERIAL_NUMBER));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_RAW_FILE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL).replaceAll(", ", "").trim()); // camera model
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SPECIFICATION));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SERIAL_NUMBER));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_SENSING_METHOD_EXIF_IFD));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_MODE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE));

            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_METERING_MODE));

            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_TIME));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME));
            //metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_IMAGE_NUMBER));  // produces error
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION));

            metadataInfo.append("</table>");
        } else {

        }
        return metadataInfo;
    }


    private String getTagValueAsHtml(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field == null) {
            return "<tr><td>" + tagInfo.name + ": </td><td> </td></tr>";
        } else {
            return "<tr><td>" + tagInfo.name + ": </td><td>" + field.getValueDescription() + "</td></tr>";
            //System.out.println(tagInfo.name + ": " + field.getValueDescription());
        }
    }

    private String getTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field == null) {
            return " null "; //tagInfo.name;
        } else {
            return field.getValueDescription(); //tagInfo.name;
        }
    }

    public ArrayList<String> getListImageInfo() {
        return lstInfo;
    }


    public static ImageMetadata getImageMetadata(Resource resource) {
        final ImageMetadata metadata;
        try {
            metadata = Imaging.getMetadata(resource.getInputStream(), resource.getFilename());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to parse the metadata of image file " + resource.getFilename(), ex);
        }
        return metadata;
    }

    public static void getJpegImageMetadata(JpegImageMetadata jpegImageMetadata) {


        final TiffImageMetadata exifMetadata = jpegImageMetadata.getExif();

        if (exifMetadata != null) {
            @SuppressWarnings("unchecked") final List<TiffImageMetadata.TiffMetadataItem> exifMetadataItems = (List<TiffImageMetadata.TiffMetadataItem>) exifMetadata
                    .getItems();

            for (TiffImageMetadata.TiffMetadataItem tiffMetadataItem : exifMetadataItems) {
                final String propertyName = tiffMetadataItem.getKeyword();
                final String propertyValue = StringUtils.hasText(tiffMetadataItem.getText())
                        ? tiffMetadataItem.getText() : "N/A";
                logger.debug("Exif Property '{}': {}.", propertyName, propertyValue);
                //directories.add(new Directory(DirectoryType.EXIF, propertyName, propertyValue));

                for (TagInfo tagInfo : MicrosoftTagConstants.ALL_MICROSOFT_TAGS) {
                    if (tagInfo.equals(tiffMetadataItem.getTiffField().getTagInfo())) {
                        logger.debug(" Windows  - '{}': {}.", propertyName, propertyValue);
                        // directories.add(new Directory(DirectoryType.WINDOWS, propertyName, propertyValue));
                    }
                }
            }
        }

        final JpegPhotoshopMetadata jpegPhotoshopMetadata = jpegImageMetadata.getPhotoshop();

        if (jpegPhotoshopMetadata != null) {
            for (ImageMetadata.ImageMetadataItem imageMetadataItem : jpegPhotoshopMetadata.getItems()) {
                if (imageMetadataItem instanceof GenericImageMetadata.GenericImageMetadataItem genericItem) {
                    final String propertyName = genericItem.getKeyword();
                    final String propertyValue = StringUtils.hasText(genericItem.getText()) ? genericItem.getText()
                            : "N/A";
                    logger.debug("IPTC Property '{}': {}.", propertyName, propertyValue);
                    //directories.add(new TiffImageMetadata.Directory(DirectoryType.IPTC, propertyName, propertyValue));
                } else {
                    throw new IllegalStateException(
                            "Unhandled ImageMetadataItem: " + imageMetadataItem.getClass().getSimpleName());
                }
            }
        }

        final TiffImageMetadata exif = jpegImageMetadata.getExif();
        if (exif == null) {

        }

        for (TiffField tiffField : exif.getAllFields()) {
            logger.info("TIFF field tag name: {}", tiffField.getTagName());
        }


    }

}
