package de.tim_greller.susserver.service.tracking;

import de.tim_greller.susserver.persistence.entity.GlobalSettingsEntity;
import de.tim_greller.susserver.persistence.repository.GlobalSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final GlobalSettingsRepository settingsRepository;
    private static final String SURVEY_URL_KEY = "survey-url";

    public String getSurveyUrl() {
        return settingsRepository.findById(SURVEY_URL_KEY).map(GlobalSettingsEntity::getSettingsValue).orElse(null);
    }

    public void setSurveyUrl(String surveyUrl) {
        var entity = GlobalSettingsEntity.builder()
                .settingsKey(SURVEY_URL_KEY)
                .settingsValue(surveyUrl)
                .build();
        settingsRepository.save(entity);
    }
}
