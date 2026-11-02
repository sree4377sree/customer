package com.customersupport.controller;

import com.customersupport.service.DashboardService;
import com.customersupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;

    @Autowired
    public DashboardController(DashboardService dashboardService, TicketService ticketService) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStatistics());
        model.addAttribute("recentTickets", ticketService.getRecentTickets());
        model.addAttribute("workloads", dashboardService.getEmployeeWorkload());
        model.addAttribute("activeNav", "dashboard");
        return "dashboard/index";
    }
}
