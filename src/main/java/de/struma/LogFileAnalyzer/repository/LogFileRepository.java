package de.struma.LogFileAnalyzer.repository;

import de.struma.LogFileAnalyzer.model.LogFileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogFileRepository extends JpaRepository<LogFileModel, Integer> {

    LogFileModel getById(Long id);
    List<LogFileModel> findByActivateTrackingTrue();
}
