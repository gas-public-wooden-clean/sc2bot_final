package com.ebeane.sc2bot.Operations.Strategy;

import com.github.ocraft.s2client.bot.gateway.ActionInterface;
import com.github.ocraft.s2client.bot.gateway.ObservationInterface;

public interface Strategy {

	public void issueOrders(ObservationInterface observer, ActionInterface actions);
	
	/*
	public void techUp(ObservationInterface observer, ActionInterface actions);
	
	public void increaseArmyProduction(ObservationInterface observer, ActionInterface actions);
	*/
	
}
