package com.easyquery.web.repository;

import com.easyquery.web.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 平台用户仓库
 *
 * @author easy-query
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 按手机号查找用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * 判断手机号是否已注册
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
}
