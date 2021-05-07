package fr.olympa.zta.primes;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.zta.OlympaZTA;

public class Prime {
	
	private final int id;
	private final OlympaPlayerInformations buyer;
	private final OlympaPlayerInformations target;
	private final double bounty;
	private final long expiration;
	
	private final String bountyFormatted;
	
	private BukkitTask expirationTask;
	private boolean expired = false;
	
	public Prime(int id, OlympaPlayerInformations buyer, OlympaPlayerInformations target, double bounty, long expiration) {
		this.id = id;
		this.buyer = buyer;
		this.target = target;
		this.bounty = bounty;
		this.expiration = expiration;
		this.bountyFormatted = OlympaMoney.format(bounty);
		
		long delay = (expiration - System.currentTimeMillis()) / 50;
		if (delay < 0) {
			expired = true;
		}else {
			expirationTask = Bukkit.getScheduler().runTaskLaterAsynchronously(OlympaZTA.getInstance(), () -> {
				expired = true;
				expirationTask = null;
				OlympaZTA.getInstance().primes.removePrime(this, null, null);
			}, delay);
		}
	}
	
	protected void removed() {
		if (expirationTask != null) expirationTask.cancel();
	}
	
	public int getID() {
		return id;
	}
	
	public OlympaPlayerInformations getBuyer() {
		return buyer;
	}
	
	public OlympaPlayerInformations getTarget() {
		return target;
	}
	
	public double getBounty() {
		return bounty;
	}
	
	public long getExpiration() {
		return expiration;
	}
	
	public boolean isExpired() {
		return expired;
	}
	
	public String getBountyFormatted() {
		return bountyFormatted;
	}
	
}
