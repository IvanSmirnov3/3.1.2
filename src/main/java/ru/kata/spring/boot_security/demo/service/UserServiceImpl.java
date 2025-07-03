package ru.kata.spring.boot_security.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleServiceImpl roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           RoleServiceImpl roleService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleService = roleService;
    }

    @Override
    public boolean deleteByIdUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean createUser(User user, List<Long> roleIds) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return false;
        }

        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleService::findById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        userRepository.save(user);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("User not found");
        return user;
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public ResultView updateUserView(Long id,
                                     User formUser,
                                     List<String> roleNames,
                                     String rawPassword,
                                     BindingResult result) {

        if (result.hasErrors()) {
            return new ResultView("updateUser")
                    .add("allRoles", roleService.findAll());
        }

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return new ResultView("updateUser")
                    .add("error", "User not found or update failed")
                    .add("allRoles", roleService.findAll());
        }

        User existingUser = optionalUser.get();
        existingUser.setUsername(formUser.getUsername());

        if (rawPassword != null && !rawPassword.isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        }

        if (roleNames != null) {
            Set<Role> roles = roleNames.stream()
                    .map(roleService::findByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        userRepository.save(existingUser);
        return new ResultView("redirect:/admin/users");
    }

    public ResultView createUserView(User user, List<Long> roleIds) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return new ResultView("addUser")
                    .add("error", "Username already exists")
                    .add("allRoles", roleService.findAll());
        }

        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleService::findById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        userRepository.save(user);
        return new ResultView("redirect:/admin/users");
    }

    public ResultView getUpdateUserFormView(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResultView("admin")
                    .add("error", "Пользователь не найден");
        }

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new ResultView("updateUser")
                .add("user", user)
                .add("userRoleNames", roleNames)
                .add("allRoles", roleService.findAll());
    }

}
