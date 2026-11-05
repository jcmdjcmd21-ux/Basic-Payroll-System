
package com.student.payroll.repository;

import com.student.payroll.StaffAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StaffAccountRepository
        extends JpaRepository<StaffAccount, Long> {

    Optional<StaffAccount> findByUsername(String username);

    Optional<StaffAccount> findByEmployeeId(String employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(String employeeId);
}
