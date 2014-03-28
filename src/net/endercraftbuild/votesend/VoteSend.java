package net.endercraftbuild.votesend;	

import com.vexsoftware.votifier.crypto.RSA;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.ConfigurationSection;
import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

//Fork of VoteSend for bukkit modified to Bungee without the crap bulking it up. Runs with Bungeefier

public class VoteSend extends ConfigurablePlugin implements Listener
{
	private List<VoteServer> voteServers = new ArrayList();

	public class VoteServer
	{
		public String name;
		public PublicKey publicKey;
		public String serverIP;
		public int serverPort;
		public String custom;

		public VoteServer(String n, String pKey, String sIP, int sPort, String c)
				throws InvalidKeySpecException, NoSuchAlgorithmException
				{
			byte[] encodedPublicKey = DatatypeConverter.parseBase64Binary(pKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			this.publicKey = keyFactory.generatePublic(publicKeySpec);
			this.serverIP = sIP;
			this.serverPort = sPort;
			this.name = n;
			this.custom = c;
				}
	}

	public void onEnable()
	{

		PluginManager manager = ProxyServer.getInstance().getPluginManager();
		manager.registerListener(this, this);
		manager.registerCommand(this, new FakeVoteCommand(this));
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists())
		{
			InputStream in = VoteSend.class.getResourceAsStream("/res/config.yml");
			if (in != null) {
				try
				{
					FileOutputStream out = new FileOutputStream(file);
					byte[] buffer = new byte[' '];
					int length = 0;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				}
				catch (Exception localException) {}
			}
		}
		reload();

	}

	public void onDisable()
	{

	}



	@EventHandler
	public void onVotifierEvent(VotifierEvent event)
	{
		processVote(event.getVote());
	}

	public void processVote(final Vote vote)
	{
		this.getProxy().getScheduler().runAsync(this, new Runnable() {

			@Override
			public void run() {

			{

				for (VoteSend.VoteServer server : VoteSend.this.voteServers) {

					try
					{
						if (server.custom.length() > 0) {
							vote.setServiceName(server.custom);
							System.out.println("custom name");
						}
						System.out.println("Prparing vote");
						String VoteString = "VOTE\n" + vote.getServiceName() + "\n" + 
								vote.getUsername() + "\n" + vote.getAddress() + "\n" + 
								vote.getTimeStamp() + "\n";
						SocketAddress sockAddr = new InetSocketAddress(server.serverIP, server.serverPort);
						Socket socket = new Socket();
						System.out.println("Connecting to: " + sockAddr);
						socket.connect(sockAddr, 1000);
						OutputStream socketOutputStream = socket.getOutputStream();
						socketOutputStream.write(RSA.encrypt(VoteString.getBytes(), server.publicKey));
						socketOutputStream.close();
						socket.close();
						System.out.println("Vote sent");

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}

		}
	});
}

	public void reload()
	{
		reloadConfig();
		getConfig().options().pathSeparator('/');

		this.voteServers.clear();

		ConfigurationSection cs = getConfig().getConfigurationSection("servers");
		if (cs != null)
		{
			Iterator<String> i = cs.getKeys(false).iterator();
			while (i.hasNext())
			{
				String server = (String)i.next();
				ConfigurationSection serverConfig = cs.getConfigurationSection(server);
				if (serverConfig != null)
				{
					server = server.toLowerCase();
					try
					{
						this.voteServers.add(new VoteServer(server, serverConfig.getString("Key"), serverConfig.getString("IP"), serverConfig.getInt("Port"), serverConfig.getString("Custom")));
					}
					catch (InvalidKeySpecException e)
					{
						e.printStackTrace();
					}
					catch (NoSuchAlgorithmException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}

