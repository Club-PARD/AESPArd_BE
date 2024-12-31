package com.pard.pree_be.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pard.pree_be.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {

}
