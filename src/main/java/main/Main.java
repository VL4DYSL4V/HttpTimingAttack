package main;

import authenticator.Authenticator;
import authenticator.FileSystemAuthenticator;
import dto.CommandLineArgsDto;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        CommandLineArgsDto dto = new CommandLineArgsDto();
        CmdLineParser parser = new CmdLineParser(dto);
        try {
            parser.parseArgument(args);

            Authenticator authenticator = new FileSystemAuthenticator(
                    Paths.get(dto.getUsersFile()),
                    Paths.get(dto.getPasswordsFile()),
                    dto.getUrl(),
                    dto.getHttpMethod(),
                    dto.getUsernameFormParameter(),
                    dto.getPasswordFormParameter(),
                    dto.getMillis()
            );
            authenticator.authenticate();
        } catch (CmdLineException | NullPointerException e) {
            // handling of wrong arguments
            System.err.printf("Message: %s%n", e.getMessage());
            parser.printUsage(System.err);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
