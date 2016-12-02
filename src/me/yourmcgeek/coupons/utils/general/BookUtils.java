package me.yourmcgeek.coupons.utils.general;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.yourmcgeek.coupons.CouponCodes;

public class BookUtils {
	
	private static final String TITLE = "Coupons Help Book", AUTHORS = "YourMCGeek & 2008Choco";
	private static final String DISPLAY_NAME = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Coupons Help Book";
	private static final List<String> LORE = Arrays.asList("A nice little guide to CouponCodes!");
	
	/* Previous code:
	   
	   ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Welcome to Coupons!\n" + 
       ChatColor.BLUE + "To create a coupon, run" + ChatColor.DARK_GREEN + " /coupons create {code} <material>[:data]|amount. \n" +
       ChatColor.BLUE + "An example would be " + ChatColor.DARK_GREEN + "/coupons create CBE dirt|64";	
	 */
	
	public static ItemStack generateBook(CouponCodes plugin) {
		if (plugin.getInfoBook() != null) return plugin.getInfoBook();
		
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		
		
		// Book information
		
		bookMeta.setTitle(TITLE);
		bookMeta.setAuthor(AUTHORS);
		bookMeta.setDisplayName(DISPLAY_NAME);
		bookMeta.setLore(LORE);
		
		
		// Page generation
		
		bookMeta.addPage(
			"THIS IS PAGE 1",
			
			"THIS IS PAGE 2",
			
			"THIS IS PAGE 3",
			
			"THIS IS PAGE 4",
			
			"THIS IS PAGE 5"
		);
		
		book.setItemMeta(bookMeta);
		return book;
	}
}