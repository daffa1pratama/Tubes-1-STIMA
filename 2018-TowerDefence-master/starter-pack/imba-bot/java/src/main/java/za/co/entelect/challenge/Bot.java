package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.CellStateContainer;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.enums.*;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Bot {

    private GameState gameState;
    private int mapHeight;
    private int mapWidth;

    /**
     * Constructor Bot
     * 
     * @param gameState
     */
    public Bot (GameState gameState) {
        this.gameState = gameState;
        this.mapHeight = gameState.gameDetails.mapHeight;
        this.mapWidth = gameState.gameDetails.mapWidth;
    }

    /**
     * Main Logic
     * 
     * @return command
     */
    public String run () {
        String command = "";
        
        // if (gameState.gameDetails.round == 0) {
        //     command = buildEnergyBuilding(0);
        // }
        
        // Kalau enemy gaada attack dan kita gaada energy, build energy dulu
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int selfEnergyAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();

                if (enemyAttackAt == 0 && selfEnergyAt == 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ENERGY)) {
                        command = buildEnergyBuilding(i);
                    }
                    break;
                }
            }
        }

        // Kalau enemy ada 2 attack di y, build defense di y
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                
                if (enemyAttackAt > 1) {
                    if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                        command = buildDefenseBuilding(i);
                    }
                    break;
                }
            }
        }

        // Kalau udah ada attack, build attack dibelakang lagi
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int selfAttackAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();

                if (selfAttackAt > 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ATTACK)) {
                        command = buildAttackBuilding(i);
                    }
                    break;
                }
            }
        }

        // Kalau enemy build attack di y, build attack di y
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int selfAttackAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                
                if (enemyAttackAt > 0 && selfAttackAt == 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ATTACK)){
                        command = buildAttackBuilding(i);
                    }
                    break;
                }
            }
        }


        // Kalau enemy punya defense y, build attack di y lain
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyDefenseAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();

                if (enemyDefenseAt > 0) {
                    if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                        int randomNumber = new Random().nextInt(7);
                        
                        while (randomNumber == i) {
                            randomNumber = new Random().nextInt(7);
                        }

                        command = buildAttackBuilding(randomNumber);
                    }
                    break;
                }
            }
        }

        // Kalau udah ada defense, build attack dibelakang
        if (command.equals("")){
            for (int i = 0; i < mapHeight; i++) {
                int selfDefenseAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();

                if (selfDefenseAt > 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ATTACK)) {
                        command = buildAttackBuilding(i);
                    }
                    break;
                }
            }
        }

        // Kalau enemy punya > 4 attack, pakai iron curtain

        // Kalau energy turah turah, baru build tesla
        if (command.equals("")) {
            if (getEnergy(PlayerType.A) >= 450) {
                command = buildTeslaTower(4);
            }
        }

        // Gaada case yang masuk, build attack
        if (command.equals("")) {
            int randomNumber = new Random().nextInt(7);

            if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                command = buildDefenseBuilding(randomNumber);
            }
        }

        return command;
    }
    
    /**
     * Get list of empty column
     * @param x
     * @return
     */
    private List <CellStateContainer> getListEmptyColumn (int x) {
        return gameState.getGameMap().stream()
            .filter(c -> c.x == x && isEmptyCell(x, c.y)).collect(Collectors.toList());
    }

    /**
     * Check if empty cell at x,y or not
     * @param x
     * @param y
     * @return
     */
    private Boolean isEmptyCell (int x, int y) {
        Optional <CellStateContainer> cellOptional = gameState.getGameMap().stream()
            .filter(c -> c.x == x && c.y == y).findFirst();
        
        if ( cellOptional.isPresent() ) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        }
        else {
            System.out.println("Invalid cell");
        }
        return true;
    }

    /**
     * Get list of all building in row y
     * @param player
     * @param filter
     * @param y
     * @return
     */
    private List <Building> getAllBuildingInRow (PlayerType player, Predicate <Building> filter, int y) {
        return gameState.getGameMap().stream()
            .filter(c -> c.cellOwner == player && c.y == y)
            .flatMap(c -> c.getBuildings().stream())
            .filter(filter).collect(Collectors.toList());
    }

    /**
     * Get energy player
     * @param player
     * @return
     */
    private int getEnergy (PlayerType player) {
        return gameState.getPlayers().stream()
            .filter(p -> p.playerType == player)
            .mapToInt(p -> p.energy).sum();
    }

    /**
     * Get building cost to build
     * @param building
     * @return
     */
    private int getBuildingCost (BuildingType building) {
        return gameState.gameDetails.buildingsStats.get(building).price;
    }

    /**
     * Check if enough energy to build a building
     * @param building
     * @return
     */
    private Boolean isEnoughEnergyToBuild (BuildingType building) {
        return getEnergy(PlayerType.A) >= getBuildingCost(building);
    }

    private String buildEnergyBuilding (int y) {
        for (int i = 0; i <= 1; i++) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.ENERGY);
            }
        }
        return "";
    }
    
    private String buildAttackBuilding (int y) {
        for (int i = 5; i >= 1; i--) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        return "";
    }

    private String buildDefenseBuilding (int y) {
        for (int i = 6; i <= 7; i++) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.DEFENSE);
            }
        }
        return "";
    }
    
    private String buildTeslaTower (int y) {
        if (isEmptyCell(5, y)) {
            return buildCommand(5, y, BuildingType.TESLA);
        }
        return "";
    }
    
    private String buildCommand (int x, int y, BuildingType building) {
        return String.format("%d,%d,%s", x, y, building.getCommandCode());
    }
}
