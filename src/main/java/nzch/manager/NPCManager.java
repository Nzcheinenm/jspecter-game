package nzch.manager;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import nzch.Jspectergame;
import nzch.systems.NPC;

import java.util.ArrayList;
import java.util.List;

public class NPCManager {
    private final List<NPC> npcs;
    private final Node rootNode;
    private final AssetManager assetManager;
    private final Jspectergame game;

    public NPCManager(Node rootNode, AssetManager assetManager, Jspectergame game) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.game = game;
        this.npcs = new ArrayList<>();
    }

    public void createNPCs() {
        // Создаем несколько NPC в разных местах
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

    public void updateInteractionIndicators(Vector3f playerPos) {
        for (NPC npc : npcs) {
            float distance = npc.getPosition().distance(playerPos);
            if (distance < 3.0f) {
                npc.showInteractionIndicator();
            } else {
                npc.hideInteractionIndicator();
            }
        }
    }

    public List<NPC> getNPCs() {
        return npcs;
    }
}