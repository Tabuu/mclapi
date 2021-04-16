package nl.tabuu.mclapi.authentication;

import java.util.Optional;

/**
 * Represents the response of the authentication process.
 */
public class AuthenticationResponse {

    private final State _state;
    private final Session _session;

    public AuthenticationResponse(State state, Session session) {
        _state = state;
        _session = session;
    }

    public AuthenticationResponse(State state) {
        this(state, null);
    }

    /**
     * Returns the state of this response.
     * @return The state of this response.
     */
    public State getState() {
        return _state;
    }


    /**
     * Returns an optional containing the session that was created by this request, if a session could be created.
     * @return An optional containing the session that was created by this request, if a session could be created.
     */
    public Optional<Session> getSession() {
        return Optional.ofNullable(_session);
    }

    public enum State {
        /**
         * The authorisation was successful, and a session was created.
         */
        SUCCESS,

        /**
         * The authorisation failed, and no session was created.
         */
        NO_AUTHENTICATION,

        /**
         * The authorisation was successful, but not session could be created.
         */
        NO_SESSION;
    }
}