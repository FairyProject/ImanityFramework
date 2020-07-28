package org.imanity.framework.bossbar;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BossBarData {

    private String text;

    /**
     *
     * Health
     * Max health = 100
     *
     */
    private float health;

}
