package org.imanity.framework.menu.pagination;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.Imanity;
import org.imanity.framework.menu.Button;
import org.imanity.framework.menu.Menu;
import org.imanity.framework.menu.buttons.BackButton;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ViewAllPagesMenu extends Menu {

	@NonNull
	@Getter
	PaginatedMenu menu;

	@Override
	public String getTitle(final Player player) {
		return "選擇頁面";
	}

	@Override
	public Map<Integer, Button> getButtons(final Player player) {
		final HashMap<Integer, Button> buttons = new HashMap<>();

		buttons.put(0, new BackButton(menu));

		int index = 10;

		for (int i = 1; i <= menu.getPages(player); i++) {
			buttons.put(index++, new JumpToPageButton(i, menu, menu.getPage() == i));

			if ((index - 8) % 9 == 0) {
				index += 2;
			}
		}

		return buttons;
	}

	@Override
	public boolean isAutoUpdate() {
		return true;
	}

}
