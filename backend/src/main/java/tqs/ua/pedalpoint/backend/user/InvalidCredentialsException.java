package tqs.ua.pedalpoint.backend.user;

/** Same message for unknown email and wrong password, so the API does not reveal which accounts exist. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
