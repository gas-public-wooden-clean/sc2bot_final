package com.ebeane.sc2bot.Operations.Building;

import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class BuildUnits {
	
	public static void trainUnit(ObservationInterface observer, ActionInterface actions, 
			Unit builder, Abilities trainUnit, Units unitToBuild, int unitLimit) { 	
    	
    	if (builder.getOrders().size() != 0) {
			return;
		}
    	
    	if (Toolbox.countUnitType(observer, unitToBuild) > unitLimit) {
    		return;
        }
    	
		if (!Resources.haveResourcesForUnit(unitToBuild)) {
			return;
		}
    	
		Resources.allocateResourcesForUnit(unitToBuild);
    	actions.unitCommand(builder, trainUnit, false);
    }
}
