package ru.andrewb.charm.back.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.validator.EmailUtils;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final ProfileDao dao;

    public AuthUserDetailsService(ProfileDao dao) {
        this.dao = dao;
    }

    @Override
    public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = normalizeLoginEmail(username);

        var profile = dao.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        return new AuthUser(
                profile.getId(),
                profile.getEmail(),
                profile.getPassword(),
                profile.getRole()
        );
    }

    private String normalizeLoginEmail(String username) {
        try {
            return EmailUtils.requireValidOrThrow(username);
        } catch (BadRequestException e) {
            throw new UsernameNotFoundException(username, e);
        }
    }
}
