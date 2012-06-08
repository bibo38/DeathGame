package me.bibo38.DeathGame;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Spieler
{	
	private Player player;
	private ItemStack inventar[];
	private boolean isDead = false;
	private Spiel spiel; // Das Spiel, in dem er drinne ist
	private GameMode oldgm = GameMode.SURVIVAL;
	
	private static String leavemsg = "Bye Bye :-(";
	private static String joinmsg = "Have Fun :-)";
	
	public Spieler(Player spieler, Spiel espiel)
	{
		player = spieler;
		player.sendMessage(joinmsg);
		spiel = espiel;
	}
	
	protected void start() // Start the Game
	{
		inventar = player.getInventory().getContents();
		player.teleport(spiel.getArena().getStartpunkt());
		player.getInventory().clear();
		
		// Gamemode sichern
		oldgm = player.getGameMode();
		player.setGameMode(GameMode.SURVIVAL);
	}
	
	public void leave()
	{
		if(spiel.isSpielAktiv() && spiel.isIngame(player, false))
		{
			// Inventory wiederherstellen
			player.getInventory().setContents(inventar);
			player.teleport(spiel.getArena().getEndpunkt());
			player.setGameMode(oldgm);
			oldgm = GameMode.SURVIVAL;
		}
		
		player.sendMessage(leavemsg);
	}
	
	public boolean isDead()
	{
		return isDead;
	}
	
	public void setDead(boolean dead)
	{
		isDead = dead;
		if(dead)
		{
			player.setGameMode(oldgm);
			oldgm = GameMode.SURVIVAL;
		}
	}
	
	protected Spieler restoreInv()
	{
		player.getInventory().setContents(inventar);
		return this;
	}
	
	protected static void setJoinMsg(String msg)
	{
		joinmsg = msg;
	}
	
	protected static void setLeaveMsg(String msg)
	{
		leavemsg = msg;
	}
	
	public static String getJoinMsg()
	{
		return joinmsg;
	}
	
	public static String getLeaveMsg()
	{
		return leavemsg;
	}
}
