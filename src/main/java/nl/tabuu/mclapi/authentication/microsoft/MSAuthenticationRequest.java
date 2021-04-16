package nl.tabuu.mclapi.authentication.microsoft;

import nl.tabuu.mclapi.authentication.IAuthenticationRequest;

/**
 * Represents an authentication request based on a Microsoft authorization token.
 */
public class MSAuthenticationRequest implements IAuthenticationRequest {

    private final String _authorisationToken;

    /**
     * Creates an authentication request based on a Microsoft authorization token.
     *
     * @param authenticationToken The authorisation token that should be retrieved from the browser.
     *                            Example URL: https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&response_type=code&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&scope=XboxLive.signin%20offline_access
     */
    public MSAuthenticationRequest(String authenticationToken) {
        _authorisationToken = authenticationToken;
    }

    /**
     * Returns the authentication token used in this request.
     * @return the authentication token used in this request.
     */
    public String getAuthorisationToken() {
        return _authorisationToken;
    }
}