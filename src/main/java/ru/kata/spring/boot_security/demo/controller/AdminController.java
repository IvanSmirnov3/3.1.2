package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public String showUsersPage(Model model) {
        List<User> users = userService.findAllUser();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping
    public String userPageOpen(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "admin";
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/addUser")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        return "addUser";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(value = "roles", required = false) List<Long> roleIds) {
        userService.createUser(user, roleIds);
        return "redirect:/admin/users";
    }

    @PostMapping("/updateUser")
    public String showUpdateUserForm(@RequestParam("id") long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
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

        boolean success = userService.updateUser(id, formUser, roleNames, rawPassword, result, model);
        return success ? "redirect:/admin/users" : "updateUser";
    }
}
