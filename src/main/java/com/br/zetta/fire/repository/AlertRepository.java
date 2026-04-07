package com.br.zetta.fire.repository;


import com.br.zetta.fire.data.entity.Alert;
import com.br.zetta.fire.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByUserListContaining(User user);

    List<Alert> findByFireEvent_IdFireEvent(UUID idFireEvent);

}
