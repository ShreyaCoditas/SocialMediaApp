package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.entity.Comment;
import com.example.UserModeratorSystem.constants.Status;
import com.example.UserModeratorSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}


