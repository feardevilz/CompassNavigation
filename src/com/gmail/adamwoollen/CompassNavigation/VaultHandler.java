package com.gmail.adamwoollen.CompassNavigation;

import net.milkbowl.vault.economy.Economy;

public class VaultHandler {

	public Economy vaultPlugin;

	public VaultHandler(Economy vaultPlugin) {
		this.vaultPlugin = vaultPlugin;
	}

	public void subtract(String player, double amount) {
		vaultPlugin.withdrawPlayer(player, amount);
	}

	public boolean hasEnough(String player, double amount) {
		return vaultPlugin.has(player, amount);
	}
}