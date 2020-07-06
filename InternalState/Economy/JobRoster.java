package com.ebeane.sc2bot.InternalState.Economy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class JobRoster {

	public enum Job {GAS, MINERALS, BUILD, IDLE};
	
	static HashMap<UnitInPool, Job> assignmentsTask;
	static HashMap<UnitInPool, UnitInPool> assignmentsLocation;
	
	public JobRoster(ObservationInterface observer) {
		assignmentsTask = new HashMap<UnitInPool, Job>();
		assignmentsLocation = new HashMap<UnitInPool, UnitInPool>();
		
		gameStartAssignment(observer);
	}
	
	private void gameStartAssignment(ObservationInterface observer) {
		UnitInPool commandCenterAsset = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER).get(0);
		List<UnitInPool> scvForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_SCV);
		
		for (UnitInPool scvAsset : scvForce) {
			assignmentsTask.put(scvAsset, Job.MINERALS);
			assignmentsLocation.put(scvAsset, commandCenterAsset);
		}
	}
	
	public static void updateForStep(ObservationInterface observer) {
		List<UnitInPool> scvForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_SCV);
		
		for (UnitInPool scvAsset : scvForce) {
			
			if (!assignmentsTask.containsKey(scvAsset)) {
				assignmentsTask.put(scvAsset, Job.IDLE);
			} else if (0 == scvAsset.unit().getOrders().size()) {
				assignmentsTask.put(scvAsset, Job.IDLE);
			}
		}
		
		HashSet<UnitInPool> activeSCVs = new HashSet<UnitInPool>(scvForce);
		Iterator<Entry<UnitInPool, Job>> assignmentIter = assignmentsTask.entrySet().iterator();
		while (assignmentIter.hasNext()) {
			Entry<UnitInPool, Job> curAssignment = assignmentIter.next();
			
			if (!activeSCVs.contains(curAssignment.getKey())) {
				assignmentIter.remove();
			}
		}
		
		/*
		System.out.println("STEP");
		for (UnitInPool scvAsset : assignmentsTask.keySet()) {
			List<UnitOrder> orders = scvAsset.unit().getOrders();
			String tagString = assignmentsLocation.get(scvAsset) != null ? assignmentsLocation.get(scvAsset).getTag().toString() : "null";
			
			if (orders.size() == 0) {
				System.out.println(scvAsset.getTag().toString() + " | " + assignmentsTask.get(scvAsset) + " | " + tagString + " | no task");
			} else {
				UnitOrder nextOrder = orders.get(0);
				System.out.println(scvAsset.getTag().toString() + " | " + assignmentsTask.get(scvAsset) + " | " + tagString + " | target tag: " + (nextOrder.getTargetedUnitTag().isPresent() ? nextOrder.getTargetedUnitTag().get() : null)+ " | target location: " + (nextOrder.getTargetedWorldSpacePosition().isPresent() ? nextOrder.getTargetedWorldSpacePosition().get() : null));
			}
			
			
		}
		System.out.println();
		*/
	}
	
	public static void assignJob(UnitInPool worker, UnitInPool newLocation, Job newTask) {
		
		if (!assignmentsTask.containsKey(worker)) {
			return;
		}
		
		assignmentsLocation.put(worker, newLocation);
		assignmentsTask.put(worker, newTask);
	}
	
	public static Job getJobForWorker(UnitInPool worker) {
		
		if (!assignmentsTask.containsKey(worker)) {
			return null;
		}
		
		return assignmentsTask.get(worker);
	}
	
	public static UnitInPool getLocationForWorker(UnitInPool worker) {
		
		if (!assignmentsLocation.containsKey(worker)) {
			//System.out.println("Location not found for worker");
			return null;
		}
		
		return assignmentsLocation.get(worker);
	}
	
	public static Optional<UnitInPool> getAvailableWorker() {
		
		for (UnitInPool workerAsset : assignmentsTask.keySet()) {
			Job currentJob = assignmentsTask.get(workerAsset);
			
			if (Job.IDLE == currentJob
				|| Job.MINERALS == currentJob) {
				return Optional.of(workerAsset);
			}
		}
		
		return Optional.ofNullable(null);
	}
}
