package me.bibo38.DeathGame;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class DeathGame extends JavaPlugin
{
	private Logger log;
	private PluginDescriptionFile pdFile;
	
	private Spiel hauptspiel = null;
	private Arena hauptarena;
	private FileConfiguration cfg;
	
	@Override
	public void onEnable()
	{
		log = this.getLogger();
		pdFile = this.getDescription();
		cfg = this.getConfig();
		cfg.options().copyDefaults(true);
		this.saveConfig();
		
		hauptarena = new Arena();
		
		if(cfg.isSet("pos1") && cfg.isSet("pos2") && cfg.isSet("world") && cfg.isSet("start") && cfg.isSet("ende"))
		{
			Vector tmp;
			World welt = this.getServer().getWorld(cfg.getString("world"));
			
			tmp = cfg.getVector("pos1");
			hauptarena.setPos1(
					new Location(
							welt,
							tmp.getX(),
							tmp.getY(),
							tmp.getZ()
							)
					);
			
			tmp = cfg.getVector("pos2");
			hauptarena.setPos2(
					new Location(
							welt,
							tmp.getX(),
							tmp.getY(),
							tmp.getZ()
							)
					);
			
			tmp = cfg.getVector("start");
			hauptarena.setStartpunkt(
					new Location(
							welt,
							tmp.getX(),
							tmp.getY(),
							tmp.getZ()
							)
					);
			
			tmp = cfg.getVector("ende");
			hauptarena.setEndpunkt(
					new Location(
							welt,
							tmp.getX(),
							tmp.getY(),
							tmp.getZ()
							)
					);
		}
		
		if(!cfg.isSet("counter"))
		{
			cfg.set("counter", 30);
		}
		
		if(!cfg.isSet("wartezeit"))
		{
			cfg.set("wartezeit", 20);
		}
		
		if(cfg.isSet("joinmsg"))
		{
			Spieler.setJoinMsg(cfg.getString("joinmsg"));
		}
		
		if(cfg.isSet("leavemsg"))
		{
			Spieler.setLeaveMsg(cfg.getString("leavemsg"));
		}
		
		hauptspiel = new Spiel(this, hauptarena);
		
		hauptspiel.setCounter(cfg.getInt("counter")).setWartezeit(cfg.getInt("wartezeit"));
		
		// Events :-)
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(hauptarena, this);
		pm.registerEvents(hauptspiel, this);
		
		log.info("DeathGame Version " + pdFile.getVersion() + " by bibo38 was activatet!");
	}
	
	@Override
	public void onDisable()
	{
		hauptarena.restore();
		
		// Configuration speichern
		log.info("Save Configuration");
		if(hauptarena.canUse())
		{
			cfg.set("start", hauptarena.getStartpunkt().toVector());
			cfg.set("ende", hauptarena.getEndpunkt().toVector());
			cfg.set("world", hauptarena.getWorld().getName());
			cfg.set("pos1", hauptarena.getPos1().toVector());
			cfg.set("pos2", hauptarena.getPos2().toVector());
			cfg.set("counter", hauptspiel.getCounter());
			cfg.set("wartezeit", hauptspiel.getWartezeit());
			cfg.set("joinmsg", Spieler.getJoinMsg());
			cfg.set("leavemsg", Spieler.getLeaveMsg());
		}
		
		this.saveConfig();
		
		log.info("DeathGame Version " + pdFile.getVersion() + " by bibo38 was deactivatet!");
	}
	
	public FileConfiguration getCfg()
	{
		return cfg;
	}
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String commandLabel, String[] args)
	{
		Player player;
		
		if(!(cs instanceof Player))
		{
			player = null;
		} else
		{
			player = (Player) cs;
		}
		
		if(cmd.getName().equalsIgnoreCase("game"))
		{
			if(args.length == 0) // Hilfe Kommando
			{
				// HILFE aufrufen
				help(cs);
			} else
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					help(cs);
				}
				
				if(args[0].equalsIgnoreCase("setstart") || args[0].equalsIgnoreCase("setend"))
				{
					// Startpunkt festlegen
					if(player == null)
					{
						cs.sendMessage(ChatColor.RED + "You must be a Player to perform this operation!");
					} else
					{
						if(args[0].equalsIgnoreCase("setstart") && hasPerm(player, "setstart"))
						{
							hauptarena.setStartpunkt(player);
						} else if(args[0].equalsIgnoreCase("setend") && hasPerm(player, "setend"))
						{
							hauptarena.setEndpunkt(player);
						}
					}
				}
				
				if(args[0].equalsIgnoreCase("stop") && hasPerm(player, "stop"))
				{
					if(hauptspiel != null)
					{
						hauptspiel.stop();
					} else
					{
						cs.sendMessage(ChatColor.RED + "No game is running!");
					}
				}
				
				if(args[0].equalsIgnoreCase("join") && hasPerm(player, "join"))
				{
					// Spieler joint
					if(player != null)
					{
						hauptspiel.addPlayer(player);
					} else
					{
						cs.sendMessage(ChatColor.RED + "You can't join a game!");
					}
				}
				
				if(args[0].equalsIgnoreCase("start") && hasPerm(player, "start"))
				{
					if(args.length == 2)
					{
						hauptspiel.startcounter(player, Integer.parseInt(args[1]));
					} else
					{
						hauptspiel.startcounter(player);
					}
				}
				
				if(args[0].equalsIgnoreCase("setarea") && hasPerm(player, "setarea"))
				{
					hauptarena.setArea(player);
				}
				
				if(args[0].equalsIgnoreCase("leave") && hasPerm(player, "join"))
				{
					if(player != null)
					{
						hauptspiel.delPlayer(player);
					} else
					{
						cs.sendMessage(ChatColor.RED + "You can't use this command!");
					}
				}
			}
		}
		
		return true;
	}
	
	private void help(CommandSender cs)
	{
		cs.sendMessage("------------------------------");
		cs.sendMessage("Help for DeathGame Version " + pdFile.getVersion());
	}
	
	// Permissions Check
	public boolean hasPerm(Player player, String perm)
	{
		if(player == null || player.isOp())
		{
			return true;
		}
		
		if(!player.hasPermission("deathgame." + perm))
		{
			player.sendMessage(ChatColor.RED + "You don't have the Permissions to perform this Operation!");
			return false;
		}
		
		return true;
	}
	
	public Arena getArena()
	{
		return hauptarena;
	}
}
