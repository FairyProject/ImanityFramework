package org.imanity.framework.menu.menus;

import org.imanity.framework.menu.Button;
import org.imanity.framework.menu.Menu;
import org.imanity.framework.menu.buttons.ConfirmationButton;
import org.imanity.framework.util.TypeCallback;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ConfirmMenu extends org.imanity.framework.menu.Menu {

	private final String title;
	private final TypeCallback<Boolean> response;
	private final boolean closeAfterResponse;
	private final org.imanity.framework.menu.Button[] centerButtons;

	public ConfirmMenu(final String title, final TypeCallback<Boolean> response, final boolean closeAfter, final org.imanity.framework.menu.Button... centerButtons) {
		this.title = title;
		this.response = response;
		this.closeAfterResponse = closeAfter;
		this.centerButtons = centerButtons;
	}

	@Override
	public Map<Integer, Button> getButtons(final Player player) {
		final HashMap<Integer, Button> buttons = new HashMap<>();

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				buttons.put(getSlot(x, y), new ConfirmationButton(true, response, closeAfterResponse));
				buttons.put(getSlot(8 - x, y), new ConfirmationButton(false, response, closeAfterResponse));
			}
		}

		if (centerButtons != null) {
			for (int i = 0; i < centerButtons.length; i++) {
				if (centerButtons[i] != null) {
					buttons.put(getSlot(4, i), centerButtons[i]);
				}
			}
		}

		return buttons;
	}

	@Override
	public String getTitle(final Player player) {
		return title;
	}

}
