package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class ProtocolLibHandler {
	
	public CompassNavigation plugin;
	
	public ProtocolLibHandler(CompassNavigation plugin) {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
	    	plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 
	    	Packets.Server.SET_SLOT, Packets.Server.WINDOW_ITEMS) {
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketID() == Packets.Server.SET_SLOT) {
					removeAttributes(new ItemStack[] { event.getPacket().getItemModifier().read(0) });
					addGlow(new ItemStack[] { event.getPacket().getItemModifier().read(0) });
				} else {
					removeAttributes(event.getPacket().getItemArrayModifier().read(0));
					addGlow(event.getPacket().getItemArrayModifier().read(0));
	            }
	        }
		});
	}
	
	public void removeAttributes(ItemStack[] stacks) {
		for (ItemStack stack : stacks) {
			if (stack != null) {
				NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
				compound.put(NbtFactory.ofList("AttributeModifiers"));
			}
		}
	}
	
    public void addGlow(ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (stack != null) {
                if (stack.getEnchantmentLevel(Enchantment.WATER_WORKER) == 4) {
                    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
                    compound.put(NbtFactory.ofList("ench"));
                }
            }
        }
    }
}