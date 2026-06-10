package com.sparkminds.ecommerce.service.impl;


import com.sparkminds.ecommerce.dto.request.LoginRequest;
import com.sparkminds.ecommerce.dto.request.RefreshTokenRequest;
import com.sparkminds.ecommerce.dto.request.RegisterRequest;
import com.sparkminds.ecommerce.dto.response.AuthResponse;
import com.sparkminds.ecommerce.entity.RefreshToken;
import com.sparkminds.ecommerce.entity.enums.Role;
import com.sparkminds.ecommerce.entity.User;
import com.sparkminds.ecommerce.exception.BadRequestException;
import com.sparkminds.ecommerce.exception.DuplicateResourceException;
import com.sparkminds.ecommerce.exception.ResourceNotFoundException;
import com.sparkminds.ecommerce.exception.UnauthorizedException;
import com.sparkminds.ecommerce.repository.InvalidatedTokenRepository;
import com.sparkminds.ecommerce.repository.RefreshTokenRepository;
import com.sparkminds.ecommerce.repository.UserRepository;
import com.sparkminds.ecommerce.service.AuthService;
import com.sparkminds.ecommerce.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparkminds.ecommerce.entity.InvalidatedToken;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${jwt.refesh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already taken");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("[REGISTER] Registered new user {}", request.getUsername());
        log.info("[REGISTER] Created refresh token {}", refreshToken);

        return AuthResponse.success(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.info("[LOGIN] Invalid username or password");
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("[LOGIN] Logged in user {}", user.getUsername());

        return AuthResponse.success(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);

            log.info("[REFRESH TOKEN] Expired {}", refreshToken);
            throw new UnauthorizedException("Refresh token has expired. Please login again");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateAccessToken(user);

        // Refresh Token Rotation: delete old token, create new one
        refreshTokenRepository.delete(refreshToken);
        String newRefreshToken = createRefreshToken(user);

        log.info("[REFRESH TOKEN] Created new refresh token {}", newRefreshToken);
        return AuthResponse.success(
                accessToken,
                newRefreshToken,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        refreshTokenRepository.delete(refreshToken); // delete refreshToken

        final String authHeader = httpRequest.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            final String jwt = authHeader.substring(7); // get access-token
//            try {
//                String jti = jwtUtil.extractId(jwt); //jwt-id
//                Date expiration = jwtUtil.extractExpiration(jwt);
//
//                if (jti != null && expiration != null) {
//                    InvalidatedToken invalidatedToken = new InvalidatedToken(jti, expiration);
////                    invalidatedToken.setId(jti);
////                    invalidatedToken.setExpiryTime(expiration);
//
//
//                    invalidatedTokenRepository.save(invalidatedToken); // save to blacklist
//                }
//            } catch (Exception e) {
//                // No accessToken => expired || revoked
//            }
//        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Access token is required");
        }
        log.info("[LOGOUT] Authorization Header: {}", authHeader);

        String accessToken = authHeader.substring(7);
        String jti = jwtUtil.extractId(accessToken);
        Date expiration = jwtUtil.extractExpiration(accessToken);

        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(jti)
                        .expiryTime(expiration)
                        .build()
        );
    }

    private String createRefreshToken(User user) {
        // Delete all existing refresh tokens for this user
        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
