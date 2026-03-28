package com.easyquery.web.repository;

import com.easyquery.web.entity.SqlQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SqlQueryRepository extends JpaRepository<SqlQueryEntity, Long> {
    List<SqlQueryEntity> findByDataSourceName(String dataSourceName);
    Optional<SqlQueryEntity> findByName(String name);
    boolean existsByName(String name);
}
