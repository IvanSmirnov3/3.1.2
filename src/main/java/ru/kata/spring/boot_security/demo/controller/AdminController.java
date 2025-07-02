package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserServiceImpl userServiceImpl;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserServiceImpl userServiceImpl, RoleService roleService) {
        this.userServiceImpl = userServiceImpl;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminHome() {
        return "admin";
    }

    @GetMapping("/users")
    public String showUsersPage(Model model) {
        List<User> users = userServiceImpl.findAllUser();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/admin")
    public String userPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "admin";
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean success = userServiceImpl.deleteByIdUser(id);
        return success ? ResponseEntity.ok("User deleted") : ResponseEntity.notFound().build();
    }

    @GetMapping("/addUser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        return "addUser";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(value = "roles", required = false) List<Long> roleIds,
                             Model model) {
        boolean success = userServiceImpl.createUser(user, roleIds);
        if (success) {
            return "redirect:/users";
        } else {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("allRoles", roleService.findAll());
            return "addUser";
        }
    }

    @PostMapping("/updateUser")
    public String showUpdateUserForm(@RequestParam("id") long id, Model model) {
        User user = userServiceImpl.findUserById(id);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
            return "admin";
        }

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        model.addAttribute("user", user);
        model.addAttribute("userRoleNames", roleNames); // 👈 важно!
        model.addAttribute("allRoles", roleService.findAll());
        return "updateUser";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") User formUser,
                             @RequestParam(value = "roleNames", required = false) List<String> roleNames,
                             @RequestParam(value = "password", required = false) String rawPassword,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAll());
            return "updateUser";
        }

        boolean success = userServiceImpl.updateUser(id, formUser, roleNames, rawPassword);

        if (!success) {
            model.addAttribute("error", "User not found or update failed");
            model.addAttribute("allRoles", roleService.findAll());
            return "updateUser";
        }
        return "redirect:/users";
    }
}
