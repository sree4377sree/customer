package com.customersupport;

import com.customersupport.dto.TicketRequestDto;
import com.customersupport.model.*;
import com.customersupport.repository.CustomerRepository;
import com.customersupport.repository.EmployeeRepository;
import com.customersupport.repository.TicketRepository;
import com.customersupport.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TicketService ticketService;

    private Customer mockCustomer;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer("John Doe", "john@example.com", "1234567890", "Test Corp");
        mockCustomer.setId(1L);

        mockEmployee = new Employee("Jane Agent", "jane@supportdesk.internal", "Support Engineer", "IT", "0987654321");
        mockEmployee.setId(10L);
    }

    @Test
    void testCreateTicket() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setTitle("Test Ticket");
        dto.setDescription("Issue details here");
        dto.setCategory(Category.TECHNICAL);
        dto.setPriority(Priority.HIGH);
        dto.setCustomerName("John Doe");
        dto.setCustomerEmail("john@example.com");
        dto.setAssignedEmployeeId(10L);

        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockCustomer));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(mockEmployee));
        when(ticketRepository.count()).thenReturn(5L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket created = ticketService.createTicket(dto, "Test Creator");

        assertNotNull(created);
        assertEquals("Test Ticket", created.getTitle());
        assertEquals(Priority.HIGH, created.getPriority());
        assertEquals(Status.OPEN, created.getStatus());
        assertEquals(mockCustomer, created.getCustomer());
        assertEquals(mockEmployee, created.getAssignedTo());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testUpdateStatus() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketId("TKT-1001");
        ticket.setTitle("Sample");
        ticket.setDescription("Sample desc");
        ticket.setStatus(Status.OPEN);
        ticket.setPriority(Priority.LOW);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket updated = ticketService.updateTicketStatus(100L, Status.RESOLVED, "Resolved by testing", "Support Lead");

        assertEquals(Status.RESOLVED, updated.getStatus());
        assertNotNull(updated.getResolvedAt());
        assertEquals("Resolved by testing", updated.getResolutionNotes());
        verify(ticketRepository, times(1)).save(ticket);
    }
}
