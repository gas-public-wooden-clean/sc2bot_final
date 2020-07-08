package com.ebeane.sc2bot.Operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.Operations.Combat.Army;
import com.ebeane.sc2bot.Operations.Combat.FighterSquadron;
import com.ebeane.sc2bot.Operations.Combat.GroundSquadron;
import com.ebeane.sc2bot.Operations.Combat.ReaperSquadron;
import com.ebeane.sc2bot.Toolbox;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Engagement implements Division {
	private ObservationInterface observer;
	private ActionInterface actions;
	private boolean enemyScouted = false;
	private ArrayList<GroundSquadron> activeArmies;
	private GroundSquadron reserveArmy;
	private FighterSquadron airArmy;
	private ReaperSquadron harassmentArmy;

	public Engagement(ObservationInterface observer, ActionInterface actions) {
		this.observer = observer;
		this.actions = actions;
		activeArmies = new ArrayList<GroundSquadron>();
		reserveArmy = new GroundSquadron();
		airArmy = new FighterSquadron();
		harassmentArmy = new ReaperSquadron();
	}

	@Override
	public void updateForStep(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		this.observer = observer;
		this.actions = actions;

		if (!enemyScouted) {
			enemyScouted = enemyPositionScouted();
		}

		updateArmies();
	}

	@Override
	public void issueCommands(BotBehaviorState botState) {

		for (GroundSquadron currArmy : activeArmies) {
			ordersGround(currArmy);
		}
		if (airArmy.isActive()) {
			ordersAir(airArmy);
		}
		
		ordersReserve();
		ordersReaper();
	}
	
	private void updateHarassmentArmies() {
		List<UnitInPool> reaperForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_REAPER);
		
		for (UnitInPool reaperAsset : reaperForce) {

			if (!harassmentArmy.hasSoldier(reaperAsset.getTag())) {
				harassmentArmy.recruitSoldier(reaperAsset.getTag());
			}
		}
		
		harassmentArmy.dismissSoldiers(reaperForce);
	}
	
	private void updateGroundArmies() {
		List<UnitInPool> marineForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_MARINE);
		
		for (UnitInPool marineAsset : marineForce) {
			boolean marineAssigned = false;

			for (Army currArmy : activeArmies) {
				if (currArmy.hasSoldier(marineAsset.getTag())) {
					marineAssigned = true;
					break;
				}
			}

			if (!marineAssigned) {
				reserveArmy.recruitSoldier(marineAsset.getTag());
				if (reserveArmy.isActive()) {
					activeArmies.add(reserveArmy);
					reserveArmy = new GroundSquadron();
				}
			}
		}
		
		Iterator<GroundSquadron> armyIter = activeArmies.iterator();
		while (armyIter.hasNext()) {
			Army currArmy = armyIter.next();
			if (currArmy.dismissSoldiers(marineForce)) {
				armyIter.remove();
			}
		}
	}
	
	private void updateAirArmy() {
		List<UnitInPool> vikingForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_VIKING_FIGHTER);
		boolean assignedToArmy = airArmy.getArmyAttachedTo().isPresent();
		boolean armyInField = activeArmies.size() != 0;
		
		for (UnitInPool vikingAsset : vikingForce) {
			if (!airArmy.hasSoldier(vikingAsset.getTag())) {
				airArmy.recruitSoldier(vikingAsset.getTag());
			}
		}

		if (!armyInField) {
			airArmy.unassignFromArmy();
		} else if (!assignedToArmy) {
			airArmy.assignToArmy(activeArmies.get(0));	
		} else {
			if (!activeArmies.contains(airArmy.getArmyAttachedTo().get())) {
				airArmy.assignToArmy(activeArmies.get(0));
			}
		}

		airArmy.dismissSoldiers(vikingForce);
	}

	public void updateArmies() {
		// Need to update ground armies first so air army knows if the army it's attached to is destroyed
		updateGroundArmies();
		updateAirArmy();
		updateHarassmentArmies();
	}

	private void ordersAir(FighterSquadron vikingArmy) {
		List<UnitInPool> airEnemies = Toolbox.getAirUnits(observer, Alliance.ENEMY);
		
		if (0 != airEnemies.size()) {
			Optional<Unit> target = airEnemies.get(0).getUnit();
			if (target.isEmpty()) {
				return;
			}
			vikingArmy.attack(observer, actions, target.get().getPosition().toPoint2d());
		} else {
			vikingArmy.follow(observer, actions);
		}
	}
	
	private void ordersGround(GroundSquadron marineArmy) {
		List<UnitInPool> presentEnemies = observer.getUnits(Alliance.ENEMY);

		Optional<Point2d> enemyPosition = Toolbox.findEnemyPosition(observer);
		if (enemyPosition.isEmpty()) {
			System.out.println("No enemy position");
			return;
		}

		if (0 != presentEnemies.size()) {

			Optional<Unit> target = presentEnemies.get(0).getUnit();
			if (target.isEmpty()) {
				return;
			}
			marineArmy.attack(observer, actions, target.get().getPosition().toPoint2d());
		} else if (!enemyScouted) {
			marineArmy.attack(observer, actions, enemyPosition.get());
		} else {
			marineArmy.scatter(observer, actions);
		}
	}
	
	private void ordersReaper() {
		List<UnitInPool> presentEnemies = observer.getUnits(Alliance.ENEMY);

		Optional<Point2d> enemyPosition = Toolbox.findEnemyPosition(observer);
		if (enemyPosition.isEmpty()) {
			System.out.println("No enemy position");
			return;
		}

		harassmentArmy.attack(observer, actions, enemyPosition.get());
	}
	
	public void ordersReserve() {
		List<UnitInPool> presentEnemies = observer.getUnits(Alliance.ENEMY);
		List<UnitInPool> ccAssets = observer.getUnits(asset -> (asset.unit().getType() == Units.TERRAN_COMMAND_CENTER));
		Unit closestEnemy = null;
		double enemyDistance = Float.MAX_VALUE;
		
		for (UnitInPool enemyAsset : presentEnemies) {
			Unit enemyUnit = enemyAsset.unit();
			Point2d enemyPosition = enemyUnit.getPosition().toPoint2d();
			
			for (UnitInPool ccAsset : ccAssets) {
				Unit commandCenter = ccAsset.unit();
				Point2d commandCenterPosition = commandCenter.getPosition().toPoint2d();
				
				double distanceToCommandCenter = enemyPosition.distance(commandCenterPosition);
				
				if (distanceToCommandCenter < enemyDistance) {
					enemyDistance = distanceToCommandCenter;
					closestEnemy = enemyUnit;
				}
			}
		}
		
		if (enemyDistance > 20) {
			return;
		}
		
		reserveArmy.attack(observer, actions, closestEnemy.getPosition().toPoint2d());
		
	}

	public boolean enemyPositionScouted() {
		List<UnitInPool> units = observer.getUnits(Alliance.SELF);
		boolean unitCanSeeEnemyStart = false;

		Optional<Point2d> enemyPosition = Toolbox.findEnemyPosition(observer);

		if (enemyPosition.isEmpty()) {
			System.out.println("No enemy position");
			return false;
		}
		for (UnitInPool unitInPool : units) {
			Unit unit = unitInPool.unit();

			double distance = unit.getPosition().toPoint2d().distance(enemyPosition.get());
			double unitSightDistance = 7;
			if (distance < unitSightDistance) {
				unitCanSeeEnemyStart = true;
				break;
			}
		}

		return unitCanSeeEnemyStart;
	}

}
