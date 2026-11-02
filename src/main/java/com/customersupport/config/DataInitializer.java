package com.customersupport.config;

import com.customersupport.model.*;
import com.customersupport.repository.CustomerRepository;
import com.customersupport.repository.EmployeeRepository;
import com.customersupport.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public DataInitializer(EmployeeRepository employeeRepository,
                           CustomerRepository customerRepository,
                           TicketRepository ticketRepository) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (ticketRepository.count() > 0) {
            return;
        }

        List<Employee> emps = employeeRepository.findAll();
        if (emps.isEmpty()) {
            Employee emp1 = new Employee("Sarah Connor", "sarah.connor@supportdesk.internal", "Senior Cloud Engineer", "Infrastructure Support", "+1 (555) 234-5671");
            Employee emp2 = new Employee("Michael Scott", "michael.scott@supportdesk.internal", "Customer Success Lead", "Customer Care", "+1 (555) 345-6782");
            Employee emp3 = new Employee("Priya Sharma", "priya.sharma@supportdesk.internal", "Senior Software Specialist", "Application Tier", "+1 (555) 456-7893");
            Employee emp4 = new Employee("David Chen", "david.chen@supportdesk.internal", "Billing & Account Specialist", "Billing Operations", "+1 (555) 567-8904");
            emps = employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4));
        }

        List<Customer> custs = customerRepository.findAll();
        if (custs.isEmpty()) {
            Customer cust1 = new Customer("Alice Walker", "alice.walker@acmeglobal.com", "+1 (555) 111-2233", "Acme Global Industries");
            Customer cust2 = new Customer("Robert Taylor", "robert.t@starlightlogistics.com", "+1 (555) 222-3344", "Starlight Logistics");
            Customer cust3 = new Customer("Elena Rostova", "elena@novatech.io", "+1 (555) 333-4455", "NovaTech Innovations");
            Customer cust4 = new Customer("Marcus Vance", "m.vance@apexhealth.org", "+1 (555) 444-5566", "Apex Healthcare");
            Customer cust5 = new Customer("Samantha Reed", "sam.reed@quantummedia.net", "+1 (555) 555-6677", "Quantum Media Labs");
            custs = customerRepository.saveAll(List.of(cust1, cust2, cust3, cust4, cust5));
        }

        // 3. Create Support Tickets
        Ticket t1 = new Ticket();
        t1.setTicketId("TKT-1001");
        t1.setTitle("Critical: Intermittent VPN gateway drops from secondary site");
        t1.setDescription("Our branch office employees in Chicago are getting disconnected from the internal VPN every 15 minutes. Ping tests show sporadic gateway timeouts.");
        t1.setCategory(Category.NETWORK);
        t1.setPriority(Priority.HIGH);
        t1.setStatus(Status.IN_PROGRESS);
        t1.setCustomer(custs.get(0));
        t1.setAssignedTo(emps.get(0));
        t1.addActivity("CREATED", "Ticket submitted by customer via portal.", "Alice Walker");
        t1.addActivity("ASSIGNED", "Assigned to Sarah Connor (Infrastructure Support)", "System");
        t1.addActivity("STATUS_CHANGED", "Status changed from Open to In Progress while investigating IPsec logs.", "Sarah Connor");

        Ticket t2 = new Ticket();
        t2.setTicketId("TKT-1002");
        t2.setTitle("Duplicate charge observed on quarterly cloud enterprise invoice");
        t2.setDescription("Invoice #INV-9824 shows a duplicate line item for compute cluster licenses amounting to $1,450. Please review and apply credit.");
        t2.setCategory(Category.BILLING);
        t2.setPriority(Priority.MEDIUM);
        t2.setStatus(Status.OPEN);
        t2.setCustomer(custs.get(1));
        t2.setAssignedTo(emps.get(3));
        t2.addActivity("CREATED", "Ticket submitted by Robert Taylor.", "Robert Taylor");
        t2.addActivity("ASSIGNED", "Assigned to David Chen for accounting verification.", "System");

        Ticket t3 = new Ticket();
        t3.setTicketId("TKT-1003");
        t3.setTitle("Database latency spike during end-of-month report generation");
        t3.setDescription("Queries against the analytics replica are timing out after 60 seconds during end-of-month consolidation. Deadlocks noted in query buffer.");
        t3.setCategory(Category.TECHNICAL);
        t3.setPriority(Priority.HIGH);
        t3.setStatus(Status.OPEN);
        t3.setCustomer(custs.get(2));
        t3.setAssignedTo(emps.get(2));
        t3.addActivity("CREATED", "Customer reported high latency and deadlocks.", "Elena Rostova");
        t3.addActivity("ASSIGNED", "Assigned to Priya Sharma.", "System");

        Ticket t4 = new Ticket();
        t4.setTicketId("TKT-1004");
        t4.setTitle("MFA reset required for lead clinical administrator");
        t4.setDescription("Administrator replaced their company mobile device and lost authenticator seed. Needs identity verification and authenticator re-enrollment.");
        t4.setCategory(Category.ACCOUNT_ACCESS);
        t4.setPriority(Priority.HIGH);
        t4.setStatus(Status.RESOLVED);
        t4.setCustomer(custs.get(3));
        t4.setAssignedTo(emps.get(1));
        t4.setResolvedAt(LocalDateTime.now().minusHours(2));
        t4.setResolutionNotes("Identity verified via secondary manager callback. Temporary one-time passcode generated and MFA successfully re-enrolled.");
        t4.addActivity("CREATED", "Ticket created by Marcus Vance.", "Marcus Vance");
        t4.addActivity("ASSIGNED", "Assigned to Michael Scott.", "System");
        t4.addActivity("STATUS_CHANGED", "Resolved: Temporary passcode supplied and verified.", "Michael Scott");

        Ticket t5 = new Ticket();
        t5.setTicketId("TKT-1005");
        t5.setTitle("Inquiry regarding Okta SAML 2.0 single sign-on integration guide");
        t5.setDescription("We are planning to transition our organization to Okta identity federation next quarter. Looking for technical docs and certificate requirements.");
        t5.setCategory(Category.GENERAL_INQUIRY);
        t5.setPriority(Priority.LOW);
        t5.setStatus(Status.OPEN);
        t5.setCustomer(custs.get(4));
        t5.setAssignedTo(null);
        t5.addActivity("CREATED", "General inquiry received.", "Samantha Reed");

        Ticket t6 = new Ticket();
        t6.setTicketId("TKT-1006");
        t6.setTitle("Feature request: Bulk CSV export for monthly incident logs");
        t6.setDescription("Requesting an automated export button or scheduled report delivered via email at 00:00 UTC on the 1st of every month.");
        t6.setCategory(Category.TECHNICAL);
        t6.setPriority(Priority.LOW);
        t6.setStatus(Status.RESOLVED);
        t6.setCustomer(custs.get(0));
        t6.setAssignedTo(emps.get(2));
        t6.setResolvedAt(LocalDateTime.now().minusDays(1));
        t6.setResolutionNotes("Configured scheduled CSV report in the analytics pipeline. Customer notified with sample output.");
        t6.addActivity("CREATED", "Feature request submitted.", "Alice Walker");
        t6.addActivity("STATUS_CHANGED", "Marked as Resolved with automated pipeline configured.", "Priya Sharma");

        ticketRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6));
    }
}
