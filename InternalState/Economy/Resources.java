package com.ebeane.sc2bot.InternalState.Economy;

import java.util.Optional;

import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.protocol.data.Units;

public class Resources {
	
	private static int availableMinerals;
	private static int availableGas;
	private static int availableSupply;
	
	private static class Requirements {
		public int minerals;
		public int gas;
		public int supply;
		
		public Requirements(int minerals, int gas, int supply) {
			this.minerals = minerals;
			this.gas = gas;
			this.supply = supply;
		}
	}
	
	public static void updateForStep(ObservationInterface observer) {
		availableMinerals = observer.getMinerals();
		availableGas = observer.getVespene();
		availableSupply = observer.getFoodCap() - observer.getFoodUsed();
	}
	
	public static boolean haveResourcesForUnit(Units unitToBuild) {
		Optional<Requirements> neededResources = getRequirementsForUnit(unitToBuild);
		
		if (neededResources.isEmpty()) {
			return false;
		}
		
		if (availableMinerals < neededResources.get().minerals
				|| availableGas < neededResources.get().gas
				|| availableSupply < neededResources.get().supply) {
			return false;
		}
		
		return true;
	}
	
	public static void allocateResourcesForUnit(Units unitToBuild) {
		Optional<Requirements> neededResources = getRequirementsForUnit(unitToBuild);
		
		if (neededResources.isEmpty()) {
			return;
		}
    	
    	availableMinerals -= neededResources.get().minerals;
    	availableGas -= neededResources.get().gas;
    	availableSupply -= neededResources.get().supply;
	}
	
	private static Optional<Requirements> getRequirementsForUnit(Units unitType) {
		int requiredMinerals = 0;
		int requiredGas = 0;
		int requiredSupply = 0;
		
    	switch(unitType) {
	    	// Units
    		case TERRAN_SCV:
    			requiredMinerals = 50;
    			requiredSupply = 1;
    			break;
	    	case TERRAN_MARINE:
    			requiredMinerals = 50;
    			requiredSupply = 1;
    			break;
	    	case TERRAN_REAPER:
	    		requiredMinerals = 50;
	    		requiredGas = 50;
	    		requiredSupply = 1;
	    		break;
	    	case TERRAN_VIKING_FIGHTER:
	    		requiredMinerals = 150;
	    		requiredGas = 75;
	    		requiredSupply = 2;
	    		break;
	    	// Buildings
	    	case TERRAN_COMMAND_CENTER:
				requiredMinerals = 400;
				break;
			case TERRAN_REFINERY:
				requiredMinerals = 75;
				break;
			case TERRAN_SUPPLY_DEPOT:
				requiredMinerals = 100;
				break;
			case TERRAN_BARRACKS:
				requiredMinerals = 150;
				break;
			case TERRAN_FACTORY:
				requiredMinerals = 150;
				requiredGas = 100;
				break;
			case TERRAN_STARPORT:
				requiredMinerals = 150;
				requiredGas = 100;
				break;
			case TERRAN_BARRACKS_TECHLAB:
			case TERRAN_FACTORY_TECHLAB:
			case TERRAN_STARPORT_TECHLAB:
				requiredMinerals = 50;
				requiredGas = 25;
				break;
			case TERRAN_BARRACKS_REACTOR:
			case TERRAN_FACTORY_REACTOR:
			case TERRAN_STARPORT_REACTOR:
				requiredMinerals = 50;
				requiredGas = 50;
				break;
			default:
				System.out.println("Unexpected buildingType: " + unitType);
	    		return Optional.ofNullable(null);
    	}
    	
    	return Optional.of(new Requirements(requiredMinerals, requiredGas, requiredSupply));
	}
}
