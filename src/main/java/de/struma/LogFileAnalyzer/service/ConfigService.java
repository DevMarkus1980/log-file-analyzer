package de.struma.LogFileAnalyzer.service;

import de.struma.LogFileAnalyzer.model.AdvancedConfigModel;
import de.struma.LogFileAnalyzer.model.LogFileModel;
import de.struma.LogFileAnalyzer.repository.AdvancedConfigRepository;
import de.struma.LogFileAnalyzer.repository.LogFileRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class ConfigService {

	LogFileRepository logFileRepository;
	AdvancedConfigRepository advancedConfigRepository;

	public ConfigService(LogFileRepository logFileRepository, AdvancedConfigRepository advancedConfigRepository){
		this.logFileRepository = logFileRepository;
		this.advancedConfigRepository = advancedConfigRepository;
	}

	public Iterable<LogFileModel> getSelectedLogFilesBeforeInit(){
		return logFileRepository.findAll();
	}

	public void save(List<LogFileModel> setDTOFiles) {

		for(LogFileModel update : setDTOFiles){
			LogFileModel getFromRepo = logFileRepository.getById(update.getId());
			getFromRepo.setActivateTracking(update.getActivateTracking());
			logFileRepository.save(getFromRepo);
		}

	}

	public AdvancedConfigModel getAdvisedConfigFromService() {
		if(advancedConfigRepository.count()<1){
			return advancedConfigRepository.save(new AdvancedConfigModel());
		}
		return advancedConfigRepository.getById(1l);
	}

	public void saveAdvisedConfigToService(AdvancedConfigModel advisedConfig) {
		advancedConfigRepository.save(advisedConfig);
	}

	public void setAdvancedConfigToDefault() {
		advancedConfigRepository.save(new AdvancedConfigModel());
	}

	public boolean saveAdvancedConfigAndCheckIfConfigChanged(AdvancedConfigModel advisedConfig) {
		if(!advancedConfigRepository.getById(1L).equals(advisedConfig)) {
			advancedConfigRepository.save(advisedConfig);
			return true;
		}
		else
			return false;

	}
}

