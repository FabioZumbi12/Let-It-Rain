package me.legault.LetItRain;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Dispenser;
import org.bukkit.util.Vector;

import java.util.*;

public class Events implements Listener {

    private static List<Player> waitP = new ArrayList<Player>();
    private static List<BlockFace> faces = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN, BlockFace.SELF);
    private LinkedList<Entity> snows = new LinkedList<Entity>();

    private static void StrikeRain(Player player, Location location) {
        World world = player.getWorld();
        world.createExplosion(location, LetItRain.dLightningPower);
        world.strikeLightning(location);
        if (LetItRain.usingZeus) {
            LetItRain.plugin.getServer().getConsoleSender().sendMessage("Using Lightning: " + player.getName());
        }
    }

    private static void strikeWait(final Player player, final Location location) {
        if (player.hasPermission("LetItRain.zeus.bypass")) {
            StrikeRain(player, location);
            return;
        }
        if (!waitP.contains(player)) {
            StrikeRain(player, location);
            waitP.add(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(LetItRain.plugin, new Runnable() {
                @Override
                public void run() {
                    waitP.remove(player);
                }
            }, LetItRain.Zeusdelay * 20);
        } else {
            Resources.privateMsg(player, LetItRain.ZeusWait);
        }
    }

    @EventHandler
    public void spawn(ProjectileHitEvent event) {

        boolean isOnFire = false;
        if (Rain.thrownedItems.containsKey(event.getEntity())) {
            isOnFire = Rain.thrownedItems.remove(event.getEntity());
            Entity ent = event.getEntity();
            if (LetItRain.dRemoveArtifact)
                event.getEntity().remove();
            if (isOnFire && ent instanceof Snowball)
                ent.getWorld().createExplosion(ent.getLocation(), 5f);
        }

        // <!-- Snow explosion -->
        if (snows.contains(event.getEntity())) {
            Entity egg = event.getEntity();
            egg.getWorld().createExplosion(egg.getLocation(), 5f);
            snows.remove(event.getEntity());
        }

        Entity ent = event.getEntity();

        if (ent instanceof Arrow && isOnFire) {

            Block target = ent.getWorld().getBlockAt(ent.getLocation());

            target.setType(Material.FIRE);

            target.getRelative(BlockFace.UP).setType(Material.FIRE);

            if (LetItRain.destructiveArrows) {
                for (Iterator<BlockFace> iterator = faces.iterator(); iterator.hasNext(); ) {
                    BlockFace blockFace = (BlockFace) iterator.next();
                    Block t = target.getRelative(blockFace);

                    //if (rdm.nextFloat() < 0.25f){
                    t.setType(Material.FIRE);
                    //}
                }
            }

        }
        //<!-- -->

        Rain.thrownedItems.remove(event.getEntity());

    }

    @EventHandler
    public void spawn(EntityDeathEvent event) {
        if (LetItRain.dRemoveArtifact && Rain.thrownedItems.containsKey(event.getEntity()))
            event.getDrops().removeAll(event.getDrops());

        Rain.thrownedItems.remove(event.getEntity());
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void interact(PlayerInteractEvent event) {

        if (event.getPlayer().getItemInHand().getType().name().equalsIgnoreCase(LetItRain.item) && event.getPlayer().hasPermission("LetItRain.launcher")) {

            Entity snow = event.getPlayer().launchProjectile(Snowball.class);
            snow.setFireTicks(1000);
            snows.add(snow);

        } else if (event.getPlayer().getItemInHand().getType().name().equalsIgnoreCase(LetItRain.itemZeus) && event.getPlayer().hasPermission("LetItRain.zeus")) {
            Player player = event.getPlayer();
            Location location = player.getTargetBlock(null, 800).getLocation();

            if (LetItRain.RedProtect) {
                Region reg1 = RedProtect.get().rm.getTopRegion(location);
                if (reg1 != null) {
                    if (reg1.canBuild(player) || reg1.canFire()) {
                        strikeWait(player, location);
                    } else {
                        RPLang.sendMessage(player, "playerlistener.region.cantuse");
                    }
                    return;
                }
            }

            strikeWait(player, location);
        }
    }

    @EventHandler
    public void dispenser(BlockDispenseEvent event) {
        if (event.getBlock().getType().equals(Material.DISPENSER) && event.getItem().getType().name().contains("SNOW_BALL") && LetItRain.dispenserWorksWithFireSnowballs) {

            event.setCancelled(true);

            InventoryHolder dispenser = (InventoryHolder) event.getBlock().getState();
            dispenser.getInventory().remove(event.getItem());
            Dispenser disp = (Dispenser) event.getBlock().getState().getData();
            Block block = event.getBlock();
            BlockFace facing = disp.getFacing();

            Snowball snowball = block.getWorld().spawn(block.getLocation().add(facing.getModX(), facing.getModY(), facing.getModZ()), Snowball.class);
            Random rdm = new Random();
            float rdmPrec = 0.2f;
            snowball.setVelocity(new Vector(facing.getModX() + rdm.nextFloat() * rdmPrec, facing.getModY() + rdm.nextFloat() * rdmPrec, facing.getModZ() + rdm.nextFloat() * rdmPrec));
            snowball.setFireTicks(300);
            snows.add(snowball);
        }
    }

    @EventHandler
    public void update(PlayerJoinEvent event) {
        if (LetItRain.isToBeUpdated)
            Resources.privateMsg(event.getPlayer(), Resources.getPluginTitle() + " needs to be updated to version " + LetItRain.newVersion);
    }

}
