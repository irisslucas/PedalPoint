package tqs.ua.pedalpoint.backend.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Maps auth errors to RFC 9457 problem responses; the message is in {@code detail}. */
@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidEmailDomainException.class)
    ProblemDetail invalidDomain(InvalidEmailDomainException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ProblemDetail duplicate(EmailAlreadyRegisteredException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Two concurrent registrations can both pass the existence check; the unique constraint decides. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail duplicateRace(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, new EmailAlreadyRegisteredException().getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(InvalidCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
