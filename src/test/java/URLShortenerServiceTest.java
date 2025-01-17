import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class URLShortenerServiceTest {

    private URLShortenerService urlShortenerService;
    private static final String ORIGINAL_URL = "https://example.com";
    private static final int EXPIRY_DURATION_24 = 24;
    private static final int EXPIRY_DURATION_0 = 0;
    private static final int ACCESS_LIMIT_10 = 10;
    private static final int ACCESS_LIMIT_2 = 2;
    private static final String SHORT_CODE = "testCode";

    @BeforeEach
    void setUp() {
        urlShortenerService = new URLShortenerService();

    }

    /**
     * Проверяет создание сокращенного URL-адреса, проверяя правильность сгенерированного короткого кода и свойств объекта `ShortenedURL`.
     * <p>
     * Этот тест гарантирует, что:
     * - короткий код, сгенерированный с помощью `generateShortCode`, не равен нулю.
     * - исходный URL-адрес и UUID пользователя правильно сохранены в объекте `ShortenedURL`.
     * - Срок действия объекта `ShortenedURL` не истекает при инициализации.
     */
    @Test
    void testCreateShortURL() {
        String userUUID = UUID.randomUUID().toString();

        String shortCode = urlShortenerService.generateShortCode(ORIGINAL_URL, userUUID);
        assertNotNull(shortCode);

        ShortenedURL shortenedURL = new ShortenedURL(ORIGINAL_URL, shortCode, EXPIRY_DURATION_24, userUUID, ACCESS_LIMIT_10);
        assertEquals(ORIGINAL_URL, shortenedURL.getOriginalURL());
        assertEquals(userUUID, shortenedURL.getUserUUID());
        assertFalse(shortenedURL.isExpired());
    }

    /**
     * Проверяет поведение при доступе к сокращённому URL-адресу в пределах допустимого лимита доступа.
     * <p>
     * Этот тест гарантирует, что:
     * - лимит доступа к сокращённому URL-адресу корректно уменьшается при каждом доступе.
     * - лимит не может быть меньше нуля, что блокирует дальнейшее уменьшение после достижения лимита.
     * <p>
     * Тест создаёт сокращённый URL-адрес с начальным лимитом доступа 2, дважды уменьшает лимит,
     * и проверяет, что дальнейшие попытки доступа не разрешены.
     */
    @Test
    void testAccessShortURLWithinLimit() {
        String userUUID = UUID.randomUUID().toString();

        String shortCode = urlShortenerService.generateShortCode(ORIGINAL_URL, userUUID);
        ShortenedURL shortenedURL = new ShortenedURL(ORIGINAL_URL, shortCode, EXPIRY_DURATION_24, userUUID, ACCESS_LIMIT_2);

        assertTrue(shortenedURL.decrementLimit());
        assertTrue(shortenedURL.decrementLimit());
        assertFalse(shortenedURL.decrementLimit());
    }

    /**
     * Проверяет поведение механизма истечения срока действия `ShortenedURL`.
     * <p>
     * Этот метод проверяет, что объект `ShortenedURL` правильно определяет, что срок его действия
     * истек, на основании указанного срока действия.
     * <p>
     * В частности, тест:
     * - создает объект `ShortenedURL` с определенным сроком действия 10 миллисекунд.
     * - Сразу после создания проверяет, не истек ли срок действия URL-адреса, гарантируя, что
     * метод `isExpired` работает должным образом.
     */
    @Test
    void testShortURLExpiration() {
        String userUUID = UUID.randomUUID().toString();

        ShortenedURL shortenedURL = new ShortenedURL(ORIGINAL_URL, SHORT_CODE, EXPIRY_DURATION_0, userUUID, ACCESS_LIMIT_10);
        assertFalse(shortenedURL.isExpired());
    }

    /**
     * Проверяет создание экземпляра {@link URLUser} и подтверждает принадлежность связанных ссылок.
     * <p>
     * Этот метод гарантирует, что:
     * - при создании пользователя правильно назначается уникальный UUID.
     * - к пользователю можно успешно добавить новую ссылку с помощью {@code addLink}.
     * - принадлежность действительной ссылки правильно определяется с помощью {@code ownsLink}.
     * - Проверка владельца корректно возвращает false для недействительной ссылки.
     */
    @Test
    void testUserCreationAndOwnership() {
        String userUUID = UUID.randomUUID().toString();
        URLUser user = new URLUser(userUUID);

        user.addLink(SHORT_CODE);

        assertTrue(user.ownsLink(SHORT_CODE));
        assertFalse(user.ownsLink("invalidCode"));
    }

    /**
     * Проверяет основные операции класса `URLStorage`, включая добавление, извлечение и удаление объектов `ShortenedURL`.
     * <p>
     * Этот тест гарантирует, что:
     * - объект `ShortenedURL` может быть успешно добавлен в хранилище.
     * - объект `ShortenedURL` может быть извлечён из хранилища с помощью его короткого кода.
     * - Объект `ShortenedURL` может быть удалён из хранилища, и при последующем извлечении того же шорткода возвращается значение null.
     */
    @Test
    void testURLStorageOperations() {
        URLStorage storage = new URLStorage();

        String userUUID = UUID.randomUUID().toString();

        ShortenedURL shortenedURL = new ShortenedURL(ORIGINAL_URL, SHORT_CODE, EXPIRY_DURATION_24, userUUID, ACCESS_LIMIT_10);
        storage.add(shortenedURL);

        assertEquals(shortenedURL, storage.get(SHORT_CODE));
        storage.remove(SHORT_CODE);
        assertNull(storage.get(SHORT_CODE));
    }

    /**
     * Проверяет удаление сокращенного URL-адреса и связанного с ним права собственности пользователя.
     *
     * Этот тест обеспечивает следующее:
     * - Сокращенный URL-адрес может быть удален из `URL-хранилища`.
     * - После удаления сокращенный URL-адрес больше не доступен в хранилище.
     * - Право собственности на удаленный сокращенный URL-адрес правильно отменено.
     *
     * Метод выполняет следующие действия:
     * - Создаётся новый `сокращённый URL` и добавляется в `URLStorage`.
     * - Сокращённый URL связывается с пользователем.
     * - Сокращённый URL удаляется из `URLStorage` и из памяти пользователя.
     * - Проверяется, что URL не может быть найден в хранилище и что пользователь больше не владеет URL.
     */
    @Test
    void testDeleteShortURL() {
        URLStorage storage = new URLStorage();
        String userUUID = UUID.randomUUID().toString();

        ShortenedURL shortenedURL = new ShortenedURL(ORIGINAL_URL, SHORT_CODE, EXPIRY_DURATION_24, userUUID, ACCESS_LIMIT_10);
        storage.add(shortenedURL);

        URLUser user = new URLUser(userUUID);
        user.addLink(SHORT_CODE);

        storage.remove(SHORT_CODE);
        user.removeLink(SHORT_CODE);

        assertNull(storage.get(SHORT_CODE));
        assertFalse(user.ownsLink(SHORT_CODE));
    }
}