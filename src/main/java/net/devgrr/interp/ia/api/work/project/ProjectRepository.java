package net.devgrr.interp.ia.api.work.project;

import java.util.List;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findAllByIsDeletedFalse();

  List<Project> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
