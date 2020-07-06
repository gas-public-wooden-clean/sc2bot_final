package com.ebeane.sc2bot.Operations.Strategy;

import java.util.List;
import java.util.Optional;

import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.Toolbox.SupplyBracket;
import com.ebeane.sc2bot.InternalState.Expansion;
import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.ebeane.sc2bot.Operations.Building.BuildStructures;
import com.ebeane.sc2bot.Operations.Building.BuildUnits;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class StrategyNormal implements Strategy {

	boolean builtReaper = false;
	
	public void issueOrders(ObservationInterface observer, ActionInterface actions) {
		
		List<UnitInPool> army = observer.getUnits(Alliance.SELF);
		for (UnitInPool asset : army)
		{
			
			Unit unit = asset.unit();
			
			switch ((Units) unit.getType())
			{
                case TERRAN_BARRACKS: {
                	barracksOrders(observer, actions, unit);
                    break;
                }
                case TERRAN_STARPORT: {
                	starportOrders(observer, actions, unit);
                	break;
                }
                case TERRAN_SUPPLY_DEPOT: {
                	depotOrders(actions, unit);
                	break;
                }
                case TERRAN_BARRACKS_TECHLAB:
                	barracksTechLabOrders(observer, actions, unit);
                default:
                	break;
			}
		}
		
		techUp(observer, actions);
		
		if (Expansion.needExpansion()) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_COMMAND_CENTER, Units.TERRAN_COMMAND_CENTER);
		} else {
			BuildStructures.buildDepot(observer, actions);
			
			if (Expansion.needGas()) {
				BuildStructures.buildRefinery(observer, actions);
			}
			
			increaseArmyProduction(observer, actions);
		}
	}
	
	private void increaseArmyProduction(ObservationInterface observer, ActionInterface actions) {
		
	   SupplyBracket currSupplyBracket = Toolbox.getSupplyBracket(observer);
	   int maxBarracks = 0;
	   int maxStarports = 0;
	   
	   switch (currSupplyBracket) {
		   case LOW:
			   maxBarracks = 3;
			   maxStarports = 0;
			   break;
		   case MEDIUM:
			   maxBarracks = 6;
			   maxStarports = 2;
			   break;
		   case HIGH:
			   maxBarracks = 10;
			   maxStarports = 4;
			   break;
		   default:
				System.out.println("Invalid supply bracket");
				return;
	   }
	   
	   	int barracksCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_BARRACKS 
				|| asset.unit().getType() == Units.TERRAN_BARRACKS_FLYING).size();
		if (barracksCount < maxBarracks) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_BARRACKS, Units.TERRAN_BARRACKS);
		}
		
		int starportCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_STARPORT
				|| asset.unit().getType() == Units.TERRAN_STARPORT_FLYING).size();
		if (starportCount < maxStarports) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_STARPORT, Units.TERRAN_STARPORT);
		}
		
	}
	
	private void techUp(ObservationInterface observer, ActionInterface actions) {
		
		int depotCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT 
				|| asset.unit().getType() == Units.TERRAN_SUPPLY_DEPOT_LOWERED).size();
		if (depotCount < 1) {
			BuildStructures.buildDepot(observer, actions);
		}
		
		int barracksCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_BARRACKS 
				|| asset.unit().getType() == Units.TERRAN_BARRACKS_FLYING).size();
		if (barracksCount < 1) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_BARRACKS, Units.TERRAN_BARRACKS);
		}
		
		int factoryCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_FACTORY 
				|| asset.unit().getType() == Units.TERRAN_FACTORY_FLYING).size();
		if (factoryCount < 1) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_FACTORY, Units.TERRAN_FACTORY);
		}
		
		int starportCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_STARPORT
				|| asset.unit().getType() == Units.TERRAN_STARPORT_FLYING).size();
		if (starportCount < 1) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_STARPORT, Units.TERRAN_STARPORT);
		}
	
		return;
	}
	
	private void starportOrders(ObservationInterface observer, ActionInterface actions, Unit starport)
	{
		if (Expansion.needExpansion()) {
			return;
		}
		
		BuildUnits.trainUnit(observer, actions, starport, Abilities.TRAIN_VIKING_FIGHTER, Units.TERRAN_VIKING_FIGHTER, 30);
	}
	
	private void depotOrders(ActionInterface actions, Unit depot) {
		actions.unitCommand(depot, Abilities.MORPH_SUPPLY_DEPOT_LOWER, false);
	}
	
	private void barracksOrders(ObservationInterface observer, ActionInterface actions, Unit barracks)
	{
		if (Expansion.needExpansion()) {
			return;
		}
		
		if (!builtReaper && Resources.haveResourcesForUnit(Units.TERRAN_REAPER)) {
			BuildUnits.trainUnit(observer, actions, barracks, Abilities.TRAIN_REAPER, Units.TERRAN_REAPER, 1);
			builtReaper = true;
		}
		else {
			BuildStructures.buildAddon(observer, actions, barracks, Abilities.BUILD_TECHLAB_BARRACKS, Units.TERRAN_BARRACKS_TECHLAB);
			BuildUnits.trainUnit(observer, actions, barracks, Abilities.TRAIN_MARINE, Units.TERRAN_MARINE, 50);
		}
	}
	

    
	private void barracksTechLabOrders(ObservationInterface observer, ActionInterface actions, Unit techlab) {
		
		List<Upgrade> researchedUpgrades = observer.getUpgrades();
		
		for (Upgrade technology : researchedUpgrades) {
			if (technology == Upgrades.COMBAT_SHIELD) {
				return;
			}
		}
		
		actions.unitCommand(techlab, Abilities.RESEARCH_COMBAT_SHIELD, false);
	}
	
}
