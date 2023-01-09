package de.struma.LogFileAnalyzer.service;

import de.struma.LogFileAnalyzer.model.LogFileEntryModel;
import de.struma.LogFileAnalyzer.repository.LogFileEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class LogEntryReaderService {
    LogFileEntryRepository logFileEntryRepository;

    public LogEntryReaderService(LogFileEntryRepository logFileEntryRepository){
        this.logFileEntryRepository = logFileEntryRepository;

    }


    public Map<String, Integer> getMainChartData() {

        Map<String, Integer> result = new TreeMap<>();
        result.put("Debug",logFileEntryRepository.findAllByStatusError("DEBUG").size());
        result.put("Errors",logFileEntryRepository.findAllByStatusError("ERROR").size());
        result.put("Warning",logFileEntryRepository.findAllByStatusError("WARN").size());
        result.put("Info",logFileEntryRepository.findAllByStatusError("INFO").size());
        return result;
    }

    public List<LogFileEntryModel> findAll() {
        return logFileEntryRepository.findAll();
    }

    public void deleteAll() {
        logFileEntryRepository.deleteAll();
    }
}
