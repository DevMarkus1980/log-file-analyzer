package de.struma.LogFileAnalyzer.service;

import de.struma.LogFileAnalyzer.model.LogFileEntryModel;
import de.struma.LogFileAnalyzer.repository.AdvancedConfigRepository;
import de.struma.LogFileAnalyzer.repository.LogFileEntryRepository;
import de.struma.LogFileAnalyzer.utility.LineRefHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import net.rationalminds.LocalDateModel;
import net.rationalminds.Parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class 
ParsingService {

    LogFileEntryRepository logFileEntryRepository;
    ConfigService configService;
    AdvancedConfigRepository advancedConfigRepository;

    public ParsingService(LogFileEntryRepository logFileEntryRepository, ConfigService configService, AdvancedConfigRepository advancedConfigRepository) {
        this.logFileEntryRepository = logFileEntryRepository;
        this.configService = configService;
        this.advancedConfigRepository = advancedConfigRepository;

    }

    public void parseFileToRepository(BufferedReader br, String applicationName) throws IOException {

        LogFileEntryModel setToLogFileEntryModel = new LogFileEntryModel();
        String lineInput ;
        Long previousID = 0L;
        int linesOfExceptionToRead = 0;

        while ((lineInput = br.readLine()) != null) {

            boolean datePresent = false;
            LineRefHolder line = new LineRefHolder(lineInput);
            try {

                // Set Timestamp and Cut All Dates and Times out of Line
                if (getTimestampFromStringAndDeleteEntry(line, setToLogFileEntryModel)) {
                    linesOfExceptionToRead = 0;
                    datePresent = true;
                    if (!logFileConditionAreOK(setToLogFileEntryModel))
                        return;
                }

                if (datePresent) {
                    // Set ErrorStatus and Cut StatusError from Line
                    getStatusErrorFromStringAndDeleteEntry(line, setToLogFileEntryModel);
                    // Set ProblemClass and Cut the ProblemClass of Line
                    getProblemClassFromStringAndDeleteEntry(line, setToLogFileEntryModel);
                    // Set ActivationProcess and Cut from line
                    getActivationProcessFromStringAndDeleteEntry(line, setToLogFileEntryModel);
                    // Set ApplicationName
                    setToLogFileEntryModel.setApplication(applicationName);

                }
                // Clean the line
                cleanLineFromDoubleSpacesAndPipes(line);

                if (!datePresent && linesOfExceptionToRead < 5) {
                    // Get Previous Entry and concat the message
                    linesOfExceptionToRead++;
                    saveToPreviousEntry(setToLogFileEntryModel, line.line, previousID);

                } else if (linesOfExceptionToRead >= 5) {
                    break;

                } else {
                    // Save LogFileEntryModel
                    setToLogFileEntryModel.setMessage(line.line);
                    logFileEntryRepository.saveAndFlush(setToLogFileEntryModel);
                }

                if (setToLogFileEntryModel.getId() != null)
                    previousID = setToLogFileEntryModel.getId();

                setToLogFileEntryModel = new LogFileEntryModel();

            } catch (Exception e) {
                log.warn("This Line makes Trouble: {}", lineInput);
            }
        }

        br.close();
    }

    private void saveToPreviousEntry(LogFileEntryModel setToLogFileEntryModel, String originLine, Long previousID) {

        setToLogFileEntryModel = logFileEntryRepository.findById(previousID);
        setToLogFileEntryModel.setMessage(setToLogFileEntryModel.getMessage() + "\n\t" + originLine);
        logFileEntryRepository.saveAndFlush(setToLogFileEntryModel);
    }

    public void cleanLineFromDoubleSpacesAndPipes(LineRefHolder ref) {
        ref.line = ref.line.replace("|", "");
        ref.line = ref.line.replaceAll("\\s{2}", " ");
    }

    private void getActivationProcessFromStringAndDeleteEntry(LineRefHolder ref, LogFileEntryModel toSetValue) {

        String[] splitWordsToArray = ref.line.split(" ");
        boolean moreThanOneIndex = false;
        int counterOfOpenBrackets = 0;
        StringBuilder value = new StringBuilder();

        for (String checkIfActivationProcess : splitWordsToArray) {

            if (!moreThanOneIndex) {
                if ((checkIfActivationProcess.startsWith("[") && checkIfActivationProcess.endsWith("]"))) {
                    value = new StringBuilder(checkIfActivationProcess);
                    ref.line = ref.line.replace(checkIfActivationProcess, "");
                    break;
                } else if ((checkIfActivationProcess.startsWith("["))) {
                    moreThanOneIndex = true;
                }
            }
            // Konkateniert das Array und beendet, wenn wieder geschlossen wird
            if (moreThanOneIndex) {

                value.append(checkIfActivationProcess);
                if ((checkIfActivationProcess.endsWith("]")))
                    counterOfOpenBrackets--;

                else if (checkIfActivationProcess.startsWith("["))
                    counterOfOpenBrackets++;

                if (counterOfOpenBrackets == 0) {
                    ref.line = ref.line.replace(value, "");
                    break;
                }

            }

        }
        toSetValue.setActivationProcess(String.valueOf(value));
    }

    private void getProblemClassFromStringAndDeleteEntry(LineRefHolder ref, LogFileEntryModel toSetValue) {
        String[] splitWordsToArray = ref.line.split(" ");

        for (String checkIfProblemClass : splitWordsToArray) {
            if (checkIfProblemClass.chars().filter(ch -> ch == '.').count() > 2) {
                toSetValue.setProblemClass(checkIfProblemClass);
                ref.line = ref.line.replace(checkIfProblemClass, "");
                ref.line = ref.line.replaceFirst("^\\s*", "");
                break;
            }
        }

    }

    private void getStatusErrorFromStringAndDeleteEntry(LineRefHolder ref, LogFileEntryModel toSetValue) {
        String[] splitWordsToArray = ref.line.split(" ");

        for (String checkIfHigherCase : splitWordsToArray) {
            if (checkIfHigherCase.chars().noneMatch(Character::isLowerCase)) {
                toSetValue.setStatusError(checkIfHigherCase);
                ref.line = ref.line.replace(checkIfHigherCase, "");
                break;
            }
        }
    }

    private boolean getTimestampFromStringAndDeleteEntry(LineRefHolder ref, LogFileEntryModel toSetValue) {

        Parser parser = new Parser();
        List<LocalDateModel> dates = parser.parse(ref.line);

        if (dates.isEmpty()){
            return  false;
        }

        StringBuilder dateLine = new StringBuilder(ref.line);
        dateLine.replace(dates.get(0).getStart() - 1, dates.get(0).getEnd() + 1, "");
        ref.line = String.valueOf(dateLine);
        Timestamp resultTimestamp = Timestamp.valueOf(dates.get(0).getDateTimeString());
        toSetValue.setDateTime(resultTimestamp);
        return true;

    }

    private boolean logFileConditionAreOK(LogFileEntryModel l) {

        Timestamp saveNothingOlderThan = Timestamp.valueOf(LocalDateTime.now().minusDays( advancedConfigRepository.getById(1L).getNotOlderThanDays()));

        if (l.getDateTime().before(saveNothingOlderThan))
            return false;

        return l.getDateTime().after(saveNothingOlderThan);
    }

}
