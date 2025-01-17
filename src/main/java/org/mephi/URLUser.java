package org.mephi;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class URLUser {
    @Getter
    private final String uuid;
    private final Set<String> links;

    public URLUser(String uuid) {
        this.uuid = uuid;
        this.links = new HashSet<>();
    }

    public void addLink(String shortCode) {
        links.add(shortCode);
    }

    public void removeLink(String shortCode) {
        links.remove(shortCode);
    }

    public boolean ownsLink(String shortCode) {
        return links.contains(shortCode);
    }

}
