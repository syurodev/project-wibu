package com.syuro.wibusystem.work.repository;

import com.syuro.wibusystem.work.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {
}
