package com.customersupport.controller;

import com.customersupport.model.Ticket;
import com.customersupport.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/track")
public class TrackTicketController {

    private final TicketService ticketService;

    @Autowired
    public TrackTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String showTrackPage(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("activeNav", "track");
        if (query != null && !query.trim().isBlank()) {
            return performTrack(query.trim(), model);
        }
        return "track/search";
    }

    @PostMapping
    public String searchTicket(@RequestParam("query") String query, Model model) {
        return performTrack(query, model);
    }

    private String performTrack(String query, Model model) {
        model.addAttribute("activeNav", "track");
        model.addAttribute("query", query);

        if (query == null || query.trim().isBlank()) {
            model.addAttribute("errorMessage", "Please enter a valid Ticket ID or Customer Email.");
            return "track/search";
        }

        String trimmed = query.trim();

        // 1. Try finding by Ticket ID
        Optional<Ticket> ticketOpt = ticketService.getTicketByTicketId(trimmed);
        if (ticketOpt.isPresent()) {
            model.addAttribute("ticket", ticketOpt.get());
            return "track/result";
        }

        // 2. Try finding by Email
        List<Ticket> ticketsByEmail = ticketService.getTicketsByCustomerEmail(trimmed);
        if (!ticketsByEmail.isEmpty()) {
            model.addAttribute("emailSearchResults", ticketsByEmail);
            return "track/search";
        }

        model.addAttribute("notFoundMessage", "No ticket found matching '" + trimmed + "'. Please check the Ticket ID and try again.");
        return "track/search";
    }
}
