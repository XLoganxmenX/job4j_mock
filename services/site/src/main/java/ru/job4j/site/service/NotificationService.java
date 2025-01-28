package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.SubscribeCategory;
import ru.job4j.site.dto.SubscribeTopicDTO;
import ru.job4j.site.dto.UserDTO;
import ru.job4j.site.dto.UserTopicDTO;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${queue.notification.topic.subscategory.add}")
    private String addSubscribeCategoryQueue;

    @Value("${queue.notification.topic.subscategory.delete}")
    private String deleteSubscribeCategoryQueue;

    @Value("${queue.notification.topic.substopic.add}")
    private String addSubscribeTopicQueue;

    @Value("${queue.notification.topic.substopic.delete}")
    private String deleteSubscribeTopicQueue;

    @Value("${service.ntf}")
    private String ntfServiceUrl;


    public void addSubscribeCategory(int userId, int categoryId) {
        kafkaTemplate.send(addSubscribeCategoryQueue, new SubscribeCategory(userId, categoryId));
    }

    public void deleteSubscribeCategory(int userId, int categoryId) {
        kafkaTemplate.send(deleteSubscribeCategoryQueue, new SubscribeCategory(userId, categoryId));
    }

    public UserDTO findCategoriesByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(ntfServiceUrl + "/subscribeCategory/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserDTO(id, list);
    }

    public void addSubscribeTopic(int userId, int topicId) {
        kafkaTemplate.send(addSubscribeTopicQueue, new SubscribeTopicDTO(userId, topicId));
    }

    public void deleteSubscribeTopic(int userId, int topicId) {
        kafkaTemplate.send(deleteSubscribeTopicQueue, new SubscribeTopicDTO(userId, topicId));
    }

    public UserTopicDTO findTopicByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(ntfServiceUrl + "/subscribeTopic/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserTopicDTO(id, list);
    }
}