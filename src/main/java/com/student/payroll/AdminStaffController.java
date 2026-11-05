
package com.student.payroll;

import com.student.payroll.repository.EmployeeRepository;
import com.student.payroll.repository.StaffAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private final StaffAccountRepository staffAccounts;
    private final EmployeeRepository employees;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;

    public AdminStaffController(
            StaffAccountRepository staffAccounts,
            EmployeeRepository employees,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:admin}") String adminUsername) {

        this.staffAccounts = staffAccounts;
        this.employees = employees;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
    }

    @GetMapping("/new")
    public String newStaffAccount(Model model) {
        model.addAttribute("staffForm", new StaffAccountForm());
        model.addAttribute("employees", employees.findAll());

        return "add-staff-account";
    }

    @PostMapping
    public String createStaffAccount(
            @ModelAttribute("staffForm") StaffAccountForm form,
            Model model,
            RedirectAttributes redirect) {

        String username = form.getUsername() == null
            ? "" : form.getUsername().trim();

        String password = form.getPassword() == null
            ? "" : form.getPassword();

        String employeeId = form.getEmployeeId() == null
            ? "" : form.getEmployeeId().trim().toUpperCase();

        if (username.isBlank() || password.isBlank() || employeeId.isBlank()) {
            model.addAttribute("errorMessage", "Please complete all fields.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        if (username.equalsIgnoreCase(adminUsername)) {
            model.addAttribute("errorMessage", "That username is reserved.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        if (password.length() < 8) {
            model.addAttribute("errorMessage",
                "Staff passwords must be at least 8 characters.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        if (staffAccounts.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Username already exists.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        if (!employees.existsById(employeeId)) {
            model.addAttribute("errorMessage", "Please select a valid employee.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        if (staffAccounts.existsByEmployeeId(employeeId)) {
            model.addAttribute("errorMessage",
                "This employee already has a Staff account.");
            model.addAttribute("staffForm", form);
            model.addAttribute("employees", employees.findAll());
            return "add-staff-account";
        }

        StaffAccount account = new StaffAccount(
            username,
            passwordEncoder.encode(password),
            employeeId
        );

        staffAccounts.save(account);

        redirect.addFlashAttribute(
            "successMessage",
            "Staff account created successfully."
        );

        return "redirect:/employees";
    }
}
