import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class URLStorage {
    private final Map<String, ShortenedURL> storage = new ConcurrentHashMap<>();

    public void add(ShortenedURL shortenedURL) {
        storage.put(shortenedURL.getShortCode(), shortenedURL);
    }

    public ShortenedURL get(String shortCode) {
        return storage.get(shortCode);
    }

    public void remove(String shortCode) {
        storage.remove(shortCode);
    }
}
