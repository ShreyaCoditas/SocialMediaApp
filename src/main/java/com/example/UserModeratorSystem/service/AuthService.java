package com.example.UserModeratorSystem.service;

import com.example.UserModeratorSystem.dto.RegisterDTO;
import com.example.UserModeratorSystem.dto.ResponseDTO;
import com.example.UserModeratorSystem.entity.Role;
import com.example.UserModeratorSystem.constants.RoleName;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.dto.LoginDTO;
import com.example.UserModeratorSystem.exception.UserAlreadyExistsException;
import com.example.UserModeratorSystem.repository.RoleRepository;
import com.example.UserModeratorSystem.repository.UserRepository;
import com.example.UserModeratorSystem.security.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private ObjectMapper objectMapper;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public User register(RegisterDTO userDto){

        if (userRepository.findByUsername(userDto.getUsername()) != null) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        User user=objectMapper.convertValue(userDto,User.class);
        user.setPassword(encoder.encode(userDto.getPassword()));

        Role defaultRole = roleRepository.findByName(RoleName.USER);
        user.setRole(defaultRole);
        return userRepository.save(user);
    }



    public ResponseDTO verify(LoginDTO userDto) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
        );

        if (authentication.isAuthenticated()) {

            String jwt = jwtUtil.generateToken(userDto.getEmail());

            User user = userRepository.findByEmail(userDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String role = user.getRole().getName().name();

            return new ResponseDTO(user.getId(),user.getUsername(),user.getEmail(),jwt,role);
        } else {
            throw new RuntimeException("Invalid Credentials");
        }
    }
}



