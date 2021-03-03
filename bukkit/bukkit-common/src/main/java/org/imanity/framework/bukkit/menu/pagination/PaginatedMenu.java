/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.bukkit.menu.pagination;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.menu.Button;
import org.imanity.framework.bukkit.menu.Menu;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.util.CC;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class PaginatedMenu extends Menu {

    @Getter
    private int page = 1;

    {
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return getPrePaginatedTitle(player) + " - " + page + "/" + getPages(player);
    }

    /**
     * Changes the page number
     *
     * @param player player viewing the inventory
     * @param mod    delta to modify the page number by
     */
    public final void modPage(Player player, int mod) {
        page += mod;
        getButtons().clear();
        openMenu(player);
    }

    /**
     * @param player player viewing the inventory
     */
    public final int getPages(Player player) {
        int buttonAmount = this.getMaxButtonSlot(player);

        if (buttonAmount == 0) {
            return 1;
        }

        return (int) Math.ceil(buttonAmount / (double) getMaxItemsPerPage(player));
    }

    private final int getMaxButtonSlot(Player player) {
        return getAllPagesButtons(player)
                .keySet()
                .stream().max(Comparator.naturalOrder()).orElse(0);
    }

    @Override
    public final Map<Integer, Button> getButtons(Player player) {
        int minIndex = (int) ((double) (page - 1) * getMaxItemsPerPage(player));
        int maxIndex = (int) ((double) (page) * getMaxItemsPerPage(player));

        HashMap<Integer, Button> buttons = new HashMap<>();

        buttons.put(0, new PageButton(-1, this));
        buttons.put(8, new PageButton(1, this));

        for (Map.Entry<Integer, Button> entry : getAllPagesButtons(player).entrySet()) {
            int ind = entry.getKey();

            if (ind >= minIndex && ind < maxIndex) {
                ind -= (int) ((double) (getMaxItemsPerPage(player)) * (page - 1)) - 9;
                buttons.put(ind, entry.getValue());
            }
        }

        Map<Integer, Button> global = getGlobalButtons(player);

        if (global != null) {
            for (Map.Entry<Integer, Button> gent : global.entrySet()) {
                buttons.put(gent.getKey(), gent.getValue());
            }
        }

        return buttons;
    }

    public int getMaxItemsPerPage(Player player) {
        return 18;
    }

    /**
     * @param player player viewing the inventory
     * @return a Map of buttons that returns items which will be present on all pages
     */
    public Map<Integer, Button> getGlobalButtons(Player player) {
        return null;
    }

    /**
     * @param player player viewing the inventory
     * @return title of the inventory before the page number is added
     */
    public abstract String getPrePaginatedTitle(Player player);

    /**
     * @param player player viewing the inventory
     * @return a map of buttons that will be paginated and spread across pages
     */
    public abstract Map<Integer, Button> getAllPagesButtons(Player player);

    /**
     * @param player The Viewer
     * @param button The Button
     * @return The display ItemStack
     */
    public ItemStack getJumpToPageButtonItem(Player player, JumpToPageButton button) {
        return new ItemBuilder(button.isCurrent() ? Material.ENCHANTED_BOOK : Material.BOOK)
                .name("&ePage " + button.getPage())
                .lore(CC.SB_BAR, button.isCurrent() ? "&aThis is the current page" : "&fClick me jump to this page", CC.SB_BAR)
                .build();
    }

    /**
     * @param player The Viewer
     * @param button The Button
     * @return The display ItemStack
     */
    public ItemStack getPageButtonItem(Player player, PageButton button) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.CARPET).lore(CC.SB_BAR);
        if (button.hasNext(player)) {
            itemBuilder.name(button.getMod() > 0 ? "&aNext Page" : "&cPrevious Page")
                    .lore(button.getMod() > 0 ? "&eLeft Click jump to next page" : "&eLeft Click me jump to previous page");
        } else {
            itemBuilder.name(button.getMod() > 0 ? "&6You are currently at First Page" : "&6You are currently at Last Page")
                    .lore("&cThere is no more page to go!");
        }

        itemBuilder.lore(" ", "&eRight Click to view all pages!", CC.SB_BAR);
        return itemBuilder.build();
    }

    /**
     * @param player The Viewer
     * @return The display title
     */
    public String getViewAllPagesMenuTitle(Player player) {
        return "&aAll Pages";
    }

}
