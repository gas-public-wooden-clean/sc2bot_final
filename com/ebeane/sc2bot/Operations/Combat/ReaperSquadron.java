package com.ebeane.sc2bot.Operations.Combat;

import java.util.List;

import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.Operations.Combat.Army.Orders;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class ReaperSquadron extends Army {

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	public void attack(ObservationInterface observer, ActionInterface actions, Point2d position) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		
		if (soldiers.size() == 0) {
			return;
		}
		
		actions.unitCommand(soldiers, Abilities.ATTACK_ATTACK, position, false);
	}
	
	public void orders(ObservationInterface observer, ActionInterface actions, Point2d position) {
		List<Unit> soldiers = Toolbox.getUnits(observer, asset -> roster.contains(asset.getTag()));
		List<UnitInPool> presentEnemies = observer.getUnits(Alliance.ENEMY);
		
		if (soldiers.size() == 0) {
			return;
		}
		
		actions.unitCommand(soldiers, Abilities.ATTACK_ATTACK, position, false);
	}
}
