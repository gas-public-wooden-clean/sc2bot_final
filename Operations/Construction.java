package com.ebeane.sc2bot.Operations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.ebeane.sc2bot.Bot.BotBehaviorState;
import com.ebeane.sc2bot.Operations.Strategy.Strategy;
import com.ebeane.sc2bot.Operations.Strategy.StrategyNormal;
import com.ebeane.sc2bot.Operations.Strategy.StrategyRush;
import com.ebeane.sc2bot.Toolbox;
import com.ebeane.sc2bot.Toolbox.SupplyBracket;
import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.QueryInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class Construction implements Division {
	private ObservationInterface observer;
	private ActionInterface actions;
	private QueryInterface query;
	
	public Construction(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		this.observer = observer;
		this.actions = actions;
		this.query = query;
	}
	
	@Override
	public void updateForStep(ObservationInterface observer, ActionInterface actions, QueryInterface query) {
		this.observer = observer;
		this.actions = actions;
		this.query = query;
	}

	@Override
	public void issueCommands(BotBehaviorState botState) {
		Strategy currentStrategy = new StrategyNormal();

		currentStrategy.issueOrders(observer, actions);		
	}
}
