package com.photo.act.photo_act.utils;

import com.photo.act.photo_act.views.components.ImageGalleryViewCard;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            System.out.println("file: " + file.getPath());

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

            System.out.println();

            final List<ImageMetadataItem> items = jpegMetadata.getItems();
            for (final ImageMetadataItem item : items) {
                System.out.println("    " + "item: " + item);
            }
            System.out.println();
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
            } catch(IOException e){
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
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME)); // date time
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE)); // camera make
            lstInfo.add(getTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL)); // camera model
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE)); // lens make
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL)); // camera model
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH)); // focal length
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT)); // focal length in ff
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO)); // iso
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE)); // shutter speed
            lstInfo.add(getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE)); // aperture

            System.out.println("file: " + file.getPath());

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
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SPECIFICATION));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_SERIAL_NUMBER));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_SENSING_METHOD_EXIF_IFD));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_MODE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_TIME));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF));
            metadataInfo.append(getTagValueAsHtml(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE));

            final List<ImageMetadataItem> items = jpegMetadata.getItems();
            for (final ImageMetadataItem item : items) {
               // System.out.println("    " + "item: " + item);
            }
            metadataInfo.append("</table>");
        }else {

        }
        return metadataInfo;
    }

    private String getTagValueAsHtml(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field == null) {
            return "<tr><td>"+tagInfo.name+": </td><td> </td></tr>";
        } else {
            return "<tr><td>"+tagInfo.name+": </td><td>"+field.getValueDescription()+"</td></tr>";
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

    public ArrayList<String> getListImageInfo(){
        return lstInfo;
    }

}
