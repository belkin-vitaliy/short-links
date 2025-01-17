import java.awt.*;
import java.net.URI;
import java.util.*;

/**
 * Класс URLShortenerService предоставляет функции для создания и доступа к
 * сокращённым URL-адресам. Он позволяет пользователям создавать сокращённые URL-адреса для заданного исходного URL-адреса
 * и взаимодействовать с сокращёнными URL-адресами с помощью пользовательских UUID. Сервис
 * также управляет сроком действия и ограничениями доступа для сокращённых ссылок.
 */
public class URLShortenerService {

    private static final ResourceBundle CONFIG = ResourceBundle.getBundle("config");
    private static final int EXPIRY_DURATION = Integer.parseInt(CONFIG.getString("expiryDuration"));
    private static final String BASE_URL = CONFIG.getString("baseUrl");
    public static final int ACCESS_URL_LIMIT = Integer.parseInt(CONFIG.getString("accessUrlLimit"));

    private final URLStorage urlStorage = new URLStorage();
    private final Map<String, URLUser> users = new HashMap<>();

    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Добро пожаловать в службу сокращения URL-адресов!");

        while (true) {
            System.out.println("1. Создайте короткий URL-адрес\n2. Откройте короткий URL-адрес\n3. Отредактируйте ограничение доступа\n4. Удалите короткий URL-адрес\n5. Выйдите");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Настройка панели инструментов…

            switch (choice) {
                case 1 -> createShortURL(scanner);
                case 2 -> accessShortURL(scanner);
                case 3 -> editAccessLimit(scanner);
                case 4 -> deleteShortURL(scanner);
                case 5 -> {
                    System.out.println("Выходящий... До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }
    }

    /**
     * Создаёт сокращённый URL на основе исходного URL, предоставленного пользователем.
     * Предлагает пользователю ввести URL и уникальный идентификатор пользователя,
     * генерирует короткий код и сохраняет сопоставление.
     *
     * @param scanner экземпляр {@code Scanner}, используемый для считывания вводимых пользователем
     *                данных для URL-адреса и, при необходимости, идентификатора пользователя.
     */
    private void createShortURL(Scanner scanner) {
        System.out.println("Введите свой оригинальный URL: ");
        String originalURL = scanner.nextLine();

        URLUser user = getOrCreateUser(scanner);
        String userUUID = user.getUuid();
        String shortCode = generateShortCode(originalURL, userUUID);
        String shortURL = BASE_URL + shortCode;

        ShortenedURL shortenedURL = new ShortenedURL(originalURL, shortCode, EXPIRY_DURATION, userUUID, ACCESS_URL_LIMIT);
        urlStorage.add(shortenedURL);
        user.addLink(shortCode);

        System.out.println("Введите короткий URL: " + shortURL);
    }

    /**
     * Управляет процессом доступа к сокращённому URL-адресу.
     * Предлагает пользователю ввести короткий URL-адрес и проверяет его доступность
     * и разрешения перед переходом на исходный URL-адрес, если он доступен.
     *
     * @param scanner экземпляр {@code Scanner}, используемый для считывания пользовательского ввода,
     *                включая короткий URL-адрес и, при необходимости, UUID пользователя.
     * @throws Exception если произошла ошибка при открытии исходного URL-адреса в браузере по умолчанию.
     */
    private void accessShortURL(Scanner scanner) throws Exception {
        System.out.println("Введите короткий URL:");
        String shortURL = scanner.nextLine();

        String shortCode = shortURL.replace(BASE_URL, "");
        ShortenedURL shortenedURL = urlStorage.get(shortCode);

        if (shortenedURL == null || shortenedURL.isExpired()) {
            System.out.println("Эта ссылка больше не активна.");
            urlStorage.remove(shortCode);
        } else {
            System.out.println("Введите свой пользовательский UUID:");
            String userUUID = scanner.nextLine();

            if (!userUUID.equals(shortenedURL.getUserUUID())) {
                System.out.println("У вас нет разрешения на доступ к этой ссылке или ее редактирование.");
                return;
            }

            if (shortenedURL.decrementLimit()) {
                Desktop.getDesktop().browse(new URI(shortenedURL.getOriginalURL()));
            } else {
                System.out.println("Достигнут лимит ссылок.");
            }
        }
    }

    /**
     * Изменяет ограничение доступа для существующего сокращенного URL-адреса. Предлагает пользователю ввести короткий URL-адрес,
     * свой UUID для аутентификации и установить новый предел доступа. Перед внесением изменений убедитесь, что у пользователя есть
     * права собственности.
     *
     * @param scanner - экземпляр {@code Scanner}, используемый для считывания пользовательского ввода, включая короткий URL-адрес,
     * UUID пользователя и новый лимит доступа.
     */
    private void editAccessLimit(Scanner scanner) {
        System.out.println("Введите короткий URL-адрес, который вы хотите отредактировать:");
        String shortURL = scanner.nextLine();

        String shortCode = shortURL.replace(BASE_URL, "");
        ShortenedURL shortenedURL = urlStorage.get(shortCode);

        if (shortenedURL == null) {
            System.out.println("Этой ссылки не существует.");
        } else {
            System.out.println("Введите свой пользовательский UUID:");
            String userUUID = scanner.nextLine();

            if (!userUUID.equals(shortenedURL.getUserUUID())) {
                System.out.println("У вас нет прав на редактирование этой ссылки.");
            } else {
                System.out.println("Enter the new access limit:");
                int newLimit = scanner.nextInt();
                scanner.nextLine(); // consume newline

                shortenedURL.setAccessLimit(newLimit);
                System.out.println("Введите новый лимит доступа:");
            }
        }
    }

    /**
     * Удаляет существующий короткий URL-адрес из хранилища после проверки прав доступа.
     * Пользователь должен предоставить URL, который он хочет удалить,
     * и свой UUID для подтверждения прав на удаление.
     *
     * @param scanner экземпляр {@code Scanner}, используемый для считывания вводимых данных,
     *                таких как короткий URL-адрес и пользовательский UUID.
     */
    private void deleteShortURL(Scanner scanner) {
        System.out.println("Введите короткий URL-адрес, который вы хотите удалить:");
        String shortURL = scanner.nextLine();

        String shortCode = shortURL.replace(BASE_URL, "");
        ShortenedURL shortenedURL = urlStorage.get(shortCode);

        if (shortenedURL == null) {
            System.out.println("Этой ссылки не существует.");
        } else {
            System.out.println("Введите свой пользовательский UUID:");
            String userUUID = scanner.nextLine();

            if (!userUUID.equals(shortenedURL.getUserUUID())) {
                System.out.println("У вас нет разрешения на удаление этой ссылки.");
            } else {
                urlStorage.remove(shortCode);
                URLUser user = users.get(userUUID);
                if (user != null) {
                    user.removeLink(shortCode);
                }
                System.out.println("Ссылка была успешно удалена.");
            }
        }
    }

    /**
     * Получает UUID пользователя из вводимых пользователем данных или генерирует новый, если он не указан.
     * Предлагает пользователю ввести UUID, а если ввод пуст, генерирует новый UUID
     * и возвращает его пользователю.
     *
     * @param scanner экземпляр {@code Scanner}, используемый для считывания вводимых пользователем данных для UUID.
     * @return User пользователь
     */
    private URLUser getOrCreateUser(Scanner scanner) {
        System.out.println("Введите свой пользовательский UUID (или нажмите Enter, чтобы сгенерировать новый).:");
        String userUUID = scanner.nextLine();

        if (userUUID.isEmpty()) {
            userUUID = UUID.randomUUID().toString();
            System.out.println("Сгенерированный новый пользовательский UUID: " + userUUID);
            URLUser user = new URLUser(userUUID);
            users.put(userUUID, user);
            return user;
        }

        return users.computeIfAbsent(userUUID, URLUser::new);
    }

    /**
     * Генерирует короткий код на основе хэша, полученного путём объединения
     * исходного URL-адреса и пользовательского UUID.
     *
     * @param originalURL исходный URL-адрес, предоставленный пользователем
     * @param userUUID    пользовательский UUID, связанный с запросом
     * @return {@code String} представляющий сгенерированный короткий код
     */
    public String generateShortCode(String originalURL, String userUUID) {
        return Integer.toHexString((originalURL + userUUID).hashCode());
    }

}
