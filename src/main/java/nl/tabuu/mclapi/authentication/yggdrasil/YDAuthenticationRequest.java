package nl.tabuu.mclapi.authentication.yggdrasil;

import nl.tabuu.mclapi.authentication.IAuthenticationRequest;

public class YDAuthenticationRequest implements IAuthenticationRequest {

    private final String _username, _password;

    public YDAuthenticationRequest(String username, String password) {
        _username = username;
        _password = password;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }
}
