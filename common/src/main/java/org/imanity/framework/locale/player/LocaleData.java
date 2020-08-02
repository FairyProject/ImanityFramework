package org.imanity.framework.locale.player;

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.annotation.StoreData;

import java.util.UUID;

@Getter
@Setter
public class LocaleData extends PlayerData {

    @StoreData
    private Locale locale = ImanityCommon.LOCALE_HANDLER.getDefaultLocale();

    public LocaleData(UUID uuid, String name) {
        super(uuid, name);
    }
}
