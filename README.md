# java-filmorate

## ER-диаграмма моего проекта Filmorate:




# Java - filmorate
Это сервис, который работает с фильмами. Предоставляет возможность ставить оценки, заводить друзей, рекомендовать фильмы
друг другу, оставлять отзывы

## Основная функциональность:
1. «Отзывы» - возможность оставлять отзывы на фильмы
2. «Поиск» - возможность поиска фильмов по названию и по режиссёру
3. «Общие фильмы» - вывод общих с другом фильмов с сортировкой по их популярности
4. «Рекомендации» - возвращает рекомендации по фильмам для просмотра
5. «Лента событий» - просмотр последних событий на платформе: добавление в друзья, удаление из друзей, лайки и отзывы,
которые оставили друзья пользователя
6. «Популярные фильмы» - возможность выводить топ-N фильмов по количеству лайков
7. «Фильмы по режиссёрам» - возможность вывода всех фильмов режиссёра, отсортированных по популярности или по дате
релиза

## ER-diagram filmorate
![2024-12-16_17-10-03](https://github.com/user-attachments/assets/5aa44ed7-e18e-477e-a93c-71778eb17f16)
## Примеры запросов в БД
### Получить список всех фильмов:
```sql
SELECT *
FROM films f;
```
### Получить фильм по id:
```sql
SELECT *
FROM films f 
WHERE f.film_id = 1;
```
### Получить список самых популярных фильмов:
```sql
SELECT f.name,
       COUNT(ll.user_id) as likes
FROM films f
LEFT JOIN like_list ll on f.film_id = ll.film_id
GROUP BY f.name
ORDER BY likes desc
LIMIT 10;
```
### Получить жанры фильмов:
```sql
SELECT f.name,
       g.genre_name
FROM films f
JOIN films_genre fg on f.film_id = fg.film_id
JOIN genre g ON fg.genre_id = g.genre_id;
```
### Получить рейтинг фильмов:
```sql
SELECT f.name,
       r.rating
FROM films f
JOIN rating r on f.rating_id = r.rating_id;
```
### Получить всех пользователей:
```sql
SELECT *
FROM users u;
```
### Получить пользователя по id:
```sql
SELECT *
FROM users u 
WHERE u.user_id = 2;
```
### Получить список друзей пользователя
```sql
SELECT *
FROM USERS
WHERE USER_ID IN (SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?);
```
### Получить список общих друзей
```sql
SELECT *
FROM USERS
WHERE USER_ID IN (SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)
AND USER_ID IN (SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?);
```

## API:
| URL                                  | HTTP - метод | Описание                                                                                                   |
|--------------------------------------|--------------|------------------------------------------------------------------------------------------------------------|
| /users                               | GET          | Получение списка всех пользователей                                                                        |
| /users/{id}                          | GET          | Получение пользователя по id                                                                               |
| /users                               | POST         | Добавить пользователя                                                                                      |
| /users                               | PUT          | Обновить данные пользователя                                                                               |
| /users/{id}                          | DELETE       | Удалить пользователя                                                                                       |
| /users/{id}/friends/{friendId}       | PUT          | Добавить пользователя в друзья                                                                             |
| /users/{id}/friends/{friendId}       | DELETE       | Удалить пользователя из друзей                                                                             |
| /users/{id}/friends                  | GET          | Получить список друзей пользователя                                                                        |
| /users/{id}/friends/common/{otherId} | GET          | Получить список общих друзей с другим пользователем                                                        |
| /users/{id}/recommendations          | GET          | Получить рекомендуемые пользователю фильмы                                                                 |
| /users/{id}/feed                     | GET          | Получить ленту событий для пользователя                                                                    |
| /reviews                             | POST         | Добавить отзыв на фильм                                                                                    |
| /reviews                             | PUT          | Отредактировать отзыв                                                                                      |
| /reviews/{id}                        | DELETE       | Удалить отзыв                                                                                              |
| /reviews/{id}                        | GET          | Получить отзыв по id                                                                                       |
| /reviews                             | GET          | Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10 |
| /reviews/{id}/like/{userId}          | PUT          | Поставить лайк отзыву                                                                                      |
| /reviews/{id}/dislike/{userId}       | PUT          | Поставить дизлайк отзыву                                                                                   |
| /reviews/{id}/like/{userId}          | DELETE       | Удалить лайк отзыву                                                                                        |
| /reviews/{id}/dislike/{userId}       | DELETE       | Удалить дизлайк отзыву                                                                                     |
| /mpa                                 | GET          | Получить список MPA - рейтингов                                                                            |
| /mpa/{id}                            | GET          | Получить MPA по id                                                                                         |
| /genres                              | GET          | Получить список жанров                                                                                     |
| /genres/{id}                         | GET          | Получение жанра по id                                                                                      |
| /films                               | GET          | Получить список всех фильмов                                                                               |
| /films/{id}                          | GET          | Получить фильм по id                                                                                       |
| /films                               | POST         | Добавить новый фильм                                                                                       |
| /films                               | PUT          | Редактировать данные о фильме                                                                              |
| /films/{id}                          | DELETE       | Удалить фильм                                                                                              |
| /films/{id}/like/{userId}            | PUT          | Поставить лайк фильму                                                                                      |
| /films/{id}/like/{userId}            | DELETE       | Удалить лайк фильму                                                                                        |
| /films/popular                       | GET          | Получить список самых популярных фильмов указанного жанра за нужный год                                    |
| /films/director/{directorId}         | GET          | Получить список фильмов режиссера отсортированных по количеству лайков или году выпуска                    |
| /films/common                        | GET          | Получить список общих с другом фильмов                                                                     |
| /films/search                        | GET          | Поиск фильмов по названию и по режиссёрам                                                                  |
| /directors                           | GET          | Получить список всех режиссёров                                                                            |
| /directors/{id}                      | GET          | Получить информацию о режиссёре по id                                                                      |
| /directors                           | POST         | Добавить информацию о режиссёре                                                                            |
| /directors                           | PUT          | Редактировать информацию о режиссёре                                                                       |
| /directors/{id}                      | DELETE       | Удалить информацию о режиссёре                                                                             |
