
package com.student.payroll;

import com.student.payroll.repository.EmployeeRepository;
import com.student.payroll.repository.StaffAccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffAccountRepository staffAccounts;
    private final EmployeeRepository employees;

    public StaffController(
            StaffAccountRepository staffAccounts,
            EmployeeRepository employees) {
        this.staffAccounts = staffAccounts;
        this.employees = employees;
    }

    private Employee getLoggedInEmployee(Authentication authentication) {
        StaffAccount account = staffAccounts
            .findByUsername(authentication.getName())
            .orElseThrow(() ->
                new IllegalStateException("Staff account not found.")
            );

        return employees.findById(account.getEmployeeId())
            .orElseThrow(() ->
                new IllegalStateException("Linked employee record not found.")
            );
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Employee employee = getLoggedInEmployee(authentication);

        model.addAttribute("employee", employee);

        return "staff-dashboard";
    }

    @GetMapping("/payslip")
    public String payslip(Authentication authentication, Model model) {
        Employee employee = getLoggedInEmployee(authentication);

        model.addAttribute("employee", employee);

        return "staff-payslip";
    }
}
