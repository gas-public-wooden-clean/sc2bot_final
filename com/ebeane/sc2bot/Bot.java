package com.ebeane.sc2bot;

import java.util.List;

import com.ebeane.sc2bot.InternalState.Expansion;
import com.ebeane.sc2bot.InternalState.Oversight;
import com.ebeane.sc2bot.InternalState.Economy.CollectionCenter;
import com.ebeane.sc2bot.InternalState.Economy.JobRoster;
import com.ebeane.sc2bot.InternalState.Economy.Resources;
import com.ebeane.sc2bot.Operations.Construction;
import com.ebeane.sc2bot.Operations.Engagement;
import com.ebeane.sc2bot.Operations.Extraction;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Bot extends S2Agent {

    private Extraction extraction;
    private Oversight oversight;
    private Engagement engagement;
    private Construction construction;
    private Expansion expansion;
    
    private CollectionCenter collectionCenter;
    private JobRoster jobRoster;
    
    private BotBehaviorState currentState = BotBehaviorState.RUSH;
    
    private int stepNumber = 0;
    
    public enum BotBehaviorState {
    	RUSH, NORMAL
    }
    
    @Override
	public void onGameStart() {
    	extraction = new Extraction(observation(), actions());
    	oversight = new Oversight(observation(), actions());
    	engagement = new Engagement(observation(), actions());
    	construction = new Construction(observation(), actions(), query());
    	expansion = new Expansion(observation(), actions(), query());
    	
    	collectionCenter = new CollectionCenter();
    	jobRoster = new JobRoster(observation());
    }
    
	@Override
    public void onStep() {
		
		if (stepNumber % 100 != 0) {
			stepNumber++;
			return;
		}
		
		/*
		List<UnitInPool> force = observation().getUnits(asset -> asset.unit().getType() == Units.TERRAN_SCV);
		for (UnitInPool asset : force) {
			
			Unit unit = asset.unit();
			Point position = unit.getPosition();
			Color color = Color.of(255,255,255);	
			debug().debugTextOut(asset.getTag().toString(), position, color, 15);
		}
		debug().sendDebug();
		*/
		
		currentState = updateBotState(currentState);
		
        extraction.updateForStep(observation(), actions(), null);
        oversight.updateForStep(observation(), actions(), null);
        engagement.updateForStep(observation(), actions(), null);
        construction.updateForStep(observation(), actions(), query());
        expansion.updateForStep(observation(), actions(), null);
		
		CollectionCenter.updateForStep(observation());
		JobRoster.updateForStep(observation());
		Resources.updateForStep(observation());
        
        extraction.issueCommands(currentState);
        engagement.issueCommands(currentState);
        construction.issueCommands(currentState);
        
        
        stepNumber++;
    }
	
	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		oversight.updateForStep(observation(), actions(), null);
		oversight.dismissUnit(unitInPool);
	}
	
	private BotBehaviorState updateBotState(BotBehaviorState botState) {
		
		if (botState == BotBehaviorState.RUSH &&
				Toolbox.countUnitType(observation(), Units.TERRAN_REAPER) < 3) {
			return BotBehaviorState.RUSH;
		}
		
		return BotBehaviorState.NORMAL;
	}
}
