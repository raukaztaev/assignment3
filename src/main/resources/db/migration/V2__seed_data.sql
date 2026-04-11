INSERT INTO app_user (username, password_hash, role, active, created_at)
VALUES
    ('planner1', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa6XfJ4zI7Di5urN6byj1Nsx3Rp3XIan', 'PLANNER', TRUE, now()),
    ('master1', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa6XfJ4zI7Di5urN6byj1Nsx3Rp3XIan', 'MASTER', TRUE, now()),
    ('operator1', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa6XfJ4zI7Di5urN6byj1Nsx3Rp3XIan', 'OPERATOR', TRUE, now());
