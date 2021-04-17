package nl.tabuu.mclapi.authentication;

import nl.tabuu.mclapi.authentication.microsoft.MSAuthenticator;
import nl.tabuu.mclapi.authentication.yggdrasil.YDAuthenticator;

public interface IAuthenticator<R extends IAuthenticationRequest> {

    /**
     * Tries to authenticate the user based on the {@link IAuthenticationRequest}.
     * @param request The request of authentication.
     * @return The response to the authentication.
     */
    AuthenticationResponse authenticate(R request);

    /**
     * Returns the authenticator for the Yggdrasil (Mojang) system.
     * @return The authenticator for the Yggdrasil (Mojang) system.
     */
    static YDAuthenticator yggdrasil() {
        return new YDAuthenticator();
    }

    /**
     * Returns the authenticator for the microsoft system.
     * @return The authenticator for the microsoft system.
     */
    static MSAuthenticator microsoft() {
        return new MSAuthenticator();
    }
}