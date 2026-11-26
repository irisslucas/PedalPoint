package tqs.ua.pedalpoint.backend.user;

public class InvalidEmailDomainException extends RuntimeException {

    public InvalidEmailDomainException() {
        super("Only @ua.pt emails can register");
    }
}
