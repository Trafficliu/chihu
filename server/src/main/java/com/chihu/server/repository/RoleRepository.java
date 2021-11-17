package com.chihu.server.repository;

import com.chihu.server.model.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    @Query("SELECT role FROM Role role WHERE role.name=:roleName")
    public Role findByName(@Param("roleName") String roleName);
}
