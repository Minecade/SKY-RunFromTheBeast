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

import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

/**
 * @author WindowsUser
 *
 */
public enum RFBInventoryEnum implements RFBInventory {
    
    INSTRUCTIONS(RunFromTheBeastPlugin.getMessage("items.instructionsbook.title"), Material.WRITTEN_BOOK) {
        
        /**
         * @return InstructionBook
         * @author kvnamo
         */
        @Override
        public ItemStack getItemStack(){
            ItemStack book = new ItemStack(getMaterial(), 1);
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setAuthor(RunFromTheBeastPlugin.getMessage("items.book.author.pag"));
            bookMeta.setTitle(RunFromTheBeastPlugin.getMessage("items.instructionsbook.title.pag"));
            bookMeta.setPages(
                String.format(RunFromTheBeastPlugin.getMessage("items.instructionsbook.instruccions")),
                String.format(RunFromTheBeastPlugin.getMessage("items.instructionsbook.rules")));
            book.setItemMeta(bookMeta);
            return book;
        }
    }, 
    LEAVE_COMPASS(RunFromTheBeastPlugin.getMessage("items.leave.title"), Material.COMPASS) {
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
            colored.add(RunFromTheBeastPlugin.getMessage("items.leave.lore"));
            meta.setLore(colored);
            
            compass.setItemMeta(meta);
            return compass;
        }
    },
    
    STATS_BOOK(RunFromTheBeastPlugin.getMessage("items.stats.title"), Material.WRITTEN_BOOK) {
        
        /**
         * @return stats book Item.
         * @author kvnamo
         */
        @Override
        public ItemStack getItemStack() {
            ItemStack stats = new ItemStack(getMaterial(), 1);
            BookMeta statsMeta = (BookMeta) stats.getItemMeta();
            statsMeta.setAuthor(RunFromTheBeastPlugin.getMessage("items.book.author.pag"));
            statsMeta.setTitle(RunFromTheBeastPlugin.getMessage("items.stats.title"));
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
