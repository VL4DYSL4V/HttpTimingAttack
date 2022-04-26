package dto;

import enums.HttpMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.args4j.Option;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class CommandLineArgsDto {

    @Option(name = "-users-file", usage = "Sets a path to file with usernames")
    private String usersFile;

    @Option(name = "-pass-file", usage = "Sets a path to file with passwords")
    private String passwordsFile;

    private URL url;

    private HttpMethod httpMethod;

    @Option(name = "-username-form-param", usage = "Sets a username form parameter")
    private String usernameFormParameter;

    @Option(name = "-password-form-param", usage = "Sets a password form parameter")
    private String passwordFormParameter;

    private int millis;

    @Option(name = "-url", usage = "Sets a url to attack")
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Option(name = "-millis", usage = "Amount of milliseconds to wait")
    public void setMillis(String millis) {
        this.millis = Integer.parseInt(millis);
    }

    @Option(name = "-http-method", usage = "Sets a http method")
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = HttpMethod.valueOf(httpMethod.toUpperCase(Locale.ROOT));
    }
}
