package com.ebeane.sc2bot.Operations.Combat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ebeane.sc2bot.Toolbox;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class GroundSquadron extends Army {
	boolean mustered;
	Optional<Point2d> armyGatherPoint;
	
	public GroundSquadron() {
		mustered = false;
		armyGatherPoint = Optional.ofNullable(null);
	}
	
	private void musterForBattle(ObservationInterface observer, ActionInterface actions) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		
		if (armyGatherPoint.isEmpty()) {
			armyGatherPoint = Optional.of(getArmyCenterOfMass(observer));
		}
		
		if (!armyGrouped(observer)) {
			actions.unitCommand(soldiers, Abilities.ATTACK_ATTACK, armyGatherPoint.get(), false);
		} else {
			armyGatherPoint = Optional.ofNullable(null);
			mustered = true;
		}
	}
	
	public Point2d getArmyCenterOfMass(ObservationInterface observer) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		Point2d centerOfMass = Point2d.of(0.0f, 0.0f);
		
		for (Unit currSoldier : soldiers) {
			Point2d soldierPosition = currSoldier.getPosition().toPoint2d();
			
			centerOfMass = centerOfMass.add(soldierPosition.getX() / soldiers.size(), soldierPosition.getY() / soldiers.size());
		}
		
		return centerOfMass;
	}
	
	private boolean armyGrouped(ObservationInterface observer) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		boolean gathered = true;
		
		for (Unit currSoldier : soldiers) {
			if (armyGatherPoint.get().distance(currSoldier.getPosition().toPoint2d()) > 15) {
				gathered = false;
			}
		}
		
		return gathered;
	}
	
	public void attack(ObservationInterface observer, ActionInterface actions, Point2d position) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		
		if (soldiers.size() == 0) {
			return;
		}
		
		if (!mustered) {
			orders = Orders.GATHER;
			musterForBattle(observer, actions);
		} else {
			orders = Orders.ATTACK;
			actions.unitCommand(soldiers, Abilities.ATTACK_ATTACK, position, false);
		}
	}
	
	public void scatter(ObservationInterface observer, ActionInterface actions) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		mustered = false;
		orders = Orders.SCOUT;
		
		for (Unit currSoldier : soldiers) {
			Point2d searchLocation = observer.getGameInfo().findRandomLocation();
			
			if (currSoldier.getOrders().size() != 0) {
				continue;
			}
			
			actions.unitCommand(currSoldier, Abilities.ATTACK_ATTACK, searchLocation, false);
		}
	}

	@Override
	public boolean isActive() {
		return roster.size() >= 30;
	}
}
