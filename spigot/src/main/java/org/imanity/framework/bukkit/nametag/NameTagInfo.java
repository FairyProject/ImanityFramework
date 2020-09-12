package org.imanity.framework.bukkit.nametag;

import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public final class NameTagInfo {

    private static final AtomicInteger TEAM_INDEX = new AtomicInteger(0);

    private String name;
    private String prefix;
    private String suffix;

    private Set<String> nameSet;

    protected NameTagInfo(final String prefix, final String suffix) {
        this.name = "ImanityTeam-" + TEAM_INDEX.getAndIncrement();
        this.prefix = prefix;
        this.suffix = suffix;
        this.nameSet = new HashSet<>();
    }

    public void addName(String name) {
        this.nameSet.add(name);
    }

    public void removeName(String name) {
        this.nameSet.remove(name);
    }

    public boolean hasName(String name) {
        return this.nameSet.contains(name);
    }
}
