package com.ebeane.sc2bot.InternalState;

public class UnitReport {
	
	private boolean isUnderAttack;
	private Float reportHealth;
	private long reportGameLoop;
	
	public UnitReport(Float health, long gameLoop) {
		reportHealth = health;
		isUnderAttack = false;
		reportGameLoop = gameLoop;
	}
	
	public Float getReportHealth() {
		return reportHealth;
	}
	
	public void setUnderAttack(boolean underAttack) {
		isUnderAttack = underAttack;
	}
	
	public boolean getUnderAttack() {
		return isUnderAttack;
	}
	
	public boolean getReportUpToDate(long gameLoop) {
		return (gameLoop < reportGameLoop + 400);
	}
	
	public String toString(){
		return new String(isUnderAttack + " | " + reportHealth + " | " + reportGameLoop);
	}
}
