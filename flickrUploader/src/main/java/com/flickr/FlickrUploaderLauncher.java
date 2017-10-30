package com.flickr;

/**
 * Created by NQRAZ66 on 20/10/2017.
 */
public class FlickrUploaderLauncher implements Runnable{

    public static void main(String[] args) throws Exception {}

    @Override
    public void run() {
        try {
            FlickrUploader flickrUploader = new FlickrUploader();
            flickrUploader.initiate();
            flickrUploader.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
