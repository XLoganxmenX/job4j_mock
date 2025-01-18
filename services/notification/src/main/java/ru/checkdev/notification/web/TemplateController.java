package ru.checkdev.notification.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.notification.domain.Notify;
import ru.checkdev.notification.domain.Template;
import ru.checkdev.notification.service.NotificationService;
import ru.checkdev.notification.service.TemplateService;

import java.util.List;

/**
 * @author Petr Arsentev (parsentev@yandex.ru)
 * @version $Id$
 * @since 0.1
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

    private final TemplateService templates;

    private final NotificationService notifications;

    @Autowired
    public TemplateController(final TemplateService templates, NotificationService notifications) {
        this.templates = templates;
        this.notifications = notifications;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/")
    public List<Template> findAll() {
        return this.templates.findAll();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/find")
    public Template updateView(@RequestParam(required = false) Integer id) {
        return id != null ? templates.findById(id) : new Template();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/")
    public Template create(@RequestBody Template template) {
        return this.templates.save(template);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/")
    public boolean delete(@RequestParam int id) {
        return this.templates.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/send")
    public Notify send(@RequestBody Notify notify) {
        return this.templates.send(notify);
    }

    @KafkaListener(topics = "${queue.topic.notify}")
    public void queue(Notify notify) {
        this.notifications.put(notify);
    }

    @GetMapping("/ping")
    public String ping() {
        return "{}";
    }
}
