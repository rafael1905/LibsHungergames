package me.libraryaddict.Hungergames.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Events.TimeSecondEvent;

public class Frosty extends AbilityListener {
    ArrayList<Player> snowRunners = new ArrayList<Player>();
    public int potionMultiplier = 1;
    public int iceRadius = 3;
    public boolean snowballsScheduler = true;
    private HashMap<Entity, Integer> ids = new HashMap<Entity, Integer>();

    @EventHandler
    public void gameStart(GameStartEvent event) {
        if (!snowballsScheduler)
            ProjectileLaunchEvent.getHandlerList().unregister(this);
        else
            ProjectileHitEvent.getHandlerList().unregister(this);
        for (Player p : Bukkit.getOnlinePlayers())
            if (hasAbility(p))
                snowRunners.add(p);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() != null
                && event.getEntity().getShooter() instanceof Player && hasAbility((Player) event.getEntity().getShooter())) {
            transform(event.getEntity().getLocation().clone());
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() != null
                && event.getEntity().getShooter() instanceof Player && hasAbility((Player) event.getEntity().getShooter())) {
            final Entity snowball = event.getEntity();
            ids.put(snowball, Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), new Runnable() {
                public void run() {
                    Material type = snowball.getLocation().getBlock().getType();
                    if (snowball.isDead() || type == Material.WATER || type == Material.STATIONARY_WATER) {
                        transform(snowball.getLocation().clone());
                        if (!snowball.isDead())
                            snowball.remove();
                        Bukkit.getScheduler().cancelTask(ids.remove(snowball));
                    }
                }
            }, 0, 0));
        }
    }

    private void transform(Location loc) {
        if (net.minecraft.server.v1_5_R3.Block.SNOW.canPlace(((CraftWorld) loc.getWorld()).getHandle(), loc.getBlockX(),
                loc.getBlockY(), loc.getBlockZ()))
            loc.getBlock().setType(Material.SNOW);
        loc.add(loc.getBlockX() + 0.5, 0, loc.getBlockZ() + 0.5);
        for (int x = -iceRadius; x <= iceRadius; x++) {
            for (int z = -iceRadius; z <= iceRadius; z++) {
                Block b = loc.clone().add(x, 0, z).getBlock();
                if (b.getLocation().distance(loc) < iceRadius
                        && (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER)) {
                    b.setType(Material.ICE);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.SNOW
                && hasAbility(event.getPlayer())
                && (event.getPlayer().getItemInHand() == null || !event.getPlayer().getItemInHand().getType().name()
                        .contains("SPADE"))) {
            event.getBlock()
                    .getWorld()
                    .dropItemNaturally(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5),
                            new ItemStack(Material.SNOW_BALL));
        }
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        for (Player p : snowRunners) {
            if (p.getLocation().getBlock().getType() == Material.SNOW) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, potionMultiplier), true);
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        snowRunners.remove(event.getKilled().getPlayer());
    }

}