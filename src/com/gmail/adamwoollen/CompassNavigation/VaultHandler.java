package com.gmail.adamwoollen.CompassNavigation;

import net.milkbowl.vault.economy.Economy;

public class VaultHandler {

	public Economy vaultPlugin;

	public VaultHandler(CompassNavigation plugin, Economy vaultPlugin) {
		this.vaultPlugin = vaultPlugin;
	}

	public boolean subtract(String player, double amount) {
		return vaultPlugin.withdrawPlayer(player, amount).transactionSuccess();
	}

	public boolean hasEnough(String player, double amount) {
		return vaultPlugin.has(player, amount);
	}
}