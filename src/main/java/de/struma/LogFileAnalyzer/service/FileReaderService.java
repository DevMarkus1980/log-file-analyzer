package de.struma.LogFileAnalyzer.service;

import de.struma.LogFileAnalyzer.model.LogFileModel;
import de.struma.LogFileAnalyzer.repository.AdvancedConfigRepository;
import de.struma.LogFileAnalyzer.repository.LogFileEntryRepository;
import de.struma.LogFileAnalyzer.repository.LogFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileReaderService {

	LogFileRepository logFileRepository;
	LogFileEntryRepository logFileEntryRepository;
	ParsingService parsingService;
	AdvancedConfigRepository advancedConfigRepository;

	@Value("${file.count.applicationNamePositionInPath}")
	String[] countForApplicationNamePositionInPath;

	@Value("${file.delete.when.contains}")
	String[] removeFromListWhenContains;

	public FileReaderService(LogFileEntryRepository logFileEntryRepository,
							 ParsingService parsingService,
							 LogFileRepository logFileRepository,
							 AdvancedConfigRepository advancedConfigRepository) {
		this.logFileEntryRepository = logFileEntryRepository;
		this.parsingService = parsingService;
		this.logFileRepository = logFileRepository;
		this.advancedConfigRepository = advancedConfigRepository;
	}

	public void initAllTrackedLogEntries(){

		for (LogFileModel logFileModel : logFileRepository.findByActivateTrackingTrue()) {

				try{
					readFile(logFileModel);

				}catch(Exception e){
					log.error("Die Datei {} war nicht im StrategyParsing zu finden", logFileModel.getPath());

				}


		}
	}
	public void readFile(LogFileModel logFileModel) {

		try {
			File file=new File(logFileModel.getPath());
			FileReader fr=new FileReader(file);
			BufferedReader br=new BufferedReader(fr);
			parsingService.parseFileToRepository(br, logFileModel.getApplication()) ;
			br.close();
			fr.close();

			}
			catch(IOException e)  {
				e.printStackTrace();
				log.warn("Die folgende Datei konnte nicht gelesen werden: {}", logFileModel.getPath());
				}
		}

	public String getNameOfAppFromPath(String path) {

		String bySeparator = checkOsSystem("\\\\","/");
		String[] getAppNameFromPath = path.split(bySeparator);
		int locationNameOfApplicationInPath = 2;

		for (String evilPathName : this.countForApplicationNamePositionInPath) {
			if (Arrays.stream(getAppNameFromPath).anyMatch(s -> s.equalsIgnoreCase(evilPathName)))
				locationNameOfApplicationInPath++;
		}

		return getAppNameFromPath[getAppNameFromPath.length - locationNameOfApplicationInPath];

	}

	private String checkOsSystem(String ifWindows, String ifUnix) {
		if (System.getProperty("os.name").startsWith("Windows")) {
			// includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
			return ifWindows;
		} else {
			// everything else
			return ifUnix;
		}
	}

	// First validation
	private void validateListFromArchivedLogFiles(List<String> toCleanListOfArchivedPath){
		for(String toDeleteWhenContains: removeFromListWhenContains)
		{
			toCleanListOfArchivedPath.removeIf(s -> s.contains(toDeleteWhenContains));
		}
	}

	private void valideListIfLogsOlderThanOneWeek(List<String> toCleanListOfArchivedPath){

		String regexDateFinder = "\\d{4}-[01]\\d-[0-3]\\d";
		String regexArchivedFiles = "[.][0-9][.]";
		Set<String> toRemoveFromToCleanListOfArchivedPath = new HashSet<>();
		LocalDate maxDate = LocalDate.now().minusDays(advancedConfigRepository.getById(1L).getNotOlderThanDays());
		LocalDate fromFile;

		for(String checkIfDateIsOlder : toCleanListOfArchivedPath){
			Matcher matcher = Pattern.compile(regexDateFinder).matcher(checkIfDateIsOlder);
			Matcher matcherArchivedFiles = Pattern.compile(regexArchivedFiles).matcher(checkIfDateIsOlder);

			if (matcher.find()) {
				fromFile = LocalDate.parse(matcher.group());
				if (fromFile.isBefore(maxDate)) {
					toRemoveFromToCleanListOfArchivedPath.add(matcher.group());
				}
			}
			if (matcherArchivedFiles.find()) {
				toRemoveFromToCleanListOfArchivedPath.add(matcherArchivedFiles.group());
			}
		}

		for(String resultsFromMatchingToDelete :toRemoveFromToCleanListOfArchivedPath) {
			toCleanListOfArchivedPath.removeIf(s -> s.contains(resultsFromMatchingToDelete));
		}
	}

	public Iterable<LogFileModel> findAllLogFilesOnServer() {

		String path = checkOsSystem("..\\", "/opt/");
		Set<LogFileModel> result = new HashSet<>();


		try (Stream<Path> walk = Files.walk(Paths.get(path))) {

			List<String> resultList = walk.filter(Files::isRegularFile)
					.map(Path::toString)
					.filter(f -> f.endsWith(".log"))
					.collect(Collectors.toList());

			validateListFromArchivedLogFiles(resultList);
			valideListIfLogsOlderThanOneWeek(resultList);


			for (String resultPath : resultList) {

				LogFileModel logFileModel = new LogFileModel();
				logFileModel.setPath(new File(resultPath).getCanonicalPath());
				logFileModel.setFileSize(getFileSize(resultPath));
				logFileModel.setApplication(getNameOfAppFromPath(resultPath));
				logFileModel.setFileName(getFileName(resultPath));
				logFileModel.setLastModified(getLocalDateTimeFromFileByAttributes(resultPath, "lastModifiedTime"));
				logFileModel.setCreatedDate(getLocalDateTimeFromFileByAttributes(resultPath, "creationTime"));
				logFileModel.setLastAccessTime(getLocalDateTimeFromFileByAttributes(resultPath, "lastAccessTime"));
				result.add(logFileModel);
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

		return logFileRepository.saveAll(result);
	}

	public String getFileName(String path) {
		File file = new File(path);
		return file.getName();
	}
	public Long getFileSize(String path){
		long size = 0L;
		try {
			Path pathToCheck = Paths.get(path);
			size = Files.size(pathToCheck);
		} catch (IOException e) {
			log.error("Die Größe der Datei {} konnte nicht gelesen werden", path);
		}
		return size;
	}

	private LocalDateTime getLocalDateTimeFromFileByAttributes(String path, String getAttribute) {
		LocalDateTime localDateTimeFromFile = null;
		try {
			Path pathToCheck = Paths.get(path);
			FileTime attributeTimeFromFile = (FileTime) Files.getAttribute(pathToCheck, getAttribute);
			localDateTimeFromFile = LocalDateTime.ofInstant(Instant.ofEpochMilli(attributeTimeFromFile.toMillis()), ZoneId.of("-2"));
			// TODO: Zeitzonen Anpassung
		} catch (IOException e) {
			log.error("Das Erstellungsdatum der Datei {} konnte nicht gelesen werden", path);
		}
		return localDateTimeFromFile;
	}

    public void saveAll(List<LogFileModel> setDTOFiles) {
		logFileRepository.saveAll(setDTOFiles);

    }

	public Iterable<LogFileModel> getRepoWithAllLogFilesFromService() {
		return logFileRepository.findAll();
	}
}

