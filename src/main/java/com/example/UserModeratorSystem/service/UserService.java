package com.example.UserModeratorSystem.service;

import com.example.UserModeratorSystem.constants.*;
import com.example.UserModeratorSystem.dto.*;
import com.example.UserModeratorSystem.entity.*;
import com.example.UserModeratorSystem.exception.*;
import com.example.UserModeratorSystem.repository.*;
import com.example.UserModeratorSystem.security.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModeratorRequestRepository requestRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewLogRepository reviewLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public User register(RegisterDTO userDto){
        String normalizedEmail=userDto.getEmail().toLowerCase();
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user=objectMapper.convertValue(userDto,User.class);
        user.setEmail(normalizedEmail);
        user.setPassword(encoder.encode(userDto.getPassword()));
        Role defaultRole = roleRepository.findByName(RoleName.USER);
        user.setRole(defaultRole);
        return userRepository.save(user);
    }

    public LoginResponseDTO login(LoginDTO userDto) {
        User user = userRepository.findByEmailIgnoreCase(userDto.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("Email not found"));

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String jwt = jwtUtil.generateToken(userDto.getEmail().toLowerCase());
                String role = user.getRole().getName().name();
                return new LoginResponseDTO(user.getId(), user.getUsername(), user.getEmail(), jwt, role);
            } else {
                throw new InvalidPasswordException("Invalid password");
            }
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new InvalidPasswordException("Invalid password");
        }
    }


    //user profile
    public UserDTO getUserProfile(User user) {
        UserDTO profileDTO = new UserDTO();
        profileDTO.setId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setRole(user.getRole().getName().name());
        return profileDTO;

    }

    //moderator request(user wants to become a moderator)
    public ApiResponseDTO<ModeratorRequestDTO> createModeratorRequest(User user) {
        Optional<ModeratorRequest> existingRequest = requestRepository.findByUserAndStatus(user, RequestStatus.PENDING);
        if (existingRequest.isEmpty()) {
            existingRequest = requestRepository.findByUserAndStatus(user, RequestStatus.APPROVED);
        }
        ModeratorRequest request;
        String message;
        if (existingRequest.isPresent()) {
            request = existingRequest.get();
            message = request.getStatus() == RequestStatus.PENDING
                    ? "Moderator request already submitted"
                    : "User is already a moderator";
        } else {
            request = new ModeratorRequest();
            request.setUser(user);
            request.setStatus(RequestStatus.PENDING);
            request = requestRepository.save(request);
            message = "Moderator request created";
        }
        ModeratorRequestDTO dto = objectMapper.convertValue(request, ModeratorRequestDTO.class);
        dto.setUserId(request.getUser().getId());
        if (request.getReviewedBy() != null) {
            dto.setReviewedBy(request.getReviewedBy().getId());
        }
        dto.setUsername(request.getUser().getUsername());
        dto.setStatus(request.getStatus().name());
        return new ApiResponseDTO<>(true, message, dto);
    }


    //  Get all moderator requests
    public List<ModeratorRequestDTO> getModeratorRequestsByStatus(RequestStatus status) {
        return requestRepository.findAll()
                .stream()
                .filter(request -> status == null || request.getStatus() == status) // filter if status is provided
                .map(request -> {
                    ModeratorRequestDTO dto = new ModeratorRequestDTO();
                    dto.setId(request.getId());
                    dto.setUsername(request.getUser().getUsername());
                    dto.setUserId( request.getUser().getId());
                    dto.setStatus(request.getStatus().name());
                    if (request.getReviewedBy() != null) {
                        dto.setReviewedBy(request.getReviewedBy().getId());
                    } else {
                        dto.setReviewedBy(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }




    // Get all users
    public List<UserDTO> getAllUsers() {
        Role userRole = roleRepository.findByName(RoleName.USER);
        List<User> users = userRepository.findByRole(userRole);
        return users.stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().getName().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    // Get all moderators
    public List<UserDTO> getAllModerators() {
        Role moderatorRole = roleRepository.findByName(RoleName.MODERATOR);
        List<User> moderators = userRepository.findByRole(moderatorRole);
        return moderators.stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().getName().name()); // set role as string
                    return dto;
                })
                .collect(Collectors.toList());
    }



    // Delete user (only if they are a normal USER)
    public String deleteUser(Long userId, User superAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

         //  Prevent Super Admin from deleting themselves
        if (user.getId().equals(superAdmin.getId())) {
            throw new CustomException("Super Admin cannot delete themselves",HttpStatus.FORBIDDEN);
        }

        String roleName = user.getRole().getName().name();
        // Prevent deleting moderators or other super admins
        if (roleName.equals("MODERATOR") || roleName.equals("SUPER_ADMIN")) {
            throw new CustomException("Cannot delete a Moderator or Super Admin",HttpStatus.FORBIDDEN);
        }
        userRepository.delete(user);
        return "User deleted successfully";
    }


    //  Delete moderator
    public String deleteModerator(Long moderatorId) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new CustomException("Moderator not found",HttpStatus.NOT_FOUND));

        if (moderator.getRole().getName() != RoleName.MODERATOR) {
            throw new CustomException("This user is not a moderator",HttpStatus.FORBIDDEN);
        }
        userRepository.delete(moderator);
        return "Moderator deleted successfully";
    }

    //To Retire as Moderator
    public UserDTO retireAsModerator(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!existingUser.getRole().getName().name().equals("MODERATOR")) {
            throw new CustomException("You are not a moderator",HttpStatus.FORBIDDEN);
        }
        Role userRole = roleRepository.findByName(RoleName.USER);
        existingUser.setRole(userRole);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
        UserDTO dto = new UserDTO();
        dto.setId(existingUser.getId());
        dto.setUsername(existingUser.getUsername());
        dto.setEmail(existingUser.getEmail());
        dto.setRole(existingUser.getRole().getName().name());
        return dto;
    }

    public ModeratorRequestDTO reviewModeratorRequest(Long id, ReviewAction reviewAction, User superAdmin) {
        ModeratorRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new CustomException("Moderator Request Id not found", HttpStatus.NOT_FOUND));

        if (request.getStatus() == RequestStatus.APPROVED)
            throw new CustomException("Request Already Approved", HttpStatus.CONFLICT);
        if (request.getStatus() == RequestStatus.REJECTED)
            throw new CustomException("Request Already Rejected", HttpStatus.CONFLICT);

        if (reviewAction == ReviewAction.APPROVED) {
            request.setStatus(RequestStatus.APPROVED);
            //promote user to moderator
            User user = request.getUser();
            if (user != null) {
                user.setRole(roleRepository.findByName(RoleName.MODERATOR));
                userRepository.save(user);
            }
        } else if (reviewAction == ReviewAction.REJECTED) {
            request.setStatus(RequestStatus.REJECTED);
        }

        request.setReviewedBy(superAdmin);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        ModeratorRequestDTO dto =new ModeratorRequestDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setReviewedBy(superAdmin.getId());
        dto.setStatus(request.getStatus().name());
        dto.setUsername(request.getUser().getUsername());
        return dto;


    }
}
