package com.student.payroll;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @NotBlank(message = "Employee ID is required")
    @Size(max = 20)
    @Column(name = "employee_id", length = 20, nullable = false, unique = true)
    private String employeeId;

    @NotBlank(message = "Full name is required")
    @Size(max = 80)
    @Column(name = "full_name", length = 80, nullable = false)
    private String fullName;

    @NotBlank(message = "Position is required")
    @Size(max = 60)
    @Column(length = 60, nullable = false)
    private String position;

    @NotNull @DecimalMin(value = "0.00", message = "Daily rate cannot be negative")
    @Column(name = "daily_rate", precision = 12, scale = 2, nullable = false)
    private BigDecimal dailyRate = BigDecimal.ZERO;

    @Min(0) @Max(31)
    @Column(name = "days_worked", nullable = false)
    private int daysWorked;

    @NotNull @DecimalMin("0.00")
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal sss = BigDecimal.ZERO;

    @NotNull @DecimalMin("0.00")
    @Column(name = "phil_health", precision = 12, scale = 2, nullable = false)
    private BigDecimal philHealth = BigDecimal.ZERO;

    @NotNull @DecimalMin("0.00")
    @Column(name = "pag_ibig", precision = 12, scale = 2, nullable = false)
    private BigDecimal pagIbig = BigDecimal.ZERO;

    protected Employee() { }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId == null ? null : employeeId.trim().toUpperCase(); }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName == null ? null : fullName.trim(); }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position == null ? null : position.trim(); }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = money(dailyRate); }
    public int getDaysWorked() { return daysWorked; }
    public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }
    public BigDecimal getSss() { return sss; }
    public void setSss(BigDecimal sss) { this.sss = money(sss); }
    public BigDecimal getPhilHealth() { return philHealth; }
    public void setPhilHealth(BigDecimal philHealth) { this.philHealth = money(philHealth); }
    public BigDecimal getPagIbig() { return pagIbig; }
    public void setPagIbig(BigDecimal pagIbig) { this.pagIbig = money(pagIbig); }

    public BigDecimal getGrossSalary() {
        return money(dailyRate.multiply(BigDecimal.valueOf(daysWorked)));
    }
    public BigDecimal getTotalDeductions() {
        return money(sss.add(philHealth).add(pagIbig));
    }
    public BigDecimal getNetSalary() {
        return money(getGrossSalary().subtract(getTotalDeductions()));
    }
    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
