package com.customersupport.service;

import com.customersupport.dto.DashboardStatsDto;
import com.customersupport.model.*;
import com.customersupport.repository.EmployeeRepository;
import com.customersupport.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public DashboardService(TicketRepository ticketRepository, EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
    }

    public DashboardStatsDto getDashboardStatistics() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(Status.OPEN);
        long inProgress = ticketRepository.countByStatus(Status.IN_PROGRESS);
        long resolved = ticketRepository.countByStatus(Status.RESOLVED);
        long closed = ticketRepository.countByStatus(Status.CLOSED);
        long high = ticketRepository.countByPriority(Priority.HIGH);

        stats.setTotalTickets(total);
        stats.setOpenTickets(open);
        stats.setInProgressTickets(inProgress);
        stats.setResolvedTickets(resolved);
        stats.setClosedTickets(closed);
        stats.setHighPriorityTickets(high);

        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (Status s : Status.values()) {
            statusMap.put(s.getDisplayName(), ticketRepository.countByStatus(s));
        }
        stats.setStatusDistribution(statusMap);

        Map<String, Long> priorityMap = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            priorityMap.put(p.getDisplayName(), ticketRepository.countByPriority(p));
        }
        stats.setPriorityDistribution(priorityMap);

        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            categoryMap.put(c.getDisplayName(), ticketRepository.countByCategory(c));
        }
        stats.setCategoryDistribution(categoryMap);

        return stats;
    }

    public List<Map<String, Object>> getEmployeeWorkload() {
        List<Employee> employees = employeeRepository.findAllByOrderByFullNameAsc();
        List<Map<String, Object>> workloadList = new ArrayList<>();

        for (Employee emp : employees) {
            long totalAssigned = ticketRepository.countByAssignedTo(emp);
            long resolvedCount = ticketRepository.countByAssignedToAndStatus(emp, Status.RESOLVED)
                               + ticketRepository.countByAssignedToAndStatus(emp, Status.CLOSED);
            long activeCount = totalAssigned - resolvedCount;

            Map<String, Object> map = new HashMap<>();
            map.put("employee", emp);
            map.put("totalAssigned", totalAssigned);
            map.put("activeCount", activeCount);
            map.put("resolvedCount", resolvedCount);
            workloadList.add(map);
        }

        return workloadList;
    }
}
