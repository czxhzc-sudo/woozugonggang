package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {
}
