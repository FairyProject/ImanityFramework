package org.imanity.framework.bukkit.visibility;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.plugin.component.ComponentHolder;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import java.util.LinkedList;
import java.util.List;

@Service(name = "visibility")
public class VisibilityService implements IService {

    private List<VisibilityAdapter> visibilityAdapters;

    @Override
    public void preInit() {
        this.visibilityAdapters = new LinkedList<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {

            @Override
            public Object newInstance(Class<?> type) {
                Object instance = super.newInstance(type);
                register((VisibilityAdapter) instance);

                return instance;
            }

            @Override
            public Class<?>[] type() {
                return new Class[] { VisibilityAdapter.class };
            }
        });
    }

    public void register(VisibilityAdapter visibilityAdapter) {
        this.visibilityAdapters.add(visibilityAdapter);
    }

    public boolean isUsed() {
        return !this.visibilityAdapters.isEmpty();
    }

    public boolean treatAsOnline(final Player target, final Player viewer) {
        return viewer.canSee(target);
    }

    public void updateAll() {

        for (Player player : Imanity.getPlayers()) {
            this.updateFromThirdSide(player);
        }

    }

    public void update(Player player) {

        if (this.isUsed()) {

            this.updateFromFirstSide(player);
            this.updateFromThirdSide(player);

        }

    }

    public void updateFromFirstSide(Player player) {
        for (Player target : Imanity.getPlayers()) {

            if (target == player) {
                continue;
            }

            if (this.canSee(player, target)) {
                player.showPlayer(target);
            } else {
                player.hidePlayer(target);
            }

        }
    }

    public void updateFromThirdSide(Player player) {
        for (Player target : Imanity.getPlayers()) {

            if (target == player) {
                continue;
            }

            if (this.canSee(target, player)) {
                target.showPlayer(player);
            } else {
                target.hidePlayer(player);
            }

        }
    }


    public boolean canSee(Player receiver, Player target) {
        for (VisibilityAdapter visibilityAdapter : this.visibilityAdapters) {
            VisibilityOption option = visibilityAdapter.check(receiver, target);

            switch (option) {
                case SHOW:
                    return true;
                case HIDE:
                    return false;
                case NOTHING:
                    break;
            }
        }

        return true;
    }
}
