package com.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.collections.CollectionsInterface;
import com.flickr4java.flickr.photosets.Photoset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class FlickrUploaderTest {

    private FlickrUploader flickrUploader;
    private Method getFlickrMethod;
    private Method getCollectionsInterface;

    @Before
    public void init() throws NoSuchMethodException {
        FlickrUploader flickrUploader = new FlickrUploader();
        getFlickrMethod = FlickrUploader.class.getDeclaredMethod("getFlickr", null);
        getFlickrMethod.setAccessible(true);
        getCollectionsInterface = FlickrUploader.class.getDeclaredMethod("setCollectionsInterface", Flickr.class);
        getCollectionsInterface.setAccessible(true);
    }

    @Test
    public void test_getLocalCollections(){
        FlickrUploader flickrUploader = new FlickrUploader();
        flickrUploader.instantiateProperties();
        Map<String, File> localCollections = flickrUploader.getLocalAlbums();
        Assert.assertNotNull(localCollections);
        Assert.assertTrue(!localCollections.isEmpty());
    }

    @Test
    public void test_getRemoteCollections() throws InvocationTargetException, IllegalAccessException, FlickrException {
        FlickrUploader flickrUploader = new FlickrUploader();
        flickrUploader.instantiateProperties();

        Flickr flickr = (Flickr) getFlickrMethod.invoke(flickrUploader, null);
        Assert.assertNotNull(flickr);
        CollectionsInterface collectionsInterface = (CollectionsInterface) getCollectionsInterface.invoke(flickrUploader, flickr);
        Assert.assertNotNull(collectionsInterface);

        Map<String, Photoset> remoteAlbums = flickrUploader.getRemoteAlbums();
        Assert.assertNotNull(remoteAlbums);
        Assert.assertTrue(!remoteAlbums.isEmpty());
    }

}