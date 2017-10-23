package com.flickr.utils;

import com.flickr.FlickrUploaderLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

    private static final String PROPERTIES_FILE = "config.properties";

    public static Properties loadProperties() {

        Properties properties = new Properties();
        InputStream input = FlickrUploaderLauncher.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        try {
            if (input == null) {
                System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        printProperties(properties);
        return properties;

    }

    private static void printProperties(Properties properties) {
        String parentFolder = properties.getProperty("parent.folder");
        System.out.println(parentFolder);
        String apiKey = properties.getProperty("api.key");
        System.out.println(apiKey);
        String sharedSecret = properties.getProperty("secret");
        System.out.println(sharedSecret);
    }
}
