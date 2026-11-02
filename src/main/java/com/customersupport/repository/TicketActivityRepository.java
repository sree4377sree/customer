package com.customersupport.repository;

import com.customersupport.model.Ticket;
import com.customersupport.model.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {
    List<TicketActivity> findByTicketOrderByTimestampDesc(Ticket ticket);
}
