package com.shubham.flashsale.user.repository;

import com.shubham.flashsale.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUuid(String uuid);

    Optional<User> findByEmail(String email);


}