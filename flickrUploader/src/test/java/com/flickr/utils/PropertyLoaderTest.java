package com.flickr.utils;


import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;


public class PropertyLoaderTest {

    @Test
    public void test_loadProperties() {
        Properties properties = PropertyLoader.loadProperties();
        Assert.assertEquals(properties.getProperty("parent.folder"), "C:\\Users\\NQRAZ66\\Pictures\\Screenshots");
        Assert.assertEquals(properties.getProperty("api.key"), "14b051fb4d1509bc0170aab11eee90bf");
        Assert.assertEquals(properties.getProperty("secret"), "e7b3f312d6ca36d0");
    }

}