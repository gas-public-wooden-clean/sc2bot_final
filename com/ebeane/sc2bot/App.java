package com.ebeane.sc2bot;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;

/**
 * Hello world!
 *
 */
public class App 
{
	private static class Bot extends S2Agent {

        public void onGameStart() {
            System.out.println("Hello world of Starcraft II bots!");
        }
    }
	
	
	public static void main(String[] args) {
	    Bot bot = new Bot();
	    S2Coordinator s2Coordinator = S2Coordinator.setup()
	            .loadSettings(args)
	            .setParticipants(
	                    S2Coordinator.createParticipant(Race.TERRAN, bot),
	                    S2Coordinator.createComputer(Race.ZERG, Difficulty.VERY_EASY))
	            .launchStarcraft()
	            .startGame(BattlenetMap.of("Triton LE"));
	    s2Coordinator.quit();
	}
}
