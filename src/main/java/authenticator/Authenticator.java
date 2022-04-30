package authenticator;

import dto.Credentials;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface Authenticator {

    CompletableFuture<Collection<Credentials>> authenticate();

}
