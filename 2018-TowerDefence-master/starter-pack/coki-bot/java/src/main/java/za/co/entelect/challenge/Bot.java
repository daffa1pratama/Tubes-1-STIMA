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
     * @param gameState
     */
    public Bot (GameState gameState) {
        this.gameState = gameState;
        this.mapHeight = gameState.gameDetails.mapHeight;
        this.mapWidth = gameState.gameDetails.mapWidth;
    }

    /**
     * Main Logic
     * @return command
     */
    public String run () {
        String command = "";

        // Jika tiap row i belum ada energy, maka bot bangun energy building di row i
        // Agar energy tetap aman dan bisa jadi tumbal serangan
        for (int i = 0; i < mapHeight; i++) {
            int countSelfEnergy = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();

            if (countSelfEnergy == 0) {
                if (isEnoughEnergyToBuild(BuildingType.ENERGY)) {
                    command = buildEnergyBuilding(i);
                }
                break;
            }
        }

        
        // Jika energy lebih dari 600, maka bisa bangun tesla
        // Ambil angka 600 karena building cost dan attack cost mahal
        if (getEnergy(PlayerType.A) >= 600) {
            command = buildTeslaTower(4);
        }

        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int enemyEnergyAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ENERGY, i).size();
                int enemyDefenseAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE , i).size();
                int selfAttackAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int totalEnemy = enemyAttackAt + enemyEnergyAt + enemyDefenseAt;

                if (totalEnemy == 0 && selfAttackAt == 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ATTACK)) {
                        command = buildAttackBuilding(i);
                    }
                }
            }
        }
        
        // Jika enemy tidak punya attack building di row i atau self punya defense di row i, maka build energy building di row i
        // Agar energy building aman dari ancaman
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int selfEnergyAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();
                int selfDefenseAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();

                if ((enemyAttackAt == 0 || selfDefenseAt > 0) && selfEnergyAt == 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ENERGY)) {
                        command = buildEnergyBuilding(i);
                    }
                    break;
                }
            }
        }

        // Jika enemy punya 1 attack building di row i, maka bot build attack building di row yang sama
        // Mengorbankan bangunan dan menghancurkan attack musuh
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int enemyDefenseAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                int selfAttackAt = getAllBuildingInRow(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                
                if (enemyAttackAt == 1 && selfAttackAt == 0 && enemyDefenseAt == 0) {
                    if (isEnoughEnergyToBuild(BuildingType.ATTACK)){
                        command = buildAttackBuilding(i);
                    }
                    break;
                }
            }
        }

        // Jika enemy punya 3 attack building di row i atau 1 attack dan self tidak punya defense, maka build defense
        // Agar pembangunan defense bisa optimal
        if (command.equals("")) {
            for (int i = 0; i < mapHeight; i++) {
                int enemyAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int selfDefenseAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                int selfAttackAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int selfEnergyAt = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ENERGY, i).size();

                if (enemyAttackAt > 3) {
                    if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                        command = buildDefenseBuilding(i);
                    }
                    break;
                }

                if (enemyAttackAt > 1 && selfDefenseAt == 0){
                    if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                        command = buildDefenseBuilding(i);
                    }
                    break;
                }
            }
        }

        // Jika bot punya 1 attack building di row i, maka build lagi attack building dibelakangnya
        // Agar serangan dapat maksimal dan menekan pertahanan lawan
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

        // Jika enemy punya minimal 1 defense building di row i, maka bot build attack di row lain
        // Agar musuh terkecoh dengan membangun defense building
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

        // Jika bot punya defense building di row i, maka build attack building di belakangnya
        // Agar penyerangan lebih aman dengan ada defense building di depan
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

        // Jika enemy punya 8 attack building pada seluruh map, maka gunakan iron curtain
        // Skill digunakan untuk menahan serangan musuh yang banyak
        if (command.equals("")) {
            int countEnemyAttack = 0;
            int ctr;

            for (int i = 0; i < mapHeight; i++) {
                ctr = getAllBuildingInRow(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                countEnemyAttack += ctr;
            }

            if (countEnemyAttack >= 8 && isEnoughEnergyToBuild(BuildingType.IRONCURTAIN)) {
                command = buildCommand(7, 7, BuildingType.IRONCURTAIN);
            }
        }

        // Jika case diatas tidak ada yang memenuhi, maka build defense pada sembarang row
        // Untuk memancing agar bot pada turn selanjutnya membangun attack building
        // sehingga saat musuh membangun defense building, attack dari bot sudah siap
        if (command.equals("")) {
            int randomNumber = new Random().nextInt(7);

            if (isEnoughEnergyToBuild(BuildingType.DEFENSE)) {
                command = buildDefenseBuilding(randomNumber);
                
                while (command.equals("")) {
                    randomNumber = new Random().nextInt(7);
                    command = buildDefenseBuilding(randomNumber);
                }
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
            System.out.println("Invalid");
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
    
    /**
     * Build energy building
     * @param y
     * @return
     */
    private String buildEnergyBuilding (int y) {
        for (int i = 0; i <= 1; i++) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.ENERGY);
            }
        }
        return "";
    }
    
    /**
     * Build attack building
     * @param y
     * @return
     */
    private String buildAttackBuilding (int y) {
        for (int i = 5; i >= 1; i--) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        return "";
    }

    /**
     * Build defense building
     * @param y
     * @return
     */
    private String buildDefenseBuilding (int y) {
        for (int i = 7; i >= 7; i--) {
            if (isEmptyCell(i, y)) {
                return buildCommand(i, y, BuildingType.DEFENSE);
            }
        }
        return "";
    }
    
    /**
     * Build tesla tower
     * @param y
     * @return
     */
    private String buildTeslaTower (int y) {
        if (isEmptyCell(5, y)) {
            return buildCommand(5, y, BuildingType.TESLA);
        }
        return "";
    }
    
    /**
     * Build command (x,y,building)
     * @param x
     * @param y
     * @param building
     * @return
     */
    private String buildCommand (int x, int y, BuildingType building) {
        return String.format("%d,%d,%s", x, y, building.getCommandCode());
    }
}
