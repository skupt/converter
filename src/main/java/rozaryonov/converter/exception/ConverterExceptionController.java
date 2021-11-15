package rozaryonov.converter.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class ConverterExceptionController {
    @ExceptionHandler(StorageException.class)
    public String handleConverterExceptions(StorageException e,  HttpServletResponse response, RedirectAttributes redirectAttrs) {
        log.error(e.getMessage(), e);
        redirectAttrs.addFlashAttribute("errorDescription", e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return "redirect:/error/403";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public String handleConverterExceptions(StorageFileNotFoundException e,  HttpServletResponse response, RedirectAttributes redirectAttrs) {
        log.error(e.getMessage(), e);
        redirectAttrs.addFlashAttribute("errorDescription", e.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return "redirect:/error/404";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleConverterExceptions(UserNotFoundException e,  HttpServletResponse response, RedirectAttributes redirectAttrs) {
        log.error(e.getMessage(), e);
        redirectAttrs.addFlashAttribute("errorDescription", e.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return "redirect:/error/404";
    }

    @ExceptionHandler(Exception.class )
    public String handleExceptions (Exception e, HttpServletResponse response, RedirectAttributes redirectAttrs) {
        log.error(e.getMessage(), e);
        redirectAttrs.addFlashAttribute("errorDescription", e.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "redirect:/error/5xx";
    }
}
