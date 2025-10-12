package com.example.UserModeratorSystem.service;

import com.example.UserModeratorSystem.constants.*;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.dto.ModeratorRequestDTO;
import com.example.UserModeratorSystem.dto.ReviewDTO;
import com.example.UserModeratorSystem.dto.UserDTO;
import com.example.UserModeratorSystem.entity.*;
import com.example.UserModeratorSystem.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    dto.setReviewedBy(request.getReviewedBy().getId() );
                    return dto;
                })
                .collect(Collectors.toList());
    }


    //  Approve moderator request
    public ModeratorRequestDTO approveModeratorRequest(Long requestId, User superAdmin) {
        ModeratorRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Moderator request not found"));

        if (request.getStatus() == RequestStatus.APPROVED)
            throw new RuntimeException("Request already approved");
        if (request.getStatus() == RequestStatus.REJECTED)
            throw new RuntimeException("Request already rejected");

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(superAdmin);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        // promote user role
        User user = request.getUser();
        if (user != null) {
            user.setRole(roleRepository.findByName(RoleName.MODERATOR));
            userRepository.save(user);
        }

        ModeratorRequestDTO dto = new ModeratorRequestDTO();
        dto.setId(request.getId());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setReviewedBy(superAdmin.getId());
        dto.setStatus(request.getStatus().name());
        return dto;
    }


    // Reject moderator request
    public ModeratorRequestDTO rejectModeratorRequest(Long requestId, User superAdmin) {
        ModeratorRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Moderator request not found"));

        if (request.getStatus() == RequestStatus.REJECTED)
            throw new RuntimeException("Request already rejected");
        if (request.getStatus() == RequestStatus.APPROVED)
            throw new RuntimeException("Request already approved");

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedBy(superAdmin);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

//        ModeratorRequestDTO dto = objectMapper.convertValue(request, ModeratorRequestDTO.class);
//        User user = request.getUser();
//        dto.setUserId(user!=null?user.getId():null);
//        dto.setReviewedBy( superAdmin!=null?superAdmin.getId():null);
//        dto.setUsername(user.getUsername());
//        dto.setStatus(request.getStatus().name());
//        return dto;

        ModeratorRequestDTO dto = new ModeratorRequestDTO();
        dto.setId(request.getId());
        dto.setUsername(request.getUser().getUsername());
        dto.setUserId(request.getUser().getId());
        dto.setStatus(request.getStatus().name());
        dto.setReviewedBy(superAdmin != null ? superAdmin.getId() : null);

        return dto;

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
                .orElseThrow(() -> new RuntimeException("User not found"));

         //  Prevent Super Admin from deleting themselves
        if (user.getId().equals(superAdmin.getId())) {
            throw new RuntimeException("Super Admin cannot delete themselves");
        }

        String roleName = user.getRole().getName().name();
        // Prevent deleting moderators or other super admins
        if (roleName.equals("MODERATOR") || roleName.equals("SUPER_ADMIN")) {
            throw new RuntimeException("Cannot delete a Moderator or Super Admin");
        }

        userRepository.delete(user);
        return "User deleted successfully";
    }


    //  Delete moderator
    public String deleteModerator(Long moderatorId) {
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new RuntimeException("Moderator not found"));

        if (moderator.getRole().getName() != RoleName.MODERATOR) {
            throw new RuntimeException("This user is not a moderator");
        }
        userRepository.delete(moderator);
        return "Moderator deleted successfully";
    }

}
