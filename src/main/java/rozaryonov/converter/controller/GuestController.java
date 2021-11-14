package rozaryonov.converter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rozaryonov.converter.exception.UserNotFoundException;
import rozaryonov.converter.model.Role;
import rozaryonov.converter.model.User;
import rozaryonov.converter.repository.UserRepository;
import rozaryonov.converter.service.impl.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class GuestController {
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String loginOrRegisterPage() {
        return "loginPage";
    }

    @GetMapping("/authorized_zone_redirection")
    @Transactional
    public String redirectToAuthrizedZone(Principal principal, HttpSession session) {
        User authenticatedUser = userRepository.findByLogin(principal.getName()).orElseThrow(
                ()-> new UserNotFoundException("User with login = " + principal.getName() + "hasn't been found"));
        session.setAttribute("currentUser", authenticatedUser);
        return "redirect:/users/" + authenticatedUser.getLogin() + "/files/";
    }

    @GetMapping("/users/new_user_form")
    public String getNewUserForm(Model model) {
        User user = new User();
        user.getRole().add(Role.ROLE_USER);
        model.addAttribute("user", user);
        return "new_user_form";
    }

    @PostMapping("/users/")
    public String createUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        String page;
        if(userService.checkcheckUserCreationForm(user, bindingResult).hasErrors()) {
            page = "new_user_form";
        } else {
            userService.encodePasswordAndSaveNewUser(user);
            page = "redirect:/";
        }
        return page;
    }

    @GetMapping("/users/{userLogin}/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
