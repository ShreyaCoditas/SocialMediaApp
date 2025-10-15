package com.example.UserModeratorSystem.repository;

import com.example.UserModeratorSystem.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comment, Long> {

}


