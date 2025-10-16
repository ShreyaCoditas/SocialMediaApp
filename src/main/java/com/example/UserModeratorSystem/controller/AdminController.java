package com.example.UserModeratorSystem.controller;

import com.example.UserModeratorSystem.constants.ReviewAction;
import com.example.UserModeratorSystem.dto.ModeratorRequestDTO;
import com.example.UserModeratorSystem.dto.ApiResponseDTO;
import com.example.UserModeratorSystem.constants.RequestStatus;
import com.example.UserModeratorSystem.dto.ReviewActionDTO;
import com.example.UserModeratorSystem.entity.User;
import com.example.UserModeratorSystem.security.UserPrincipal;
import com.example.UserModeratorSystem.service.CommentService;
import com.example.UserModeratorSystem.service.PostService;
import com.example.UserModeratorSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    //  View all moderator requests
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/moderator-requests")
    public ResponseEntity<ApiResponseDTO<List<ModeratorRequestDTO>>> getAllModeratorRequests(
            @RequestParam(value = "status", required = false) RequestStatus status) {

        List<ModeratorRequestDTO> requests = userService.getModeratorRequestsByStatus(status);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Fetched moderator requests", requests));
    }


//    // Approve moderator request
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping("/moderator-requests/{id}/approve")
//    public ResponseEntity<ApiResponseDTO<ModeratorRequestDTO>> approveModerator(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserPrincipal userPrincipal) {
//
//        User superAdmin = userPrincipal.getUser();
//        ModeratorRequestDTO dto = userService.approveModeratorRequest(id, superAdmin);
//        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Moderator request approved", dto));
//    }

//    //  Reject moderator request
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @PostMapping("/moderator-requests/{id}/reject")
//    public ResponseEntity<ApiResponseDTO<ModeratorRequestDTO>> rejectModerator(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserPrincipal userPrincipal) {
//
//        User superAdmin = userPrincipal.getUser();
//        ModeratorRequestDTO dto = userService.rejectModeratorRequest(id, superAdmin);
//        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Moderator request rejected", dto));
//    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/moderator-requests/{id}/review")
    public ResponseEntity<ApiResponseDTO<ModeratorRequestDTO>> reviewModeratorRequest(
            @PathVariable Long id,
            @RequestBody ReviewActionDTO reviewActionDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        User superAdmin=userPrincipal.getUser();
        ModeratorRequestDTO moderatorRequestDTO=userService.reviewModeratorRequest(id,reviewActionDTO.getReviewAction(),superAdmin);
        String message= (reviewActionDTO.getReviewAction()== ReviewAction.APPROVED)
                ?"Moderator Request Approved"
                :"Moderator Request Rejected";

        return ResponseEntity.ok(new ApiResponseDTO<>(true,message,moderatorRequestDTO));
    }


    // Delete a user
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponseDTO<String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User superAdmin = userPrincipal.getUser();
        String message = userService.deleteUser(id, superAdmin);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, message));
    }


    //  Delete a moderator
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/moderators/{id}")
    public ResponseEntity<ApiResponseDTO<String>> deleteModerator(@PathVariable Long id) {
        String message = userService.deleteModerator(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, message));
    }

}
