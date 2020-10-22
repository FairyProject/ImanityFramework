/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.imanity.framework.player.IPlayerBridge;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public abstract class PlayerData extends AbstractData {

    public static IPlayerBridge PLAYER_BRIDGE;

    public PlayerData(Object player) {
        this(PlayerData.PLAYER_BRIDGE.getUUID(player),
                PlayerData.PLAYER_BRIDGE.getName(player));
    }

    @JsonProperty
    private String name;

    public PlayerData(UUID uuid, String name) {
        super(uuid);
        this.name = name;
    }

    public PlayerData(UUID uuid) {
        this(uuid, "");
    }

    public static PlayerDataBuilder builder() {
        return new PlayerDataBuilder();
    }

    public void disconnect() {

    }

}
