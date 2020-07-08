package com.ebeane.sc2bot.Operations.Strategy;

import java.util.List;

import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.InternalState.Expansion;
import com.ebeane.sc2bot.Operations.Building.BuildStructures;
import com.ebeane.sc2bot.Operations.Building.BuildUnits;
import com.ebeane.sc2bot.Toolbox.SupplyBracket;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class StrategyRush implements Strategy {

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
                case TERRAN_SUPPLY_DEPOT: {
                	depotOrders(actions, unit);
                	break;
                }
                default:
                	break;
			}
		}
		
		techUp(observer, actions);
		BuildStructures.buildDepot(observer, actions);
		if (Expansion.needGas()) {
			BuildStructures.buildRefinery(observer, actions);
		}
		increaseArmyProduction(observer, actions);
	}
	
	private void increaseArmyProduction(ObservationInterface observer, ActionInterface actions) {
		
	   /*SupplyBracket currSupplyBracket = Toolbox.getSupplyBracket(observer);
	   int maxBarracks = 0;
	   
	   switch (currSupplyBracket) {
		   case LOW:
			   maxBarracks = 3;
			   break;
		   case MEDIUM:
			   maxBarracks = 6;
			   break;
		   case HIGH:
			   maxBarracks = 10;
			   break;
		   default:
				System.out.println("Invalid supply bracket");
				return;
	   }
	   
	   	int barracksCount = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_BARRACKS 
				|| asset.unit().getType() == Units.TERRAN_BARRACKS_FLYING).size();
		if (barracksCount < maxBarracks) {
			BuildStructures.buildStructure(observer, actions, Abilities.BUILD_BARRACKS, Units.TERRAN_BARRACKS);
		}*/
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
	
		return;
	}
	
	private void barracksOrders(ObservationInterface observer, ActionInterface actions, Unit barracks) {
		if (Expansion.needExpansion()) {
			return;
		}
		
		BuildUnits.trainUnit(observer, actions, barracks, Abilities.TRAIN_REAPER, Units.TERRAN_REAPER, 10);
	}
	
	private void depotOrders(ActionInterface actions, Unit depot) {
		actions.unitCommand(depot, Abilities.MORPH_SUPPLY_DEPOT_LOWER, false);
	}
}
