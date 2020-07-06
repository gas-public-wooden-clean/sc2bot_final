package com.ebeane.sc2bot.Operations;

import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;

public interface Division {
	
	/* Refresh game data for division*/
	void updateForStep(ObservationInterface observer, ActionInterface actions, QueryInterface query);
	
	/* Issue commands to all units in division */
	void issueCommands(BotBehaviorState botState);
}
