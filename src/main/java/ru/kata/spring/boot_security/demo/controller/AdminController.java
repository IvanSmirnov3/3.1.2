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
import ru.kata.spring.boot_security.demo.service.ResultView;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

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
    public String userPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "admin";
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteByIdUser(id);
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
        ResultView resultView = userService.createUserView(user, roleIds);
        model.addAllAttributes(resultView.getModelAttributes());
        return resultView.getViewName();
    }


    @PostMapping("/updateUser")
    public String showUpdateUserForm(@RequestParam("id") long id, Model model) {
        ResultView resultView = userService.getUpdateUserFormView(id);
        model.addAllAttributes(resultView.getModelAttributes());
        return resultView.getViewName();
    }


    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") User formUser,
                             @RequestParam(value = "roleNames", required = false) List<String> roleNames,
                             @RequestParam(value = "password", required = false) String rawPassword,
                             BindingResult result,
                             Model model) {

        ResultView rv = userService.updateUserView(id, formUser, roleNames, rawPassword, result);
        model.addAllAttributes(rv.getModelAttributes());
        return rv.getViewName();
    }

}
