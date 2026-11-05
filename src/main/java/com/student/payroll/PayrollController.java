
package com.student.payroll;

import com.student.payroll.repository.EmployeeRepository;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PayrollController {

    private final EmployeeRepository employees;

    public PayrollController(EmployeeRepository employees) {
        this.employees = employees;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // DASHBOARD
    @GetMapping("/")
    public String dashboard(Model model) {
        List<Employee> records = employees.findAll();

        BigDecimal gross = records.stream()
                .map(Employee::getGrossSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = records.stream()
                .map(Employee::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("employees", records);
        model.addAttribute("employeeCount", records.size());
        model.addAttribute("totalGross", gross);
        model.addAttribute("totalPayroll", net);
        model.addAttribute("newEmployee", new Employee());

        return "index";
    }

    // EMPLOYEE LIST PAGE
    @GetMapping("/employees")
    public String employeeList(Model model) {
        List<Employee> records = employees.findAll();

        model.addAttribute("employees", records);
        model.addAttribute("employeeCount", records.size());

        return "employees";
    }

    // ADD EMPLOYEE PAGE
    @GetMapping("/employees/new")
    public String addEmployeePage(Model model) {
        if (!model.containsAttribute("newEmployee")) {
            model.addAttribute("newEmployee", new Employee());
        }

        return "add-employee";
    }

    // SAVE EMPLOYEE
    @PostMapping("/employees")
    public String add(
            @Valid @ModelAttribute("newEmployee") Employee employee,
            BindingResult result,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            redirect.addFlashAttribute(
                    "errorMessage",
                    result.getAllErrors().get(0).getDefaultMessage()
            );

            return "redirect:/employees/new";
        }

        if (employee.getNetSalary().compareTo(BigDecimal.ZERO) < 0) {
            redirect.addFlashAttribute(
                    "errorMessage",
                    "Deductions cannot be greater than gross pay."
            );

            return "redirect:/employees/new";
        }

        if (employees.existsById(employee.getEmployeeId())) {
            redirect.addFlashAttribute(
                    "errorMessage",
                    "That employee ID already exists."
            );

            return "redirect:/employees/new";
        }

        try {
            employees.save(employee);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Employee payroll saved successfully."
            );

        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute(
                    "errorMessage",
                    "Employee ID already exists."
            );

            return "redirect:/employees/new";
        }

        return "redirect:/employees";
    }

    // EMPLOYEE DETAILS PAGE
    @GetMapping("/employees/{id}")
    public String employeeDetails(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirect) {

        return employees.findById(id)
                .map(employee -> {
                    model.addAttribute("employee", employee);
                    return "employee-details";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute(
                            "errorMessage",
                            "Employee record not found."
                    );

                    return "redirect:/employees";
                });
    }

    // DELETE EMPLOYEE
    @PostMapping("/employees/delete/{id}")
    public String delete(
            @PathVariable String id,
            RedirectAttributes redirect) {

        if (employees.existsById(id)) {
            employees.deleteById(id);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Employee record deleted."
            );
        }

        return "redirect:/employees";
    }
}
