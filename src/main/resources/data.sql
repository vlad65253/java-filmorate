SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE FILMS_GENRE CASCADE;
TRUNCATE TABLE REVIEWS CASCADE;
TRUNCATE TABLE LIKE_LIST CASCADE;
TRUNCATE TABLE EVENTS CASCADE;
TRUNCATE TABLE FRIENDS_LIST CASCADE;
TRUNCATE TABLE FILMS CASCADE;
TRUNCATE TABLE GENRE CASCADE;
TRUNCATE TABLE DIRECTORS CASCADE;
TRUNCATE TABLE RATING CASCADE;
TRUNCATE TABLE USERS CASCADE;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES
('ivan.petrov@example.com', 'ivanp', 'Иван Петров', '1990-05-15'),
('elena.sidorova@example.com', 'elenas', 'Елена Сидорова', '1985-08-22'),
('maksim.kuznetsov@example.com', 'maksk', 'Максим Кузнецов', '1992-12-03'),
('olga.volkova@example.com', 'olgav', 'Ольга Волкова', '1988-03-27'),
('sergey.ivanov@example.com', 'sergiy', 'Сергей Иванов', '1995-07-19'),
('anna.kovaleva@example.com', 'annak', 'Анна Ковалёва', '1993-09-10'),
('dmitry.smirnov@example.com', 'dmitrys', 'Дмитрий Смирнов', '1987-04-18'),
('tatyana.leonova@example.com', 'tanyal', 'Татьяна Леонова', '1991-01-25'),
('oleg.morozov@example.com', 'olegm', 'Олег Морозов', '1984-11-30'),
('sophia.vasileva@example.com', 'sophiav', 'София Васильева', '1994-06-07');

INSERT INTO GENRE (GENRE_NAME) VALUES
('Комедия'),
('Драма'),
('Триллер'),
('Фантастика'),
('Мультфильм'),
('Боевик'),
('Мелодрама'),
('Ужасы'),
('Документальный'),
('Приключения'),
('Мюзикл'),
('Исторический'),
('Фэнтези'),
('Криминал'),
('Спорт');

INSERT INTO DIRECTORS (DIRECTOR_NAME) VALUES
('Алексей Балабанов'),
('Наталья Михалкова'),
('Кирилл Серебренников'),
('Фёдор Бондарчук'),
('Андрей Тарковский'),
('Эльдар Рязанов'),
('Владимир Меньшов'),
('Пётр Буслов'),
('Александр Сокуров'),
('Дмитрий Дьяченко'),
('Мария Шукшина'),
('Юрий Быков'),
('Елена Метлицкая'),
('Александр Петров'),
('Светлана Кузнецова');

INSERT INTO RATING (RATING_NAME) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17'),
('E'),
('T'),
('M');

INSERT INTO FILMS (RATING_ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, DIRECTOR_ID) VALUES
(1, 'Веселый ветер', 'Комедия о жизни в небольшом городе.', '2018-06-12', 95, 1),
(2, 'Тени прошлого', 'Драма о семейных тайнах.', '2019-11-05', 120, 2),
(3, 'Последний час', 'Триллер о поиске истины.', '2020-03-22', 110, 3),
(4, 'Звездные врата', 'Фантастический фильм о космических путешествиях.', '2021-07-30', 130, 4),
(5, 'Сказка о Храбрецах', 'Мультфильм для всей семьи.', '2022-12-15', 80, 5),
(6, 'Музыкальная ночь', 'Мюзикл о мечтах и любви.', '2023-05-20', 105, 11),
(7, 'Истории нашего города', 'Исторический фильм о становлении города.', '2024-09-10', 140, 12),
(8, 'Магический мир', 'Фэнтези о волшебных приключениях.', '2025-01-15', 115, 13),
(9, 'Криминальный след', 'Криминальный триллер о расследовании преступлений.', '2023-08-08', 100, 14),
(10, 'Спортивный дух', 'Фильм о преодолении и достижении целей.', '2024-02-20', 90, 15);

INSERT INTO FILMS_GENRE (FILM_ID, GENRE_ID) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 11),
(7, 12),
(8, 13),
(9, 14),
(10, 15),
(3, 2),
(4, 3),
(7, 2),
(8, 3),
(9, 3),
(10, 2);

INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) VALUES
('Отличный фильм! Очень понравилось.', TRUE, 1, 1, 10),
('Сюжет слишком затянутый.', FALSE, 2, 2, 2),
('Интрига держала до конца.', TRUE, 3, 3, 5),
('Спецэффекты на высоте.', TRUE, 4, 4, 8),
('Мультфильм получился очень добрым.', TRUE, 5, 5, 7),
('Прекрасная музыка и сюжет.', TRUE, 6, 6, 9),
('Не хватило исторической достоверности.', FALSE, 7, 7, 3),
('Фэнтези получилось очень захватывающим.', TRUE, 8, 8, 6),
('Некоторые сцены были предсказуемыми.', FALSE, 9, 3, 1),
('Отличная работа режиссёра!', TRUE, 10, 4, 4),
('Фильм поднял настроение.', TRUE, 1, 6, 8),
('Сюжет сложный, но интересный.', TRUE, 2, 7, 5),
('Визуально красивый, но слабый сюжет.', FALSE, 3, 8, 2),
('Отличный криминальный триллер.', TRUE, 4, 9, 7),
('Мотивирующий спортивный фильм.', TRUE, 5, 10, 6),
('Персонажи глубоко проработаны.', TRUE, 6, 2, 9),
('Музыкальные номера великолепны.', TRUE, 7, 6, 8),
('Исторический контекст хорошо раскрыт.', TRUE, 8, 7, 7),
('Фэнтезийный мир захватывает.', TRUE, 9, 8, 5),
('Интересное расследование преступления.', TRUE, 10, 9, 6),
('Персонажи живо проработаны.', TRUE, 1, 2, 7),
('Нравится музыка и сюжет.', TRUE, 2, 6, 8),
('Фильм понравился, хотя немного затянутый.', FALSE, 3, 5, 3),
('Хороший спортивный фильм.', TRUE, 4, 10, 5),
('Плохой сюжет, но хорошая игра.', FALSE, 5, 1, 2),
('Интересный исторический фильм.', TRUE, 6, 7, 6),
('Фэнтези получилось магическим.', TRUE, 7, 8, 7),
('Спортивный фильм мотивирует.', TRUE, 8, 10, 4),
('Комедия с множеством шутками.', TRUE, 9, 1, 5),
('Драма с глубоким сюжетом.', TRUE, 10, 2, 6);

INSERT INTO LIKE_LIST (FILM_ID, USER_ID) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 1),
(4, 4),
(5, 5),
(3, 2),
(4, 1),
(5, 3),
(6, 1),
(6, 2),
(7, 3),
(8, 4),
(6, 5),
(7, 1),
(8, 2),
(8, 5),
(7, 4),
(9, 6),
(10, 7),
(9, 8),
(10, 9),
(10, 10),
(7, 5),
(8, 3),
(9, 2),
(10, 1),
(5, 4),
(4, 5),
(3, 6),
(2, 7),
(1, 8),
(2, 9),
(3, 10);

INSERT INTO EVENTS (USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES
(1, 'Просмотр фильма', 'Добавить', 1),
(2, 'Лайк', 'Поставить', 1),
(3, 'Написать отзыв', 'Создать', 3),
(4, 'Добавить в избранное', 'Добавить', 4),
(5, 'Просмотр фильма', 'Добавить', 5),
(1, 'Лайк', 'Поставить', 4),
(2, 'Написать отзыв', 'Создать', 2),
(6, 'Просмотр фильма', 'Добавить', 6),
(7, 'Лайк', 'Поставить', 7),
(8, 'Написать отзыв', 'Создать', 6),
(9, 'Добавить в избранное', 'Добавить', 8),
(10, 'Просмотр фильма', 'Добавить', 4),
(6, 'Лайк', 'Поставить', 6),
(7, 'Написать отзыв', 'Создать', 7),
(8, 'Просмотр фильма', 'Добавить', 8),
(9, 'Лайк', 'Поставить', 9),
(10, 'Написать отзыв', 'Создать', 10),
(1, 'Добавить в избранное', 'Добавить', 2),
(2, 'Просмотр фильма', 'Добавить', 3),
(3, 'Лайк', 'Поставить', 5),
(4, 'Написать отзыв', 'Создать', 4),
(5, 'Лайк', 'Поставить', 7),
(6, 'Добавить в избранное', 'Добавить', 6),
(7, 'Просмотр фильма', 'Добавить', 7),
(8, 'Лайк', 'Поставить', 8),
(9, 'Написать отзыв', 'Создать', 9),
(10, 'Лайк', 'Поставить', 10);

INSERT INTO FRIENDS_LIST (USER_ID, FRIEND_ID) VALUES
(1, 2),
(1, 3),
(2, 4),
(3, 5),
(4, 5),
(6, 7),
(6, 8),
(7, 9),
(8, 10),
(9, 10),
(1, 5),
(2, 6),
(3, 7),
(4, 8),
(5, 9),
(1, 4),
(2, 5),
(3, 6),
(4, 7),
(5, 8),
(6, 9),
(7, 10),
(2, 10),
(3, 9),
(4, 6),
(5, 7),
(8, 9);