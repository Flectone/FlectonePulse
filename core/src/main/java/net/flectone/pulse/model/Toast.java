package net.flectone.pulse.model;

import lombok.Getter;
import net.flectone.pulse.util.AdvancementType;

@Getter
public class Toast {

	private final String icon;
	private final AdvancementType style;

	public Toast(String icon, AdvancementType style) {
		this.icon = icon;
		this.style = style;
	}
}
