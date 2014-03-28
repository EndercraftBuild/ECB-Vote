package net.endercraftbuild.votesend;

import java.util.Date;

import com.vexsoftware.votifier.model.Vote;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class FakeVoteCommand extends Command {
	
	private final VoteSend plugin;
	
	public FakeVoteCommand(VoteSend plugin) {
		super("sv", "ecb.sendvote", "sendvote");
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer))
			return;
		
		if (args.length < 2) {
			sender.sendMessage(ChatColor.GOLD + "Usage: /sendvote <player> <service>");
			return;
		}
		
			Vote fakeVote = new Vote();
	      fakeVote.setUsername(args[0]);
	      fakeVote.setServiceName(args[1] == null ? "testVote" : args[1]);
	      fakeVote.setAddress("1.2.3.4");
	      Date date = new Date();
	      fakeVote.setTimeStamp(String.valueOf(date.getTime()));
	      plugin.processVote(fakeVote);
	      sender.sendMessage(ChatColor.RED + "ECB> Vote sent!");
	}
	
}