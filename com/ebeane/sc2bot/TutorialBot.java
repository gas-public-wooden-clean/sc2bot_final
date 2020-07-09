package com.ebeane.sc2bot;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Supplier;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.setting.PlayerSettings;
import com.github.ocraft.s2client.bot.syntax.SettingsSyntax;
import com.github.ocraft.s2client.bot.syntax.StartGameSyntax;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Race;

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

        if (parsedArgs.getUsage()) {
            return;
        }

        SettingsSyntax settings = S2Coordinator.setup()
                .setShowCloaked(true)
                .setShowBurrowed(true)
                .setRealtime(parsedArgs.getRealtime())
                .setRawAffectsSelection(false);

        PlayerSettings bot = S2Coordinator.createParticipant(Race.TERRAN, new Bot(), "DMS Dagger");

        S2Coordinator coordinator;

        if (parsedArgs.getLadderServer() == null) {
            System.out.println("Playing without the ladder.");
            PlayerSettings p1;
            PlayerSettings p2;
            if (parsedArgs.getComputerOpponent()) {
                p1 = bot;
                p2 = S2Coordinator.createComputer(parsedArgs.getOpponentRace(), parsedArgs.getComputerDifficulty(), parsedArgs.getComputerBuild());
            } else {
                p1 = S2Coordinator.createParticipant(parsedArgs.getOpponentRace(), new Meatbag(parsedArgs.getRealtime()), "Meatbag");
                p2 = bot;
            }
            coordinator = settings
                    .loadSettings(new String[0])
                    .setParticipants(p1, p2)
                    .launchStarcraft()
                    .startGame(BattlenetMap.of(parsedArgs.getMap()));
        } else {
            System.out.println("Tournament mode engaged.");
            StartGameSyntax startGameSyntax;
            if (!parsedArgs.getComputerOpponent()) {
                startGameSyntax = settings.setParticipants(bot);
            } else {
                startGameSyntax = settings.setParticipants(
                        bot,
                        S2Coordinator.createComputer(parsedArgs.getOpponentRace(), parsedArgs.getComputerDifficulty(), parsedArgs.getComputerBuild()));
            }
            Supplier<Integer> portStart = () -> parsedArgs.getStartPort();
            coordinator = startGameSyntax.connect(parsedArgs.getLadderServer(), Integer.valueOf(parsedArgs.getGamePort()));
            coordinator.setupPorts(parsedArgs.getComputerOpponent() ? 1 : 2, portStart, false);
            coordinator.joinGame();
        }

        while (coordinator.update()) {
        }
        if (parsedArgs.getReplay() != null) {
            try {
                bot.getAgent().control().saveReplay(Paths.get(parsedArgs.getReplay()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        coordinator.quit();
    }
}
