package com.customersupport.controller;

import com.customersupport.dto.TicketRequestDto;
import com.customersupport.model.*;
import com.customersupport.service.EmployeeService;
import com.customersupport.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final EmployeeService employeeService;

    @Autowired
    public TicketController(TicketService ticketService, EmployeeService employeeService) {
        this.ticketService = ticketService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String listTickets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long employeeId,
            Model model) {

        List<Ticket> tickets = ticketService.searchTickets(keyword, status, priority, category, employeeId);

        model.addAttribute("tickets", tickets);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedEmployeeId", employeeId);

        model.addAttribute("allStatuses", Status.values());
        model.addAttribute("allPriorities", Priority.values());
        model.addAttribute("allCategories", Category.values());
        model.addAttribute("employees", employeeService.getActiveEmployees());
        model.addAttribute("activeNav", "tickets");

        return "tickets/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("ticketDto")) {
            model.addAttribute("ticketDto", new TicketRequestDto());
        }
        populateFormReferenceData(model);
        model.addAttribute("activeNav", "new-ticket");
        return "tickets/create";
    }

    @PostMapping("/new")
    public String createTicket(@Valid @ModelAttribute("ticketDto") TicketRequestDto ticketDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            populateFormReferenceData(model);
            model.addAttribute("activeNav", "new-ticket");
            return "tickets/create";
        }

        Ticket created = ticketService.createTicket(ticketDto, "Agent Web Portal");
        redirectAttributes.addFlashAttribute("successMessage", 
                "Ticket #" + created.getTicketId() + " created successfully!");
        return "redirect:/tickets/" + created.getId();
    }

    @GetMapping("/{id}")
    public String viewTicketDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ticketService.getTicketById(id).map(ticket -> {
            model.addAttribute("ticket", ticket);
            model.addAttribute("allStatuses", Status.values());
            model.addAttribute("allPriorities", Priority.values());
            model.addAttribute("employees", employeeService.getActiveEmployees());
            model.addAttribute("activeNav", "tickets");
            return "tickets/detail";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket not found with ID: " + id);
            return "redirect:/tickets";
        });
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ticketService.getTicketById(id).map(ticket -> {
            TicketRequestDto dto = new TicketRequestDto();
            dto.setId(ticket.getId());
            dto.setTicketId(ticket.getTicketId());
            dto.setTitle(ticket.getTitle());
            dto.setDescription(ticket.getDescription());
            dto.setCategory(ticket.getCategory());
            dto.setPriority(ticket.getPriority());
            dto.setStatus(ticket.getStatus());
            dto.setResolutionNotes(ticket.getResolutionNotes());
            if (ticket.getAssignedTo() != null) {
                dto.setAssignedEmployeeId(ticket.getAssignedTo().getId());
            }
            if (ticket.getCustomer() != null) {
                dto.setCustomerName(ticket.getCustomer().getName());
                dto.setCustomerEmail(ticket.getCustomer().getEmail());
                dto.setCustomerPhone(ticket.getCustomer().getPhone());
                dto.setCustomerCompany(ticket.getCustomer().getCompanyOrDept());
            }

            model.addAttribute("ticketDto", dto);
            populateFormReferenceData(model);
            model.addAttribute("activeNav", "tickets");
            return "tickets/edit";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket not found with ID: " + id);
            return "redirect:/tickets";
        });
    }

    @PostMapping("/{id}/edit")
    public String updateTicket(@PathVariable Long id,
                               @Valid @ModelAttribute("ticketDto") TicketRequestDto ticketDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            ticketDto.setId(id);
            populateFormReferenceData(model);
            model.addAttribute("activeNav", "tickets");
            return "tickets/edit";
        }

        Ticket updated = ticketService.updateTicket(id, ticketDto, "Support Specialist");
        redirectAttributes.addFlashAttribute("successMessage", 
                "Ticket #" + updated.getTicketId() + " updated successfully!");
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam Status status,
                               @RequestParam(required = false) String resolutionNotes,
                               RedirectAttributes redirectAttributes) {
        Ticket updated = ticketService.updateTicketStatus(id, status, resolutionNotes, "Support Agent");
        redirectAttributes.addFlashAttribute("successMessage", 
                "Status for #" + updated.getTicketId() + " updated to " + status.getDisplayName());
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignEmployee(@PathVariable Long id,
                                 @RequestParam(required = false) Long employeeId,
                                 RedirectAttributes redirectAttributes) {
        Ticket updated = ticketService.assignEmployee(id, employeeId, "Helpdesk Manager");
        redirectAttributes.addFlashAttribute("successMessage", 
                "Assignment updated for Ticket #" + updated.getTicketId());
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/priority")
    public String updatePriority(@PathVariable Long id,
                                 @RequestParam Priority priority,
                                 RedirectAttributes redirectAttributes) {
        Ticket updated = ticketService.updatePriority(id, priority, "Support Specialist");
        redirectAttributes.addFlashAttribute("successMessage", 
                "Priority for Ticket #" + updated.getTicketId() + " updated to " + priority.getDisplayName());
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ticketService.deleteTicket(id);
        redirectAttributes.addFlashAttribute("successMessage", "Ticket successfully deleted.");
        return "redirect:/tickets";
    }

    private void populateFormReferenceData(Model model) {
        model.addAttribute("allStatuses", Status.values());
        model.addAttribute("allPriorities", Priority.values());
        model.addAttribute("allCategories", Category.values());
        model.addAttribute("employees", employeeService.getActiveEmployees());
    }
}
