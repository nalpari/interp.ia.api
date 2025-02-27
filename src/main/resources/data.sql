-- admin / admin
INSERT IGNORE INTO member (user_id, password, email, name, ROLE, created_at, updated_at)
VALUES ('admin', '$2a$10$O3HqdgCpDAOdYBMJBh5/lO5vaLDol61vCY5txRhztLwJ.nFYKdtqO',
        'admin@admin.com', 'admin', 'ADMIN', now(), NULL);
-- user1 / password
INSERT IGNORE INTO member (user_id, password, email, name, ROLE, created_at, updated_at)
VALUES ('user1', '$2a$10$z1NLrjhkoPJYzHucx7XVDeENGlYgs1zPt/hQIgtAqentc.Qrkv7m6',
        'user1@naver.com', 'user1', 'USER', now(), NULL);
-- user2 / password
INSERT IGNORE INTO member (user_id, password, email, name, ROLE, created_at, updated_at)
VALUES ('user2', '$2a$10$z1NLrjhkoPJYzHucx7XVDeENGlYgs1zPt/hQIgtAqentc.Qrkv7m6',
        'user2@google.com', 'user2', 'USER', now(), NULL);