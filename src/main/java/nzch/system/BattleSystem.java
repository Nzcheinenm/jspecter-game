package nzch.system;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import nzch.Jspectergame;
import nzch.character.CombatCharacter;
import nzch.ui.BattleUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class BattleSystem {
    private final Jspectergame game;
    private final GridSystem gridSystem;
    private final List<CombatCharacter> turnOrder;
    private int currentTurnIndex = 0;
    private BattleUI battleUI;
    private boolean waitingForAnimation = false;
    private float animationTimer = 0f;
    private CombatCharacter animationAttacker;
    private CombatCharacter animationTarget;
    private Vector3f animationStartPos;
    private Vector3f animationEndPos;
    private String currentAnimation = null;

    public BattleSystem(Jspectergame game, GridSystem gridSystem) {
        this.game = game;
        this.gridSystem = gridSystem;
        this.turnOrder = new ArrayList<>();
        this.battleUI = new BattleUI(game.getGuiNode(), game.getGuiFont(), game);
    }

    public void startBattle(List<CombatCharacter> playerTeam, List<CombatCharacter> enemyTeam) {
        turnOrder.clear();

        List<CombatCharacter> allCharacters = new ArrayList<>();
        allCharacters.addAll(playerTeam);
        allCharacters.addAll(enemyTeam);

        allCharacters.sort((c1, c2) -> c2.getInitiative() - c1.getInitiative());
        turnOrder.addAll(allCharacters);

        currentTurnIndex = 0;

        System.out.println("Бой начался! Участники:");
        for (CombatCharacter character : turnOrder) {
            System.out.println("- " + character.getName() + " (инициатива: " + character.getInitiative() + ")");
        }
    }

    public void showAvailableActions(CombatCharacter character) {
        if (character.isAlive()) {
            gridSystem.highlightMovementRange(character.getPosition(), character.getMovementRange());
            battleUI.updateTurnInfo("Ход: " + character.getName());
        }
    }

    public boolean canAttack(CombatCharacter attacker, CombatCharacter target) {
        if (attacker == null || target == null || !attacker.isAlive() || !target.isAlive()) {
            return false;
        }

        Vector3f attackerPos = gridSystem.worldToGridPosition(attacker.getPosition());
        Vector3f targetPos = gridSystem.worldToGridPosition(target.getPosition());

        int distance = (int) (Math.abs(targetPos.x - attackerPos.x) + Math.abs(targetPos.z - attackerPos.z));
        boolean canAttack = distance <= attacker.getAttackRange();

        System.out.println(attacker.getName() + " может атаковать " + target.getName() + "? " + canAttack + " (дистанция: " + distance + ")");
        return canAttack;
    }

    public void attack(CombatCharacter attacker, CombatCharacter target) {
        if (waitingForAnimation) return;

        System.out.println(attacker.getName() + " атакует " + target.getName());

        int damage = calculateDamage(attacker, target);
        target.takeDamage(damage);

        // Запускаем анимацию в основном потоке
        startAttackAnimation(attacker, target, damage);

        battleUI.updateCharacterInfo(target);

        if (target.getCurrentHealth() <= 0) {
            onCharacterDeath(target);
        }
    }

    private void startAttackAnimation(CombatCharacter attacker, CombatCharacter target, int damage) {
        waitingForAnimation = true;
        currentAnimation = "attack";
        animationTimer = 0f;
        animationAttacker = attacker;
        animationTarget = target;
        animationStartPos = attacker.getPosition();

        // Сохраняем оригинальную позицию для возврата
        animationEndPos = animationStartPos.clone();

        System.out.println("Начало анимации атаки");
    }

    public void moveCharacter(CombatCharacter character, Vector3f targetGridPos) {
        if (waitingForAnimation) return;

        Vector3f worldPos = gridSystem.gridToWorldPosition(targetGridPos);
        System.out.println(character.getName() + " двигается в позицию " + targetGridPos);

        // Запускаем анимацию движения
        startMovementAnimation(character, worldPos);

        gridSystem.hideMovementRange();
    }

    private void startMovementAnimation(CombatCharacter character, Vector3f targetPos) {
        waitingForAnimation = true;
        currentAnimation = "move";
        animationTimer = 0f;
        animationAttacker = character;
        animationStartPos = character.getPosition();
        animationEndPos = targetPos;

        System.out.println("Начало анимации движения");
    }

    public void update(float tpf) {
        if (!waitingForAnimation) return;

        animationTimer += tpf;

        switch (currentAnimation) {
            case "attack":
                updateAttackAnimation(tpf);
                break;
            case "move":
                updateMovementAnimation(tpf);
                break;
        }
    }

    private void updateAttackAnimation(float tpf) {
        float attackDuration = 0.5f;

        if (animationTimer < attackDuration * 0.3f) {
            // Фаза атаки - движение к цели
            Vector3f targetPos = animationTarget.getPosition();
            Vector3f direction = targetPos.subtract(animationStartPos).normalize();
            Vector3f attackPosition = animationStartPos.add(direction.mult(0.5f * (animationTimer / (attackDuration * 0.3f))));
            animationAttacker.getNode().setLocalTranslation(attackPosition);
        } else if (animationTimer < attackDuration * 0.6f) {
            // Фаза возврата
            float returnProgress = (animationTimer - attackDuration * 0.3f) / (attackDuration * 0.3f);
            Vector3f currentPos = animationAttacker.getNode().getLocalTranslation();
            Vector3f newPos = animationStartPos.add(currentPos.subtract(animationStartPos).mult(1 - returnProgress));
            animationAttacker.getNode().setLocalTranslation(newPos);
        } else {
            // Завершение анимации
            animationAttacker.getNode().setLocalTranslation(animationStartPos);
            animationAttacker.setPosition(animationStartPos);
            finishAnimation();
        }
    }

    private void updateMovementAnimation(float tpf) {
        float moveDuration = 0.8f;

        if (animationTimer < moveDuration) {
            // Плавное перемещение
            float progress = animationTimer / moveDuration;
            Vector3f newPos = animationStartPos.interpolateLocal(animationEndPos, progress);
            animationAttacker.getNode().setLocalTranslation(newPos);
        } else {
            // Завершение движения
            animationAttacker.getNode().setLocalTranslation(animationEndPos);
            animationAttacker.setPosition(animationEndPos);
            finishAnimation();
        }
    }

    private void finishAnimation() {
        waitingForAnimation = false;
        currentAnimation = null;
        animationAttacker = null;
        animationTarget = null;

        // Завершаем ход после анимации
        game.enqueue(() -> game.endCurrentTurn());
    }

    private void playSimpleAttackAnimation(CombatCharacter attacker, CombatCharacter target, int damage) {
        // Простая анимация - кратковременное смещение
        Vector3f originalPos = attacker.getPosition();
        Vector3f targetPos = target.getPosition();
        Vector3f direction = targetPos.subtract(originalPos).normalize();

        // Слегка сдвигаем атакующего к цели
        Vector3f attackOffset = direction.mult(0.3f);
        attacker.getNode().setLocalTranslation(originalPos.add(attackOffset));

        // Показываем урон
        showSimpleDamageEffect(target, damage);

        // Возвращаем на место
        game.getTimerJdk().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                attacker.getNode().setLocalTranslation(originalPos);
            }
        }, 200);
    }

    private void playMovementAnimation(CombatCharacter character, Vector3f targetPos) {
        waitingForAnimation = true;

        // Плавное перемещение
        Vector3f startPos = character.getPosition();
        float duration = 0.5f;
        float elapsed = 0f;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Плавное перемещение
                character.setPosition(targetPos);
                character.getNode().setLocalTranslation(targetPos);
                waitingForAnimation = false;
                game.endCurrentTurn(); // Завершаем ход после движения
            }
        }, 600);
    }

    private void showSimpleDamageEffect(CombatCharacter target, int damage) {
        // Простой эффект - изменение цвета
        Node targetNode = target.getNode();
        if (targetNode.getQuantity() > 0) {
            Geometry targetGeo = (Geometry) targetNode.getChild(0);
            Material originalMat = targetGeo.getMaterial();
            Material hitMat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            hitMat.setColor("Color", ColorRGBA.Red);

            targetGeo.setMaterial(hitMat);

            // Возвращаем оригинальный материал
            game.getTimerJdk().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    targetGeo.setMaterial(originalMat);
                }
            }, 300);
        }

        System.out.println("Нанесен урон: " + damage);
    }

    private int calculateDamage(CombatCharacter attacker, CombatCharacter target) {
        int baseDamage = attacker.getAttack();
        int targetArmor = target.getArmor();
        int damage = Math.max(1, baseDamage - targetArmor);
        damage += (int) (Math.random() * 4) - 2;
        damage = Math.max(1, damage);

        System.out.println(attacker.getName() + " наносит " + damage + " урона " + target.getName());
        return damage;
    }

    private void onCharacterDeath(CombatCharacter character) {
        System.out.println(character.getName() + " погиб!");

        // Анимация смерти
        character.getNode().rotate(0, 0, (float) Math.toRadians(90)); // Падение
        character.getNode().setLocalTranslation(
                character.getPosition().x,
                character.getPosition().y - 0.5f,
                character.getPosition().z
        );
    }

    public boolean isWaitingForAnimation() {
        return waitingForAnimation;
    }


    public Jspectergame getGame() {
        return game;
    }

    public List<CombatCharacter> getTurnOrder() {
        return turnOrder;
    }

    // Добавляем геттер для GridSystem
    public GridSystem getGridSystem() {
        return gridSystem;
    }

    public CombatCharacter getNextTurnCharacter() {
        if (turnOrder.isEmpty()) {
            System.out.println("Нет персонажей в порядке ходов");
            return null;
        }

        // Ищем следующего живого персонажа, начиная с текущего индекса
        int startIndex = currentTurnIndex;
        int attempts = 0;

        do {
            CombatCharacter character = turnOrder.get(currentTurnIndex);
            System.out.println("Проверяем персонажа: " + character.getName() +
                    " (HP: " + character.getCurrentHealth() + ", alive: " + character.isAlive() + ")");

            if (character.isAlive()) {
                System.out.println("Следующий ход: " + character.getName());
                return character;
            }

            // Переходим к следующему персонажу
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
            attempts++;

        } while (currentTurnIndex != startIndex && attempts < turnOrder.size());

        System.out.println("Не найден живой персонаж для хода");
        return null;
    }

    public void endTurn(CombatCharacter character) {
        System.out.println("Завершение хода для: " + character.getName());

        // Переходим к следующему персонажу
        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        System.out.println("Следующий индекс хода: " + currentTurnIndex);
    }

    private void showAttackEffect(CombatCharacter attacker, CombatCharacter target) {
        // Можно добавить частицы, анимации и т.д.
        System.out.println(attacker.getName() + " атакует " + target.getName());
    }


    public void showBattleUI() {
        if (battleUI == null) {
            battleUI = new BattleUI(game.getGuiNode(), game.getGuiFont(), game);
        }
        battleUI.show();
    }

    public void hideBattleUI() {
        if (battleUI != null) {
            battleUI.hide();
        }
    }
}
