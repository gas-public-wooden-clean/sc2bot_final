package com.ebeane.sc2bot.InternalState.Economy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import com.github.ocraft.s2client.bot.gateway.ObservationInterface;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class CollectionCenter {

	static HashMap<UnitInPool, Integer> collectionPointCapacities;
	
	public CollectionCenter( ) {
		collectionPointCapacities = new HashMap<UnitInPool, Integer>();
	}
	
	
	public static void updateForStep(ObservationInterface observer) {
		List<UnitInPool> collectionPointForce = observer.getUnits(asset -> asset.unit().getType() == Units.TERRAN_COMMAND_CENTER
				|| asset.unit().getType() == Units.TERRAN_REFINERY);
		
		for (UnitInPool collectionPointAsset : collectionPointForce) {
			Unit collectionPoint = collectionPointAsset.unit();
			int currentCapacity = collectionPoint.getIdealHarvesters().get() - collectionPoint.getAssignedHarvesters().get();
			
			if (!collectionPointCapacities.containsKey(collectionPointAsset)) {
				collectionPointCapacities.put(collectionPointAsset, currentCapacity);
			} else {
				collectionPointCapacities.replace(collectionPointAsset, currentCapacity);
			}
		}
		
		HashSet<UnitInPool> activeCollectionPoints = new HashSet<UnitInPool>(collectionPointForce);
		Iterator<Entry<UnitInPool, Integer>> collectionIter = collectionPointCapacities.entrySet().iterator();
		while (collectionIter.hasNext()) {
			Entry<UnitInPool, Integer> curCollectionPoint = collectionIter.next();
			
			if (!activeCollectionPoints.contains(curCollectionPoint.getKey())) {
				collectionIter.remove();
			}
		}
	}
	
	public static Optional<UnitInPool> getUnderMannedCollectionPoint(UnitType collectionPointType) {
		
		for (UnitInPool collectionPointAsset : collectionPointCapacities.keySet()) {
			int availableCapacity = collectionPointCapacities.get(collectionPointAsset);
			
			if (collectionPointAsset.unit().getType() != collectionPointType) {
				continue;
			}
			
			if (availableCapacity > 0) {
				return Optional.of(collectionPointAsset);
			}
		}
		
		return Optional.ofNullable(null);
	}
	
	public static int getCollectionPointCapacity(UnitInPool collectionPoint) {
		
		if (!collectionPointCapacities.containsKey(collectionPoint)) {
			//System.out.println("Assignment not found for collectionPoint");
			return -1;
		}
		
		return collectionPointCapacities.get(collectionPoint);
	}
	
	public static void hireWorker(UnitInPool collectionPoint) {
		
		if (!collectionPointCapacities.containsKey(collectionPoint)) {
			//System.out.println("Assignment not found for collectionPoint");
			return;
		}
		
		int currentCapacity = collectionPointCapacities.get(collectionPoint);
		collectionPointCapacities.replace(collectionPoint, currentCapacity - 1);
	}
	
public static void dismissWorker(UnitInPool collectionPoint) {
		
		if (!collectionPointCapacities.containsKey(collectionPoint)) {
			//System.out.println("Assignment not found for collectionPoint");
			return;
		}
		
		int currentCapacity = collectionPointCapacities.get(collectionPoint);
		collectionPointCapacities.replace(collectionPoint, currentCapacity + 1);
	}
}
