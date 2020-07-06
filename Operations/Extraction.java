package com.ebeane.sc2bot.Operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.InternalState.Oversight;
import com.ebeane.sc2bot.InternalState.Economy.CollectionCenter;
import com.ebeane.sc2bot.InternalState.Economy.JobRoster;
import com.ebeane.sc2bot.InternalState.Economy.JobRoster.Job;
import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.ebeane.sc2bot.Toolbox;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class Extraction implements Division {
	private ObservationInterface observer;
	private ActionInterface actions;
	
	private HashMap<Tag, UnitInPool> scvToCollectionPoint;
	
	public Extraction(ObservationInterface observer, ActionInterface actions) {
		this.observer = observer;
		this.actions = actions;
		
		this.scvToCollectionPoint = new HashMap<Tag, UnitInPool>();
	}
	
	@Override
	public void updateForStep(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		this.observer = observer;
		this.actions = actions;
	}
	
	@Override
	public void issueCommands(BotBehaviorState botState) {
		List<UnitInPool> ccForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER);
		List<UnitInPool> scvForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_SCV);
		
		// Issue scv orders
		if (baseUnderAttack()) {
			scvDefense();
		} else {
			for (UnitInPool asset : scvForce) {		
				scvOrders(asset);
			}
		}
		
		// Issue Command center orders
		for (UnitInPool asset : ccForce) {		
			commandCenterOrders(asset);
		}
	}
	
	private boolean baseUnderAttack() {
		List<UnitInPool> ccForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER);
		List<UnitInPool> enemyForce = observer.getUnits(Alliance.ENEMY);
		
		if (ccForce.size() != 1) {
			return false;
		}
		
		int enemiesInBase = 0;
		Point2d ccPosition = ccForce.get(0).unit().getPosition().toPoint2d();
		for (UnitInPool enemyAsset : enemyForce) {
			Point2d enemyPosition = enemyAsset.unit().getPosition().toPoint2d();
			
			if (enemyPosition.distance(ccPosition) < 15) {
				enemiesInBase++;
			}
		}
		
		return (enemiesInBase > 2);
	}
	
	private void scvDefense() {
		List<UnitInPool> scvForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_SCV);
		List<UnitInPool> ccForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER);
		
		Point2d ccPosition = ccForce.get(0).unit().getPosition().toPoint2d();
		
		Optional<Unit> target = Toolbox.findNearestUnit(observer, ccPosition, Alliance.ENEMY);
		
		if (target.isEmpty()) {
			return;
		}
		
		for (UnitInPool scvAsset : scvForce) {
			Unit scv = scvAsset.unit();
			
			actions.unitCommand(scv, Abilities.ATTACK_ATTACK, target.get().getPosition().toPoint2d(), false);
		}
		return;
	}
	
	private void commandCenterOrders(UnitInPool ccAsset) {
		Unit commandCenter = ccAsset.unit();
		if (commandCenter.getOrders().size() != 0)
		{
			return;
		}
    	
    	if (Toolbox.countUnitType(observer, Units.TERRAN_SCV) > 50)
        {
    		return;
        }
    	
    	if (!Resources.haveResourcesForUnit(Units.TERRAN_SCV)) {
    		return;
    	}
    	
    	Resources.allocateResourcesForUnit(Units.TERRAN_SCV);
    	actions.unitCommand(commandCenter, Abilities.TRAIN_SCV, false);
	}
	
	private void scvOrders(UnitInPool scvAsset) {
		Unit scv = scvAsset.unit();
	
		if (Oversight.unitUnderAttack(scvAsset) && !observer.getUnits(Alliance.ENEMY).isEmpty()) {
			/* Fight back */
			Toolbox.findNearestUnit(observer, scv.getPosition().toPoint2d(), Alliance.ENEMY)
				.ifPresent(enemyPath -> actions.unitCommand(scv, Abilities.ATTACK_ATTACK, enemyPath.getPosition().toPoint2d(), false));
			return;
		}

		updateCollectionJob(scvAsset);
	}
	
	private void updateCollectionJob(UnitInPool scvAsset) {
		Job workerJob = JobRoster.getJobForWorker(scvAsset);
		Optional<UnitInPool> underMannedRefinery = CollectionCenter.getUnderMannedCollectionPoint(Units.TERRAN_REFINERY);
		Optional<UnitInPool> underMannedCommandCenter= CollectionCenter.getUnderMannedCollectionPoint(Units.TERRAN_COMMAND_CENTER);
		UnitInPool currentLocation = JobRoster.getLocationForWorker(scvAsset);
		
		Job newJob;
		UnitInPool newLocation;
		Unit target;
		
		if (Job.GAS == workerJob
				|| Job.BUILD == workerJob) {
			return;
		}
		
		if (Job.MINERALS == workerJob) {
			
			// If your current spot isn't at maxium efficiency, stay here
			boolean workerLocationFull = CollectionCenter.getCollectionPointCapacity(currentLocation) < 0;
			if (!workerLocationFull) {
				return;
			}
			
			// If there's no better place to be right now, stay here
			if (underMannedRefinery.isEmpty() 
					&& underMannedCommandCenter.isEmpty()) {
				return;
			}
		}
		
		// Time to find a new job/location
		if (underMannedRefinery.isPresent()) {
			newJob = Job.GAS;
			newLocation = underMannedRefinery.get();
			target = newLocation.unit();
		}
		else if (underMannedCommandCenter.isPresent()) {
			newJob = Job.MINERALS;
			newLocation = underMannedCommandCenter.get();
			target = Toolbox.findNearestUnit(observer, newLocation.unit().getPosition().toPoint2d(), Toolbox.isMineralPatch(), Alliance.NEUTRAL).get();
		} else {
			Optional<UnitInPool> randomCommandCenter = Toolbox.getRandomUnit(observer, Units.TERRAN_COMMAND_CENTER);
			if (randomCommandCenter.isEmpty()) {
				return;
			}
			newJob = Job.MINERALS;
			newLocation = randomCommandCenter.get();
			target = Toolbox.findNearestUnit(observer, newLocation.unit().getPosition().toPoint2d(), Toolbox.isMineralPatch(), Alliance.NEUTRAL).get();
		}
		
		if (null != currentLocation) {
			CollectionCenter.dismissWorker(currentLocation);
		}
		
		JobRoster.assignJob(scvAsset, newLocation, newJob);
		CollectionCenter.hireWorker(newLocation);
		actions.unitCommand(scvAsset.unit(), Abilities.SMART, target, false);
	}
}
