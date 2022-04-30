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

            Authenticator authenticator = new FileSystemAuthenticator(dto);
            authenticator.authenticate().thenAccept((credentials) -> {
                if (credentials.size() != 0) {
                    System.out.println("Credentials that gave timeout:");
                    credentials.forEach(c -> System.out.printf("\t%s\t-\t%s\t%n", c.getUsername(), c.getPassword()));
                } else {
                    System.out.println("No credentials satisfied specified parameters");
                }
            });
        } catch (CmdLineException | NullPointerException e) {
            // handling of wrong arguments
            System.err.printf("Message: %s%n", e.getMessage());
            parser.printUsage(System.err);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
