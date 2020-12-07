package org.imanity.framework.bukkit.command.test;

import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.imanity.framework.bukkit.command.event.BukkitCommandEvent;
import org.imanity.framework.command.annotation.Command;
import org.imanity.framework.command.annotation.CommandHolder;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.locale.LocaleRepository;

@Component
public class TestCommand implements CommandHolder {

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private LocaleHandler localeHandler;

    @Command(names = "t")
    public void test(BukkitCommandEvent event) {
        event.getPlayer().sendMessage(localeRepository.find(event.getPlayer().getUniqueId()).getLocale().getName());
    }

    @Command(names = "tset")
    public void tset(BukkitCommandEvent event, String locale) {
        localeRepository.find(event.getPlayer().getUniqueId()).setLocale(localeHandler.getLocale("zh_tw"));
    }

}
