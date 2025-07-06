package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void createUser(User user, List<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        if (roleIds != null) {
            roles.addAll(roleService.getRolesByIds(roleIds));
        }
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean updateUser(Long id, User formUser, List<String> roleNames, String rawPassword, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", formUser);
            model.addAttribute("allRoles", roleService.findAll());
            model.addAttribute("errors", result.getAllErrors());
            return false;
        }

        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userFromDb.setUsername(formUser.getUsername());

        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleService.getRolesByNames(roleNames));
            userFromDb.setRoles(roles);
        }

        if (rawPassword != null && !rawPassword.isEmpty()) {
            userFromDb.setPassword(passwordEncoder.encode(rawPassword));
        }

        userRepository.save(userFromDb);
        return true;
    }
}
