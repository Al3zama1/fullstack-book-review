package com.example.fullstackbookreview.user;

public interface UserService {
    User getOrCreateUser(String name, String email);
}
