package com.example.fullstackbookreview.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepository mockUserRepository;
    @InjectMocks
    private UserServiceImp cut;

    @Test
    void shouldIncludeCurrentDateTimeWhenCreatingNewUser() {
        // Given
        given(mockUserRepository.findByNameAndEmail("john", "john@spring.io"))
                .willReturn(Optional.empty());
        given(mockUserRepository.save(any(User.class))).willAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L);
            return userToSave;
        });

        LocalDateTime defaultLocalDateTime = LocalDateTime.of(
                2022, 12, 13, 12, 15);

        /*
        the mocked version of LocalDateTime is only available within the try with resources block. Outside of it, we
        still have normal behavior of LocalDateTime
         */
        try(MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(defaultLocalDateTime);

            // When
            User savedUser = cut.getOrCreateUser("john", "john@spring.io");

            // Then
            assertThat(savedUser.getCreatedAt()).isEqualTo(defaultLocalDateTime);
        }
    }

}