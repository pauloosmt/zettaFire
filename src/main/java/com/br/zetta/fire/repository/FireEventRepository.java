package com.br.zetta.fire.repository;

import com.br.zetta.fire.data.entity.FireEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FireEventRepository extends JpaRepository<FireEvent, UUID> {
}
