package com.photo.act.photo_act.services;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.galleries.GalleriesInterface;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoUrl;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.stats.StatsSort;
import com.flickr4java.flickr.test.TestInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PhotoFlickrService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoFlickrService.class);

    private final String API_KEY;
    private final String SHARED_SECRET;

    private Flickr f;

    /*
    String apiKey = "YOUR_API_KEY";
String sharedSecret = "YOUR_SHARED_SECRET";
Flickr f = new Flickr(apiKey, sharedSecret, new REST());
TestInterface testInterface = f.getTestInterface();
Collection results = testInterface.echo(Collections.EMPTY_MAP);
     */
    public PhotoFlickrService(){

        API_KEY = "07d227f4935dfcbd50132ba6f70ec419";
        SHARED_SECRET = "f5c6a32d2190d693";

        f = new Flickr(API_KEY, SHARED_SECRET, new REST());
        logger.info("PhotoFlickrService");
        TestInterface testInterface = f.getTestInterface();
        logger.info("PhotoFlickrService Test ok");

        try {
            Collection results = testInterface.echo(Collections.EMPTY_MAP);
            logger.info("PhotoFlickrService collection ok");
            List list = results.stream().toList();
            logger.info("init size: "+list.size());
            for (Object o : list){
                logger.info("init: "+o.toString());
            }


        } catch (FlickrException e) {
            throw new RuntimeException(e);
        }

    }

    public List getGalleriesOfUser(String flickrUserId){


        GalleriesInterface galleriesInterface;
        List galleryList;
        logger.info("getGalleriesOfUser "+f.getApiKey()+" "+f.getSharedSecret());
        try {
            SearchParameters sparams = new SearchParameters();
            sparams.setText("Budapest");

            List<Photo> photoList = f.getPhotosInterface().search(sparams,8,1);

            logger.info("photo size:"+photoList.size());
            for(int i=0; i<photoList.size(); i++){
                photoList.get(i).getTitle();
                photoList.get(i).getSmall320Url();
            }

             galleryList = f.getGalleriesInterface().getList(flickrUserId,8,1).stream().toList();

            logger.info("galleries size:"+galleryList.size());

        } catch (FlickrException e) {
            throw new RuntimeException(e);
        }


        return galleryList;

    }

    public ArrayList<Photo> findPhotos(String text, int count){

        ArrayList<Photo> photoList;
        ArrayList<Photo> photoListSize;
        //logger.info("findPhotos "+text+"  "+f.getApiKey()+" "+f.getSharedSecret());
        try {
            SearchParameters sparams = new SearchParameters();
            sparams.setText(text);

            sparams.setSort(StatsSort.favorites.ordinal());

            photoList = f.getPhotosInterface().search(sparams,count,1);//.search(sparams,count,1);

          //  f.getPeopleInterface().getInfo()
            logger.info("photo size for "+text+" :"+photoList.size());
//            for(int i=0; i<photoList.size(); i++){
//               User user = photoList.get(i).getOwner();
//                user.getId();
//                user.getRealName();
//                user.getProfileurl();
//                user.getPhotosCount();
//                user.getPhotosurl();
//
//              Photo photo =  photoList.get(i); //.getTitle();
//
////                Image image = new Image(listPhotosLayout.get(p),destination);
////            image.setHeight("180px");
////            image.setWidth("auto");
////                layoutPhotos.add(image);
//
//                photoUrls.add(photoList.get(i).getThumbnailUrl());//.getSmall320Url());
//             //   layoutPhotos.add(photoList.get(i).getThumbnailUrl());
//            }



        } catch (FlickrException e) {
            throw new RuntimeException(e);
        }

        return photoList;
    }

    public String getUserName(String userId){
        try {
         return   f.getPeopleInterface().getInfo(userId).getRealName();
        } catch (FlickrException e) {
            throw new RuntimeException(e);
        }
    }
}
