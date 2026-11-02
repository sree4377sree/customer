package com.customersupport.repository;

import com.customersupport.model.Category;
import com.customersupport.model.Employee;
import com.customersupport.model.Priority;
import com.customersupport.model.Status;
import com.customersupport.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketIdIgnoreCase(String ticketId);

    List<Ticket> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    List<Ticket> findTop5ByOrderByCreatedAtDesc();

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByAssignedTo(Employee employee);

    long countByStatus(Status status);

    long countByPriority(Priority priority);

    long countByCategory(Category category);

    long countByAssignedTo(Employee employee);

    long countByAssignedToAndStatus(Employee employee, Status status);

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(t.ticketId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.customer.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:employeeId IS NULL OR (t.assignedTo IS NOT NULL AND t.assignedTo.id = :employeeId)) " +
           "ORDER BY t.createdAt DESC")
    List<Ticket> searchTickets(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            @Param("priority") Priority priority,
            @Param("category") Category category,
            @Param("employeeId") Long employeeId
    );
}
