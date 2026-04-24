package com.example.CompressorWebApp.services;

import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }


    @Test
    void GetCurrentUser_shouldReturnUserFromSecurityContext() {

        String login = "john_doe";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(login);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        User expectedUser = new User();
        expectedUser.setLogin(login);
        when(userRepository.findByLogin(login)).thenReturn(expectedUser);


        User result = userService.GetCurrentUser();


        assertThat(result).isSameAs(expectedUser);
        verify(userRepository).findByLogin(login);
    }


    @Test
    void findByLogin_shouldReturnUserFromRepository() {
        String login = "test_user";
        User mockUser = new User();
        when(userRepository.findByLogin(login)).thenReturn(mockUser);

        User result = userService.findByLogin(login);

        assertThat(result).isSameAs(mockUser);
        verify(userRepository).findByLogin(login);
    }


    @Test
    void registerUser_shouldEncodePasswordAndSave() {
        // given
        String rawPassword = "secret";
        String encodedPassword = "encoded_secret";
        User user = new User();
        user.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        userService.registerUser(user);

        // then
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        verify(userRepository).save(user);
    }


    @Test
    void findByStationId_shouldReturnListFromRepository() {
        Long stationId = 5L;
        List<User> mockUsers = List.of(new User(), new User());
        when(userRepository.findByStationId(stationId)).thenReturn(mockUsers);

        List<User> result = userService.findByStationId(stationId);

        assertThat(result).hasSize(2);
        verify(userRepository).findByStationId(stationId);
    }


    @Test
    void closeShift_shouldSetInWorkFalseAndSave() {
        User user = new User();
        user.setInWork(true);

        userService.closeShift(user);

        assertThat(user.isInWork()).isFalse();
        verify(userRepository).save(user);
    }


    @Test
    void openShift_shouldSetInWorkTrueAndSave() {
        User user = new User();
        user.setInWork(false);

        userService.openShift(user);

        assertThat(user.isInWork()).isTrue();
        verify(userRepository).save(user);
    }


    @Test
    void findCurrentShiftUserByStationId_shouldReturnFirstUserWithInWorkTrue() {
        Long stationId = 10L;
        User user1 = new User();
        user1.setInWork(false);
        User user2 = new User();
        user2.setInWork(true);
        User user3 = new User();
        user3.setInWork(true);

        when(userRepository.findByStationId(stationId)).thenReturn(List.of(user1, user2, user3));

        Optional<User> result = userService.findCurrentShiftUserByStationId(stationId);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(user2);
        verify(userRepository).findByStationId(stationId);
    }

    @Test
    void findCurrentShiftUserByStationId_shouldReturnEmptyOptional_whenNoUserWithInWorkTrue() {
        Long stationId = 20L;
        User user1 = new User();
        user1.setInWork(false);
        User user2 = new User();
        user2.setInWork(false);

        when(userRepository.findByStationId(stationId)).thenReturn(List.of(user1, user2));

        Optional<User> result = userService.findCurrentShiftUserByStationId(stationId);

        assertThat(result).isEmpty();
        verify(userRepository).findByStationId(stationId);
    }

    @Test
    void findCurrentShiftUserByStationId_shouldReturnEmptyOptional_whenNoUsers() {
        Long stationId = 30L;
        when(userRepository.findByStationId(stationId)).thenReturn(List.of());

        Optional<User> result = userService.findCurrentShiftUserByStationId(stationId);

        assertThat(result).isEmpty();
        verify(userRepository).findByStationId(stationId);
    }


    @Test
    void save_shouldCallRepositorySave() {
        User user = new User();
        userService.save(user);
        verify(userRepository).save(user);
    }


    @Test
    void countByRole_shouldReturnCountFromRepository() {
        String role = "ADMIN";
        when(userRepository.countByRole(role)).thenReturn(5L);

        long count = userService.countByRole(role);

        assertThat(count).isEqualTo(5L);
        verify(userRepository).countByRole(role);
    }
}