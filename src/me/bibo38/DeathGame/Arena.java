package me.bibo38.DeathGame;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class Arena implements Listener
{
	private Spiel spiel = null;
	
	private HashMap<Location, Integer> blockid;
	private HashMap<Location, Byte> blockdata;
	
	private Location pos1, pos2 = null;
	private Location startpunkt, endpunkt = null;
	
	private Player setter = null; // Der Arena Setter
	private ItemStack setterHand = null;
	private int blockId = 7; // Bedrock zum positionieren
	
	public Arena(Location epos1, Location epos2)
	{
		pos1 = epos1;
		pos2 = epos2;
		
		blockid = new HashMap<Location, Integer>();
		blockdata = new HashMap<Location, Byte>();
		blockid.clear();
		blockdata.clear();
	}
	
	public Arena()
	{
		blockid = new HashMap<Location, Integer>();
		blockdata = new HashMap<Location, Byte>();
		blockid.clear();
		blockdata.clear();
	}
	
	public void setSpiel(Spiel espiel)
	{
		spiel = espiel;
	}
	
	public void setArea(Player esetter)
	{
		if(setter != null)
		{
			esetter.sendMessage(ChatColor.RED + "You can't set the Area!");
			return;
		}
		
		setter = esetter;
		setterHand = setter.getItemInHand();
		setter.setItemInHand(new ItemStack(blockId, 1)); // Bedrock
	}
	
	public void restore()
	{
		// Blöcke wiederherstellen
		World welt = pos1.getWorld();
		
		Iterator<Location> it = blockid.keySet().iterator();
		
		while(it.hasNext())
		{
			Location loc = it.next();
			
			if(blockid.get(loc) == null)
			{
				// Es wurde ein Block gesetzt
				// Weghauen :-)
				welt.getBlockAt(loc).setTypeId(0);
			} else
			{
				welt.getBlockAt(loc).setTypeIdAndData(blockid.get(loc), blockdata.get(loc), false);
			}
		}
		
		blockid.clear();
		blockdata.clear();
	}
	
	public void setStartpunkt(Player player)
	{
		if(pos1 == null)
		{
			player.sendMessage(ChatColor.RED + "You must first set the area!");
			return;
		}
		
		if(!Arena.isInside(pos1, pos2, player.getLocation()))
		{
			player.sendMessage(ChatColor.RED + "You must be in the Area!");
			return;
		}
		
		startpunkt = player.getLocation(); // Startpunkt setzen
		player.sendMessage("Startpoint succesful setted!");
	}
	
	protected Arena setStartpunkt(Location loc)
	{
		startpunkt = loc;
		return this;
	}
	
	protected Arena setPos1(Location loc)
	{
		pos1 = loc;
		return this;
	}
	
	protected Arena setPos2(Location loc)
	{
		pos2 = loc;
		return this;
	}
	
	public void setEndpunkt(Player player)
	{
		if(pos1 == null || pos2 == null)
		{
			player.sendMessage(ChatColor.RED + "You must first set the area!");
			return;
		}
		
		if(Arena.isInside(pos1, pos2, player.getLocation()))
		{
			player.sendMessage(ChatColor.RED + "You must be outside of the Area!");
			return;
		}
		
		endpunkt = player.getLocation(); // Startpunkt setzen
		player.sendMessage("Endpoint succesful settet!");
	}
	
	protected Arena setEndpunkt(Location loc)
	{
		endpunkt = loc;
		return this;
	}
	
	public Location getStartpunkt()
	{
		return startpunkt;
	}
	
	public Location getEndpunkt()
	{
		return endpunkt;
	}
	
	public Location getPos1()
	{
		return pos1;
	}
	
	public Location getPos2()
	{
		return pos2;
	}
	
	public World getWorld()
	{
		return pos1.getWorld();
	}
	
	public boolean canUse()
	{
		if(pos1 == null || pos2 == null || startpunkt == null || endpunkt == null)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean isInside(Location pos1, Location pos2, Location pos)
	{
		if(pos1.getX() < pos2.getX()) // X Achse
		{
			if(!(pos.getX() >= pos1.getX() && pos.getX() <= pos2.getX()))
			{
				return false;
			}
		} else
		{
			if(!(pos.getX() >= pos2.getX() && pos.getX() <= pos1.getX()))
			{
				return false;
			}
		}
		
		if(pos1.getY() < pos2.getY()) // Y Achse
		{
			if(!(pos.getY() >= pos1.getY() && pos.getY() <= pos2.getY()))
			{
				return false;
			}
		} else
		{
			if(!(pos.getY() >= pos2.getY() && pos.getY() <= pos1.getY()))
			{
				return false;
			}
		}
		
		if(pos1.getZ() < pos2.getZ()) // Z Achse
		{
			if(!(pos.getZ() >= pos1.getZ() && pos.getZ() <= pos2.getZ()))
			{
				return false;
			}
		} else
		{
			if(!(pos.getZ() >= pos2.getZ() && pos.getZ() <= pos1.getZ()))
			{
				return false;
			}
		}
		
		return true; // Auf allen Achsen ok
	}
	
	// -------------------------------------------------------
	
	public void onBlockChange(Location loc)
	{
		if(pos1 != null && pos2 != null) // Solange noch nicht gesetzt nichts machen
		{
			if(Arena.isInside(pos1, pos2, loc)) // Passiert es im Bereich?
			{
				if(!blockid.containsKey(loc)) // Noch nicht ver�ndert ver�ndert?
				{
					blockid.put(loc, pos1.getWorld().getBlockAt(loc).getTypeId()); // Block eintragen
					blockdata.put(loc, pos1.getWorld().getBlockAt(loc).getData());
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt)
	{
		onBlockChange(evt.getBlock().getLocation());
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent evt)
	{
		if(pos1 != null && pos2 != null) // Solange noch nicht gesetzt nichts machen
		{
			if(Arena.isInside(pos1, pos2, evt.getBlock().getLocation())) // Passiert es im Bereich?
			{
				if(!blockid.containsKey(evt.getBlock().getLocation())) // Noch nicht ver�ndert ver�ndert?
				{
					blockid.put(evt.getBlock().getLocation(), null); // Block eintragen als Luft null
					blockdata.put(evt.getBlock().getLocation(), null);
				}
			}
		}
		
		// Area setzen, falls vorhanden
		if(setter != null && evt.getPlayer().equals(setter) && evt.getBlockPlaced().getTypeId() == blockId)
		{
			if(pos1 == null) // Schon die erste Position gesetzt?
			{
				pos1 = evt.getBlockPlaced().getLocation();
				pos1.setY(0);
				setter.sendMessage("[DeathGame] Pos1: " + pos1.toVector().toString());
			} else
			{
				pos2 = evt.getBlockPlaced().getLocation();
				pos2.setY(pos2.getWorld().getMaxHeight()); // H�chste Grenze
				
				if(pos2.getWorld().equals(pos1.getWorld()))
				{
					setter.sendMessage("[DeathGame] Pos2: " + pos2.toVector().toString());
					setter.setItemInHand(setterHand);
					setter = null;
				} else
				{
					setter.sendMessage(ChatColor.RED + "Both Locations must be in the same World!");
				}
			}
			
			evt.setCancelled(true); // Abbrechen
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent evt)
	{
		onBlockChange(evt.getBlock().getLocation());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent evt)
	{
		if(pos1 == null || pos2 == null || spiel == null || !spiel.isSpielAktiv())
		{
			// Noch nichts gesetzt:
			return;
		}
		
		Location hin = evt.getTo();
		
		if(spiel.isIngame(evt.getPlayer(), false)) // Ist der Spieler im Game?
		{
			// Ist er im Bereich?
			if(!isInside(pos1, pos2, hin))
			{
				// War er auch schon vorher nicht drin?
				// Teleporte werden speziell geblockt, sonst gibt es Probleme
				
				evt.setCancelled(true);
				evt.getPlayer().sendMessage(ChatColor.RED + "You can't leave the Game! Type /game leave to leave it!");
			}
		} else
		{
			// Ist er ausserhalb des Bereichs?
			if(isInside(pos1, pos2, hin))
			{
				// War er auch schon vorher drin?
				// Teleporte werden speziell geblockt, um Probleme zu vermeiden
				
				evt.setCancelled(true);
				evt.getPlayer().sendMessage(ChatColor.RED + "You can't go in the Game! Type /game join to join it!");
			}
		}
	}
}
