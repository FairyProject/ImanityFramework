package org.imanity.framework.locale.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.data.PlayerData;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class LocaleData extends PlayerData {

    @JsonProperty
    private Locale locale = ImanityCommon.LOCALE_HANDLER.getDefaultLocale();

    public LocaleData(UUID uuid, String name) {
        super(uuid, name);
    }
}
