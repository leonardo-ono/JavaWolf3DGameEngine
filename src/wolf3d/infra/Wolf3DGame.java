package wolf3d.infra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import wolf3d.infra.Objs.EnemyObj;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.DEAD;
import static wolf3d.infra.Settings.*;
import static wolf3d.infra.Wolf3DGame.Difficulty.HARD;
import wolf3d.scene.Credits;
import wolf3d.scene.GameDifficulty;
import wolf3d.scene.GameOptions;
import wolf3d.scene.Initializing;
import wolf3d.scene.LevelClearedStatistics;
import wolf3d.scene.OLPresents;
import wolf3d.scene.ProfoundCarnage13;
import wolf3d.scene.Quit;
import wolf3d.scene.Stage;
import wolf3d.scene.Title;

/**
 * Wolf3DGame class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Wolf3DGame {

    public static enum Difficulty { EASY, NORMAL, HARD, VERY_HARD }
    
    private static Difficulty difficulty = HARD;
    
    private static int floor;
    private static int levelIndex;

    private static int lives;
    private static int lifeEnergy;
    private static int score;
    
    private static int collectedTreasuresCount;
    private static int secretDoorsFoundCount;
    
    private static EnemyObj closestEnemyInSight;
    
    private static boolean playing;
    private static boolean backToGame;
    
    public static void reset() {
        levelIndex = 0;
        floor = Resource.getIntProperty("MAP_LEVEL_" + levelIndex) + 1;
        nextFloor = floor;
        lives = 3;
        lifeEnergy = 100;
        score = 0;
        collectedTreasuresCount = 0;
        secretDoorsFoundCount = 0;
    }

    public static int getFloor() {
        return floor;
    }

    public static int getLives() {
        return lives;
    }

    public static int getLifeEnergy() {
        return lifeEnergy;
    }

    public static void addLifeEnergy(int add) {
        lifeEnergy += add;
        if (lifeEnergy < 0) {
            lifeEnergy = 0;
        }
        else if (lifeEnergy > 100) {
            lifeEnergy = 100;
        }
    }

    public static int getScore() {
        return score;
    }

    public static void addScorePoints(int points) {
        score += points;
    }

    public static int getCollectedTreasuresCount() {
        return collectedTreasuresCount;
    }

    public static void incCollectedTreasuresCount() {
        collectedTreasuresCount++;
    }

    public static int getSecretDoorsFoundCount() {
        return secretDoorsFoundCount;
    }

    public static void incSecretDoorsFoundCount() {
        secretDoorsFoundCount++;
    }
    
    public static void setLives(int lives) {
        Wolf3DGame.lives = lives;
    }

    public static void incLives() {
        lives++;
    }

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(Difficulty difficulty) {
        Wolf3DGame.difficulty = difficulty;
    }

    public static EnemyObj getClosestEnemyInSight() {
        return closestEnemyInSight;
    }

    public static void setClosestEnemyInSight(EnemyObj closestEnemyInSight) {
        Wolf3DGame.closestEnemyInSight = closestEnemyInSight;
    }

    private static int nextFloor;
    
    public static void gotoSecretLevel() {
        nextFloor = Resource.getIntProperty("MAP_LEVEL_SECRET") + 1;
        SceneManager.switchTo("level_cleared_statistics");
        Audio.playSound("LEVELDONE");
    }

    public static void gotoNextLevel() {
        levelIndex++;
        nextFloor = Resource.getIntProperty("MAP_LEVEL_" + levelIndex) + 1;
        SceneManager.switchTo("level_cleared_statistics");
        Audio.playSound("LEVELDONE");
    }

    public static void startNextLevel() {
        floor = nextFloor;
        GameMap.loadByFloorNumber(floor);
        playing = true;
    }

    public static boolean isPlaying() {
        return playing;
    }
    
    public static void newGame() {
        reset();
        Weapons.reset();
        FizzleFade.reset();
        SceneManager.switchTo("stage");
    }
    
    // for debugging purposes
    public static Point playerOverrideLocation;
    public static void newGame(int startFloor, Point playerOverrideLocation) {
        Wolf3DGame.playerOverrideLocation = playerOverrideLocation;
        newGame();
        levelIndex = startFloor - 1;
        floor = startFloor;
        nextFloor = floor;
    }

    public static void backToGame() {
        backToGame = true;
        SceneManager.switchTo("stage");
    }

    public static boolean isBackToGame() {
        return backToGame;
    }

    public static void setBackToGame(boolean backToGame) {
        Wolf3DGame.backToGame = backToGame;
    }

    public static boolean tryNextLife() {
        lives--;
        if (lives <= 0) {
            playing = false;
            SceneManager.switchTo("title");
            return false;
        }
        GameMap.loadByFloorNumber(floor);
        Weapons.getCurrentPlayerWeapon().reset();
        lifeEnergy = 100;
        return true;
    }

    public static void gameCleared() {
        playing = false;
        SceneManager.switchTo("level_cleared_statistics");
        // TODO goto hiscore
    }

    // --- statistics ---
    
    public static int getStatisticsKill() {
        int totalEnemies = GameMap.getTotalEnemies();
        int deadEnemies = 0;
        for (EnemyObj enemy : Enemies.getEnemies()) {
            if (enemy.getEnemyState() == DEAD) deadEnemies++;
        }
        int statisticsKill = 0;
        if (totalEnemies > 0) {
            statisticsKill = (int) (100 * deadEnemies / (double) totalEnemies);
        }
        return statisticsKill;
    }

    public static int getStatisticsSecret() {
        int totalSecrets = GameMap.getTotalSecrets();
        int statisticsSecret = 0;
        if (totalSecrets > 0) {
            statisticsSecret 
                = (int) (100 * secretDoorsFoundCount / (double) totalSecrets);
        }
        return statisticsSecret;
    }

    public static int getStatisticsTreasures() {
        int totalTreasures = GameMap.getTotalTreasures();
        int statisticsTreasures = 0;
        if (totalTreasures > 0) {
            statisticsTreasures 
                = (int) (100 * collectedTreasuresCount / (double) totalTreasures);
        }
        return statisticsTreasures;
    }
    
    // ---
    
    public void start() {
        Resource.initialize();
        Audio.initialize();
        Weapons.createPlayerWeapons();

        SceneManager.addState(new Initializing());
        SceneManager.addState(new OLPresents());
        SceneManager.addState(new ProfoundCarnage13());
        SceneManager.addState(new Credits());
        SceneManager.addState(new Title());
        SceneManager.addState(new GameOptions());
        SceneManager.addState(new GameDifficulty());
        SceneManager.addState(new Stage());
        SceneManager.addState(new LevelClearedStatistics());
        SceneManager.addState(new Quit());
        SceneManager.startAll();
        
        SceneManager.switchTo("initializing");
    }
    
    public void update(double delta) {
        SceneManager.update(delta);
    }

    public void fixedUpdate() {
        SceneManager.fixedUpdate();
    }
    
    public void draw(Graphics2D g) {
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        SceneManager.draw(g);
    }

}