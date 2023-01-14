package com.example.fullstackbookreview.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService{

    private final UserRepository userRepository;

    @Override
    public User getOrCreateUser(String name, String email) {
        Optional<User> userOptional = userRepository.findByNameAndEmail(name, email);

        if (userOptional.isPresent()) return userOptional.get();

       User user = User.builder()
               .name(name)
               .email(email)
               .createdAt(LocalDateTime.now())
               .build();

       user = userRepository.save(user);

       return user;
    }
}
