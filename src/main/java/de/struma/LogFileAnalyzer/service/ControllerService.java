package de.struma.LogFileAnalyzer.service;

import de.struma.LogFileAnalyzer.model.AdvancedConfigModel;
import de.struma.LogFileAnalyzer.model.LogFileEntryModel;
import de.struma.LogFileAnalyzer.model.LogFileModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Data
@Service
@Slf4j
public class ControllerService {

    ConfigService configService;
    FileReaderService fileReaderService;
    LogEntryReaderService logEntryReaderService;


    public ControllerService(ConfigService configService,  FileReaderService fileReaderService, LogEntryReaderService logEntryReaderService){
        this.configService = configService;
        this.fileReaderService = fileReaderService;
        this.logEntryReaderService = logEntryReaderService;
    }
    /**
     * Alle Services für den Main Controller
     */

    public boolean isFirstInitOnServer(){
        return !fileReaderService.getRepoWithAllLogFilesFromService().iterator().hasNext();
    }

    public void firstInitOfLogFilesOnServer(){
        configService.setAdvancedConfigToDefault();
        fileReaderService.findAllLogFilesOnServer();

    }

    public void reloadLogFileRepositoryAndConfigLogFilePathRepositoryIfEmpty(Model model){

        if(isFirstInitOnServer()){
            fileReaderService.findAllLogFilesOnServer();
        }
        if(!isFirstInitOnServer()){
            fileReaderService.initAllTrackedLogEntries();
        }
    }

    public Map<String, Integer> getMainChartData() {
        return logEntryReaderService.getMainChartData();
    }

    /***
     * Alle Services welche den LogFileController dienen
     */
    public String getAllLogs(Model model){
        return runForEachRequest(model , "all",logEntryReaderService.findAll());
    }

      public String reloadAllLogs(Model model, String application) {
          logEntryReaderService.deleteAll();
            reloadLogFileRepositoryAndConfigLogFilePathRepositoryIfEmpty(model);
        return runForEachRequest(model , application,logEntryReaderService.findAll());

    }

    public String runForEachRequest(Model model, String application,List<LogFileEntryModel> checkIfEmptyForPlaceholderRequest) {
        reloadLogFileRepositoryAndConfigLogFilePathRepositoryIfEmpty(model);
        model.addAttribute("logs", checkIfEmptyForPlaceholderRequest);
        if(checkIfEmptyForPlaceholderRequest==null)
            return placeholderHandler(model, "Sides/Logs/placeholder" );
        return "Sides/Logs/log_all";
    }

    public String placeholderHandler(Model model, String path) {
        model.addAttribute("placeholdertitle", path);
        model.addAttribute("placeholderbody", "Gibt kein Resultat zurück");
        log.debug("Problem die folgende Seite darzustellen: {}",path);
        return path;
    }

    /***
     * Alle Services welche den LogFileController dienen
     */
    public Iterable<LogFileModel> getSelectedLogFilesList() {
        return configService.getSelectedLogFilesBeforeInit();
    }

    public void saveAllAfterFirstInit(List<LogFileModel> setDTOFiles) {
        configService.save(setDTOFiles);
    }


    public AdvancedConfigModel getAdvisedConfig() {
        return configService.getAdvisedConfigFromService();
    }

    public void saveAdvisedConfig(AdvancedConfigModel advisedConfig) {
        configService.saveAdvisedConfigToService(advisedConfig);
    }

    public boolean saveAdvancedConfigAndCheckIfConfigChanged(AdvancedConfigModel advisedConfig) {
        return configService.saveAdvancedConfigAndCheckIfConfigChanged(advisedConfig);

    }
}
