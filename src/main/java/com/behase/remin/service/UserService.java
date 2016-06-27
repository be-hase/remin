package com.behase.remin.service;

import com.behase.remin.model.LoginUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    LoginUser getUser(String username) throws Exception;

    List<LoginUser> getUsers() throws Exception;

    void addUser(String username, String displayName, String password, String role) throws Exception;

    void changePassword(String username, String oldPassword, String password) throws Exception;

    void updateUser(String username, String displayName, String role) throws Exception;

    void deleteUser(String username) throws Exception;
}
