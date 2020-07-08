package com.ebeane.sc2bot.Operations.Combat;

import java.util.List;
import java.util.Optional;

import com.ebeane.sc2bot.Toolbox;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class FighterSquadron extends Army {

	private Optional<GroundSquadron> armyAttachedTo;
	
	public FighterSquadron() {
		armyAttachedTo = Optional.ofNullable(null);
	}
	
	public void assignToArmy(GroundSquadron groundArmy) {
		armyAttachedTo = Optional.ofNullable(groundArmy);
	}
	
	public void unassignFromArmy() {
		armyAttachedTo = Optional.ofNullable(null);
	}
	
	public Optional<GroundSquadron> getArmyAttachedTo() {
		return armyAttachedTo;
	}
	
	public void follow(ObservationInterface observer, ActionInterface actions) {
		List<Unit> fighters = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));

		if (armyAttachedTo.isEmpty()) {
			orders = Orders.WAIT;
		} else if (armyAttachedTo.get().getOrders() == Orders.SCOUT) {
			orders = Orders.SCOUT;
			scout(observer, actions);
		} else {
			orders = Orders.FOLLOW;
			Point2d groundArmyCenter = armyAttachedTo.get().getArmyCenterOfMass(observer);
			actions.unitCommand(fighters, Abilities.ATTACK_ATTACK, groundArmyCenter, false);
		}
	}
	
	public void attack(ObservationInterface observer, ActionInterface actions, Point2d position) {
		List<Unit> fighters = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		orders = Orders.ATTACK;
		actions.unitCommand(fighters, Abilities.ATTACK_ATTACK, position, false);
	}
	
	private void scout(ObservationInterface observer, ActionInterface actions) {
		List<Unit> fighters = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		
		for (Unit currFighter : fighters) {
			Point2d searchLocation = Toolbox.getRandomMapCorner(observer);
			
			if (currFighter.getOrders().size() != 0) {
				continue;
			}
			
			actions.unitCommand(currFighter, Abilities.ATTACK_ATTACK, searchLocation, false);
		}
	}

	@Override
	public boolean isActive() {
		return roster.size() > 0;
	}
}
