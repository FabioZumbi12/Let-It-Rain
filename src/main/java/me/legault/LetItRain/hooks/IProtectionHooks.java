package me.legault.LetItRain.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IProtectionHooks {
    boolean canBuild(Location location, Player player);
}
