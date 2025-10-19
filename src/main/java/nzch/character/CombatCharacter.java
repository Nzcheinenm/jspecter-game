package nzch.character;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

// Базовый класс для всех боевых персонажей
public abstract class CombatCharacter {
    protected String name;
    protected int maxHealth;
    protected int currentHealth;
    protected int attack;
    protected int armor;
    protected int movementRange;
    protected int attackRange;
    protected int initiative;
    protected Vector3f position;
    protected Node node;

    public CombatCharacter(String name, int maxHealth, int attack, int armor) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.attack = attack;
        this.armor = armor;
        this.movementRange = 4;
        this.attackRange = 1;
        this.initiative = 10 + (int) (Math.random() * 10);
    }

    public String getName() {
        return name;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getAttack() {
        return attack;
    }

    public int getArmor() {
        return armor;
    }

    public int getMovementRange() {
        return movementRange;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public int getInitiative() {
        return initiative;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Node getNode() {
        return node;
    }

    public void takeDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
    }

    public void resetActions() {
        // Сброс действий для нового хода
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }
}



