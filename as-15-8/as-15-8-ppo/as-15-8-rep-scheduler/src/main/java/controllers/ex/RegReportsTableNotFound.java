package controllers.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: asinjavin
 * Date: 01.11.2019
 * Time: 13:54
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Report table not found")
class RegReportsTableNotFound extends RuntimeException
{
}
