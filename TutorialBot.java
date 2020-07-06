package com.ebeane.sc2bot;

import java.util.function.Supplier;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.syntax.SettingsSyntax;
import com.github.ocraft.s2client.bot.syntax.StartGameSyntax;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.Race;
import com.ebeane.sc2bot.CommandLineArguments;

/**
 * Hello world!
 *
 */
public class TutorialBot {
	
	public static void main(String[] args) {	    
	    CommandLineArguments parsedArgs;

        try {
            parsedArgs = new CommandLineArguments(args);
        } catch (CommandLineParseException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
            return;
        }

        if (parsedArgs.getLadderServer() == null) {
            System.out.println("Playing without the ladder.");
            Bot bot = new Bot();
            S2Coordinator coordinator = S2Coordinator.setup()
                    .loadSettings(args)
                    .setParticipants(
                            S2Coordinator.createParticipant(Race.TERRAN, bot),
                            S2Coordinator.createComputer(Race.ZERG, Difficulty.VERY_EASY))
                    .launchStarcraft()
                    .startGame(BattlenetMap.of("Triton LE"));
            while (coordinator.update()) {
            }
            coordinator.quit();
        } else {
            System.out.println("Tournament mode engaged.");
            SettingsSyntax settings = S2Coordinator.setup();
            StartGameSyntax startGameSyntax;
            S2Coordinator coordinator;
            if (!parsedArgs.getComputerOpponent()) {
                startGameSyntax = settings.setParticipants(S2Coordinator.createParticipant(Race.TERRAN, new Bot()));
            } else {
                startGameSyntax = settings.setParticipants(
                        S2Coordinator.createParticipant(Race.TERRAN, new Bot()),
                        S2Coordinator.createComputer(Race.ZERG, Difficulty.VERY_EASY));
            }
            Supplier<Integer> portStart = () -> parsedArgs.getStartPort();
            coordinator = startGameSyntax.connect(parsedArgs.getLadderServer(), Integer.valueOf(parsedArgs.getGamePort()));
            coordinator.setupPorts(parsedArgs.getComputerOpponent() ? 1 : 2, portStart, false);
            coordinator.joinGame();
            while (coordinator.update()) {
            }
            coordinator.quit();
        }
	}
}
