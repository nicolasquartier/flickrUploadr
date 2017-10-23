package com.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.collections.CollectionsInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

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
        List<File> localCollections = flickrUploader.getLocalCollections();
        Assert.assertNotNull(localCollections);
        Assert.assertTrue(!localCollections.isEmpty());
    }

    @Test
    public void test_getRemoteCollections() throws InvocationTargetException, IllegalAccessException {
        FlickrUploader flickrUploader = new FlickrUploader();
        flickrUploader.instantiateProperties();

        Flickr flickr = (Flickr) getFlickrMethod.invoke(flickrUploader, null);
        Assert.assertNotNull(flickr);
        CollectionsInterface collectionsInterface = (CollectionsInterface) getCollectionsInterface.invoke(flickrUploader, flickr);
        Assert.assertNotNull(collectionsInterface);

        List<Collection> remoteCollections = flickrUploader.getRemoteCollections();
        Assert.assertNotNull(remoteCollections);
        Assert.assertTrue(!remoteCollections.isEmpty());
    }

}