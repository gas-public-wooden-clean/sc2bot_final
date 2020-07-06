package com.ebeane.sc2bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.spatial.RectangleI;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class Toolbox {
    
	public enum SupplyBracket {
		LOW, MEDIUM, HIGH
	}
	
	public static SupplyBracket getSupplyBracket(ObservationInterface observer) {
		if (observer.getFoodUsed() < 40) {
			return SupplyBracket.LOW;
		} else if (observer.getFoodUsed() >= 40 && observer.getFoodUsed() < 100) {
			return SupplyBracket.MEDIUM;
		} else {
			return SupplyBracket.HIGH;
		}
	}
	
	
	public static int countUnitType(ObservationInterface observer, Units unitType) {
        return observer.getUnits(Alliance.SELF, UnitInPool.isUnit(unitType)).size();
    }
    
	public static Optional<Unit> findNearestUnit(ObservationInterface observer, Point2d start, Predicate<UnitInPool> unitType, Alliance alliance) {
		List<UnitInPool> units = observer.getUnits(alliance, unitType);
		double distance = Double.MAX_VALUE;
		Unit target = null;
		for (UnitInPool unitInPool : units) {
			Unit unit = unitInPool.unit();
			double d = unit.getPosition().toPoint2d().distance(start);
			if (d < distance) {
				distance = d;
				target = unit;
			}
		}
		return Optional.ofNullable(target);
	}
	
	public static Optional<Unit> findNearestUnit(ObservationInterface observer, Point2d start, Alliance alliance) {
		List<UnitInPool> units = observer.getUnits(alliance);
		double distance = Double.MAX_VALUE;
		Unit target = null;
		for (UnitInPool unitInPool : units) {
			Unit unit = unitInPool.unit();
			double d = unit.getPosition().toPoint2d().distance(start);
			if (d < distance) {
				distance = d;
				target = unit;
			}
		}
		return Optional.ofNullable(target);
	}
	
	public static Optional<Point2d> findEnemyPosition(ObservationInterface observer) {
		ResponseGameInfo gameInfo = observer.getGameInfo();

		Optional<StartRaw> startRaw = gameInfo.getStartRaw();
		if (startRaw.isPresent()) {
			Set<Point2d> startLocations = new HashSet<Point2d>(startRaw.get().getStartLocations());
			startLocations.remove(observer.getStartLocation().toPoint2d());
			if (startLocations.isEmpty())
				return Optional.empty();
			return Optional.of(
					new ArrayList<Point2d>(startLocations).get(ThreadLocalRandom.current().nextInt(startLocations.size())));
		} else {
			return Optional.empty();
		}
	}
    
    public static Predicate<UnitInPool> isMineralPatch() {
    	Predicate<UnitInPool> mineralCondition = asset -> 
    	(asset.unit().getType() == Units.NEUTRAL_LAB_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_LAB_MINERAL_FIELD750 ||
    	asset.unit().getType() == Units.NEUTRAL_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_MINERAL_FIELD750 ||
    	asset.unit().getType() == Units.NEUTRAL_RICH_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_RICH_MINERAL_FIELD750 ||
    	asset.unit().getType() == Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD750 ||
    	asset.unit().getType() == Units.NEUTRAL_PURIFIER_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_PURIFIER_MINERAL_FIELD750 ||
    	asset.unit().getType() == Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD ||
    	asset.unit().getType() == Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD750);
    	
    	return mineralCondition;
    }
    
    public static Predicate<UnitInPool> isVespeneGeyser() {
    	Predicate<UnitInPool> vespeneCondition = asset -> 
    	(asset.unit().getType() == Units.NEUTRAL_VESPENE_GEYSER ||
    	asset.unit().getType() == Units.NEUTRAL_RICH_VESPENE_GEYSER ||
    	asset.unit().getType() == Units.NEUTRAL_SHAKURAS_VESPENE_GEYSER ||
    	asset.unit().getType() == Units.NEUTRAL_PROTOSS_VESPENE_GEYSER ||
    	asset.unit().getType() == Units.NEUTRAL_PURIFIER_VESPENE_GEYSER ||
    	asset.unit().getType() == Units.NEUTRAL_SPACE_PLATFORM_GEYSER);
    	
    	return vespeneCondition;
    }
    
    public static Predicate<UnitInPool> isBuilding() {
    	Predicate<UnitInPool> buildingCondition = asset -> 
    	(asset.unit().getType() == Units.TERRAN_ARMORY ||
    	asset.unit().getType() == Units.TERRAN_BARRACKS ||
    	asset.unit().getType() == Units.TERRAN_BARRACKS_FLYING ||
    	asset.unit().getType() == Units.TERRAN_BARRACKS_REACTOR ||
    	asset.unit().getType() == Units.TERRAN_BARRACKS_TECHLAB ||
    	asset.unit().getType() == Units.TERRAN_BUNKER ||
    	asset.unit().getType() == Units.TERRAN_COMMAND_CENTER ||
    	asset.unit().getType() == Units.TERRAN_COMMAND_CENTER_FLYING ||
    	asset.unit().getType() == Units.TERRAN_ENGINEERING_BAY ||
    	asset.unit().getType() == Units.TERRAN_FACTORY ||
    	asset.unit().getType() == Units.TERRAN_FACTORY_FLYING ||
    	asset.unit().getType() == Units.TERRAN_FACTORY_REACTOR ||
    	asset.unit().getType() == Units.TERRAN_FACTORY_TECHLAB ||
    	asset.unit().getType() == Units.TERRAN_FUSION_CORE ||
    	asset.unit().getType() == Units.TERRAN_GHOST_ACADEMY ||
    	asset.unit().getType() == Units.TERRAN_MISSILE_TURRET ||
    	asset.unit().getType() == Units.TERRAN_ORBITAL_COMMAND ||
    	asset.unit().getType() == Units.TERRAN_ORBITAL_COMMAND_FLYING ||
    	asset.unit().getType() == Units.TERRAN_PLANETARY_FORTRESS ||
    	asset.unit().getType() == Units.TERRAN_REFINERY ||
    	asset.unit().getType() == Units.TERRAN_SENSOR_TOWER ||
    	asset.unit().getType() == Units.TERRAN_STARPORT ||
    	asset.unit().getType() == Units.TERRAN_STARPORT_FLYING ||
    	asset.unit().getType() == Units.TERRAN_STARPORT_REACTOR ||
    	asset.unit().getType() == Units.TERRAN_STARPORT_TECHLAB ||
    	asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT ||
    	asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT_LOWERED);
    	
    	return buildingCondition;
    }
    
	public static Optional<UnitInPool> getRandomUnit(ObservationInterface observer, UnitType unitType) {
		   List<UnitInPool> units = observer.getUnits(Alliance.SELF, UnitInPool.isUnit(unitType));
		   UnitInPool retUnit = null;
		   
		   for (UnitInPool asset : units) {
			   List<UnitOrder> currOrders = asset.unit().getOrders();
			   
			   if (currOrders.size() == 0) {
				   retUnit = asset;
				   break;
			   }
			   
			   UnitOrder firstOrder = currOrders.get(0);
			   boolean gatherer = 
						(firstOrder.getAbility() == Abilities.HARVEST_RETURN ||
						firstOrder.getAbility() == Abilities.HARVEST_GATHER);
			   
			   if (gatherer) {
				   retUnit = asset;
				   break;
			   }
			   
		   }
		   
		   return Optional.ofNullable(retUnit);
		}
	
	public static boolean positionsEqual (Point2d firstPosition, Point2d secondPosition) {
		
		if (firstPosition.getX() == secondPosition.getX() &&
				firstPosition.getY() == secondPosition.getY()) {
			return true;
		}	
		
		return false;
	}
	
	public static List<Unit> getUnits(ObservationInterface observer, Predicate<UnitInPool> condition) {
		return observer.getUnits(condition)
				.stream()
				.map(asset -> asset.unit())
				.collect(Collectors.toList());
	}
	
	public static Point2d getRandomMapCorner(ObservationInterface observer) {
		ResponseGameInfo gameInfo = observer.getGameInfo();
		RectangleI mapBoundaries = gameInfo.getStartRaw().get().getPlayableArea();

		Point2d cornerPoint = null;

		switch (ThreadLocalRandom.current().nextInt(4)) {
			case 1:
				cornerPoint = mapBoundaries.getP0().toPoint2d();
				break;
			case 2:
				cornerPoint = Point2d.of(mapBoundaries.getP1().getX(), 0);
				break;
			case 3:
				cornerPoint = Point2d.of(0, mapBoundaries.getP1().getY());
				break;
			case 0:
				cornerPoint = mapBoundaries.getP1().toPoint2d();
				break;
			default:
				cornerPoint = Point2d.of(0, 0);
		}
			
		return cornerPoint;
	}
	
	public static List<UnitInPool> getAirUnits(ObservationInterface observer, Alliance alliance) {
		List<UnitInPool> allUnits = observer.getUnits(alliance);
		List<UnitInPool> airUnits = new ArrayList<UnitInPool>();
		
		for (UnitInPool asset : allUnits) {
			Unit unit = asset.unit();
			
			if (unit.getFlying().isEmpty()) {
				continue;
			}
			
			if (unit.getFlying().get()) {
				airUnits.add(asset);
			}
		}
		
		return airUnits;
	}
}
