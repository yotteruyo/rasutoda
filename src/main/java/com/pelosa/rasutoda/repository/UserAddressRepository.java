package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
}
