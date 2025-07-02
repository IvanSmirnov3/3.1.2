package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    List<User> findAllUser();
    boolean deleteByIdUser(Long userId);
    boolean createUser(User user, List<Long> roleIds);
}
