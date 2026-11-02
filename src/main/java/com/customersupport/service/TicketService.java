package com.customersupport.service;

import com.customersupport.dto.TicketRequestDto;
import com.customersupport.model.*;
import com.customersupport.repository.CustomerRepository;
import com.customersupport.repository.EmployeeRepository;
import com.customersupport.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    private static final AtomicLong SEQUENCE = new AtomicLong(1001);

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                         CustomerRepository customerRepository,
                         EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
    }

    public synchronized String generateNextTicketId() {
        long count = ticketRepository.count();
        long nextVal = Math.max(SEQUENCE.get(), count + 1001);
        SEQUENCE.set(nextVal + 1);
        return "TKT-" + nextVal;
    }

    public Ticket createTicket(TicketRequestDto dto, String creator) {
        // Find or create customer
        Customer customer = customerRepository.findByEmail(dto.getCustomerEmail().trim().toLowerCase())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(
                            dto.getCustomerName().trim(),
                            dto.getCustomerEmail().trim().toLowerCase(),
                            dto.getCustomerPhone() != null ? dto.getCustomerPhone().trim() : null,
                            dto.getCustomerCompany() != null ? dto.getCustomerCompany().trim() : null
                    );
                    return customerRepository.save(newCustomer);
                });

        // Update customer details if changed
        if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank()) {
            customer.setName(dto.getCustomerName().trim());
        }
        if (dto.getCustomerPhone() != null && !dto.getCustomerPhone().isBlank()) {
            customer.setPhone(dto.getCustomerPhone().trim());
        }
        if (dto.getCustomerCompany() != null && !dto.getCustomerCompany().isBlank()) {
            customer.setCompanyOrDept(dto.getCustomerCompany().trim());
        }
        customerRepository.save(customer);

        Ticket ticket = new Ticket();
        ticket.setTicketId(generateNextTicketId());
        ticket.setTitle(dto.getTitle().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setCategory(dto.getCategory() != null ? dto.getCategory() : Category.TECHNICAL);
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
        ticket.setStatus(Status.OPEN);
        ticket.setCustomer(customer);

        // Assign employee if specified
        if (dto.getAssignedEmployeeId() != null) {
            employeeRepository.findById(dto.getAssignedEmployeeId()).ifPresent(ticket::setAssignedTo);
        }

        ticket.addActivity(
                "CREATED",
                "Ticket opened with priority " + ticket.getPriority().getDisplayName() +
                (ticket.getAssignedTo() != null ? " and assigned to " + ticket.getAssignedTo().getFullName() : " (Unassigned)"),
                creator != null ? creator : "System"
        );

        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(Long id, TicketRequestDto dto, String updatedBy) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with ID " + id + " not found"));

        ticket.setTitle(dto.getTitle().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setCategory(dto.getCategory());
        
        Priority oldPriority = ticket.getPriority();
        if (oldPriority != dto.getPriority()) {
            ticket.setPriority(dto.getPriority());
            ticket.addActivity("PRIORITY_CHANGED", "Priority updated from " + oldPriority.getDisplayName() + " to " + dto.getPriority().getDisplayName(), updatedBy);
        }

        // Reassignment check
        Employee oldEmp = ticket.getAssignedTo();
        if (dto.getAssignedEmployeeId() != null) {
            Employee newEmp = employeeRepository.findById(dto.getAssignedEmployeeId()).orElse(null);
            if (newEmp != null && (oldEmp == null || !oldEmp.getId().equals(newEmp.getId()))) {
                ticket.setAssignedTo(newEmp);
                ticket.addActivity("REASSIGNED", "Reassigned to " + newEmp.getFullName(), updatedBy);
            }
        } else if (oldEmp != null) {
            ticket.setAssignedTo(null);
            ticket.addActivity("UNASSIGNED", "Ticket unassigned from " + oldEmp.getFullName(), updatedBy);
        }

        // Status check
        if (dto.getStatus() != null && dto.getStatus() != ticket.getStatus()) {
            updateStatusInternal(ticket, dto.getStatus(), dto.getResolutionNotes(), updatedBy);
        }

        return ticketRepository.save(ticket);
    }

    public Ticket updateTicketStatus(Long id, Status newStatus, String resolutionNotes, String updatedBy) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with ID " + id + " not found"));
        updateStatusInternal(ticket, newStatus, resolutionNotes, updatedBy);
        return ticketRepository.save(ticket);
    }

    private void updateStatusInternal(Ticket ticket, Status newStatus, String resolutionNotes, String updatedBy) {
        Status oldStatus = ticket.getStatus();
        if (oldStatus == newStatus && (resolutionNotes == null || resolutionNotes.isBlank())) {
            return;
        }

        ticket.setStatus(newStatus);
        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
            if (resolutionNotes != null && !resolutionNotes.isBlank()) {
                ticket.setResolutionNotes(resolutionNotes.trim());
            }
        }

        String noteText = (resolutionNotes != null && !resolutionNotes.isBlank()) ? " [Resolution: " + resolutionNotes.trim() + "]" : "";
        ticket.addActivity("STATUS_CHANGED", "Status changed from " + oldStatus.getDisplayName() + " to " + newStatus.getDisplayName() + noteText, updatedBy);
    }

    public Ticket assignEmployee(Long ticketId, Long employeeId, String assignedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with ID " + ticketId + " not found"));

        if (employeeId == null || employeeId == 0) {
            Employee previous = ticket.getAssignedTo();
            ticket.setAssignedTo(null);
            ticket.addActivity("UNASSIGNED", "Ticket unassigned" + (previous != null ? " from " + previous.getFullName() : ""), assignedBy);
        } else {
            Employee emp = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee with ID " + employeeId + " not found"));
            ticket.setAssignedTo(emp);
            ticket.addActivity("ASSIGNED", "Assigned to " + emp.getFullName() + " (" + emp.getRole() + ")", assignedBy);
        }

        return ticketRepository.save(ticket);
    }

    public Ticket updatePriority(Long ticketId, Priority newPriority, String updatedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with ID " + ticketId + " not found"));

        Priority oldPriority = ticket.getPriority();
        if (oldPriority != newPriority) {
            ticket.setPriority(newPriority);
            ticket.addActivity("PRIORITY_CHANGED", "Priority changed from " + oldPriority.getDisplayName() + " to " + newPriority.getDisplayName(), updatedBy);
            return ticketRepository.save(ticket);
        }
        return ticket;
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketByTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) return Optional.empty();
        return ticketRepository.findByTicketIdIgnoreCase(ticketId.trim());
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByCustomerEmail(String email) {
        if (email == null || email.isBlank()) return List.of();
        return ticketRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email.trim());
    }

    @Transactional(readOnly = true)
    public List<Ticket> searchTickets(String keyword, Status status, Priority priority, Category category, Long employeeId) {
        return ticketRepository.searchTickets(
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                status,
                priority,
                category,
                employeeId
        );
    }

    @Transactional(readOnly = true)
    public List<Ticket> getRecentTickets() {
        return ticketRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
