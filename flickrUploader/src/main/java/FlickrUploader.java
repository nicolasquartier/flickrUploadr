import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.test.TestInterface;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by NQRAZ66 on 20/10/2017.
 */
public class FlickrUploader {

    private static final String PROPERTIES_FILE = "config.properties";
    private static String apiKey;
    private static String sharedSecret;
    private static String parentFolder;


    void launch() throws InterruptedException, ExecutionException, IOException, FlickrException {
        Properties properties = loadProperties();
        instantiateProperties(properties);

        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST("www.flickr.com"));
        loadAuthStore(flickr, properties);





        Flickr.debugStream = false;
        AuthInterface authInterface = f.getAuthInterface();

        Scanner scanner = new Scanner(System.in);

        Token token = authInterface.getRequestToken();
        System.out.println("token: " + token);

        String url = authInterface.getAuthorizationUrl(token, Permission.DELETE);
        System.out.println("Follow this URL to authorise yourself on Flickr");
        System.out.println(url);
        System.out.println("Paste in the token it gives you:");
        System.out.print(">>");

        String tokenKey = scanner.nextLine();
        scanner.close();

        Token requestToken = authInterface.getAccessToken(token, new Verifier(tokenKey));
        System.out.println("Authentication success");

        Auth auth = authInterface.checkToken(requestToken);

        // This token can be used until the user revokes it.
        System.out.println("Token: " + requestToken.getToken());
        System.out.println("Secret: " + requestToken.getSecret());
        System.out.println("nsid: " + auth.getUser().getId());
        System.out.println("Realname: " + auth.getUser().getRealName());
        System.out.println("Username: " + auth.getUser().getUsername());
        System.out.println("Permission: " + auth.getPermission().getType());


    }

    private void instantiateProperties(Properties properties) {
        printProperties(properties);
        apiKey = properties.getProperty("api.key");
        sharedSecret = properties.getProperty("secret");
        parentFolder = properties.getProperty("parent.folder");
    }

    private void printProperties(Properties properties) {
        String parentFolder = properties.getProperty("parent.folder");
        System.out.println(parentFolder);
        String apiKey = properties.getProperty("api.key");
        System.out.println(apiKey);
        String sharedSecret = properties.getProperty("secret");
        System.out.println(sharedSecret);
    }

    private void loadAuthStore(Flickr flickr, Properties properties) {
        setAuthStore();
        AuthInterface authInterface = flickr.getAuthInterface();
        Token accessToken = authInterface.getRequestToken();

        // Try with DELETE permission. At least need write permission for upload and add-to-set.
        String url = authInterface.getAuthorizationUrl(accessToken, Permission.DELETE);
        System.out.println("Follow this URL to authorise yourself on Flickr");
        System.out.println(url);
        System.out.println("Paste in the token it gives you:");
        System.out.print(">>");

        Scanner scanner = new Scanner(System.in);
        String tokenKey = scanner.nextLine();

        Token requestToken = authInterface.getAccessToken(accessToken, new Verifier(tokenKey));

        Auth auth = null;
        try {
            auth = authInterface.checkToken(requestToken);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        RequestContext.getRequestContext().setAuth(auth);
        this.authStore.store(auth);
        scanner.close();
        System.out.println("Thanks.  You probably will not have to do this every time. Auth saved for user: " + auth.getUser().getUsername() + " nsid is: "
                + auth.getUser().getId());
        System.out.println(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());
    }

    private void setAuthStore() {
        AuthStore authStore = new FileAuthStore();
    }

    private void testInterface(Flickr f) {
        TestInterface testInterface = f.getTestInterface();
        try {
            Collection results = testInterface.echo(Collections.EMPTY_MAP);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        InputStream input = FlickrUploaderLauncher.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        try {
            if (input == null) {
                System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
            } else {
                properties.load(input);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        return properties;
    }
}
