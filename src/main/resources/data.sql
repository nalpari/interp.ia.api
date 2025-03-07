-- admin / admin
INSERT IGNORE INTO member (email, password, name, position, department, job, ROLE, is_active,
                           created_date, updated_date)
VALUES ('admin@admin.com', '$2a$10$O3HqdgCpDAOdYBMJBh5/lO5vaLDol61vCY5txRhztLwJ.nFYKdtqO',
        'admin', 'position', 'department', 'job', 'ADMIN', 1, now(), NULL);
-- user1 / password
INSERT IGNORE INTO member (email, password, name, position, department, job, ROLE, is_active,
                           created_date, updated_date)
VALUES ('user1@naver.com', '$2a$10$z1NLrjhkoPJYzHucx7XVDeENGlYgs1zPt/hQIgtAqentc.Qrkv7m6',
        'user1', 'position', 'department', 'job', 'USER', 1, now(), NULL);
-- user2 / password
INSERT IGNORE INTO member (email, password, name, position, department, job, ROLE, is_active,
                           created_date, updated_date)
VALUES ('user2@google.com', '$2a$10$z1NLrjhkoPJYzHucx7XVDeENGlYgs1zPt/hQIgtAqentc.Qrkv7m6',
        'user2', 'position', 'department', 'job', 'USER', 1, now(), NULL);