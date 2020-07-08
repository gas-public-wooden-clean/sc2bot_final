package com.ebeane.sc2bot.InternalState;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.Operations.Division;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Oversight implements Division {
	private ObservationInterface observer;
	private ActionInterface actions;
	static HashMap<Tag, UnitReport> armyStatus;
	
	public Oversight(ObservationInterface observer, ActionInterface actions) {
		this.observer = observer;
		this.actions = actions;
		armyStatus = new HashMap<Tag, UnitReport>();
		
		List<UnitInPool> startingArmy = observer.getUnits(Alliance.SELF);
		for (UnitInPool asset : startingArmy) {		
			armyStatus.put(asset.getTag(), new UnitReport(asset.getUnit().get().getHealth().get(), observer.getGameLoop()));
		}
	}

	@Override
	public void updateForStep(ObservationInterface gameObserver, ActionInterface gameActions, QueryInterface query) {
		observer = gameObserver;
		actions = gameActions;
		
		updateUnitStatus();
	}
	
	private void updateUnitStatus() {
		List<UnitInPool> army = observer.getUnits(Alliance.SELF);
		for (UnitInPool asset : army) {
			
			if (!armyStatus.containsKey(asset.getTag())) {
				armyStatus.put(asset.getTag(), new UnitReport(asset.getUnit().get().getHealth().get(), observer.getGameLoop()));
			} else {
				UnitReport assetStatus = armyStatus.get(asset.getTag());
				Float currentHealth = asset.getUnit().get().getHealth().get();
				
				if (currentHealth < assetStatus.getReportHealth()) {
					assetStatus.setUnderAttack(true);
				} else if (assetStatus.getUnderAttack() && assetStatus.getReportUpToDate(observer.getGameLoop())) {
					continue;
				} else {
					assetStatus.setUnderAttack(false);
				}
				
				armyStatus.replace(asset.getTag(), assetStatus);
			}
			

		}
	}
	
	public void dismissUnit(UnitInPool asset) {
		armyStatus.remove(asset.getTag());
	}
	
	@Override
	public void issueCommands(BotBehaviorState botState) {
		// TODO Auto-generated method stub};
	}
	
	public static boolean unitUnderAttack(UnitInPool asset) {
		UnitReport assetStatus = armyStatus.get(asset.getTag());

		if (assetStatus == null) {
			return false;
		}
		
		return assetStatus.getUnderAttack();
	}

}
