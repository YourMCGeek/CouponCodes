package me.yourmcgeek.coupons.utils;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.yourmcgeek.coupons.CouponCodes;

public class BookUtils {
	
	private static final String TITLE = "Coupons Help Book", AUTHORS = "YourMCGeek & 2008Choco";
	private static final String DISPLAY_NAME = "Coupons Help Book", COLOR_DISPLAY_NAME = DARK_AQUA.toString() + BOLD + "Coupons Help Book";
	private static final List<String> LORE = Arrays.asList("A nice little guide to CouponCodes!");
	
	public static ItemStack generateBook(CouponCodes plugin) {
		if (plugin.getInfoBook() != null) return plugin.getInfoBook();
		
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		
		// Book information
		bookMeta.setTitle(TITLE);
		bookMeta.setAuthor(AUTHORS);
		bookMeta.setLore(LORE);
		bookMeta.setDisplayName(plugin.getConfig().getBoolean("ColoredDisplayName") ? COLOR_DISPLAY_NAME : DISPLAY_NAME);
		
		// Page generation
		bookMeta.addPage(
			DARK_GREEN + "" + BOLD + UNDERLINE + "Welcome to Coupons!\n\n" + RESET + DARK_AQUA +
			"To create a coupon, do " + DARK_GREEN + "\n/coupons create {code} <material>[:data][;amount]\n\n" 
			+ DARK_AQUA + "An example would be   " + DARK_GREEN 
			+ "/coupons create CBE dirt|64",
			
			DARK_BLUE + "To view the current list of coupons, you can try running the command\n\n" + DARK_AQUA + "/coupons list"
			 + DARK_BLUE + "\n\nType it in chat to see all your coupons!" ,
			
			DARK_PURPLE + "To redeem a coupon, run the command\n\n " + LIGHT_PURPLE + "/coupon redeem {code}\n\n" + 
			DARK_PURPLE + "Lets redeem our coupon by doing the command\n\n" + LIGHT_PURPLE + "/coupons redeem CBE",
			
			DARK_BLUE + "Since we no longer need our coupon, lets delete it. To delete a coupon, run the command " +
			BLUE + "/coupons delete {code}" + DARK_BLUE + "\n\n" + "To delete our coupon lets run\n\n" +
					BLUE + "/coupons delete CBE",
			
			DARK_RED + "Other Coupon commands include\n\n" + RED + "/coupons help" + "\n\n/coupons book\n\n" +
			DARK_RED + "Make sure to check " + RED + "   /coupons help" + DARK_RED + 
			" as that will always be updated with current commands.",
			
			"\n\n" + GOLD + MAGIC + "1234567891234567890" + "\n\n" + 
			DARK_GRAY + "   Happy Discounting!\n   ~YourMCGeek and \n      2008Choco!! <3" +
					"\n\n" + GOLD + MAGIC + "1234567891234567890"
			
		);
		
		book.setItemMeta(bookMeta);
		return book;
	}
}