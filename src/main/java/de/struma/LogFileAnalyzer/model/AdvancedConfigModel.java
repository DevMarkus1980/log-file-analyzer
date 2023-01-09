package de.struma.LogFileAnalyzer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AdvancedConfigModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;

    Integer notOlderThanDays = 14;
    Integer maximumLinesToReadEachFile = 500;

    Boolean trackArchivedLogFiles = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AdvancedConfigModel that = (AdvancedConfigModel) o;
        return id != null && Objects.equals(id, that.id) && Objects.equals(notOlderThanDays, that.notOlderThanDays)&&
                Objects.equals(maximumLinesToReadEachFile, that.maximumLinesToReadEachFile) && Objects.equals(trackArchivedLogFiles, that.trackArchivedLogFiles);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
