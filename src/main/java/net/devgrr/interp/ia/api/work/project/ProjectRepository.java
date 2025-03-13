package net.devgrr.interp.ia.api.work.project;

import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository
    extends JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project>,
        QuerydslPredicateExecutor<Project> {}
