package com.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.collections.CollectionsInterface;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.flickr.utils.PropertyLoader.loadProperties;

/**
 * Created by NQRAZ66 on 20/10/2017.
 */
public class FlickrUploader implements Runnable {

    public static boolean isRunning = false;
    private Thread thread;
    private String apiKey;
    private String sharedSecret;
    private File parentFolder;
    private String nsid;
    private Flickr flickr;
    private AuthStore authStore;
    private CollectionsInterface collectionsInterface;
    private PhotosetsInterface photosetsInterface;
    private Uploader uploader;
private JProgressBar progressBar;

    public FlickrUploader(JProgressBar progressBar) {
        this.progressBar = progressBar;
        thread = new Thread(this);
    }

    public FlickrUploader() {
        this(new JProgressBar());
    }

    void start() throws Exception {
        initiate();
        progressBar.setValue(50);
        thread.start();
    }

    void initiate() throws Exception {
        instantiateProperties();
        System.out.println("Properties loaded and instantiated");

        this.flickr = getFlickr();
        getFlickrInterfaces();
        System.out.println("Initiation done.");
    }

    void instantiateProperties() {
        Properties properties = loadProperties();
        this.apiKey = properties.getProperty("api.key");
        this.sharedSecret = properties.getProperty("secret");
        this.parentFolder = new File(properties.getProperty("parent.folder"));
        this.nsid = properties.getProperty("nsid");
    }

    private Flickr getFlickr() {
        return new Flickr(apiKey, sharedSecret, new REST("www.flickr.com"));
    }

    private void getFlickrInterfaces() throws IOException, FlickrException {
        this.authStore = loadAuthStore(flickr, parentFolder);
        System.out.println("Auth store loaded.");

        this.collectionsInterface = flickr.getCollectionsInterface();
        System.out.println("CollectionsInterface loaded.");

        this.photosetsInterface = flickr.getPhotosetsInterface();
        System.out.println("PhotoSetsInterface loaded.");

        uploader = this.flickr.getUploader();
        System.out.println("Uploader loaded.");
    }

