package com.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.collections.CollectionsInterface;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.flickr.utils.PropertyLoader.loadProperties;

/**
 * Created by NQRAZ66 on 20/10/2017.
 */
public class FlickrUploader {

    private String apiKey;
    private String sharedSecret;
    private File parentFolder;
    private AuthStore authStore;
    private CollectionsInterface collectionsInterface;
    private String nsid;


    void launch() throws InterruptedException, ExecutionException, IOException, FlickrException {
        instantiateProperties();
        System.out.println("Properties loaded and instantiated");

        Flickr flickr = getFlickr();
        this.authStore = loadAuthStore(flickr, parentFolder);
        System.out.println("Auth store loaded.");

        this.collectionsInterface = setCollectionsInterface(flickr);
        System.out.println("CollectionsInterface loaded.");

        List<File> collectionsToUpload = getCollectionsToUpload();

//        uploadColections(flickr, collectionsToUpload);
    }

//    private void uploadColections(Flickr flickr, List<File> collectionsToUpload) {
//        PhotoSet photoSet = new PhotoSet();
//        PhotoList photoList = new PhotoList();
//
//
//        PhotosetsInterface pi = flickr.getPhotosetsInterface();
//        Photo photo = new Photo();
//        photo.setTitle();
//        photo.setPhotoUrl();
//        pi.create();
//        pi.addPhoto();
//
//        Uploader uploader = flickr.getUploader();
//        for (File collectionToUpload : collectionsToUpload) {
//            Collection collection = new Collection();
//            for (File fileToUpload : collectionsToUpload) {
//                if(fileToUpload.isFile()) {
//                    UploadMetaData metaData = new UploadMetaData();
//                    metaData.setPublicFlag(false);
//                    uploader.upload(fileToUpload, )
//                } else {
//                    System.out.println(fileToUpload.getAbsolutePath() + " is a directory.");
//                }
//            }
//        }
//        uploader.upload()
//    }

    private CollectionsInterface setCollectionsInterface(Flickr flickr) {
        this.collectionsInterface = flickr.getCollectionsInterface();
        return collectionsInterface;
    }

    private Flickr getFlickr() {
        return new Flickr(apiKey, sharedSecret, new REST("www.flickr.com"));
    }

    private List<File> getCollectionsToUpload() {
        List<Collection> existingFlickrCollections = getRemoteCollections();
        List<File> existingLocalCollections = getLocalCollections();

        System.out.println("Collections received: ");
        for (Collection collection : existingFlickrCollections) {
            System.out.println(collection.getTitle());
        }

        existingLocalCollections.removeAll(existingFlickrCollections.stream().map(collection -> new File(parentFolder + File.separator + collection.getTitle())).collect(Collectors.toList()));

        if (existingLocalCollections.size() > 0) {
            System.out.println("Collections to upload: ");
            existingLocalCollections.forEach(file -> System.out.println(file.getName()));
        } else {
            System.out.println("No collections to upload");
        }

        return existingLocalCollections;
    }

    List<Collection> getRemoteCollections() {
        List<Collection> remoteCollections = null;
        try {
            remoteCollections = collectionsInterface.getTree(null, null);
        } catch (FlickrException e) {
            e.printStackTrace();
        }

        return remoteCollections;
    }

    List<File> getLocalCollections() {
        return Arrays.asList(parentFolder.list()).stream()
                .map(collection -> new File(parentFolder + File.separator + collection))
                .filter(collection -> {
                    return collection.isDirectory();
                })
                .collect(Collectors.toList());
    }

    void instantiateProperties() {
        Properties properties = loadProperties();
        this.apiKey = properties.getProperty("api.key");
        this.sharedSecret = properties.getProperty("secret");
        this.parentFolder = new File(properties.getProperty("parent.folder"));
        this.nsid = properties.getProperty("nsid");
    }

    private AuthStore loadAuthStore(Flickr flickr, File parentFolder) throws IOException {
        this.authStore = setAuthStore(parentFolder.getAbsolutePath());
        Auth auth = this.authStore.retrieve(this.nsid);
        RequestContext.getRequestContext().setAuth(auth);
        this.authStore.store(auth);

        return this.authStore;

//        AuthInterface authInterface = flickr.getAuthInterface();
//        Token accessToken = authInterface.getRequestToken();
//
//        // Try with DELETE permission. At least need write permission for upload and add-to-set.
//        String url = authInterface.getAuthorizationUrl(accessToken, Permission.DELETE);
//        System.out.println("Follow this URL to authorise yourself on Flickr");
//        System.out.println(url);
//        System.out.println("Paste in the token it gives you:");
//        System.out.print(">>");
//
//        Scanner scanner = new Scanner(System.in);
//        String tokenKey = scanner.nextLine();
//
//        Token requestToken = authInterface.getAccessToken(accessToken, new Verifier(tokenKey));
//
//        Auth auth = null;
//        try {
//            auth = authInterface.checkToken(requestToken);
//        } catch (FlickrException e) {
//            e.printStackTrace();
//        }
//        RequestContext.getRequestContext().setAuth(auth);
//        this.authStore.store(auth);
//        scanner.close();
//        System.out.println("Thanks.  You probably will not have to do this every time. Auth saved for user: " + auth.getUser().getUsername() + " nsid is: "
//                + auth.getUser().getId());
//        System.out.println(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());
//
//        return this.authStore;
    }

    private AuthStore setAuthStore(String parentFolder) {
        AuthStore authStore = null;
        try {
            return new FileAuthStore(new File(parentFolder));
        } catch (FlickrException e) {
            e.printStackTrace();
            System.out.println("ERROR while setting AuthStore");
        }
        return authStore;
    }
}
