package rozaryonov.converter.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rozaryonov.converter.service.ConverterService;
import rozaryonov.converter.util.XlsPdfConverter;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/users/{userLogin}/filesToConvert/")
public class FileConverterController {

    private final ConverterService converterService;
    private final XlsPdfConverter xlsPdfConverter;

    @GetMapping
    public String listFilesToConvert(Model model, Principal principal){
        model.addAttribute("currentUser", principal.getName());
        List<String> listOfUserFilenames = converterService.listOfConvertableFiles(principal.getName());
        model.addAttribute("listOfUserFilenames", listOfUserFilenames);
        return "convertForm";
    }

    @PostMapping
    public String convertFile(@RequestParam ("filename") String filename, Principal principal) {
        InputStream inputStream = converterService.getInputStream(filename, principal.getName());
        OutputStream outputStream = converterService.getOutputStream(filename, principal.getName());
        xlsPdfConverter.writeXlsToPdf(inputStream, outputStream);
        return "redirect:/users/" + principal.getName() + "/files/";
    }
}
