package rozaryonov.converter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rozaryonov.converter.service.StorageService;

import java.io.IOException;
import java.security.Principal;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users/{userLogin}/files/")
public class FileUploadController {
    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model, @PathVariable ("userLogin") String userLogin, Principal principal,
                                    @Value("${server.servlet.context-path}") String contextPath) throws IOException {
        model.addAttribute("currentUser", principal.getName());
        model.addAttribute("files", storageService.loadAll(principal.getName()).map(
                path -> MvcUriComponentsBuilder.fromController(GuestController.class)
                        .build().toUri().resolve((contextPath + "/users/"
                                + principal.getName() + "/files/" + path.toString()).replaceAll("\\s", "%20"))
                        ).collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, Principal principal) {

        Resource file = storageService.loadAsResource(filename, principal.getName());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, Principal principal) {

        storageService.store(file, principal.getName());
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/users/" + principal.getName() + "/files/";
    }

}
