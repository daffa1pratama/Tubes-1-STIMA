package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.*;

import static za.co.entelect.challenge.enums.BuildingType.ATTACK;
import static za.co.entelect.challenge.enums.BuildingType.DEFENSE;

public class Bot {
    private static final String NOTHING_COMMAND = "";
    private GameState gameState;
    private GameDetails gameDetails;
    private int gameWidth;
    private int gameHeight;
    private Player myself;
    private Player opponent;
    private List<Building> buildings;
    private List<Missile> missiles;

    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameDetails = gameState.getGameDetails();
        gameWidth = gameDetails.mapWidth;
        gameHeight = gameDetails.mapHeight;
        myself = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.A).findFirst().get();
        opponent = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.B).findFirst().get();

        buildings = gameState.getGameMap().stream()
                .flatMap(c -> c.getBuildings().stream())
                .collect(Collectors.toList());

        missiles = gameState.getGameMap().stream()
                .flatMap(c -> c.getMissiles().stream())
                .collect(Collectors.toList());
    }

    public String run() {

        String command = "";

        // If i can build iron curtain and have more than 130 energy, then build iron curtain
        if (myself.energy >= 130 &&
            myself.ironCurtainAvailable &&
            !myself.isIronCurtainActive && 
            opponent.isIronCurtainActive)
        {
            command = buildIronCurtain();
        }

        //If the enemy has an attack building and I don't have a blocking wall or
        //enemy has more than 3 attack building and i have less than 2 then build defense building
        if (command.equals(""))
        {
            for (int i = 0; i < gameHeight; i++) 
            {
                int enemyAttackOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int enemyDefenseOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                int myAttackOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();                
                int myDefenseOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                if (enemyAttackOnRow == 1 && myAttackOnRow + myDefenseOnRow == 0 && enemyDefenseOnRow == 0)
                {
                    if (canAffordBuilding(BuildingType.ATTACK))
                        command = buildAttackBuilding(i);
                    else
                        command = "";
                }
                if ((enemyAttackOnRow > 0 && myDefenseOnRow == 0) || (enemyAttackOnRow > 3 && myDefenseOnRow < 2)) 
                {
                    if (canAffordBuilding(BuildingType.DEFENSE))
                        command = buildDefenseBuilding(i);
                    else
                        command = "";
                    break;
                }
            }
        }

        //If there is a row where I don't have energy and there is no enemy attack building 
        //or i have a defense building, then build energy in the back row.
        if (command.equals("")) 
        {
            for (int i = 0; i < gameHeight; i++) 
            {
                int enemyAttackOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int myAttackOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int myDefenseOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                int myEnergyOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();

                if ((enemyAttackOnRow == 0 && myEnergyOnRow == 0) || (myDefenseOnRow+myAttackOnRow > 0 && myEnergyOnRow == 0)) 
                {
                    if (canAffordBuilding(BuildingType.ENERGY))
                        command = buildEnergyBuilding(i);
                    break;
                }
            }
        }

        // Build by max point
        if (command.equals(""))
            command = buildByMaxPoint();
        
        return command;
    }

    private int buildingOnRow(int y)
    {
        int enemyBuildingOnRow = getAllBuildingsForPlayer(PlayerType.B, b -> b.constructionTimeLeft < 4, y).size();
        int myBuildingOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.constructionTimeLeft < 4, y).size();
        return enemyBuildingOnRow + myBuildingOnRow;
    }

    private String doNothingCommand() {
        return NOTHING_COMMAND;
    }

    private boolean canAffordBuilding(BuildingType buildingType) {
        return myself.energy >= gameDetails.buildingsStats.get(buildingType).price;
    }

    private List<Building> getAllBuildingsForPlayer(PlayerType playerType, Predicate<Building> filter, int y) {
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y)
                .flatMap(c -> c.getBuildings().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }

    private List<Building> getAllBuildingsForPlayer(PlayerType playerType, int y)
    {
        return getAllBuildingsForPlayer(playerType, b -> b.constructionTimeLeft < 2, y);
    }

    private int getBuildingRowHealthForPlayer(PlayerType playerType, int y)
    {
        List<Building> playerRowBuildings = getAllBuildingsForPlayer(playerType, b -> b.buildingType != BuildingType.ENERGY, y);
        int health = 0;
        for (int i = 0; i < playerRowBuildings.size(); i++)
            health += playerRowBuildings.get(i).health;
        
        return health;
    }

    private int getBuildingRowDamageForPlayer(PlayerType playerType, int y)
    {
        List<Building> playerRowBuildings = getAllBuildingsForPlayer(playerType, y);
        int damage = 0;
        for (int i = 0; i < playerRowBuildings.size(); i++)
            damage += playerRowBuildings.get(i).weaponDamage;

        return damage;
    }

    private int getRowPoint(int y)
    {
        int point = 0;

        /* Do simulation */
        point = (getBuildingRowDamageForPlayer(PlayerType.A, y) - getBuildingRowHealthForPlayer(PlayerType.B, y));        
        if (point > 0)
            return point;
        point = (getBuildingRowHealthForPlayer(PlayerType.A, y) - getBuildingRowDamageForPlayer(PlayerType.B, y));
        if (point < 0)
            return point;
        
        return 0;
    }

    private int getRowPointByAddBuilding(BuildingType buildingType, int y)
    {
        int point = 0;
        
        /* Add new building */
        Building newBuilding = new Building();
        newBuilding.health = gameDetails.buildingsStats.get(buildingType).health;
        newBuilding.weaponDamage = gameDetails.buildingsStats.get(buildingType).weaponDamage;

        /* Do simulation */
        point = (getBuildingRowDamageForPlayer(PlayerType.A, y) + newBuilding.weaponDamage - getBuildingRowHealthForPlayer(PlayerType.B, y));
        if (point > 0)
            return point;
        point = (getBuildingRowHealthForPlayer(PlayerType.A, y) + newBuilding.health - getBuildingRowDamageForPlayer(PlayerType.B, y));
        if (point < 0)
            return point;

        return 0;
    }

    private int getTotalPoint()
    {
        int point = 0;
        for (int i = 0; i < gameHeight; i++)
            point += getRowPoint(i);
 
        return point;
    }

    private String buildByMaxPoint()
    {
        /* Initial */
        BuildingType buildingType = ATTACK;
        int totalPoint = getTotalPoint();
        int maxPoint = totalPoint-getRowPoint(0)+getRowPointByAddBuilding(ATTACK, 0);
        int row = 0;
        List<Integer> rowList = new ArrayList<>();
        /* */
        
        for (int i = 0; i < gameHeight; i++)
        {
            int myDefenseOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();
            int myAttackOnRow = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
            int thisRowPoint = getRowPoint(i);
            int thisMaxPoint =  totalPoint-thisRowPoint+getRowPointByAddBuilding(ATTACK, i);
            if (thisMaxPoint == maxPoint && buildingType == ATTACK)
            {
                rowList.add(i);
                continue;
            }
            else if (thisMaxPoint > maxPoint && myAttackOnRow < 5)
            {
                rowList = new ArrayList<>();
                rowList.add(i);
                row = i;
                maxPoint = thisMaxPoint;
                buildingType = ATTACK;
            }
            thisMaxPoint = totalPoint-thisRowPoint+getRowPointByAddBuilding(DEFENSE, i);
            if (thisMaxPoint == maxPoint && buildingType == DEFENSE)
            {
                rowList.add(i);
                continue;
            }
            else if (thisMaxPoint > maxPoint && myDefenseOnRow < 2)
            {
                rowList = new ArrayList<>();
                rowList.add(i);
                row = i;
                maxPoint = thisMaxPoint;
                buildingType = DEFENSE;
            }
            
        }
        return buildRandomOnGivenRows(rowList, buildingType);
    }

    private String buildRandomOnGivenRows(List<Integer> rowList, BuildingType buildingType)
    {
        int randomRowIdx = (new Random()).nextInt(rowList.size());

        if (buildingType == ATTACK)
            return buildAttackBuilding(rowList.get(randomRowIdx));
        else if (buildingType == DEFENSE)
            return buildDefenseBuilding(rowList.get(randomRowIdx));

        return "";
    }

    /* Build */
    private String buildAttackBuilding(int y)
    {
        /* If has enough energy to build */
        if (canAffordBuilding(ATTACK))
        {
            List<CellStateContainer> emptyCells = gameState.getGameMap().stream()
                .filter(c -> c.getBuildings().isEmpty() && c.x > 0 && c.x <= 5 && c.y == y)
                .collect(Collectors.toList());
    
            if (!emptyCells.isEmpty())
            {
                CellStateContainer mostForwardEmptyCell = emptyCells.get(emptyCells.size()-1);
                BuildingType buildingType = BuildingType.ATTACK;
                
                return buildingType.buildCommand(mostForwardEmptyCell.x, mostForwardEmptyCell.y);
            }
        }
        return doNothingCommand();
    }

    private String buildDefenseBuilding(int y)
    {
        /* If has enough energy to build */
        if (canAffordBuilding(DEFENSE))
        {
            List<CellStateContainer> emptyCells = gameState.getGameMap().stream()
                .filter(c -> c.getBuildings().isEmpty() && c.x > 5 && c.x <= 7 && c.y == y)
                .collect(Collectors.toList());

            CellStateContainer mostForwardEmptyCell= emptyCells.get(0);
            
            BuildingType buildingType = BuildingType.DEFENSE;
            
            return buildingType.buildCommand(mostForwardEmptyCell.x, mostForwardEmptyCell.y);
        }
        return "";
    }

    private String buildEnergyBuilding(int y)
    {
        List<CellStateContainer> emptyCells = gameState.getGameMap().stream()
                .filter(c -> c.getBuildings().size() == 0 && c.x < 1 && c.y == y)
                .collect(Collectors.toList());
        
        if (!emptyCells.isEmpty())
        {
            CellStateContainer mostBackEmptyCell = emptyCells.get(0);
            return BuildingType.ENERGY.buildCommand(mostBackEmptyCell.x, mostBackEmptyCell.y);
        }
        return "";
    }

    private String buildIronCurtain()
    {
        return "0,0,5";
    }
}

