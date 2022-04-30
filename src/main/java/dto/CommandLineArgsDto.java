package dto;

import enums.HttpMethod;
import enums.TimeOutOption;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.args4j.Option;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class CommandLineArgsDto {

    private Path usersFile;

    private Path passwordsFile;

    private URL url;

    private HttpMethod httpMethod;

    @Option(required = true, name = "-username-form-param", usage = "Sets a username form parameter")
    private String usernameFormParameter;

    @Option(required = true, name = "-password-form-param", usage = "Sets a password form parameter")
    private String passwordFormParameter;

    private int millis;

    private int threadCount = 1;

    private TimeOutOption timeOutOption;

    @Option(required = true, name = "-url", usage = "Sets a url to attack")
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Option(required = true, name = "-millis", usage = "Amount of milliseconds to wait")
    public void setMillis(String millis) {
        this.millis = Integer.parseInt(millis);
    }

    @Option(required = true, name = "-http-method", usage = "Sets a http method")
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = HttpMethod.valueOf(httpMethod.toUpperCase(Locale.ROOT));
    }

    @Option(name = "-thread-count", usage = "Sets a thread count")
    public void setThreadCount(String threadCount) {
        int countAsInt = Integer.parseInt(threadCount);
        this.threadCount = Math.min(Math.max(1, countAsInt), Runtime.getRuntime().availableProcessors());
    }

    @Option(
            required = true,
            name = "-timeout-option",
            usage = "Sets a timeout option: credentials are considered valid if authentication takes less/more seconds. Available options - MORE, LESS"
    )
    public void setTimeOutOption(String timeOutOption) {
        this.timeOutOption = TimeOutOption.valueOf(timeOutOption.toUpperCase(Locale.ROOT));
    }

    @Option(required = true, name = "-users-file", usage = "Sets a path to file with usernames")
    public void setUsersFilePath(String filePath) {
        this.usersFile = Paths.get(filePath);
    }

    @Option(required = true, name = "-pass-file", usage = "Sets a path to file with passwords")
    public void setPassFilePath(String filePath) {
        this.passwordsFile = Paths.get(filePath);
    }
}
