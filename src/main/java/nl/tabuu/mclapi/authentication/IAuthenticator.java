package nl.tabuu.mclapi.authentication;

import nl.tabuu.mclapi.authentication.microsoft.MSAuthenticator;
import nl.tabuu.mclapi.authentication.microsoft.MSAuthenticationRequest;
import nl.tabuu.mclapi.authentication.yggdrasil.YDAuthenticationRequest;
import nl.tabuu.mclapi.authentication.yggdrasil.YDAuthenticator;

import java.util.concurrent.CompletableFuture;

public interface IAuthenticator<R extends IAuthenticationRequest> {

    /**
     * Tries to authenticate the user based on the {@link IAuthenticationRequest}.
     * @param request The request of authentication.
     * @return The response to the authentication.
     */
    CompletableFuture<AuthenticationResponse> authenticate(R request);

    /**
     * Returns the authenticator for the Yggdrasil (Mojang) system.
     * @return The authenticator for the Yggdrasil (Mojang) system.
     */
    static YDAuthenticator yggdrasil() {
        return new YDAuthenticator();
    }

    /**
     * A shortcut for authenticating with {@link YDAuthenticator}.
     * @param username The username for the {@link YDAuthenticationRequest}.
     * @param password The password for the {@link YDAuthenticationRequest}.
     * @return The {@link AuthenticationResponse} for the request.
     */
    static CompletableFuture<AuthenticationResponse> yggdrasil(String username, String password) {
        return yggdrasil().authenticate(new YDAuthenticationRequest(username, password));
    }

    /**
     * Returns the authenticator for the microsoft system.
     * @return The authenticator for the microsoft system.
     */
    static MSAuthenticator microsoft() {
        return new MSAuthenticator();
    }

    /**
     * A shortcut for authenticating with {@link MSAuthenticator}.
     * @param authenticationCode The username for the {@link MSAuthenticationRequest}.
     * @return The {@link AuthenticationResponse} for the request.
     */
    static CompletableFuture<AuthenticationResponse> microsoft(String authenticationCode) {
        return microsoft().authenticate(new MSAuthenticationRequest(authenticationCode));
    }
}