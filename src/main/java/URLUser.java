import java.util.HashSet;
import java.util.Set;

public class URLUser {
    private final String uuid;
    private final Set<String> links;

    public URLUser(String uuid) {
        this.uuid = uuid;
        this.links = new HashSet<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void addLink(String shortCode) {
        links.add(shortCode);
    }

    public boolean ownsLink(String shortCode) {
        return links.contains(shortCode);
    }

}
