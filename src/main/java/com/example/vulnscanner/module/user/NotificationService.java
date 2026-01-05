package com.example.vulnscanner.module.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(String message, String link, String type) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setLink(link);
        notification.setType(type);
        notification.setChecked(false);
        notificationRepository.save(notification);
    }

    public List<Notification> getUncheckedNotifications() {
        return notificationRepository.findByCheckedFalseOrderByCreatedAtDesc();
    }

    public long getUncheckedCount() {
        return notificationRepository.countByCheckedFalse();
    }

    @Transactional
    public void markAsChecked(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setChecked(true);
            notificationRepository.save(notification);
        });
    }
}
