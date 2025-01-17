package org.mephi;

/**
 * Класс URLShortenerApp служит точкой входа в приложение для сокращения URL-адресов.
 * Он инициализирует и выполняет основную логику сервиса.
 * <p>
 * Приложение позволяет пользователям создавать сокращённые URL-адреса и получать к ним доступ,
 * предоставляя удобный интерфейс для взаимодействия со службой сокращения URL-адресов.
 * <p>
 * Этот класс делегирует основные функции классу URLShortenerService,
 * который управляет созданием, хранением и извлечением сокращённых URL-адресов.
 * <p>
 * Ключевые функции, предоставляемые этим приложением, включают:
 * 1. Создание сокращённых URL-адресов на основе исходных URL-адресов, предоставленных пользователями.
 * 2. Доступ к сокращённым URL-адресам и их проверка при соблюдении правил использования.
 * 3. Управление данными сеанса пользователя, ограничениями и связанными с ними сокращёнными ссылками.
 */
public class URLShortenerApp {
    public static void main(String[] args) throws Exception {
        URLShortenerService service = new URLShortenerService();
        service.run();
    }
}
