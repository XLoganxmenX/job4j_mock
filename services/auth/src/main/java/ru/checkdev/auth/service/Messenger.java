package ru.checkdev.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.checkdev.auth.domain.Notify;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Petr Arsentev (parsentev@yandex.ru)
 * @version $Id$
 * @since 0.1
 */
@Service
public class Messenger {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final String topic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Messenger(final @Value("${queue.topic.notify.notification}") String topic,
                                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }


    public void send(Notify notify) {
        this.scheduler.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    kafkaTemplate.send(topic, notify);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @PreDestroy
    public void close() {
        this.scheduler.shutdown();
    }
}
