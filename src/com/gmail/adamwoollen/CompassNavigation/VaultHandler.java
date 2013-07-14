package com.gmail.adamwoollen.CompassNavigation;

import net.milkbowl.vault.economy.Economy;

public class VaultHandler {

  private final Economy vaultPlugin;

	VaultHandler(CompassNavigation plugin, Economy vaultPlugin) {
		this.vaultPlugin = vaultPlugin;
	}

	boolean subtract(String player, double amount) {
		return vaultPlugin.withdrawPlayer(player, amount).transactionSuccess();
	}

	boolean hasEnough(String player, double amount) {
		return vaultPlugin.has(player, amount);
	}
}
