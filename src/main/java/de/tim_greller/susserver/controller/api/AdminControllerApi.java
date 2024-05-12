package de.tim_greller.susserver.controller.api;

import java.util.Comparator;
import java.util.List;

import de.tim_greller.susserver.persistence.entity.UserEventTrackingEntity;
import de.tim_greller.susserver.service.tracking.UserEventTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class AdminControllerApi {

    private final UserEventTrackingService userEventTrackingService;

    @GetMapping("${paths.api}/admin/data")
    public @ResponseBody List<UserEventTrackingEntity> data() {
        return userEventTrackingService.getAllEvents().stream()
                .sorted(Comparator.comparingLong(UserEventTrackingEntity::getTimestamp).reversed())
                .toList();
    }
}
