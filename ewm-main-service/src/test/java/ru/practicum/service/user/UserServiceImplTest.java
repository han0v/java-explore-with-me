package ru.practicum.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private final User testUser = new User(1L, "user@example.com", "User Name");
    private final NewUserRequest newUserRequest = new NewUserRequest("user@example.com", "User Name");
    private final UserDto userDto = new UserDto(1L, "user@example.com", "User Name");

    @Test
    void createUser_shouldCreateNewUser() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(newUserRequest)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toUserDto(testUser)).thenReturn(userDto);

        UserDto result = userService.createUser(newUserRequest);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_shouldThrowConflictExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(newUserRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(newUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_shouldReturnAllUsersWhenIdsNotSpecified() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserDto(testUser)).thenReturn(userDto);

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void getUsers_shouldReturnFilteredUsersWhenIdsSpecified() {
        List<Long> ids = List.of(1L);
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);

        when(userRepository.findAllByIds(ids)).thenReturn(users);
        when(userMapper.toUserDto(testUser)).thenReturn(userDto);

        List<UserDto> result = userService.getUsers(ids, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void getUsers_shouldReturnEmptyListWhenNoUsersFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of());

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteUser_shouldDeleteUserWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowNotFoundExceptionWhenUserNotExists() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}