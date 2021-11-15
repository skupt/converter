package rozaryonov.converter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error/")
public class ErrorViewController {

    @GetMapping("5xx")
    public String showException5xx(Model model) {
        return "/error/5xx";
    }

    @GetMapping("404")
    public String showException404(Model model) {
        return "/error/404";
    }

    @GetMapping("403")
    public String showException403(Model model) {
        return "/error/403";
    }



}
