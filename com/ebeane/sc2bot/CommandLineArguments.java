package com.ebeane.sc2bot;

import com.github.ocraft.s2client.protocol.game.AiBuild;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.Race;

import java.util.Random;

public class CommandLineArguments {
    public CommandLineArguments(String[] args) {
        ArgsIterator iter = new ArgsIterator(args);
        while (iter.moveNext()) {
            if (iter.current().equals("--GamePort") || iter.current().equals("-g")) {
                iter.moveNext();
                try {
                    gamePort = Short.parseShort(iter.current());
                } catch (NumberFormatException ex) {
                    throw new CommandLineParseException("Invalid short " + iter.current() + ".");
                }
            } else if (iter.current().equals("--StartPort") || iter.current().equals("-o")) {
                iter.moveNext();
                try {
                    startPort = Integer.parseInt(iter.current());
                } catch (NumberFormatException ex) {
                    throw new CommandLineParseException("Invalid short " + iter.current() + ".");
                }
            } else if (iter.current().equals("--LadderServer") || iter.current().equals("-l")) {
                iter.moveNext();
                ladderServer = iter.current();
            } else if (iter.current().equals("--ComputerOpponent") || iter.current().equals("-c")) {
                computerOpponent = true;
            } else if (iter.current().equals("--OpponentRace") || iter.current().equals("-a")) {
                iter.moveNext();
                String race = iter.current();
                if (race.equalsIgnoreCase("Terran")) {
                    opponentRace = Race.TERRAN;
                } else if (race.equalsIgnoreCase("Protoss")) {
                    opponentRace = Race.PROTOSS;
                } else if (race.equalsIgnoreCase("Zerg")) {
                    opponentRace = Race.ZERG;
                }
            } else if (iter.current().equals("--ComputerDifficulty") || iter.current().equals("-d")) {
                iter.moveNext();
                String difficulty = iter.current();
                if (difficulty.equalsIgnoreCase("VeryEasy")) {
                    computerDifficulty = Difficulty.VERY_EASY;
                } else if (difficulty.equalsIgnoreCase("Easy")) {
                    computerDifficulty = Difficulty.EASY;
                } else if (difficulty.equalsIgnoreCase("Medium")) {
                    computerDifficulty = Difficulty.MEDIUM;
                } else if (difficulty.equalsIgnoreCase("Hard")) {
                    computerDifficulty = Difficulty.MEDIUM_HARD;
                } else if (difficulty.equalsIgnoreCase("Harder")) {
                    computerDifficulty = Difficulty.HARD;
                } else if (difficulty.equalsIgnoreCase("VeryHard")) {
                    computerDifficulty = Difficulty.HARDER;
                } else if (difficulty.equalsIgnoreCase("Elite")) {
                    computerDifficulty = Difficulty.VERY_HARD;
                } else if (difficulty.equalsIgnoreCase("CheatVision")) {
                    computerDifficulty = Difficulty.CHEAT_VISION;
                } else if (difficulty.equalsIgnoreCase("CheatMoney")) {
                    computerDifficulty = Difficulty.CHEAT_MONEY;
                } else if (difficulty.equalsIgnoreCase("CheatInsane")) {
                    computerDifficulty = Difficulty.CHEAT_INSANE;
                }
            } else if (iter.current().equals("--ComputerBuild")) {
                iter.moveNext();
                String build = iter.current();
                if (build.equalsIgnoreCase("Air")) {
                    computerBuild = AiBuild.AIR;
                } else if (build.equalsIgnoreCase("Macro")) {
                    computerBuild = AiBuild.MACRO;
                } else if (build.equalsIgnoreCase("Timing")) {
                    computerBuild = AiBuild.TIMING;
                } else if (build.equalsIgnoreCase("Rush")) {
                    computerBuild = AiBuild.RUSH;
                } else if (build.equalsIgnoreCase("Power")) {
                    computerBuild = AiBuild.POWER;
                }
            } else if (iter.current().equals("--OpponentId") || iter.current().equals("-x")) {
                iter.moveNext();
                opponentId = iter.current();
            } else if (iter.current().equals("--Realtime")) {
                realtime = true;
            } else if (iter.current().equals("--Map")) {
                iter.moveNext();
                map = iter.current();
            } else if (iter.current().equals("--Replay")) {
                iter.moveNext();
                replay = iter.current();
            } else if (iter.current().equals("-?") || iter.current().equalsIgnoreCase("-h")) {
                System.out.println("Options:");
                System.out.println("--ComputerOpponent / -c");
                System.out.println("\tWhether the bot (and possibly you) should play against the built-in AI. Otherwise, you play against the bot.");
                System.out.println("--OpponentRace / -a <Protoss|Terran|Zerg>");
                System.out.println("\tWhat race the bot should play against. If not specified, the bot will choose a race at random rather than the game so that the bot knows the race without having to scout.");
                System.out.println("--ComputerDifficulty / -d <VeryEasy|Easy|Medium|Hard|Harder|VeryHard|Elite|CheatVision|CheatMoney|CheatInsane>");
                System.out.println("\tDifficulty of the built-in AI, if applicable.");
                System.out.println("--ComputerBuild <Air|Macro|Power|Rush|Timing>");
                System.out.println("\tBuild that the built-in AI should use, if applicable. If not specified, the game will use a random build.");
                System.out.println("--Realtime");
                System.out.println("\tWhether the game should be played in real-time. Otherwise the game will play as fast (or slow) as the bot can handle.");
                System.out.println("\tReal-time will be more comfortable for humans, but may cause bugs.");
                System.out.println("--Map <name without extension e.g. Triton LE>");
                System.out.println("\tMap to play on.");
                System.out.println("--Replay <path including .SC2Replay>");
                System.out.println("\tSave a replay.");
                usage = true;
            }
        }
    }

    private short gamePort;
    private Integer startPort;
    private String ladderServer;
    private boolean computerOpponent;
    private Race opponentRace;
    private Difficulty computerDifficulty;
    private AiBuild computerBuild;
    private String opponentId;
    private boolean realtime;
    private String map;
    private String replay;
    private boolean usage;

    public boolean getUsage() {
        return usage;
    }

    public short getGamePort() {
        return gamePort;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public String getLadderServer() {
        return ladderServer;
    }

    public boolean getComputerOpponent() {
        return computerOpponent;
    }

    public Race getOpponentRace() {
        if (opponentRace == null) {
            switch (new Random().nextInt(3)) {
                case 0:
                    opponentRace = Race.TERRAN;
                    break;
                case 1:
                    opponentRace = Race.PROTOSS;
                    break;
                case 2:
                    opponentRace = Race.ZERG;
                    break;
            }
        }
        return opponentRace;
    }

    public Difficulty getComputerDifficulty() {
        if (computerDifficulty == null) {
            return Difficulty.EASY;
        }
        return computerDifficulty;
    }

    public AiBuild getComputerBuild() {
        if (computerBuild == null) {
            return AiBuild.RANDOM_BUILD;
        }
        return computerBuild;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public boolean getRealtime() {
        return realtime;
    }

    public String getMap() {
        if (map == null) {
            return "Triton LE";
        }
        return map;
    }

    public String getReplay() {
        return replay;
    }

    private class ArgsIterator {
        public ArgsIterator(String[] args) {
            this.args = args;
            currentIndex = -1;
        }

        private String[] args;
        private int currentIndex;

        public boolean moveNext() {
            return ++currentIndex < args.length;
        }

        public String current() {
            if (currentIndex < 0 || currentIndex >= args.length) {
                throw new CommandLineParseException("Expected more arguments.");
            }

            return args[currentIndex];
        }
    }
}
