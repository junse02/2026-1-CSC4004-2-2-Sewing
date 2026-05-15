package com.mediator.cider.domain.attachment.repository;

import com.mediator.cider.domain.attachment.entity.UserAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAttachmentRepository extends JpaRepository<UserAttachment, Long> {
    Optional<UserAttachment> findByUserId(Long userId);
}
