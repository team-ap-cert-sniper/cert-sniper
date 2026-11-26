-- 회원 삭제(탈퇴 처리) 시 본인 데이터는 함께 삭제하고, 남의 글에 남긴 신고·처리 기록은 회원 ID만 비운다.
-- 관리자 쪽 외래 키(notices.author_id, inquiry_answers.admin_id, admin_audit_logs.admin_id, account_status_histories.admin_id)는 그대로 둔다.

-- 본인 합격수기와 그 글이 받은 신고·처리 기록
ALTER TABLE `pass_stories` DROP FOREIGN KEY `fk_pass_stories_1`;
ALTER TABLE `pass_stories` ADD CONSTRAINT `fk_pass_stories_1`
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `pass_story_reports` DROP FOREIGN KEY `fk_pass_story_reports_1`;
ALTER TABLE `pass_story_reports` ADD CONSTRAINT `fk_pass_story_reports_1`
  FOREIGN KEY (`story_id`) REFERENCES `pass_stories` (`id`) ON DELETE CASCADE;

ALTER TABLE `pass_story_moderation_logs` DROP FOREIGN KEY `fk_pass_story_moderation_logs_1`;
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_1`
  FOREIGN KEY (`story_id`) REFERENCES `pass_stories` (`id`) ON DELETE CASCADE;

ALTER TABLE `pass_story_moderation_logs` DROP FOREIGN KEY `fk_pass_story_moderation_logs_3`;
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_3`
  FOREIGN KEY (`report_id`) REFERENCES `pass_story_reports` (`id`) ON DELETE CASCADE;

-- 본인 문의(답변은 문의 삭제 시 이미 함께 삭제), 탈퇴·정지 이력, 관리자가 이 회원을 수정한 기록
ALTER TABLE `inquiries` DROP FOREIGN KEY `fk_inquiries_1`;
ALTER TABLE `inquiries` ADD CONSTRAINT `fk_inquiries_1`
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `account_status_histories` DROP FOREIGN KEY `fk_account_status_histories_1`;
ALTER TABLE `account_status_histories` ADD CONSTRAINT `fk_account_status_histories_1`
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `admin_audit_logs` DROP FOREIGN KEY `fk_admin_audit_logs_2`;
ALTER TABLE `admin_audit_logs` ADD CONSTRAINT `fk_admin_audit_logs_2`
  FOREIGN KEY (`target_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- 남의 글에 남긴 신고·처리 기록: 기록은 남기고 회원 ID만 비운다 (다른 회원의 숨김 처리 근거 유지)
ALTER TABLE `pass_story_reports` DROP FOREIGN KEY `fk_pass_story_reports_2`;
ALTER TABLE `pass_story_reports` MODIFY `reporter_id` bigint NULL COMMENT '신고 회원 ID (탈퇴 시 NULL)';
ALTER TABLE `pass_story_reports` ADD CONSTRAINT `fk_pass_story_reports_2`
  FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `pass_story_moderation_logs` DROP FOREIGN KEY `fk_pass_story_moderation_logs_2`;
ALTER TABLE `pass_story_moderation_logs` MODIFY `actor_id` bigint NULL COMMENT '처리자 ID (탈퇴 시 NULL)';
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_2`
  FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;
