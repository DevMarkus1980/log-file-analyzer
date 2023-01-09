package de.struma.LogFileAnalyzer.utility;

import de.struma.LogFileAnalyzer.model.LogFileModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LogFileListDTO {
    private List<LogFileModel> setDTOFiles;

    public LogFileListDTO (){
        setDTOFiles = new ArrayList<>();
    }

    public void addFile(LogFileModel logFileModel) {

        this.setDTOFiles.add(logFileModel);
    }


}
