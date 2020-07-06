package com.ebeane.sc2bot.InternalState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.Operations.Division;
import com.ebeane.sc2bot.Operations.Building.BuildingIntersect;
import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.Toolbox.SupplyBracket;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.query.QueryBuildingPlacement;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.spatial.RectangleI;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Expansion implements Division {
	
	static HashSet<Point2d> baseLocations;
	static HashSet<Point2d> buildingLocations;
	
	static HashMap<Point2d, Boolean> claimedBases;
	static HashMap<Point2d, Boolean> claimedBuildingLocations;
	static ObservationInterface observer;
	static ActionInterface actions;
	static QueryInterface query;
	
	public Expansion(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		Expansion.observer = observer;
		Expansion.actions = actions;
		Expansion.query = query;
		
		Expansion.claimedBases = new HashMap<Point2d, Boolean>();
		Expansion.claimedBuildingLocations = new HashMap<Point2d, Boolean>();
		
		Expansion.baseLocations = getBaseLocations();
		Expansion.buildingLocations = getBuildingLocations();
	}
	

	@Override
	public void updateForStep(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		Expansion.observer = observer;
		Expansion.actions = actions;
		Expansion.query = query;
		
		updateClaimedBases();
		updateClaimedBuildingLocations();
	}

	@Override
	public void issueCommands(BotBehaviorState botState) {
		// TODO Auto-generated method stub
		
	}
	
	private HashSet<Point2d> getBuildingLocations() {
		RectangleI playableArea = observer.getGameInfo().getStartRaw().get().getPlayableArea();
		List<QueryBuildingPlacement> queries = new ArrayList<QueryBuildingPlacement>();
		HashSet<Point2d> locations = new HashSet<Point2d>();
		
		for (float x = 0.0f; x < playableArea.getP1().getX(); x += 6) {			
			for (float y = 0.0f; y < playableArea.getP1().getY(); y += 4) {
				Point2d buildingLocationCandidate = Point2d.of(x, y);
				
				QueryBuildingPlacement buildingQuery = 
						QueryBuildingPlacement.placeBuilding()
						.useAbility(Abilities.BUILD_BARRACKS)
						.on(buildingLocationCandidate)
						.build();
				
				queries.add(buildingQuery);
			}
		}
		
		List<Boolean> results = query.placement(queries);
		
		for (int i = 0; i < queries.size(); i++) {
			Point2d buildingLocationCandidate = queries.get(i).getTarget();
			
			if (!results.get(i)) {
				continue;
			}
			
			locations.add(buildingLocationCandidate);
		}
		return locations;
	}
	
	private HashSet<Point2d> getBaseLocations(){
		HashSet<Point2d> locations = new HashSet<Point2d>();
		
		locations.add(Point2d.of(55.5f, 157.5f));
		locations.add(Point2d.of(82.5f,  160.5f));
		locations.add(Point2d.of(112.5f, 159.5f));
		locations.add(Point2d.of(154.5f, 157.5f));
		
		locations.add(Point2d.of(49.5f, 114.5f));
		locations.add(Point2d.of(71.5f, 131.5f));
		locations.add(Point2d.of(120.5f, 132.5f));
		locations.add(Point2d.of(150.5f, 131.5f));
		
		locations.add(Point2d.of(65.5f, 72.5f));
		locations.add(Point2d.of(95.5f, 71.5f));
		locations.add(Point2d.of(144.5f, 72.5f));
		locations.add(Point2d.of(166.5f, 89.5f));
		
		locations.add(Point2d.of(61.5f, 46.5f));
		locations.add(Point2d.of(103.5f, 44.5f));
		locations.add(Point2d.of(133.5f, 43.5f));
		locations.add(Point2d.of(160.5f, 46.5f));
		
		return locations;
	}
	
	private void updateClaimedBases() {
		List<UnitInPool> allAssets = observer.getUnits();
		
		for (Point2d location : baseLocations) {
			
			boolean baseClaimed = false;
			
			for (UnitInPool asset : allAssets) {
				Unit unit = asset.unit();
				
				if (BuildingIntersect.doBuildingsIntersect(Units.TERRAN_COMMAND_CENTER, location, unit)) {
					baseClaimed = true;
					break;
				}
			}
			
			if (!claimedBases.containsKey(location)) {
				claimedBases.put(location, baseClaimed);
			} else {
				claimedBases.replace(location, baseClaimed);
			}	
		}
	}
	
	private void updateClaimedBuildingLocations() {
		List<UnitInPool> allAssets = observer.getUnits();
		
		for (Point2d location : buildingLocations) {
			
			boolean buildingBlocked = false;
			
			for (UnitInPool asset : allAssets) {
				Unit unit = asset.unit();
				
				if (BuildingIntersect.doBuildingsIntersect(Units.TERRAN_BARRACKS, location, unit)) {
					buildingBlocked = true;
					break;
				}
			}
			
			if (!claimedBuildingLocations.containsKey(location)) {
				claimedBuildingLocations.put(location, buildingBlocked);
			} else {
				claimedBuildingLocations.replace(location, buildingBlocked);
			}
		}
	}
	
	public static Optional<Unit> getNextGasPosition() {
		double nearestGeyserdistance = Double.MAX_VALUE;
		Unit nearestGeyser = null;
		List<UnitInPool> ccAssets = observer.getUnits(asset -> (asset.unit().getType() == Units.TERRAN_COMMAND_CENTER));	
		List<UnitInPool> vgAssets = observer.getUnits(Toolbox.isVespeneGeyser());
		List<UnitInPool> gasCollectors = observer.getUnits(asset -> asset.unit().getType() == Units.PROTOSS_ASSIMILATOR ||
				asset.unit().getType() == Units.ZERG_EXTRACTOR ||
				asset.unit().getType() == Units.TERRAN_REFINERY);
				
		
		for (UnitInPool gasAsset : vgAssets) {
			Unit gas = gasAsset.unit();
			Point2d gasLocation = gas.getPosition().toPoint2d();
			boolean gasClaimed = false;
			
			for (UnitInPool collector : gasCollectors) {
				Point2d collectionPoint = collector.unit().getPosition().toPoint2d();
				
				if (collectionPoint.distance(gasLocation) == 0) {
					gasClaimed = true;
					break;
				}
			}
			
			if (gasClaimed) {
				continue;
			}

			for (UnitInPool ccAsset : ccAssets) {
				Unit commandCenter = ccAsset.unit();
				Point2d commandCenterPosition = commandCenter.getPosition().toPoint2d();
				
				double dinstanceToCommandCenter = gasLocation.distance(commandCenterPosition);
				
				if (dinstanceToCommandCenter < nearestGeyserdistance) {
					nearestGeyserdistance = dinstanceToCommandCenter;
					nearestGeyser = gas;
				}
			}
		}
		
		return Optional.ofNullable(nearestGeyser);
	}
	
	public static Optional<Point2d> getNextExpansionPosition() {
		Point2d closestExpansion = null;
		double closestExpansionDistance = Double.MAX_VALUE;
		
		List<UnitInPool> selfCommandCenters = observer.getUnits(Alliance.SELF, asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER);
		
		/* Find the next unclaimed expansion closest to all current bases */
		for (Point2d basePosition : claimedBases.keySet()) {
			double currentAverageDistance = 0;
			
			if (claimedBases.get(basePosition)) {
				continue;
			} 
			
			for (UnitInPool ccAsset : selfCommandCenters) {
				Point2d ccPosition = ccAsset.unit().getPosition().toPoint2d();
				
				currentAverageDistance += basePosition.distance(ccPosition) / selfCommandCenters.size();
			}
			
			if (currentAverageDistance < closestExpansionDistance) {
				closestExpansion = basePosition;
				closestExpansionDistance = currentAverageDistance;
			}
		}
		
		return Optional.ofNullable(closestExpansion);
	}
	
	public static Optional<Point2d> getNextBuildingPosition() {
		
		Point2d retPosition = null;
		double distance = Double.MAX_VALUE;
		
		List<UnitInPool> selfCommandCenters = observer.getUnits(Alliance.SELF, asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER);
		
		/* Find the next unclaimed building location closest to all current bases */
		for (Point2d buildingLocation : claimedBuildingLocations.keySet()) {
			boolean tooCloseToCommandCenter = false;
			
			if (claimedBuildingLocations.get(buildingLocation)) {
				continue;
			} 
			
			for (UnitInPool ccAsset : selfCommandCenters) {
				
				double distanceToCommandCenter = buildingLocation.distance(ccAsset.unit().getPosition().toPoint2d());			
				if (distanceToCommandCenter < 10) {
					tooCloseToCommandCenter = true;
					break;
				}
			}
			
			if (tooCloseToCommandCenter) {
				continue;
			}
			
			for (UnitInPool ccAsset : selfCommandCenters) {
				
				double distanceToCommandCenter = buildingLocation.distance(ccAsset.unit().getPosition().toPoint2d());			
				if (distanceToCommandCenter < distance) {
					retPosition = buildingLocation;
					distance = distanceToCommandCenter;
				}
			}
		}
		
		return Optional.ofNullable(retPosition);
	}
	
	public static boolean needGas() {
		int refineryCount = Toolbox.countUnitType(observer, Units.TERRAN_REFINERY);
		int baseCount = Toolbox.countUnitType(observer, Units.TERRAN_COMMAND_CENTER);
		int supplyCount = observer.getFoodUsed();
		
		
		if (supplyCount < 20 && refineryCount < 1) {
			return true;
		} else if (refineryCount < (baseCount * 2)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean needExpansion() {
		int baseCount = Toolbox.countUnitType(observer, Units.TERRAN_COMMAND_CENTER);
		
		SupplyBracket currSupplyBracket = Toolbox.getSupplyBracket(observer);
		int neededBases = 0;
		switch (currSupplyBracket) {
		   case LOW:
			   neededBases = 1;
			   break;
		   case MEDIUM:
			   neededBases = 2;
			   break;
		   case HIGH:
			   neededBases = 3;
			   break;
		   default:
				System.out.println("Invalid supply bracket");
				return false;
	   }
		
		if (baseCount >= neededBases) {
			return false;
		}
		
		return true;
	}
}
