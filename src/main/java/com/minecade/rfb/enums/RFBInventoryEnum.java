/**
 * @author jdgil
 */
package com.minecade.rfb.enums;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author WindowsUser
 *
 */
public enum RFBInventoryEnum implements RFBInventory{
    
    INSTRUCTIONS(ChatColor.RED + "Instructions book", Material.WRITTEN_BOOK) {
        
        /**
         * @return InstructionBook
         * @author kvnamo
         */
        @Override
        public ItemStack getItemStack(){
            ItemStack book = new ItemStack(getMaterial(), 1);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setAuthor("Run From The Beast");
            bookMeta.setTitle("Instructions and Rules");
            bookMeta.setPages(
                String.format("%s%sWELCOME TO RUN FROM THE BEST! \n\n\n%s" +
                    "Run from the beast until get own weapon and armor in th end of the world and come back to kill him! Runners alive when the beast death wins the game", 
                    ChatColor.BOLD, ChatColor.RED, ChatColor.DARK_GRAY),
                String.format("%s%sRULES! \n\n\n %s1. Run far away as you can.\n" +
                    "2. Get your weapon and armor in the end of the world.\n" +
                    "3. Come back, look for the beast and kill him!" +
                    "4. If you are the beast: kill everybody!!!",
                    ChatColor.BOLD, ChatColor.RED, ChatColor.DARK_GRAY));
            book.setItemMeta(bookMeta);
            return book;
        }
    }, 
    LEAVE_COMPASS(ChatColor.RED + "Leave Game", Material.COMPASS) {
        /**
         * @return leaveCompass Item.
         * @author jdgil
         */
        @Override
        public ItemStack getItemStack() {
            ItemStack compass = new ItemStack(getMaterial(), 1);
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + getName());
            
            List<String> colored = new ArrayList<>();
            colored.add(ChatColor.GRAY + "Click to Go to Lobby");
            meta.setLore(colored);
            
            compass.setItemMeta(meta);
            return compass;
        }
    },
    
    STATS_BOOK(ChatColor.RED + "Stats book", Material.WRITTEN_BOOK) {
        
        /**
         * @return stats book Item.
         * @author kvnamo
         */
        @Override
        public ItemStack getItemStack() {
            ItemStack stats = new ItemStack(getMaterial(), 1);
            BookMeta statsMeta = (BookMeta) stats.getItemMeta();
            statsMeta.setAuthor("ButterSlap");
            statsMeta.setTitle("Stats Book");
            stats.setItemMeta(statsMeta);
            return stats;
        }
    };
    private final String name;
    private final Material material;
    
    private RFBInventoryEnum(String name, Material material){
        this.name = name;
        this.material = material;
    }

    /**
     * @return the name
     * @author jdgil
     */
    public String getName() {
        return name;
    }

    /**
     * @return the material
     * @author jdgil
     */
    public Material getMaterial() {
        return material;
    }

}
