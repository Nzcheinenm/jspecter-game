package nzch.manager;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import nzch.Jspectergame;
import nzch.character.Enemy;
import nzch.character.NPC;

import java.util.ArrayList;
import java.util.List;

public class NPCManager {
    private final List<NPC> npcs;
    private final List<Enemy> enemies;
    private final Node rootNode;
    private final AssetManager assetManager;
    private final Jspectergame game;

    public NPCManager(Node rootNode, AssetManager assetManager, Jspectergame game) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.game = game;
        this.npcs = new ArrayList<>();
        this.enemies = new ArrayList<>();
    }

    public void createNPCs() {
        // Создаем мирных NPC
        createNPC("Стражник", "guard_dialogue", new Vector3f(5, 0, 5));
        createNPC("Торговец", "merchant_dialogue", new Vector3f(-8, 0, 3));
        createNPC("Волшебник", "wizard_dialogue", new Vector3f(2, 0, -6));
        createNPC("Крестьянин", "peasant_dialogue", new Vector3f(-5, 0, -4));
    }

    private void createNPC(String name, String dialogueKey, Vector3f position) {
        NPC npc = new NPC(name, dialogueKey, position, assetManager);
        npcs.add(npc);
        rootNode.attachChild(npc.getNode());
        rootNode.attachChild(npc.getInteractionIndicator());
    }

    // Методы для работы с врагами
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        rootNode.attachChild(enemy.getNode());
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
        rootNode.detachChild(enemy.getNode());
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    // Метод для получения врага по Geometry (для обработки кликов)
    public Enemy getEnemyAtGeometry(Geometry geometry) {
        for (Enemy enemy : enemies) {
            if (enemy.getGeometry() == geometry) {
                return enemy;
            }

            // Также проверяем дочерние Geometry
            if (enemy.getNode() != null) {
                for (int i = 0; i < enemy.getNode().getQuantity(); i++) {
                    if (enemy.getNode().getChild(i) == geometry) {
                        return enemy;
                    }
                }
            }
        }
        return null;
    }

    public NPC findNearestNPC(Vector3f playerPos, float maxDistance) {
        NPC nearest = null;
        float minDistance = maxDistance;

        for (NPC npc : npcs) {
            float distance = npc.getPosition().distance(playerPos);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = npc;
            }
        }

        return nearest;
    }

    public Enemy findNearestEnemy(Vector3f playerPos, float maxDistance) {
        Enemy nearest = null;
        float minDistance = maxDistance;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = enemy.getPosition().distance(playerPos);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = enemy;
                }
            }
        }

        return nearest;
    }

    public void updateInteractionIndicators(Vector3f playerPos) {
        // Обновляем индикаторы для мирных NPC
        for (NPC npc : npcs) {
            float distance = npc.getPosition().distance(playerPos);
            if (distance < 3.0f) {
                npc.showInteractionIndicator();
            } else {
                npc.hideInteractionIndicator();
            }
        }

        // Можно добавить индикаторы для врагов (например, красный если близко)
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = enemy.getPosition().distance(playerPos);
                // Можно добавить визуальную индикацию для врагов
            }
        }
    }

    public List<NPC> getNPCs() {
        return npcs;
    }
}