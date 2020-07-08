package com.ebeane.sc2bot.Operations.Combat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Tag;

public abstract class Army {
	protected HashSet<Tag> roster;
	protected Orders orders;
	
	public enum Orders {WAIT, GATHER, ATTACK, SCOUT, FOLLOW}

	public Army() {
		roster = new HashSet<Tag>();
		orders = Orders.WAIT;
	}
	
	public boolean hasSoldier(Tag soldierTag) {
		return roster.contains(soldierTag);
	}
	
	public Orders getOrders() {
		return this.orders;
	}
	
	public boolean dismissSoldiers(List<UnitInPool> activeAssets) {
		List<Tag> activeTags = activeAssets.stream().map(asset -> asset.getTag()).collect(Collectors.toList());
		HashSet<Tag> tagHash = new HashSet<Tag>(activeTags);
		
		Iterator<Tag> rosterIter = roster.iterator();
		while (rosterIter.hasNext()) {
			Tag soldierTag = rosterIter.next();
			
			if (!tagHash.contains(soldierTag)) {
				rosterIter.remove();
			}
		}
		
		return roster.size() == 0;
	}
	
	public void recruitSoldier(Tag soldierTag) {
		roster.add(soldierTag);
	}
	
	public abstract boolean isActive();
}
