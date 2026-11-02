package com.customersupport.controller;

import com.customersupport.model.Employee;
import com.customersupport.service.DashboardService;
import com.customersupport.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DashboardService dashboardService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, DashboardService dashboardService) {
        this.employeeService = employeeService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        model.addAttribute("workloads", dashboardService.getEmployeeWorkload());
        model.addAttribute("activeNav", "employees");
        return "employees/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new Employee());
        }
        model.addAttribute("activeNav", "employees");
        return "employees/create";
    }

    @PostMapping("/new")
    public String createEmployee(@Valid @ModelAttribute("employee") Employee employee,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "employees");
            return "employees/create";
        }

        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute("successMessage", 
                "Support employee " + employee.getFullName() + " registered successfully!");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.toggleActiveStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Employee active status updated.");
        return "redirect:/employees";
    }
}
