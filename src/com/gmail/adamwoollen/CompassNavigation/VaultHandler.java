package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultHandler {

	public Economy economy;

	public VaultHandler(CompassNavigation plugin) {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
	}

	public void subtract(String player, double amount) {
		economy.withdrawPlayer(player, amount);
	}

	public boolean hasEnough(String player, double amount) {
		return economy.has(player, amount);
	}
}