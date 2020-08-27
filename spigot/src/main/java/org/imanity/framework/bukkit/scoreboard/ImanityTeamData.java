package org.imanity.framework.bukkit.scoreboard;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ImanityTeamData {

    private final String name;
    private final String prefix;
    private final boolean friendlyInvisibles;
    private final boolean friendlyFire;

    private final List<String> nameSet = new ArrayList<>();

    public void addName(String name) {
        if (!this.nameSet.contains(name)) {
            this.nameSet.add(name);
        }
    }

    public void removeName(String name) {
        this.nameSet.remove(name);
    }

}
