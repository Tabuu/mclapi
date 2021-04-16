package nl.tabuu.mclapi.authentication;

public interface IAuthenticator<R extends IAuthenticationRequest> {

    /**
     * Tries to authenticate the user based on the {@link IAuthenticationRequest}.
     * @param request The request of authentication.
     * @return The response to the authentication.
     */
    AuthenticationResponse authenticate(R request);
}