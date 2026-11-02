package com.customersupport.controller;

import com.customersupport.dto.DashboardStatsDto;
import com.customersupport.model.Ticket;
import com.customersupport.service.DashboardService;
import com.customersupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TicketApiController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;

    @Autowired
    public TicketApiController(DashboardService dashboardService, TicketService ticketService) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStatistics());
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable String ticketId) {
        return ticketService.getTicketByTicketId(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
