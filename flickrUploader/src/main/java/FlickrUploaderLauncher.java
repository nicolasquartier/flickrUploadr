import com.flickr4java.flickr.FlickrException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by NQRAZ66 on 20/10/2017.
 */
public class FlickrUploaderLauncher {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, FlickrException {
        FlickrUploader flickrUploader = new FlickrUploader();
        flickrUploader.launch();
    }
}
