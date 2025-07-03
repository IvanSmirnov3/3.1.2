package ru.kata.spring.boot_security.demo.service;

import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    List<User> findAllUser();
    boolean deleteByIdUser(Long userId);
    boolean createUser(User user, List<Long> roleIds);
    User findUserById(Long id);
    ResultView updateUserView(Long id, User formUser, List<String> roleNames, String rawPassword, BindingResult result);
    ResultView createUserView(User user, List<Long> roleIds);
    ResultView getUpdateUserFormView(Long id);
}
