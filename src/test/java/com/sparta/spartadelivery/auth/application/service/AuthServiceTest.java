package com.sparta.spartadelivery.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.presentation.dto.request.ReqLoginDto;
import com.sparta.spartadelivery.auth.presentation.dto.request.ReqSignupDto;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 시 비밀번호를 암호화하고 사용자 정보를 저장한다")
    void signup() {
        ReqSignupDto request = signupRequest();
        when(userRepository.existsByUsername("user01")).thenReturn(false);
        when(userRepository.existsByEmail("user01@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.signup(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("user01");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getPassword()).isNotEqualTo("Password1!");
        assertThat(response.username()).isEqualTo("user01");
        assertThat(response.email()).isEqualTo("user01@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("이미 사용 중인 사용자 ID이면 회원가입을 거부한다")
    void signupWithDuplicateUsername() {
        ReqSignupDto request = signupRequest();
        when(userRepository.existsByUsername("user01")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_USERNAME);

        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입을 거부한다")
    void signupWithDuplicateEmail() {
        ReqSignupDto request = signupRequest();
        when(userRepository.existsByUsername("user01")).thenReturn(false);
        when(userRepository.existsByEmail("user01@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 시 인증 후 JWT access token을 발급한다")
    void login() {
        ReqLoginDto request = new ReqLoginDto("user01@example.com", "Password1!");
        UserEntity user = createUser("user01", "user01@example.com", Role.CUSTOMER);
        when(userRepository.findByEmailAndDeletedAtIsNull("user01@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");

        var response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user01@example.com", "Password1!")
        );
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.username()).isEqualTo("user01");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("인증 정보가 올바르지 않으면 로그인을 거부한다")
    void loginWithInvalidCredentials() {
        ReqLoginDto request = new ReqLoginDto("user01@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("invalid credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(userRepository, never()).findByEmailAndDeletedAtIsNull(any());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    private ReqSignupDto signupRequest() {
        return new ReqSignupDto(
                "user01",
                "Password1!",
                "유저01",
                "user01@example.com",
                Role.CUSTOMER
        );
    }

    private UserEntity createUser(String username, String email, Role role) {
        return UserEntity.builder()
                .username(username)
                .nickname("유저01")
                .email(email)
                .password("encoded-password")
                .role(role)
                .isPublic(true)
                .build();
    }
}
