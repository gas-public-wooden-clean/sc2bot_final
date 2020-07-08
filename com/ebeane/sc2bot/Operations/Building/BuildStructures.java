package com.ebeane.sc2bot.Operations.Building;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.InternalState.Expansion;
import com.ebeane.sc2bot.InternalState.Economy.CollectionCenter;
import com.ebeane.sc2bot.InternalState.Economy.JobRoster;
import com.ebeane.sc2bot.InternalState.Economy.JobRoster.Job;
import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.ebeane.sc2bot.Toolbox.SupplyBracket;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class BuildStructures {
	
	private static boolean canBuildDepot(ObservationInterface observer) {
		
	   if (200 == observer.getFoodCap()) {
		   return false;
	   }
		
		if (!Resources.haveResourcesForUnit(Units.TERRAN_SUPPLY_DEPOT)) {
			return false;
		}
		
	   SupplyBracket currSupplyBracket = Toolbox.getSupplyBracket(observer);
	   int maxDepotBuilders = 0;
	   int depotSupplyHeadStart = 0;
		   
	   switch (currSupplyBracket) {
		   case LOW:
			   maxDepotBuilders = 1;
			   depotSupplyHeadStart = 2;
			   break;
		   case MEDIUM:
			   maxDepotBuilders = 2;
			   depotSupplyHeadStart = 4;
			   break;
		   case HIGH:
			   maxDepotBuilders = 3;
			   depotSupplyHeadStart = 6;
			   break;
		   default:
				System.out.println("Invalid supply bracket");
				return false;
	   }

	   if (observer.getFoodUsed() <= observer.getFoodCap() - depotSupplyHeadStart) {
	       return false;
	   }
	   
	   if (observer.getUnits(Alliance.SELF, isWorkerBuildingStructure(Abilities.BUILD_SUPPLY_DEPOT)).size() > maxDepotBuilders) {
	       return false;
	   }
		
		return true;
		
	}
	
	private static boolean haveStructure(ObservationInterface observer, Units requiredStructure) {
		Predicate<UnitInPool> requiredStructureCondition;
		
		if (null == requiredStructure) {
			return true;
		}
		
		switch (requiredStructure) {
			case TERRAN_COMMAND_CENTER:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_COMMAND_CENTER
						|| asset.unit().getType() == Units.TERRAN_COMMAND_CENTER_FLYING
						|| asset.unit().getType() == Units.TERRAN_ORBITAL_COMMAND
						|| asset.unit().getType() == Units.TERRAN_ORBITAL_COMMAND_FLYING
						|| asset.unit().getType() == Units.TERRAN_PLANETARY_FORTRESS);
				break;
			case TERRAN_ENGINEERING_BAY:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_ENGINEERING_BAY);
				break;
			case TERRAN_SUPPLY_DEPOT:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT
						|| asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT_LOWERED);
				break;
			case TERRAN_BARRACKS:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_BARRACKS
						|| asset.unit().getType() == Units.TERRAN_BARRACKS_FLYING);
				break;
			case TERRAN_FACTORY:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_FACTORY
						|| asset.unit().getType() == Units.TERRAN_FACTORY_FLYING);
				break;
			case TERRAN_STARPORT:
				requiredStructureCondition = asset -> (asset.unit().getType() == Units.TERRAN_STARPORT
						|| asset.unit().getType() == Units.TERRAN_STARPORT_FLYING);
				break;
			default:
				System.out.println("Unable to find required structure. Unknown unit type:" + requiredStructure);
				return false;
		}
		
		List<UnitInPool> requiredBuildings = observer.getUnits(requiredStructureCondition);
		boolean haveCompletedStructure = false;
		
		for (UnitInPool requiredBuildingAsset : requiredBuildings) {
			Unit requiredBuilding = requiredBuildingAsset.unit();
			
			if (requiredBuilding.getBuildProgress() == 1.0) {
				haveCompletedStructure = true;
			}
		}
		
		return haveCompletedStructure;
	}
	
	private static boolean haveStructureTechRequirements(ObservationInterface observer, Units structureToBuild) {
		Units precedingBuilding = null;
		
		switch (structureToBuild) {
			case TERRAN_COMMAND_CENTER:
			case TERRAN_SUPPLY_DEPOT:
			case TERRAN_REFINERY:
				precedingBuilding = null;
				break;
			case TERRAN_ENGINEERING_BAY:
				precedingBuilding = Units.TERRAN_COMMAND_CENTER;
				break;
			case TERRAN_SENSOR_TOWER:
			case TERRAN_MISSILE_TURRET:
				precedingBuilding = Units.TERRAN_ENGINEERING_BAY;
				break;
			case TERRAN_BARRACKS:
				precedingBuilding = Units.TERRAN_SUPPLY_DEPOT;
				break;
			case TERRAN_BUNKER:
			case TERRAN_FACTORY:
			case TERRAN_GHOST_ACADEMY:
				precedingBuilding = Units.TERRAN_BARRACKS;
				break;
			case TERRAN_STARPORT:
			case TERRAN_ARMORY:
				precedingBuilding = Units.TERRAN_FACTORY;
				break;
			case TERRAN_FUSION_CORE:
				precedingBuilding = Units.TERRAN_STARPORT;
			default:
				System.out.println("Unable to build structure. Unknown unit type:" + structureToBuild);
				return false;
		}
		
		return haveStructure(observer, precedingBuilding);
	}
	
	
	public static void buildDepot(ObservationInterface observer, ActionInterface actions) {
		
		if (!canBuildDepot(observer)) {
			return;
		}
		
		tryBuildDepot(observer, actions);
	}
	    

    public static void buildRefinery(ObservationInterface observer, ActionInterface actions) {
    	
    	if (!Resources.haveResourcesForUnit(Units.TERRAN_REFINERY)) {
			return;
		}
    	
    	/* Get our builder */
    	Optional<UnitInPool> scvPool = Toolbox.getRandomUnit(observer, Units.TERRAN_SCV);
    	if (scvPool.isEmpty())
    	{
    		return;
    	}
    	
    	Unit scv = scvPool.get().unit();
    	Optional<Unit> geyser = Expansion.getNextGasPosition();
    	if (geyser.isEmpty())
    	{
    		return;
    	}
    	
    	UnitInPool currentLocation = JobRoster.getLocationForWorker(scvPool.get());
    	
    	JobRoster.assignJob(scvPool.get(), null, Job.GAS);
 	   	CollectionCenter.dismissWorker(currentLocation);
    	Resources.allocateResourcesForUnit(Units.TERRAN_REFINERY);
    	actions.unitCommand(scv, Abilities.BUILD_REFINERY, geyser.get(), false);
    }
    
	public static void buildAddon(ObservationInterface observer, ActionInterface actions,
			Unit structure, Abilities constructAddon, Units addonToBuild) {
		
		if (structure.getOrders().size() != 0)
		{
			return;
		}
		
		if (structure.getAddOnTag().isPresent()) {
			return;
		}
		
		if (!Resources.haveResourcesForUnit(addonToBuild)) {
			return;
		}
		
		if (observer.getUnits(asset -> asset.unit().getType() == addonToBuild).size() > 0) {
			return;
		}
		
		Resources.allocateResourcesForUnit(addonToBuild);
		actions.unitCommand(structure, constructAddon, false);
	}
	    
	public static void buildStructure(ObservationInterface observer, ActionInterface actions, Ability buildStructure, Units structureToBuild) {

		if (!haveStructureTechRequirements(observer, structureToBuild)) {
			return;
		}
		
		if (!Resources.haveResourcesForUnit(structureToBuild)) {
			return;
		}
		
	   Optional<UnitInPool> builder = JobRoster.getAvailableWorker();
	   
	   if (builder.isEmpty()) {
		   return;
	   }
	   
	   UnitInPool currentLocation = JobRoster.getLocationForWorker(builder.get());
	   
	   Unit scv = builder.get().unit();
	   Optional<Point2d> buildingSite;
	   if (Units.TERRAN_COMMAND_CENTER == structureToBuild) {
		   buildingSite = Expansion.getNextExpansionPosition();
	   } else {
		   buildingSite = Expansion.getNextBuildingPosition();
	   }
	   
	   if (buildingSite.isEmpty()) {
		   return;
	   }
	   
	   JobRoster.assignJob(builder.get(), null, Job.BUILD);
	   CollectionCenter.dismissWorker(currentLocation);
	   Resources.allocateResourcesForUnit(structureToBuild);
	   actions.unitCommand(scv, buildStructure, buildingSite.get(), false);
	}
	
	private static void tryBuildDepot(ObservationInterface observer, ActionInterface actions) {

		Optional<UnitInPool> builder = JobRoster.getAvailableWorker();
		   
	   if (builder.isEmpty()) {
		   return;
	   }
	   
	   UnitInPool currentLocation = JobRoster.getLocationForWorker(builder.get());
	   
	   Unit scv = builder.get().unit();
	   Point2d scvPosition = scv.getPosition().toPoint2d();
	   
	   Optional<Unit> ccOptional = Toolbox.findNearestUnit(observer, scvPosition, asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER, Alliance.SELF);
	   if (ccOptional.isEmpty()) {
		   return;
	   }
	   
	   Unit closestCommandCenter = ccOptional.get();
	   Point2d buildingSite = closestCommandCenter.getPosition().toPoint2d().add(Point2d.of(getRandomScalar(), getRandomScalar()).mul(5.0f));
	   
	   JobRoster.assignJob(builder.get(), null, Job.BUILD);
	   CollectionCenter.dismissWorker(currentLocation);
	   Resources.allocateResourcesForUnit(Units.TERRAN_SUPPLY_DEPOT);
	   actions.unitCommand(scv, Abilities.BUILD_SUPPLY_DEPOT, buildingSite, false);
	   return;
	}
	
	private static Predicate<UnitInPool> isWorkerBuildingStructure(Ability abilityTypeForStructure) {
	   return unitInPool -> unitInPool.unit()
	           .getOrders()
	           .stream()
	           .anyMatch(unitOrder -> abilityTypeForStructure.equals(unitOrder.getAbility()));
	}

	private static float getRandomScalar() {
		   return ThreadLocalRandom.current().nextFloat() * 2 - 1;
	}
}
