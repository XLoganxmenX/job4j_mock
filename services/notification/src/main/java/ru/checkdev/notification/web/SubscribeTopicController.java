package ru.checkdev.notification.web;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.service.SubscribeTopicService;
import java.util.List;

@RestController
@RequestMapping("/subscribeTopic")
@AllArgsConstructor
public class SubscribeTopicController {
    private final SubscribeTopicService service;

    @GetMapping("/{id}")
    public ResponseEntity<List<Integer>> findTopicByUserId(@PathVariable int id) {
        List<Integer> list = service.findTopicByUserId(id);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @KafkaListener(topics = "${queue.topic.substopic.add}")
    public void toAddSubscribeTopic(SubscribeTopic subscribetopic) {
        service.save(subscribetopic);
    }

    @KafkaListener(topics = "${queue.topic.substopic.delete}")
    public void toDeleteSubscribeTopic(SubscribeTopic subscribeTopic) {
        service.delete(subscribeTopic);
    }
}