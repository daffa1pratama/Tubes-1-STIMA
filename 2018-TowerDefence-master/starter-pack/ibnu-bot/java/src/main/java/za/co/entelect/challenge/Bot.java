package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.CellStateContainer;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Bot {

    private GameState gameState;
    private static int TeslaCheck;
    /**
     * Constructor
     *
     * @param gameState the game state
     **/
    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
    }

    /**
     * Run
     *
     * @return the result
     **/
    public String run() {
        String command = "";
        int enemyAttackOnRow;
        // Membangun energy building hingga 1 kolom sudah terpenuhi
        int fullEnergy = 1;
        for (int i = 0; i <= 7; i++) {
            int myEnergyOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();
            if (myEnergyOnRow == 0 && getEnergy(PlayerType.A) >= 20) {
                return command = buildCommand(0, i, BuildingType.ENERGY);
            }
        }
        if(getEnergy(PlayerType.A)>=100){
        	return "7,7,5";
        }
        // Membangun attack/defense jika energy sudah penuh dalam 1 kolom
        if (fullEnergy == 1 && getEnergy(PlayerType.A)>=30) {
            int baris, max;
            baris = 0;
            max = 0;
            for (int y = 0; y <= 7; y++) {
                enemyAttackOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, y).size();
                int myDefenseOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, y).size();
                if (enemyAttackOnRow > max && checkNumberDefenseBuilding(y)){
                    max = enemyAttackOnRow;
                    baris = y;
                }
            }
            if (max !=0){   
                int z = 5;
                while (z < 8){
                    if (isCellEmpty(z, baris) == true){
                        return command = buildCommand(z, baris, BuildingType.DEFENSE);
                    }
                    else{
                        z++;
                    }
                }
            }
            //SKEMA BANGUN ATTACK
            baris = 0;
            max = 10;
            for (int i = 0; i < 8; i++){
                enemyAttackOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                enemyAttackOnRow += getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                enemyAttackOnRow += getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ENERGY, i).size();
                int myAttackOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                if (enemyAttackOnRow < max && myAttackOnRow < 5){
                    max = enemyAttackOnRow;
                    baris = i;
                }
            }
            int x = 4;
            while (x>0){
                if (isCellEmpty(x, baris) == true){
                    return command = buildCommand(x, baris, BuildingType.ATTACK);
                }
                else{
                    x--;
                }
            }
        }
        return command;
    }

    /**
     * Place attack building in column x in row 2/3
     *
     * @param x            the x
     * @return the result
     **/
    private String placeAttackBuildingInColumn(int x) {
        for (int i = 2; i <= 3; i++) {
            if (isCellEmpty(i, x)) {
                return buildCommand(i, x, BuildingType.ATTACK);
            }
        }
        return "";
    }

    /**
     * Place defense building in column x in row 4/5
     *
     * @param x            the x
     * @return the result
     **/
    private String placeDefenseBuildingInColumn(int x) {
        for (int i = 4; i <= 5; i++) {
            if (isCellEmpty(i, x)) {
                return buildCommand(i, x, BuildingType.DEFENSE);
            }
        }
        return "";
    }

    /**
     * Place energy building in column x in row 0/1
     *
     * @param x            the x
     * @return the result
     **/
    private String placeEnergyBuildingInColumn(int x) {
        for (int i = 0; i <= 1; i++) {
            if (isCellEmpty(i, x)) {
                return buildCommand(i, x, BuildingType.ENERGY);
            }
        }
        return "";
    }
    /**
     * Place building in row y nearest to the front
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/
    private String placeBuildingInRowFromFront(BuildingType buildingType, int y) {
        for (int i = (gameState.gameDetails.mapWidth / 2) - 1; i >= 0; i--) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }

    /**
     * Place building in row y nearest to the back
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/
    private String placeBuildingInRowFromBack(BuildingType buildingType, int y) {
        for (int i = 0; i < gameState.gameDetails.mapWidth / 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }
    /**
     * Construct build command
     *
     * @param x            the x
     * @param y            the y
     * @param buildingType the building type
     * @return the result
     **/
    private String buildCommand(int x, int y, BuildingType buildingType) {
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getCommandCode());
    }

    /**
     * Get all buildings for player in row y
     *
     * @param playerType the player type
     * @param filter     the filter
     * @param y          the y
     * @return the result
     **/
    private List<Building> getAllBuildingsForPlayer(PlayerType playerType, Predicate<Building> filter, int y) {
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y)
                .flatMap(c -> c.getBuildings().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Get all empty cells for column x
     *
     * @param x the x
     * @return the result
     **/
    private List<CellStateContainer> getListOfEmptyCellsForColumn(int x) {
        return gameState.getGameMap().stream()
                .filter(c -> c.x == x && isCellEmpty(x, c.y))
                .collect(Collectors.toList());
    }

    /**
     * Checks if cell at x,y is empty
     *
     * @param x the x
     * @param y the y
     * @return the result
     **/
    private boolean isCellEmpty(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else {
            System.out.println("Invalid cell selected");
        }
        return true;
    }

    /**
     * Checks if building can be afforded
     *
     * @param buildingType the building type
     * @return the result
     **/
    private boolean canAffordBuilding(BuildingType buildingType) {
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }

    /**
     * Gets energy for player type
     *
     * @param playerType the player type
     * @return the result
     **/
    private int getEnergy(PlayerType playerType) {
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }

    /**
     * Gets price for building type
     *
     * @param buildingType the player type
     * @return the result
     **/
    private int getPriceForBuilding(BuildingType buildingType) {
        return gameState.gameDetails.buildingsStats.get(buildingType).price;
    }

    /**
     * Gets price for most expensive building type
     *
     * @return the result
     **/
    private int getMostExpensiveBuildingPrice() {
        return gameState.gameDetails.buildingsStats
                .values().stream()
                .mapToInt(b -> b.price)
                .max()
                .orElse(0);
    }

    /*
        Check apakah building defence kita dapat menghandle building attack musuh bray (dari segi jumlah)
    */
    private boolean checkNumberDefenseBuilding(int row){
        int myDefenseOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, row).size();
        int enemyAttackOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, row).size();
        if(enemyAttackOnRow==0){
            return false;
        }
        else if(enemyAttackOnRow<3 && myDefenseOnRow<1){
            return true;
        }
        else if(enemyAttackOnRow<5 && myDefenseOnRow<2){
        	return true;
        }
        else if(myDefenseOnRow<3){
        	return true;
        }
        return false;
    }
    private List<CellStateContainer> getListofBuildingCoordinateForRow(PlayerType playerType, Predicate<Building> filter, int y){
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y && isCellAttack(c.x,y))
                .collect(Collectors.toList());
    }
    private boolean isCellAttack(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().toString()=="0";
        } else {
            System.out.println("Invalid cell selected");
        }
        return false;
    }
}