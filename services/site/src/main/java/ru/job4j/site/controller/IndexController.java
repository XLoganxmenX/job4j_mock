package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.service.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static ru.job4j.site.controller.RequestResponseTools.getToken;

@Controller
@AllArgsConstructor
@Slf4j
public class IndexController {
    private final CategoriesService categoriesService;
    private final InterviewsService interviewsService;
    private final AuthService authService;
    private final NotificationService notifications;
    private final ProfilesService profilesService;
    private final TopicsService topicsService;

    @GetMapping({"/", "index"})
    public String getIndexPage(Model model, HttpServletRequest req) throws JsonProcessingException {
        RequestResponseTools.addAttrBreadcrumbs(model,
                "Главная", "/"
        );
        try {
            model.addAttribute("categories", categoriesService.getMostPopular());
            var token = getToken(req);
            if (token != null) {
                var userInfo = authService.userInfo(token);
                model.addAttribute("userInfo", userInfo);
                model.addAttribute("userDTO", notifications.findCategoriesByUserId(userInfo.getId()));
                RequestResponseTools.addAttrCanManage(model, userInfo);
            }
        } catch (Exception e) {
            log.error("Remote application not responding. Error: {}. {}, ", e.getCause(), e.getMessage());
        }
        var interviews = interviewsService.getByType(1);
        Set<ProfileDTO> profiles = interviews.stream()
                .map(x -> profilesService.getProfileById(x.getSubmitterId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        Map<Integer, String> usernames = profiles.stream()
                .collect(Collectors.toMap(ProfileDTO::getId, ProfileDTO::getUsername));
        Map<Integer, Long> interviewsCntByCategory = getMapOfCategoryAndTopicCount(interviews);
        model.addAttribute("new_interviews", interviews);
        model.addAttribute("usernames", usernames);
        model.addAttribute("interviewsCount", interviewsCntByCategory);
        return "index";
    }

    private Map<Integer, Long> getMapOfCategoryAndTopicCount(List<InterviewDTO> interviews) {
        return interviews.stream()
                .map(interview -> {
                    try {
                        return topicsService.getById(interview.getTopicId());
                    } catch (Exception e) {
                        log.error("Error when get Topic: {}", interview.getTopicId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(topic -> topic.getCategory().getId())
                .collect(Collectors.groupingBy(categoryId -> categoryId, Collectors.counting()));
    }
}