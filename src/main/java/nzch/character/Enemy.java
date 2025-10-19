package nzch.character;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import nzch.system.BattleSystem;

// Класс врага
public class Enemy extends CombatCharacter {
    private Geometry geometry;
    private final AssetManager assetManager;

    public Enemy(String name, Vector3f position, AssetManager assetManager) {
        super(name, 30, 12, 15);
        this.position = position;
        this.assetManager = assetManager;
        createEnemyModel();
    }

    public void executeAITurn(BattleSystem battleSystem) {
        if (battleSystem.isWaitingForAnimation()) {
            System.out.println(name + " ждет завершения анимации...");
            return;
        }

        PlayerCombatCharacter playerCharacter = getNearestPlayer(battleSystem);
        if (playerCharacter == null || !playerCharacter.isAlive()) {
            System.out.println(name + ": игрок не найден, завершаю ход");
            battleSystem.getGame().endCurrentTurn();
            return;
        }

        System.out.println(name + " делает ход");

        Vector3f playerPos = playerCharacter.getPosition();
        Vector3f enemyPos = position;

        Vector3f playerGrid = battleSystem.getGridSystem().worldToGridPosition(playerPos);
        Vector3f enemyGrid = battleSystem.getGridSystem().worldToGridPosition(enemyPos);

        int distance = (int) (Math.abs(playerGrid.x - enemyGrid.x) + Math.abs(playerGrid.z - enemyGrid.z));

        System.out.println("Дистанция до игрока: " + distance + ", радиус атаки: " + attackRange);

        if (distance <= attackRange) {
            System.out.println(name + " атакует игрока!");
            battleSystem.attack(this, playerCharacter);
        } else {
            System.out.println(name + " двигается к игроку...");
            simpleMoveTowardsPlayer(battleSystem, playerGrid, enemyGrid);
        }
    }

    private void simpleMoveTowardsPlayer(BattleSystem battleSystem, Vector3f playerGrid, Vector3f enemyGrid) {
        // Простой алгоритм движения
        int dx = Integer.compare((int) playerGrid.x, (int) enemyGrid.x);
        int dz = Integer.compare((int) playerGrid.z, (int) enemyGrid.z);

        System.out.println("Направление движения: dx=" + dx + ", dz=" + dz);

        // Пробуем двигаться по диагонали
        if (dx != 0 && dz != 0) {
            Vector3f newPos = new Vector3f(enemyGrid.x + dx, 0, enemyGrid.z + dz);
            if (canMoveTo(battleSystem, newPos)) {
                System.out.println("Двигаюсь по диагонали в " + newPos);
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }

        // Пробуем двигаться по X
        if (dx != 0) {
            Vector3f newPos = new Vector3f(enemyGrid.x + dx, 0, enemyGrid.z);
            if (canMoveTo(battleSystem, newPos)) {
                System.out.println("Двигаюсь по X в " + newPos);
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }

        // Пробуем двигаться по Z
        if (dz != 0) {
            Vector3f newPos = new Vector3f(enemyGrid.x, 0, enemyGrid.z + dz);
            if (canMoveTo(battleSystem, newPos)) {
                System.out.println("Двигаюсь по Z в " + newPos);
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }

        System.out.println("Не могу двигаться, завершаю ход");
        battleSystem.getGame().endCurrentTurn();
    }

    private PlayerCombatCharacter getNearestPlayer(BattleSystem battleSystem) {
        for (CombatCharacter character : battleSystem.getTurnOrder()) {
            if (character instanceof PlayerCombatCharacter && character.isAlive()) {
                return (PlayerCombatCharacter) character;
            }
        }
        return null;
    }

    private void moveTowardsPlayer(BattleSystem battleSystem, Vector3f playerGrid, Vector3f enemyGrid) {
        // Упрощенный AI - двигаемся прямо к игроку
        int dx = (int) Math.signum(playerGrid.x - enemyGrid.x);
        int dz = (int) Math.signum(playerGrid.z - enemyGrid.z);
        // Предпочитаем движение по диагонали если возможно
        if (dx != 0 && dz != 0 && Math.random() > 0.5) {
            Vector3f newPos = new Vector3f(enemyGrid.x + dx, 0, enemyGrid.z + dz);
            if (canMoveTo(battleSystem, newPos)) {
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }
        // Движение по X
        if (dx != 0) {
            Vector3f newPos = new Vector3f(enemyGrid.x + dx, 0, enemyGrid.z);
            if (canMoveTo(battleSystem, newPos)) {
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }
        // Движение по Z
        if (dz != 0) {
            Vector3f newPos = new Vector3f(enemyGrid.x, 0, enemyGrid.z + dz);
            if (canMoveTo(battleSystem, newPos)) {
                battleSystem.moveCharacter(this, newPos);
                return;
            }
        }
        // Не можем двигаться - пропускаем ход
        System.out.println(name + " не может двигаться, пропускает ход");
        battleSystem.getGame().endCurrentTurn();
    }

    private boolean canMoveTo(BattleSystem battleSystem, Vector3f gridPos) {
        return battleSystem.getGridSystem().isValidGridPosition(gridPos) &&
                isPositionFree(battleSystem, gridPos);
    }

    private boolean isPositionFree(BattleSystem battleSystem, Vector3f gridPos) {
        for (CombatCharacter character : battleSystem.getTurnOrder()) {
            if (character != this && character.isAlive()) {
                Vector3f charPos = battleSystem.getGridSystem().worldToGridPosition(character.getPosition());
                if (charPos.equals(gridPos)) {
                    return false; // Клетка занята
                }
            }
        }
        return true;
    }

    private void createEnemyModel() {
        node = new Node("Enemy_" + name);

        Box body = new Box(0.4f, 0.8f, 0.4f);
        geometry = new Geometry("EnemyBody", body);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Red);
        mat.setColor("Diffuse", ColorRGBA.Red);
        geometry.setMaterial(mat);
        geometry.setLocalTranslation(0, 0.8f, 0);

        node.attachChild(geometry);
        node.setLocalTranslation(position);
    }

    public void setStats(int health, int attack, int armor) {
        this.maxHealth = health;
        this.currentHealth = health;
        this.attack = attack;
        this.armor = armor;
    }

    // Добавляем геттер для geometry (для NPCManager)
    public Geometry getGeometry() {
        return geometry;
    }
}
