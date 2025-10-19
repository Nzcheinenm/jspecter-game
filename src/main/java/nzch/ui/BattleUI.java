package nzch.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import nzch.Jspectergame;
import nzch.character.CombatCharacter;
import nzch.character.PlayerCombatCharacter;

import java.util.ArrayList;
import java.util.List;

public class BattleUI {
    private final Node guiNode;
    private final BitmapFont guiFont;
    private final Jspectergame game;
    private final List<BitmapText> characterInfoTexts;
    private BitmapText turnInfoText;
    private BitmapText playerHealthText;
    private boolean visible = false;

    public BattleUI(Node guiNode, BitmapFont guiFont, Jspectergame game) {
        this.guiNode = guiNode;
        this.guiFont = guiFont;
        this.game = game;
        this.characterInfoTexts = new ArrayList<>();
        createUI();
    }

    private void createUI() {
        // Текст с информацией о текущем ходе
        turnInfoText = new BitmapText(guiFont, false);
        turnInfoText.setSize(guiFont.getCharSet().getRenderedSize());
        turnInfoText.setColor(ColorRGBA.Yellow);
        turnInfoText.setLocalTranslation(100, 100, 0);

        // Текст с здоровьем игрока
        playerHealthText = new BitmapText(guiFont, false);
        playerHealthText.setSize(guiFont.getCharSet().getRenderedSize() * 0.8f);
        playerHealthText.setColor(ColorRGBA.Green);
        playerHealthText.setLocalTranslation(100, 150, 0);

        guiNode.attachChild(turnInfoText);
        guiNode.attachChild(playerHealthText);
    }

    public void show() {
        visible = true;
        turnInfoText.setText("БОЙ НАЧАЛСЯ! Ваш ход.");
        updatePlayerHealth();
    }

    public void hide() {
        visible = false;
        turnInfoText.setText("");
        playerHealthText.setText("");

        for (BitmapText text : characterInfoTexts) {
            guiNode.detachChild(text);
        }
        characterInfoTexts.clear();
    }

    public void updateCharacterInfo(CombatCharacter character) {
        if (character instanceof PlayerCombatCharacter) {
            updatePlayerHealth();
        }
    }

    private void updatePlayerHealth() {
        PlayerCombatCharacter player = game.getPlayerCombatCharacter();
        if (player != null) {
            playerHealthText.setText("Здоровье: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
        }
    }

    public void updateTurnInfo(String info) {
        if (visible) {
            turnInfoText.setText(info);
        }
    }

    public void update(float tpf) {
        // Обновление UI
        if (visible) {
            updatePlayerHealth();
        }
    }
}