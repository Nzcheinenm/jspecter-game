package nzch.character;

import com.jme3.scene.Node;

public class PlayerCombatCharacter extends CombatCharacter {
    private final Node playerNode;

    public PlayerCombatCharacter(Node playerNode, String name, int health, int attack, int armor) {
        super(name, health, attack, armor);
        this.playerNode = playerNode;
        this.position = playerNode.getLocalTranslation();
        this.node = playerNode;
        this.movementRange = 6; // Игрок может двигаться дальше
    }
}