    private AuthStore loadAuthStore(Flickr flickr, File parentFolder) throws IOException, FlickrException {
        this.authStore = new FileAuthStore(new File(parentFolder.getAbsolutePath()));
        Auth auth = this.authStore.retrieve(this.nsid);
        RequestContext.getRequestContext().setAuth(auth);
        this.authStore.store(auth);
        return this.authStore;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            Map<String, Photoset> existingFlickrAlbums = getRemoteAlbums();
            Map<String, File> localAlbums = getLocalAlbums();
            Iterator<String> localAlbumsIterator = localAlbums.keySet().iterator();
            while (isRunning && localAlbumsIterator.hasNext()) {
                String localAlbumName = localAlbumsIterator.next();
                //if album already on flickr
                if (existingFlickrAlbums.containsKey(localAlbumName)) {
                    System.out.println(String.format("%s already exists. Start looping photos.", localAlbumName));
                    //loop photos and look for new ones
                    String photoSetId = existingFlickrAlbums.get(localAlbumName).getId();
                    Map<String, Photo> photosInFlickPhotoSet = getPhotosInPhotoSet(photoSetId);
                    Map<String, File> photosInLocalAlbum = getPhotosInLocalAlbum(localAlbums.get(localAlbumName));

                    Iterator<String> photosInLocalAlbumIterator = photosInLocalAlbum.keySet().iterator();
                    while (isRunning && photosInLocalAlbumIterator.hasNext()) {
                        String localPhotoName = photosInLocalAlbumIterator.next();
                        if (!photosInFlickPhotoSet.containsKey(localPhotoName)) {
                            File localPhoto = photosInLocalAlbum.get(localPhotoName);
                            addPhotoToPhotoSet(photoSetId, localPhoto);
                            System.out.println(String.format("%s added to %s", localPhotoName, localAlbumName));

                        } else {
                            System.out.println(String.format("%s already in Flickr album %s", localPhotoName, localAlbumName));
                        }
                    }

                } else {
                    //create new album on flickr and add all photos
                    System.out.println(String.format("Add album to flickr", localAlbumName));
                    uploadAlbum(localAlbums.get(localAlbumName));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void addPhotoToPhotoSet(String photoSetId, File photo) throws Exception {
        String photoId = uploadFile(photo);
        photosetsInterface.addPhoto(photoSetId, photoId);
    }

    private Map<String, File> getPhotosInLocalAlbum(File albumDirectory) {
        String[] fileList = albumDirectory.list();
        if (fileList != null) {
            return Arrays.stream(fileList)
                    .map(fileName -> new File(parentFolder + File.separator + albumDirectory.getName() + File.separator + fileName))
                    .filter(File::isFile)
                    .collect(Collectors.toMap(File::getName, Function.identity()));
        }
        return Collections.emptyMap();
    }

    Map<String, Photoset> getRemoteAlbums() throws FlickrException {
        java.util.Collection<Photoset> photosets = this.photosetsInterface.getList(this.nsid).getPhotosets();
        Map<String, Photoset> flickreRemoteAlbums = photosets.stream().collect(Collectors.toMap(Photoset::getTitle, Function.identity()));
        System.out.println("Remote Flickr Albums: ");
        for (String photoSetName : flickreRemoteAlbums.keySet()) {
            System.out.println(photoSetName);
        }
        return flickreRemoteAlbums;
    }

    Map<String, File> getLocalAlbums() {
        String[] localAlbumsList = parentFolder.list();
        if (localAlbumsList != null) {
            return Arrays.stream(localAlbumsList)
                    .map(collection -> new File(parentFolder + File.separator + collection))
                    .filter(File::isDirectory)
                    .collect(Collectors.toMap(File::getName, Function.identity()));
        }
        return Collections.emptyMap();
    }

    private Map<String, Photo> getPhotosInPhotoSet(String photoSetId) throws FlickrException {
        PhotoList<Photo> photos = this.photosetsInterface.getPhotos(photoSetId, Integer.MAX_VALUE, 0);
        return photos.stream().collect(Collectors.toMap(Photo::getTitle, Function.identity()));
    }

    private void uploadAlbum(File localAlbum) throws Exception {
        String photosetId = null;
        Map<String, File> photosInLocalAlbum = getPhotosInLocalAlbum(localAlbum);
        if (!photosInLocalAlbum.isEmpty()) {
            for (Map.Entry<String, File> localPhoto : photosInLocalAlbum.entrySet()) {
                if (photosetId == null) {
                    String photoId = uploadFile(localPhoto.getValue());
                    Photoset photoset = this.photosetsInterface.create(localAlbum.getName(), localAlbum.getName(), photoId);
                    photosetId = photoset.getId();
                    System.out.println(String.format("Album created on Flickr: %s with id %s", localAlbum.getName(), photosetId));
                } else {
                    addPhotoToPhotoSet(photosetId, localPhoto.getValue());
                    System.out.println(String.format("%s added to %s", localPhoto.getKey(), localAlbum.getName()));
                }
            }
        } else {
            System.out.println(String.format("Local Album %s to upload doesn't contain any photo's.", localAlbum.getName()));
        }
    }


    private String uploadFile(File file) throws Exception {
        String basefilename = file.getName(); // "image.jpg";

        UploadMetaData metaData = new UploadMetaData();
        metaData.setFilename(basefilename);
        metaData.setTitle(basefilename);
        metaData.setHidden(true);
        metaData.setSafetyLevel(Flickr.SAFETYLEVEL_RESTRICTED);

        String suffix = basefilename.substring(basefilename.lastIndexOf('.') + 1);
        if (suffix.equalsIgnoreCase("png")) {
            metaData.setFilemimetype("image/png");
        } else if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("jpeg")) {
            metaData.setFilemimetype("image/jpeg");
        }

        String photoId = uploader.upload(file, metaData);
        System.out.println(" File : " + file + " uploaded: photoId = " + photoId);
        return photoId;
    }


}
