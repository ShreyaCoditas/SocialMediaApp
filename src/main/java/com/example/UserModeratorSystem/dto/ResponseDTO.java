package com.example.UserModeratorSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String token;
    private String role;
}





//public ResponseDTO(String token) {
//    this.token = token;
//}