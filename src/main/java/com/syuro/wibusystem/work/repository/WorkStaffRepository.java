package com.syuro.wibusystem.work.repository;

import com.syuro.wibusystem.work.entity.WorkStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkStaffRepository extends JpaRepository<WorkStaff, Long> {
}
