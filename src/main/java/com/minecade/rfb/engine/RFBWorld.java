package com.minecade.rfb.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterators;
import com.minecade.engine.utils.EngineUtils;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

public class RFBWorld {
    
    RFBWorldName worldName;
    private List<Vector> playerSpawnLocations = new ArrayList<Vector>();
    private List<Vector> playerFreeSpawnLocations = new ArrayList<Vector>();
    private Vector beastSpawnLocation;
    private Vector spectatorSpawnLocation;
    private ConcurrentMap<String, Vector> buttonLocations;
    private ConcurrentMap<String, Vector> chestLocations;
    private final World world;
    private Iterator<Vector> playerSpawnCycleIterator;

    public RFBWorld(RFBWorldName name) {
        this(name, Environment.NORMAL);
    }

    public RFBWorld(RFBWorldName name, Environment environment) {
        this.worldName = name;
        // initialize world related objects
        WorldCreator worldCreator = new WorldCreator(worldName.name());
        worldCreator.generator(RunFromTheBeastPlugin.getInstance().getEmptyGenerator());
        worldCreator.environment(environment);
        world = worldCreator.createWorld();
        EngineUtils.setupWorld(world);
        this.setupRFBWorld(this.worldName);
        this.playerSpawnCycleIterator = Iterators.cycle(this.playerSpawnLocations);
    }

