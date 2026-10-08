package com.example.shop.service;

import com.example.shop.dto.NotificationDtos.NotificationResponse;
import com.example.shop.dto.PageResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.AppUser;
import com.example.shop.model.Notification;
import com.example.shop.model.NotificationType;
import com.example.shop.repository.NotificationRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository repo;
    private final CurrentUser currentUser;
    private final SimpMessagingTemplate messaging;

    public void send(AppUser user, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        repo.save(notification);
        messaging.convertAndSendToUser(user.getEmail(), "/queue/notifications",
                NotificationResponse.from(notification));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(int page, int size) {
        return PageResponse.from(repo.findByUserOrderByCreatedAtDesc(currentUser.require(),
                PageRequest.of(page, size)).map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return repo.countByUserAndSeenFalse(currentUser.require());
    }

    public void markRead(Long id) {
        mine(id).setSeen(true);
    }

    public void markAllRead() {
        repo.markAllSeen(currentUser.require());
    }

    public void delete(Long id) {
        repo.delete(mine(id));
    }

    private Notification mine(Long id) {
        return repo.findByIdAndUser(id, currentUser.require())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }
}
