package de.struma.LogFileAnalyzer.repository;

import java.util.List;

import de.struma.LogFileAnalyzer.model.LogFileEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface LogFileEntryRepository extends JpaRepository<LogFileEntryModel, Integer>{

	List<LogFileEntryModel> findAllByStatusError(String string);
	List<LogFileEntryModel> findAllByApplicationAndStatusError(String application, String statusError);
	@Query("select l from LogFileEntryModel l where upper(l.application) like upper(?1)")
	List<LogFileEntryModel> getAllByApplicationName(@NonNull String application);

	LogFileEntryModel findById(Long idFromPreviousEntry);

}
