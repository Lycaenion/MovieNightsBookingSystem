package program.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Det finns inga filmer med den titeln")
public class NotFoundException extends RuntimeException {
}
