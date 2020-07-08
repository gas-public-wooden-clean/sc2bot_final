package com.ebeane.sc2bot.Operations.Building;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class BuildingIntersect {

	
	public static boolean doBuildingsIntersect (UnitType toBuildType, Point2d toBuildPosition, Unit existingBuilding) {
		
		float toPlaceRadius = getBuildingRadius(toBuildType);
		float existingRadius = getBuildingRadius(existingBuilding.getType());
		
		if (0 == toPlaceRadius || 0 == existingRadius) {
			return false;
		}
		
		Point2d existingPosition = existingBuilding.getPosition().toPoint2d();
		
		Point2d toPlaceBottomLeft = Point2d.of(toBuildPosition.getX() - toPlaceRadius, toBuildPosition.getY() - toPlaceRadius);
		Point2d toPlaceTopRight = Point2d.of(toBuildPosition.getX() + toPlaceRadius, toBuildPosition.getY() + toPlaceRadius);
		Point2d existingBottomLeft = Point2d.of(existingPosition.getX() - existingRadius, existingPosition.getY() - existingRadius);
		Point2d existingTopRight = Point2d.of(existingPosition.getX() + existingRadius, existingPosition.getY() + existingRadius);
		
		/*
		System.out.println();
		System.out.println(toPlaceBottomLeft.getX() + " | " + existingTopRight.getX() + " | " + (toPlaceBottomLeft.getX() > existingTopRight.getX()) );
		System.out.println(toPlaceTopRight.getX() + " | " + existingBottomLeft.getX() + " | " + (toPlaceTopRight.getX() < existingBottomLeft.getX()) );
		System.out.println(toPlaceBottomLeft.getY() + " | " + existingTopRight.getY() + " | " + (toPlaceBottomLeft.getY() > existingTopRight.getY()) );
		System.out.println(toPlaceTopRight.getY() + " | " + existingBottomLeft.getY() + " | " + (toPlaceTopRight.getY() < existingBottomLeft.getY()));
		*/
		
		if (toPlaceBottomLeft.getX() >= existingTopRight.getX() 				//To place is to the right of existing
				|| toPlaceTopRight.getX() <= existingBottomLeft.getX()		//To place is to the left of existing
				|| toPlaceBottomLeft.getY() >= existingTopRight.getY()		//To place is above existing
				|| toPlaceTopRight.getY() <= existingBottomLeft.getY()) {	//To place is below existing
			return false;
		}
		
		
		return true;
	}
	
	
	private static float getBuildingRadius(UnitType buildingType) {
		
		if (buildingType == Units.TERRAN_SENSOR_TOWER
				|| buildingType == Units.ZERG_CREEP_TUMOR
				|| buildingType == Units.ZERG_CREEP_TUMOR_BURROWED
				|| buildingType == Units.ZERG_CREEP_TUMOR_QUEEN) {
			return 0.5f;
		} else if (buildingType == Units.TERRAN_BARRACKS_REACTOR
				|| buildingType == Units.TERRAN_BARRACKS_TECHLAB
				|| buildingType == Units.TERRAN_FACTORY_REACTOR
				|| buildingType == Units.TERRAN_FACTORY_TECHLAB
				|| buildingType == Units.TERRAN_MISSILE_TURRET
				|| buildingType == Units.TERRAN_STARPORT_REACTOR
				|| buildingType == Units.TERRAN_STARPORT_TECHLAB
				|| buildingType == Units.TERRAN_SUPPLY_DEPOT
				|| buildingType == Units.TERRAN_SUPPLY_DEPOT_LOWERED
				|| buildingType == Units.ZERG_GREATER_SPIRE
				|| buildingType == Units.ZERG_SPINE_CRAWLER
				|| buildingType == Units.ZERG_SPINE_CRAWLER_UPROOTED
				|| buildingType == Units.ZERG_SPIRE
				|| buildingType == Units.ZERG_SPORE_CRAWLER
				|| buildingType == Units.ZERG_SPORE_CRAWLER_UPROOTED
				|| buildingType == Units.PROTOSS_DARK_SHRINE
				|| buildingType == Units.PROTOSS_PHOTON_CANNON
				|| buildingType == Units.PROTOSS_PYLON
				|| buildingType == Units.PROTOSS_PYLON_OVERCHARGED) {
			return 1.0f;
		} else if (buildingType == Units.TERRAN_ARMORY
				|| buildingType == Units.TERRAN_BARRACKS
				|| buildingType == Units.TERRAN_BUNKER
				|| buildingType == Units.TERRAN_ENGINEERING_BAY
				|| buildingType == Units.TERRAN_FACTORY
				|| buildingType == Units.TERRAN_FUSION_CORE
				|| buildingType == Units.TERRAN_GHOST_ACADEMY
				|| buildingType == Units.TERRAN_REFINERY
				|| buildingType == Units.TERRAN_STARPORT
				|| buildingType == Units.ZERG_BANELING_NEST
				|| buildingType == Units.ZERG_EVOLUTION_CHAMBER
				|| buildingType == Units.ZERG_EXTRACTOR
				|| buildingType == Units.ZERG_HYDRALISK_DEN
				|| buildingType == Units.ZERG_INFESTATION_PIT
				|| buildingType == Units.ZERG_LURKER_DEN_MP
				|| buildingType == Units.ZERG_NYDUS_CANAL
				|| buildingType == Units.ZERG_NYDUS_NETWORK
				|| buildingType == Units.ZERG_ROACH_WARREN
				|| buildingType == Units.ZERG_ULTRALISK_CAVERN
				|| buildingType == Units.ZERG_SPAWNING_POOL
				|| buildingType == Units.PROTOSS_ASSIMILATOR
				|| buildingType == Units.PROTOSS_CYBERNETICS_CORE
				|| buildingType == Units.PROTOSS_FLEET_BEACON
				|| buildingType == Units.PROTOSS_FORGE
				|| buildingType == Units.PROTOSS_GATEWAY
				|| buildingType == Units.PROTOSS_ROBOTICS_BAY
				|| buildingType == Units.PROTOSS_ROBOTICS_FACILITY
				|| buildingType == Units.PROTOSS_STARGATE
				|| buildingType == Units.PROTOSS_TEMPLAR_ARCHIVE
				|| buildingType == Units.PROTOSS_TWILIGHT_COUNCIL
				|| buildingType == Units.PROTOSS_WARP_GATE) {
			return 1.5f;
		} else if (buildingType == Units.TERRAN_COMMAND_CENTER
				|| buildingType == Units.TERRAN_ORBITAL_COMMAND
				|| buildingType == Units.TERRAN_PLANETARY_FORTRESS
				|| buildingType == Units.ZERG_HATCHERY
				|| buildingType == Units.ZERG_HIVE
				|| buildingType == Units.ZERG_LAIR
				|| buildingType == Units.PROTOSS_NEXUS) {
			return 2.5f;
		}
		return 0.0f;
	}
}
