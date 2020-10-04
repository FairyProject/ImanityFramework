package org.imanity.framework.locale;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.data.PlayerDataBuilder;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(name = "locale")
public class LocaleHandler implements IService {

    private Map<String, Locale> locales;
    @Getter
    private Locale defaultLocale;

    public void init() {
        this.locales = new HashMap<>();
        this.defaultLocale = this.getOrRegister(ImanityCommon.CORE_CONFIG.DEFAULT_LOCALE);

        new PlayerDataBuilder()
                .loadOnJoin(true)
                .saveOnQuit(true)
                .name("locale")
                .playerDataClass(LocaleData.class)
                .build();
    }

    public Locale getOrRegister(String name) {
        Locale locale;

        if (this.locales.containsKey(name)) {
            locale = this.locales.get(name);
        } else {
            locale = new Locale(name);
            this.locales.put(name, locale);
        }

        return locale;
    }

    public Locale registerFromYml(File file) {

        Map<String, Object> map = ImanityCommon.BRIDGE.loadYaml(file);
        String name = map.get("locale").toString();

        Locale locale = this.getOrRegister(name);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("locale")) {
                continue;
            }
            locale.registerEntry(entry.getKey(), entry.getValue().toString());
        }

        return locale;

    }

    public Locale registerFromYml(InputStream inputStream) {
        Map<String, Object> map = ImanityCommon.BRIDGE.loadYaml(inputStream);
        String name = map.get("locale").toString();

        Locale locale = this.getOrRegister(name);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("locale")) {
                continue;
            }

            if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                locale.registerEntry(entry.getKey(), (String[]) list.stream().map(Object::toString).toArray(String[]::new));
            } else if (entry.getValue() instanceof String) {
                locale.registerEntry(entry.getKey(), entry.getValue().toString());
            }
        }

        return locale;
    }

    public void unregisterLocale(String name) {
        this.locales.remove(name);
    }

    public Locale getLocale(String name) {
        if (this.locales.containsKey(name)) {
            return this.locales.get(name);
        }

        return null;
    }

}
