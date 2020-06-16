package me.legault.LetItRain;

/**
 * @title GrenadeLaucher
 * @author Bathlamos, Maty241
 * @version 1.0
 * <p>
 * CraftBukkit 1.0.1+
 */

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.regex.Pattern;

public class Launcher implements CommandExecutor {

    public Launcher(LetItRain plugin) {
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player;
        if (!(sender instanceof Player))
            Resources.privateMsg(sender, "Only a player can execute this command");
        else {
            //Permissions
            if (!sender.hasPermission("LetItRain.launcher"))
                return true;


            player = (Player) sender;
            PlayerInventory inventory = player.getInventory();

            inventory.addItem(new ItemStack(Material.getMaterial(LetItRain.item)));

            String outputMsg = LetItRain.dGrenadeMsg;
            outputMsg = outputMsg.replaceAll(Pattern.quote("[player]"), player.getName());
            if (!outputMsg.isEmpty())
                Resources.broadcast(outputMsg);
        }

        return true;

    }

}
