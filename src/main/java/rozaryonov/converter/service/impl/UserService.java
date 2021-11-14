package rozaryonov.converter.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import rozaryonov.converter.model.User;
import rozaryonov.converter.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User modelUser = userRepository.findByLogin(username).orElse(null);
        if (modelUser!=null) {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            modelUser.getRole().stream().forEach(e -> grantedAuthorities.add(new SimpleGrantedAuthority(e.toString())));
            return (UserDetails) new org.springframework.security.core.userdetails.User(
                    modelUser.getLogin(),
                    modelUser.getPassword(),
                    grantedAuthorities);
        }
        throw new UsernameNotFoundException("User for login = " + username + "hasn't been found.");
    }

    public BindingResult checkcheckUserCreationForm(User user, BindingResult bindingResult) {
        if (userRepository.findByLogin(user.getLogin()).orElse(null) != null) bindingResult
                .addError(new FieldError("user", "login", "Please, choose other login."));
        return bindingResult;
    }

    @Transactional
    public void encodePasswordAndSaveNewUser(User user) {
        String passwordEncoded = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncoded);
        userRepository.save(user);
    }
}
