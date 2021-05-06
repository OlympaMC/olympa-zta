package fr.olympa.zta.primes;

import fr.olympa.api.player.OlympaPlayerInformations;

public class Prime {
	
	private final int id;
	private final OlympaPlayerInformations buyer;
	private final OlympaPlayerInformations target;
	private final double bounty;
	
	public Prime(int id, OlympaPlayerInformations buyer, OlympaPlayerInformations target, double bounty) {
		this.id = id;
		this.buyer = buyer;
		this.target = target;
		this.bounty = bounty;
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
	
}
