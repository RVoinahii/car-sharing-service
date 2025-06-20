INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id, status)
VALUES (1,
        CURRENT_DATE,
        DATEADD('DAY', 2, CURRENT_DATE),
        NULL,
        1,
        3,
        'ACTIVE');