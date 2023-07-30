package DCT.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import DCT.common.Character;
import DCT.common.FantasyNameGenerator;
import DCT.common.Character.ActionType;
import DCT.common.Character.CharacterType;
import DCT.common.PoorMansDB.AsyncFileAppender;
import DCT.common.PoorMansDB.AsyncFileLoader;
import DCT.common.PoorMansDB.AsyncFileWriter;
import DCT.common.PoorMansDB.LineIndexer;
import DCT.common.PoorMansDB.UniqueFileNameGenerator;

public class CharacterFactory {
    public enum ControllerType {
        PLAYER, NPC
    }

    private static final Random random = new Random();
    private static int statMod = 5; // Modifier for stat calculations
    private static final Logger logger = Logger.getLogger(CharacterFactory.class.getName());
    // Static reference to the LineIndexer
    private static LineIndexer indexer;
    static {
        try {
            indexer = new LineIndexer(Paths.get(System.getProperty("user.dir"), "Characters", ".Characters.data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new character of a specific type and level.
     *
     * @param type       The type of the character.
     * @param level      The initial level of the character.
     * @param actionType The action type of the character.
     * @return The newly created character.
     */
    public static void createCharacter(ControllerType controllerType, CharacterType type, int level,
            ActionType actionType, Consumer<Character> callback) {
        System.out.println("Creating Character");
        Character character = new Character();
        if (controllerType == ControllerType.PLAYER) {
            character.setProgressionRate(random.nextInt(50) + 10); // Random progression rate between 10-50
        }
        character.setLuck((int) Math.sqrt(random.nextDouble() * 10)); // Random luck between 0-10, skewed towards lower
                                                                      // numbers
        character.setType(type);
        character.setActionType(actionType);
        System.out.println("Generating States");
        // Generate initial stats for the character
        generateStats(character, type, level);
        System.out.println("Setting Range");
        // Set the range based on the action type
        switch (actionType) {
            case MELEE:
                character.setRange(1);
                break;
            case MISSILE:
                character.setRange(2 + random.nextInt(2)); // Range varies from 2 to 3
                break;
            case GLOBAL:
                character.setRange(10);
                break;
        }
        if (controllerType == ControllerType.NPC) {
            character.setName(FantasyNameGenerator.generateCharacterName());
            logger.info("NPC Character created: " + character.getName());
            if (callback != null) {
                callback.accept(character);
            }
        } else {
            System.out.println("Generating name");
            String name = FantasyNameGenerator.generateCharacterName();
            logger.info("Checking name " + name);
            UniqueFileNameGenerator ufg = new UniqueFileNameGenerator(System.getProperty("user.dir"), "Characters");
            CompletableFuture<String> futureName = ufg.generateUniqueFileName(name.replace("'", "_"), ".data",
                    (count) -> {
                        logger.info("Triggering roman numerals");
                        return FantasyNameGenerator.convertToRomanNumerals(count);
                    });
            futureName.thenAccept(filename -> {
                handleNewCharacter(character, filename, (handledCharacter) -> {
                    // save the character with generated code
                    AsyncFileWriter.writeFileContent(filename, handledCharacter, (success) -> {
                        callback.accept(handledCharacter);
                    });

                });
            }).exceptionally(ex -> {
                logger.severe("Error generating unique file name: " + ex.getMessage());
                if (callback != null) {
                    callback.accept(character);
                }
                return null;
            });
        }

    }

    public static void saveCharacter(Character character, Consumer<Boolean> callback) {
        Path filePath = Paths.get(System.getProperty("user.dir"), "Characters",
                character.getName().replace("'", "_").replace(" ", "-") + ".data");
        if (character.getCode() == null || character.getCode().length() == 0) {
            logger.severe("Character missing code: " + character.toString());
            callback.accept(false);
            return;
        }
        AsyncFileWriter.writeFileContent(filePath.toString(), character, callback);
    }

    public static void loadCharacter(String position, String code, Consumer<Character> callback) {
        logger.info(String.format("Load Character checking line [%s] and code [%s]", position, code));
        try {
            indexer.getLine(Integer.parseInt(position), (line) -> {
                if (line == null) {
                    logger.severe("Error looking up character with code " + code);
                    Character error = new Character();
                    error.setCode("Invalid index");
                    callback.accept(error); // error response
                } else {
                    // Format is #-Name-code
                    String[] data = line.split("-");
                    if (data.length >= 3) {
                        if (code.trim().equalsIgnoreCase(data[3])) {
                            logger.info("Code matched");
                            String characterFile = data[1] + ".data";
                            Path filePath = Paths.get(System.getProperty("user.dir"), "Characters", characterFile);
                            AsyncFileLoader.loadFileContent(filePath.toString(), Character.class, Character::new,
                                    callback); // success response
                        } else {
                            logger.info("Code didn't match");
                            // being super lazy using a Character class to relay an error message
                            // this isn't great design
                            Character error = new Character();
                            error.setCode("Invalid code");
                            callback.accept(error); // error response
                        }
                    } else {
                        logger.severe("Line didn't have expected format");
                        Character error = new Character();
                        error.setCode("Invalid index reference");
                        callback.accept(error);// error response
                    }

                }
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Character error = new Character();
            error.setCode("Invalid position value");
            callback.accept(error);// error response
        }
    }

    private static void handleNewCharacter(Character character, String filename, Consumer<Character> callback) {
        try {
            String name = Paths.get(filename).getFileName().toString().replace(".data", "").replace("_", "'")
                    .replace("-", " ");
            character.setName(name);
            byte[] bytes = character.serialize();
            Files.write(Paths.get(filename), bytes);
            logger.info("Player Character created: " + character.getName());
            String charactersFile = Paths.get(System.getProperty("user.dir"), "Characters", ".Characters.data")
                    .toString();
            try {
                Path charFilePath = Paths.get(charactersFile);
                long lineCount = Files.exists(charFilePath) ? Files.lines(charFilePath).count() : 0;
                lineCount++;
                String code = lineCount + "-" + (10000000 + random.nextInt(90000000));
                String characterEntry = String.join("-", new String[] { lineCount + "", name, code });
                AsyncFileAppender.appendToFile(charactersFile, characterEntry, (success) -> {
                    if (success) {
                        character.setCode(code);

                    } else {
                        character.setCode(code + "-failed");
                    }
                    if (callback != null) {
                        callback.accept(character);
                    }
                }, indexer);
            } catch (IOException e) {
                e.printStackTrace();
                logger.severe("Failed to count lines in characters file");
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.accept(character);
            }
        }
        logger.info("Unique file name generated: " + filename);
    }

    /**
     * Levels up a character and updates their stats.
     *
     * @param character    The character to level up.
     * @param levelsGained The number of levels gained.
     */
    public static void levelUp(Character character, int levelsGained) {
        int newLevel = character.getLevel() + levelsGained;
        generateStats(character, character.getType(), newLevel); // Update the character's stats
        character.setLevel(newLevel); // Update the character's level

        logger.info("Character leveled up: " + character.getName() + ", new level: " + newLevel);
    }

    /**
     * Calculates a stat value based on level, progression rate, and luck bonus.
     *
     * @param level           Levels to gain.
     * @param progressionRate The progression rate of the character.
     * @param luckBonus       The luck bonus of the character.
     * @return The calculated stat value.
     */
    private static int rollStat(int level, int progressionRate, int luckBonus) {
        int total = 0;
        for (int i = 0; i < level; i++) {
            int roll = (random.nextInt(statMod) + 1) + random.nextInt(luckBonus);
            roll += Math.ceil(roll * progressionRate * .01);
            total += roll;
        }

        return total;
    }

    /**
     * Generates or updates the stats of a character.
     *
     * @param character The character whose stats are to be generated or updated.
     * @param type      The type of the character.
     * @param level     The level of the character.
     */
    private static void generateStats(Character character, CharacterType type, int level) {
        int luckBonus = 0;
        if (character.getLuck() >= 0 && character.getLuck() <= 3) {
            luckBonus = 1;
        } else if (character.getLuck() >= 4 && character.getLuck() <= 6) {
            luckBonus = 2;
        } else if (character.getLuck() >= 7 && character.getLuck() <= 9) {
            luckBonus = 3;
        } else if (character.getLuck() >= 10) {
            luckBonus = 5;
        }
        // If it's the first level, set the initial stats
        if (level == 1) {
            switch (type) {
                case TANK:
                    character.setVitality(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher
                                                                                                           // vitality
                    character.setDefense(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher
                                                                                                          // defense
                    character.setAttack(rollStat(level, character.getProgressionRate(), luckBonus));
                    character.setWill(rollStat(level, character.getProgressionRate(), luckBonus));
                    break;
                case DAMAGE:
                    character.setAttack(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher
                                                                                                         // attack
                    character.setWill(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher will

                    character.setVitality(rollStat(level, character.getProgressionRate(), luckBonus));
                    character.setDefense(rollStat(level, character.getProgressionRate(), luckBonus));
                    break;
                case SUPPORT:
                    character.setVitality(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher
                                                                                                           // vitality
                    character.setWill(rollStat(level, character.getProgressionRate(), luckBonus) + 5); // Higher will
                    character.setAttack(rollStat(level, character.getProgressionRate(), luckBonus));
                    character.setDefense(rollStat(level, character.getProgressionRate(), luckBonus));
                    break;
            }
        } else {
            // If it's not the first level, update the stats
            level -= character.getLevel(); // Roll for the difference
            logger.info("New level: " + level);
            character.setAttack(character.getAttack() + rollStat(level, character.getProgressionRate(), luckBonus));
            character.setVitality(character.getVitality() + rollStat(level, character.getProgressionRate(), luckBonus));
            character.setDefense(character.getDefense() + rollStat(level, character.getProgressionRate(), luckBonus));
            character.setWill(character.getWill() + rollStat(level, character.getProgressionRate(), luckBonus));
        }
    }

    public static void main(String[] args) {
        Consumer<Character> callback = character -> {
            try {
                System.out.println("Character created: " + character.getName());
                System.out.println("Character level: " + character.getLevel());
                System.out.println("Character type: " + character.getType());
                System.out.println("Character action type: " + character.getActionType());
                System.out.println("Character stats: ");
                System.out.println("Attack: " + character.getAttack());
                System.out.println("Vitality: " + character.getVitality());
                System.out.println("Defense: " + character.getDefense());
                System.out.println("Will: " + character.getWill());
                System.out.println("Luck: " + character.getLuck());
                System.out.println("Progression Rate: " + character.getProgressionRate());
                System.out.println("Range: " + character.getRange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        createCharacter(ControllerType.PLAYER, CharacterType.TANK, 1, ActionType.MELEE, callback);
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            break;
        }
    }
}
