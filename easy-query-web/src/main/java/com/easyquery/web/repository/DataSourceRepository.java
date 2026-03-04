package com.easyquery.web.repository;

import com.easyquery.web.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceEntity, Long> {
    Optional<DataSourceEntity> findByName(String name);
    boolean existsByName(String name);
}
