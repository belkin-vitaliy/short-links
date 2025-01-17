# Сервис коротких ссылок

Этот проект реализует сервис сокращения URL-адресов на Java, предоставляя следующие функции:

## Особенности
1. **Создание коротких URL-адресов**: преобразование длинного URL-адреса в короткий и уникальный URL-адрес.
2. **Идентификация пользователя**: каждый пользователь идентифицируется по UUID, сгенерированному при первом взаимодействии.
3. **Ограничения доступа**: определение максимального количества обращений к каждому короткому URL-адресу.
4. **Срок действия**: URL-адреса имеют срок действия, который можно настроить (по умолчанию: 24 часа).
5. **Безопасный доступ**: только создатель URL-адреса может получить к нему доступ или управлять им.
6. **Сохранение в памяти**: для простоты URL-адреса хранятся в памяти.
7. **Взаимодействие с консолью**: пользователи могут взаимодействовать со службой через консоль.

## Как запустить
1. Клонируйте репозиторий и перейдите в каталог проекта.
2. Убедитесь, что в вашей системе установлена Java.
3. Запустите приложение:
   ```
   javac URLShortenerService.java
   java URLShortenerService
   ```

## Настройка
 Приложение использует файл `config.properties` для настройки. Добавьте следующие ключи в файл `config.properties`:
```
  expiryDuration=24  # Время в часах для истечения срока действия URL
  accessLimit=10     # Максимальное количество обращений по короткому URL-адресу
```

## Использование
1. **Создание короткого URL-адреса**:
   - Выберите опцию для создания нового короткого URL-адреса.
   - Введите исходный URL-адрес и свой UUID (или нажмите Enter, чтобы сгенерировать новый).
   - Будет сгенерирован и отображен короткий URL-адрес.

2. **Доступ к короткому URL-адресу**:
   - Введите короткий URL-адрес и свой UUID.
   - Если URL-адрес действителен и у вас есть разрешение, вы будете перенаправлены на исходный URL-адрес.

3. **Выход**:
   - Выберите опцию выхода, чтобы завершить работу программы.

## Классы
- **URLShortenerService**: основной класс, управляющий сервисом.
- **ShortenedURL**: представляет собой короткий URL-адрес с метаданными, такими как срок действия и ограничение доступа.
- **URLUser**: представляет собой пользователя с уникальным UUID и связанными ссылками.
- **URLStorage**: хранилище в памяти для управления короткими URL-адресами.

## Ограничения
- Данные хранятся в памяти и будут потеряны при завершении работы приложения.
- Отсутствует сохранение данных или интеграция с базой данных.

# Тестирование сервиса сокращения URL-адресов

В этом документе описывается тестовое покрытие и способы запуска тестов для сервиса сокращения URL-адресов.

## Тестовое покрытие
Модульные тесты охватывают следующие функции сервиса сокращения URL-адресов:

1. **Создание коротких URL-адресов**
   - Проверяет, что короткий URL-адрес правильно сгенерирован на основе исходного URL-адреса и UUID пользователя.
   - Проверяет, что метаданные, такие как срок действия и принадлежность пользователю, установлены правильно.

2. **Ограничения доступа**
   - Проверяет функциональность уменьшения ограничений доступа.
   - Проверяет, что доступ прекращается при достижении ограничения.

3. **Срок действия**
   - Подтверждает, что срок действия URL-адресов истекает через заданный промежуток времени.

4. **Управление пользователями**
   - Проверяет создание пользователей и привязку UUID.
   - Проверяет проверку прав собственности для коротких URL-адресов.

5. **Операции с хранилищем**
   - Проверка добавления, извлечения и удаления коротких URL-адресов в хранилище на основе памяти.

## Запуск тестов
### Предварительные требования
- Убедитесь, что в вашей системе установлена Java.
- Установите JUnit 5 в качестве среды тестирования.

### Шаги
1. Скомпилируйте тестовый файл вместе с основным сервисом:
   ```
   javac -cp .:junit-platform-console-standalone-1.9.2.jar *.java
   ```
2. Запустите тесты:
   ```
   java -jar junit-platform-console-standalone-1.9.2.jar --class-path . --scan-class-path
   ```

### Пример вывода
```plaintext
Test run started
Test run finished after 54 ms
[         PASS   ] 5 tests executed

Success: All tests passed.
```