    private void setupRFBWorld(RFBWorldName name){
        List<Vector> playerSpawnLocations = new ArrayList<Vector>();
        List<Vector> playerFreeSpawnLocations = new ArrayList<Vector>();
        this.buttonLocations = new ConcurrentHashMap<String, Vector>();
        this.chestLocations = new ConcurrentHashMap<String, Vector>();
        switch (name) {
        case DamnedTunnelsWorld:
            this.world.getBlockAt(20, 70, 69).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "damnedtunnelsworld-finish"));
            this.world.getBlockAt(22, 96, 59).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "damnedtunnelsworld-back-finish"));
            this.world.getBlockAt(22, 96, 57).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "damnedtunnelsworld-back-beggining"));
            playerSpawnLocations.add(new Vector(12, 33, -37));
            playerSpawnLocations.add(new Vector(12, 33, -37));
            playerSpawnLocations.add(new Vector(12, 33, -37));
            playerSpawnLocations.add(new Vector(12, 33, -37));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(20, 62.5, -60));
            playerFreeSpawnLocations.add(new Vector(20, 62.5, -60));
            playerFreeSpawnLocations.add(new Vector(20, 62.5, -60));
            playerFreeSpawnLocations.add(new Vector(20, 62.5, -60));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(36, 34, -37);
            spectatorSpawnLocation = new Vector(20, 62.5, -60);
            buttonLocations.put("damnedtunnelsworld-finish", new Vector(18, 95, 57.75));
            buttonLocations.put("damnedtunnelsworld-back-finish", new Vector(21.5, 65, 60.5));
            buttonLocations.put("damnedtunnelsworld-back-beggining", new Vector(20.5, 62.5, -59.5));
            //chest
            chestLocations.put("finalchestone", new Vector(13, 95, 58));
            chestLocations.put("finalchesttwo", new Vector(18, 95, 53));
            chestLocations.put("finalchestthree", new Vector(18, 95, 63));
            break;
        case HaloRaceWorld:
            this.world.getBlockAt(538, 4, -886).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "haloraceworld-initial"));
            this.world.getBlockAt(396, 24, -845).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "haloraceworld-finish"));
            this.world.getBlockAt(397, 23, -835).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "haloraceworld-back-finish"));
            this.world.getBlockAt(395, 23, -835).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "haloraceworld-back-beggining"));
            
            playerSpawnLocations.add(new Vector(540.5, 5, -869.4));
            playerSpawnLocations.add(new Vector(540.5, 5, -871.5));
            playerSpawnLocations.add(new Vector(538, 5, -871.5));
            playerSpawnLocations.add(new Vector(538, 5, -869.4));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(538, 5, -874));
            playerFreeSpawnLocations.add(new Vector(538, 5, -874));
            playerFreeSpawnLocations.add(new Vector(538, 5, -874));
            playerFreeSpawnLocations.add(new Vector(538, 5, -874));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(534.56, 5, -875.43);
            spectatorSpawnLocation = new Vector(523.5, 4.5, -872.5);
            buttonLocations.put("haloraceworld-initial", new Vector(523.5, 4, -872.73));
            buttonLocations.put("haloraceworld-finish", new Vector(396.6, 23, -838.3));
            buttonLocations.put("haloraceworld-back-finish", new Vector(397.5, 23, -844.5));
            buttonLocations.put("haloraceworld-back-beggining", new Vector(538.5, 5, -873.5));
            chestLocations.put("finalchestone", new Vector(399, 23, -840));
            chestLocations.put("finalchesttwo", new Vector(399, 23, -841));
            break;
        case IslandWorld:
          this.world.getBlockAt(-12, 72, 991).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "islandworld-finishOne"));
          this.world.getBlockAt(-21, 72, 976).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "islandworld-finishTwo"));
            playerSpawnLocations.add(new Vector(-84, 65, 1102));
            playerSpawnLocations.add(new Vector(-84, 65, 1102));
            playerSpawnLocations.add(new Vector(-84, 65, 1102));
            playerSpawnLocations.add(new Vector(-84, 65, 1102));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
            playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
            playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
            playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(-69, 66, 1106);
            spectatorSpawnLocation = new Vector(-56, 65, 1097);
            buttonLocations.put("islandworld-finishOne", new Vector(-21.33, 73, 1056.72));
            buttonLocations.put("islandworld-finishTwo", new Vector(8.5, 85, 1138.96));
            chestLocations.put("finalchestone", new Vector(-20, 71, 975));
            chestLocations.put("finalchesttwo", new Vector(-11, 71, 991));
            break;
        case IslandCopyWorld:
            this.world.getBlockAt(-12, 72, 991).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "islandCopyworld-finishOne"));
            this.world.getBlockAt(-21, 72, 976).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "islandCopyworld-finishTwo"));
              playerSpawnLocations.add(new Vector(-84, 65, 1102));
              playerSpawnLocations.add(new Vector(-84, 65, 1102));
              playerSpawnLocations.add(new Vector(-84, 65, 1102));
              playerSpawnLocations.add(new Vector(-84, 65, 1102));
              this.playerSpawnLocations = playerSpawnLocations;
              playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
              playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
              playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
              playerFreeSpawnLocations.add(new Vector(7, 85, 1138));
              this.playerFreeSpawnLocations = playerFreeSpawnLocations;
              beastSpawnLocation = new Vector(-69, 66, 1106);
              spectatorSpawnLocation = new Vector(-56, 65, 1097);
              buttonLocations.put("islandCopyworld-finishOne", new Vector(-21.33, 73, 1056.72));
              buttonLocations.put("islandCopyworld-finishTwo", new Vector(8.5, 85, 1138.96));
              chestLocations.put("finalchestone", new Vector(-20, 71, 975));
              chestLocations.put("finalchesttwo", new Vector(-11, 71, 991));
              break;
        case NetherCustomWorld:
            this.world.getBlockAt(163, 56, 55).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "netherworld-finish"));
            this.world.getBlockAt(153, 55, 54).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "netherworld-back-finish"));
            this.world.getBlockAt(153, 55, 56).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "netherworld-back-beggining"));
            playerSpawnLocations.add(new Vector(84.13, 34.0, 8.0));
            playerSpawnLocations.add(new Vector(85.13, 34.0, 8.0));
            playerSpawnLocations.add(new Vector(84.13, 34.0, 7.0));
            playerSpawnLocations.add(new Vector(85.13, 34.0, 7.0));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(64.5, 60, -4.5));
            playerFreeSpawnLocations.add(new Vector(64.5, 60, -4.5));
            playerFreeSpawnLocations.add(new Vector(64.5, 60, -4.5));
            playerFreeSpawnLocations.add(new Vector(64.5, 60, -4.5));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(79.5, 34, 8);
            spectatorSpawnLocation = new Vector(131, 81, -9);
            buttonLocations.put("netherworld-finish", new Vector(154.5, 54.5, 55.5));
            buttonLocations.put("netherworld-back-finish", new Vector(181.5, 64, 30.26));
            buttonLocations.put("netherworld-back-beggining", new Vector(64.5, 60, -4.5));
            chestLocations.put("finalchestone", new Vector(155, 54, 58));
            chestLocations.put("finalchesttwo", new Vector(157, 54, 55));
            chestLocations.put("finalchestthree", new Vector(155, 54, 52));
            break;
        case BeastCave:
            this.world.getBlockAt(-162, 59, 820).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "beastcave-finish"));
            this.world.getBlockAt(-187, 45, 761).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "beastcave-middle"));
            this.world.getBlockAt(-184, 59, 818).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "beastcave-back-finish"));
            this.world.getBlockAt(-188, 59, 818).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "beastcave-back-beggining"));
            playerSpawnLocations.add(new Vector(-6, 6.5, 678));
            playerSpawnLocations.add(new Vector(-6, 6.5, 679));
            playerSpawnLocations.add(new Vector(-7, 6.5, 678));
            playerSpawnLocations.add(new Vector(-7, 6.5, 679));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(-185.57, 47, 677.64));
            playerFreeSpawnLocations.add(new Vector(-186.57, 47, 677.64));
            playerFreeSpawnLocations.add(new Vector(-185.57, 47, 678.64));
            playerFreeSpawnLocations.add(new Vector(-186.57, 47, 678.64));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(25, 7, 678.72);
            spectatorSpawnLocation = new Vector(-185.57, 47, 677.64);
            buttonLocations.put("beastcave-finish", new Vector(-185, 58, 828));
            buttonLocations.put("beastcave-middle", new Vector(-186.55, 42, 765.5));
            buttonLocations.put("beastcave-back-finish", new Vector(-161.52, 58, 822.97));
            buttonLocations.put("beastcave-back-beggining", new Vector(-185.57, 45, 677.64));
            chestLocations.put("finalchestone", new Vector(-194, 58, 827));
            chestLocations.put("finalchesttwo", new Vector(-186, 58, 835));
            chestLocations.put("finalchestthree", new Vector(-178, 58, 827));
            break;
        case LevelsWorld:
            this.world.getBlockAt(13, 60, 7).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "levelsworld-finish"));
            this.world.getBlockAt(6, 69, 6).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "levelsworld-back-finish"));
            this.world.getBlockAt(6, 69, 8).setMetadata("buttonId", new FixedMetadataValue(RunFromTheBeastPlugin.getInstance(), "levelsworld-back-beggining"));
            playerSpawnLocations.add(new Vector(-9, 22, -12));
            playerSpawnLocations.add(new Vector(-9, 22, -12));
            playerSpawnLocations.add(new Vector(-9, 22, -12));
            playerSpawnLocations.add(new Vector(-9, 22, -12));
            this.playerSpawnLocations = playerSpawnLocations;
            playerFreeSpawnLocations.add(new Vector(-8.6, 20, -1.32));
            playerFreeSpawnLocations.add(new Vector(-8.6, 20, 2.26));
            playerFreeSpawnLocations.add(new Vector(-10.30, 20, 2.26));
            playerFreeSpawnLocations.add(new Vector(-10.30, 20, -1.32));
            this.playerFreeSpawnLocations = playerFreeSpawnLocations;
            beastSpawnLocation = new Vector(-3.6, 22, -12.3);
            spectatorSpawnLocation = new Vector(-8.6, 20, -1.32);
            buttonLocations.put("levelsworld-finish", new Vector(12.5, 69, 7.5));
            buttonLocations.put("levelsworld-back-finish", new Vector(12.5, 60, 7.5));
            buttonLocations.put("levelsworld-back-beggining", new Vector(-8.5, 20, 0.5));
            chestLocations.put("finalchestone", new Vector(16, 69, 7));
            chestLocations.put("finalchesttwo", new Vector(11, 69, 2));
            chestLocations.put("finalchestthree", new Vector(11, 69, 12));
            break;
        default:
            break;
        }
        populateChest();
    }
    
    public void reloadWorld() {
        clearEntities();
        clearPlayers();
        populateChest();
    }
    
    public void clearEntities() {
        if (null != world) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    private void clearPlayers() {
        if (null != world) {
            List<Player> players = world.getPlayers();
            for (Player player : players) {
                player.kickPlayer("This world is reloading...");
            }
        }
    }
    
    private void populateChest(){
        if(null != worldName){
            Map<Enchantment, Integer> armorEnchantments = new HashMap<Enchantment, Integer>();
            armorEnchantments.put(Enchantment.DURABILITY , Enchantment.DURABILITY.getMaxLevel());
            armorEnchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel());
            armorEnchantments.put(Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_EXPLOSIONS.getMaxLevel());
            armorEnchantments.put(Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_FALL.getMaxLevel());
            armorEnchantments.put(Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_FIRE.getMaxLevel());
            armorEnchantments.put(Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_PROJECTILE.getMaxLevel());
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            //helmet
            ItemStack helmet = new ItemStack((Material.DIAMOND_HELMET));
            helmet.addUnsafeEnchantments(armorEnchantments);
            items.add(helmet);
            //chestplate
            ItemStack chestplate = new ItemStack((Material.DIAMOND_CHESTPLATE));
            chestplate.addUnsafeEnchantments(armorEnchantments);
            items.add(chestplate);
            //leggins
            ItemStack leggins = new ItemStack((Material.DIAMOND_LEGGINGS));
            leggins.addUnsafeEnchantments(armorEnchantments);
            items.add(leggins);
            //boots
            ItemStack boots = new ItemStack((Material.DIAMOND_BOOTS));
            boots.addUnsafeEnchantments(armorEnchantments);
            items.add(boots);
            //sword
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
            Map<Enchantment, Integer> swordEnchantments = new HashMap<Enchantment, Integer>();
            swordEnchantments.put(Enchantment.KNOCKBACK, Enchantment.KNOCKBACK.getMaxLevel());
            swordEnchantments.put(Enchantment.DURABILITY, Enchantment.DURABILITY.getMaxLevel());
            swordEnchantments.put(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ALL.getMaxLevel());
            swordEnchantments.put(Enchantment.FIRE_ASPECT, Enchantment.FIRE_ASPECT.getMaxLevel());
            sword.addEnchantments(swordEnchantments);
            items.add(sword);
            //bow and arrow
            ItemStack bow = new ItemStack(Material.BOW, 1);
            Map<Enchantment, Integer> bowEnchantments = new HashMap<Enchantment, Integer>();
            bowEnchantments.put(Enchantment.ARROW_INFINITE, Enchantment.ARROW_INFINITE.getMaxLevel());
            bowEnchantments.put(Enchantment.ARROW_FIRE, Enchantment.ARROW_FIRE.getMaxLevel());
            //bowEnchantments.put(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ALL.getMaxLevel());
            bowEnchantments.put(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_DAMAGE.getMaxLevel());
            bow.addEnchantments(bowEnchantments);
            items.add(bow);
            items.add(new ItemStack((Material.ARROW), 1));
            
            ArrayList<Chest> chests = new ArrayList<Chest>();
            switch (this.worldName) {
            case NetherCustomWorld:
            case DamnedTunnelsWorld:
            case BeastCave:
                //TODO add chest for beast with potions
            case LevelsWorld:
                // FIXME fix this
//                Block levelsBlock = this.world.getBlockAt(this.getChestLocations("finalchestthree"));
//                Chest levelsChest = (Chest) levelsBlock.getState();
                //TODO add potions to chest
            case HaloRaceWorld:
                //chests
                Block haloBlock = this.world.getBlockAt(this.getChestLocations("finalchestone"));
                Chest haloChest = (Chest) haloBlock.getState();
                chests.add(haloChest);
                haloBlock = this.world.getBlockAt(this.getChestLocations("finalchesttwo"));
                haloChest = (Chest) haloBlock.getState();
                chests.add(haloChest);
                //TODO add potions to chest
                break;
            case IslandWorld:
            case IslandCopyWorld:
                Block islandBlock = this.world.getBlockAt(this.getChestLocations("finalchesttwo"));
                Chest islandChest = (Chest) islandBlock.getState();
                islandChest.getInventory().clear();
                islandChest.getInventory().setItem(0, new ItemStack(Material.AIR));
                islandChest.getInventory().setItem(1, bow);
                islandChest.getInventory().setItem(2, new ItemStack(Material.AIR));
                islandChest.getInventory().setItem(3, new ItemStack((Material.ARROW), 1));
                //TODO add potion of night vision to cheast two
                islandBlock = this.world.getBlockAt(this.getChestLocations("finalchestone"));
                islandChest = (Chest) islandBlock.getState();
                //TODO add all the potions to the chest one
                chests.add(islandChest);
                break;
            default:
                break;
            }
            Iterator<ItemStack> itemsCycleIterator =  Iterators.cycle(items);
            for(Chest worldChest : chests){
                worldChest.getInventory().clear();
                
                for(int j = 0 ; j * 2 < worldChest.getInventory().getSize() ; j++){
                    ItemStack item = itemsCycleIterator.next();
                    worldChest.getInventory().setItem(j * 2, item);
                    int pos = ((j * 2) - 1) <= 0 ? 1 : (j * 2) - 1;
                    worldChest.getInventory().setItem(pos, new ItemStack(Material.AIR));
                    
                }
            }
        }
    }
    
    public Location getBeastSpawnLocation(){
        if(beastSpawnLocation != null){
            return new Location(world, beastSpawnLocation.getX(), beastSpawnLocation.getY(), beastSpawnLocation.getZ());
        }
        return null;
    }
    
    public Location getPlayerRandomSpawnLocation() {
        Vector spawnLocationVector = playerSpawnLocations.get(RunFromTheBeastPlugin.getInstance().getRandom().nextInt(playerSpawnLocations.size()));
        return new Location(world, spawnLocationVector.getX(), spawnLocationVector.getY(), spawnLocationVector.getZ());
    }
    
    public Location getPlayerFreeRandomSpawnLocation() {
        Vector spawnLocationVector = playerFreeSpawnLocations.get(RunFromTheBeastPlugin.getInstance().getRandom().nextInt(playerFreeSpawnLocations.size()));
        return new Location(world, spawnLocationVector.getX(), spawnLocationVector.getY(), spawnLocationVector.getZ());
    }
    
    public Location getNextPlayerSpawn() {
        return playerSpawnCycleIterator.next().toLocation(world);
    }
    
    public String getName() {
        return null != world ? world.getName() : null;
    }

    public Location getSpectatorSpawnLocation() {
        if(spectatorSpawnLocation != null){
            return new Location(world, spectatorSpawnLocation.getX(), spectatorSpawnLocation.getY(), spectatorSpawnLocation.getZ()).clone();
        }
        return null;
    }

    /**
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * @return the buttonLocations
     */
    public Location getButtonLocationByName(String metaDataTag) {
        if(metaDataTag != null && this.buttonLocations.containsKey(metaDataTag)){
            return this.buttonLocations.get(metaDataTag).toLocation(world);
        }
        return null;
    }

    /**
     * @return the chestLocations
     */
    public Location getChestLocations(String chestName) {
        if(chestName != null && this.chestLocations.containsKey(chestName)){
            return this.chestLocations.get(chestName).toLocation(world);
        }
        return null;
    }
}
