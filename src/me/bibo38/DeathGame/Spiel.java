package me.bibo38.DeathGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Spiel implements Runnable, Listener
{
	private Arena arena;
	private DeathGame main;
	
	private boolean warten = false;
	private boolean aktspiel = false;
	
	private int counter = 30; // In Sec
	private int tmpcounter = -1;
	private int counterid;
	private int wartezeit = 20; // In Min
	private int tmpwartezeit;
	
	private HashMap<Player, Spieler> spieler;
	private ArrayList<Player> respawn; // Alle, die neu am Endpunkt spawnen sollen
	
	public Spiel(DeathGame emain, Arena earena)
	{
		arena = earena;
		main = emain;
		
		spieler = new HashMap<Player, Spieler>();
		spieler.clear();
		respawn = new ArrayList<Player>();
		respawn.clear();
		
		arena.setSpiel(this);
	}
	
	@Override
	public void run()
	{
		if(tmpcounter <= 0)
		{
			// Eine lange Wartezeit
			main.getServer().getLogger().info("LAANGE");
			tmpwartezeit--;
			if((tmpwartezeit % 5) == 0)
			{
				Iterator<Player> it = spieler.keySet().iterator();
				
				while(it.hasNext())
				{
					it.next().sendMessage("[DeathGame] " + ChatColor.RED + "New Game in " + tmpwartezeit + " Minutes!");
				}
			}
			
			if(tmpwartezeit == 2)
			{
				main.getServer().broadcastMessage("[DeatGame] A new Game starts in 2 Minutes! Type /game join to join it!");
			}
			
			if(tmpwartezeit * 60 > counter && (tmpwartezeit - 1) * 60 < counter)
			{
				// Neue Zeit setzen
				main.getServer().getScheduler().cancelTask(counterid);
				counterid = -1;
				main.getServer().getScheduler().scheduleSyncDelayedTask(main, this, 20L * (tmpwartezeit * 60 - counter));
			}
			
			if(tmpwartezeit * 60 < counter)
			{
				// Starten
				main.getServer().getScheduler().cancelTask(counterid); // Vorher diesen Thread killen
				
				tmpcounter = counter;
				counterid = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, this, 20L, 20L); // Jede Sekunde
			}
		} else
		{
			// Der Countdown
			tmpcounter--;
			if((tmpcounter % 5) == 0 || tmpcounter < 5)
			{
				Iterator<Player> it = spieler.keySet().iterator();
				
				while(it.hasNext())
				{
					it.next().sendMessage("[DeathGame] " + ChatColor.RED + tmpcounter);
				}
			}
			
			if(tmpcounter <= 0)
			{
				start();
				main.getServer().getScheduler().cancelTask(counterid);
				counterid = -1;
			}
		}
	}
	
	protected Spiel setCounter(int cnt)
	{
		if(cnt >= 5 && cnt < 120)
		{
			counter = cnt;
		}
		
		return this;
	}
	
	protected Spiel setWartezeit(int warte)
	{
		if(warte > 0)
		{
			wartezeit = warte;
		}
		
		return this;
	}
	
	public int getCounter()
	{
		return counter;
	}
	
	public int getWartezeit()
	{
		return wartezeit;
	}
	
	public void startcounter(Player player)
	{
		if(!arena.canUse())
		{
			player.sendMessage(ChatColor.RED + "The Arena isn't already set!");
			return;
		}
		
		if(counter < 5)
		{
			player.sendMessage(ChatColor.RED + "The Minimal Value of the Counter is 5 Seconds!");
			return;
		}
		
		if(counter >= 120)
		{
			player.sendMessage(ChatColor.RED + "The maximal Timer Value is 119 Seconds!");
		}
		
		tmpcounter = counter;
		counterid = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, this, 20L, 20L); // Jede Sekunde
	}
	
	public void startcounter(Player player, int neucounter)
	{
		counter = neucounter;
		startcounter(player);
	}
	
	private void start() // Ein Spiel starten 
	{
		if(!arena.canUse())
		{
			main.getLogger().info(ChatColor.RED + "Cannot use the Arena!");
			return;
		}
		
		arena.restore(); // Arena wiederherstellen
		
		// TODO Aktivieren der Prüfung
		/* if(spieler.size() < 2) // Nicht genug Spieler
		{
			stop();
			return;
		} */
		
		Iterator<Player> it = spieler.keySet().iterator();
		
		while(it.hasNext()) // Spiel f�r die Spieler starten
		{
			Player play = it.next();
			main.getLogger().info("Player " + play.getName() + " has started!");
			getSpieler(play).setDead(false);
			getSpieler(play).start();
		}
		
		aktspiel = true;
		warten = false;
	}
	
	private void ende() // Spielende
	{
		aktspiel = false;
		warten = true;
		arena.restore();
		
		Iterator<Player> it = spieler.keySet().iterator();
		while(it.hasNext())
		{
			Spieler next = getSpieler(it.next());
			if(!next.isDead())
			{
				next.setDead(true); // Damit er sein Inventory zurückbekommt
				next.restoreInv();
			}
		}
		
		tmpwartezeit = wartezeit + 1; // F�r eine Meldung
		tmpcounter = -1;
		counterid = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, this, 0L, 20L * 60L);
	}
	
	private void check() // Spiel checken
	{
		if(aktspiel)
		{
			if(!arena.canUse())
			{
				stop(); // Dann stoppen
			} else
			{
				// Aktive Spieler z�hlen
				Iterator<Player> it = spieler.keySet().iterator();
				int cnt = 0; // Der Counter
				while(it.hasNext())
				{
					if(!spieler.get(it.next()).isDead()) // Alle nichttoten
					{
						cnt++;
					}
				}
				
				if(cnt < 2)
				{
					stop();
				}
			}
		}
	}
	
	public void stop()
	{
		// If the Game is ingame end it
		if(aktspiel)
		{
			Iterator<Player> it = spieler.keySet().iterator();
			while(it.hasNext())
			{
				spieler.get(it.next()).leave();
			}
		}
		
		if(counterid >= 0)
		{
			main.getServer().getScheduler().cancelTask(counterid);
			counterid = -1;
		}
		
		main.getLogger().info("Stopped Game!");
		
		// Spielende
		aktspiel = false;
		warten = false;
		arena.restore();
		spieler.clear();
		System.gc();
	}
	
	public boolean isIngame(Player player, boolean death) // Ist er im Spiel?
	{
		if(spieler.containsKey(player))
		{
			if(death)
			{
				// Z�hlen die Toten auch dazu?
				return true; // Dann stimmts
			} else
			{
				return !(spieler.get(player).isDead());
			}
		}
		
		return false;
	}
	
	public Spieler getSpieler(Player player)
	{
		if(spieler.containsKey(player))
		{
			return spieler.get(player);
		} else
		{
			return null; // Gibts nicht
		}
	}
	
	public void addPlayer(Player player) // Spieler dem Spiel hinzuf�gen
	{
		if(this.isIngame(player, true))
		{
			player.sendMessage(ChatColor.RED + "You are already in the game! Type /game leave to leave it!");
			return;
		}
		
		Spieler neu = new Spieler(player, this);
		spieler.put(player, neu);
		// TODO Entfernen des TNT Sounds
		Spiel.makeTNTSound(player);
		main.getLogger().info("Player " + player.getName() + " has joined the game!");
	}
	
	public void delPlayer(Player player)
	{
		if(!this.isIngame(player, true))
		{
			player.sendMessage(ChatColor.RED + "You aren't in the game! Type /game join to join it!");
			return;
		}
		
		spieler.get(player).leave();
		spieler.remove(player);
		
		check();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent evt)
	{
		if(this.isIngame(evt.getPlayer(), true))
		{
			this.delPlayer(evt.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent evt)
	{
		if(this.isIngame(evt.getPlayer(), true))
		{
			this.delPlayer(evt.getPlayer());
		}
	}
	
	public boolean isSpielAktiv()
	{
		return aktspiel;
	}
	
	public boolean isWarten()
	{
		return warten;
	}
	
	public Arena getArena()
	{
		return arena;
	}
	
	public static void makeTNTSound(Player player)
	{
		Location neu = player.getLocation();
		neu.setY(player.getLocation().getY() + 10);
		
		World welt = player.getWorld();
		welt.createExplosion(neu, 0L, false);
	}
	
	// ----------------------------------------------------------
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt)
	{
		Player player = evt.getEntity();
		
		if(isIngame(player, false))
		{
			spieler.get(player).setDead(true);
			respawn.add(player); // Er soll auch respawnen
			
			Iterator<Player> it = spieler.keySet().iterator();
			String ingame = "";
			int cnt = 0;
			
			while(it.hasNext())
			{
				Player aktplay = it.next();
				
				if(!getSpieler(aktplay).isDead())
				{
					Spiel.makeTNTSound(aktplay);
					ingame += "," + aktplay.getName();
					cnt++;
				}
			}
			
			if(cnt < 2)
			{
				ende();
				return;
			}
			
			it = spieler.keySet().iterator();
			
			while(it.hasNext())
			{
				Player aktplay = it.next();
				
				if(!getSpieler(aktplay).isDead())
				{
					aktplay.sendMessage(ingame.substring(1));
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent evt)
	{
		if(respawn.contains(evt.getPlayer()))
		{
			respawn.remove(evt.getPlayer());
			evt.setRespawnLocation(arena.getEndpunkt());
			getSpieler(evt.getPlayer()).restoreInv();
		}
	}
}
