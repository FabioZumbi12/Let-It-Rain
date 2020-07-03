package me.legault.LetItRain.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RedProtectHook implements IProtectionHooks {
    @Override
    public boolean canBuild(Location location, Player player) {
        Region reg1 = RedProtect.get().getAPI().getRegion(location);
        if (reg1 != null) {
            if (reg1.canBuild(player) || reg1.canFire()) {
                return true;
            } else {
                RedProtect.get().getAPI().getMessageApi().sendMessage(player, "playerlistener.region.cantuse");
            }
        }
        return false;
    }
}
