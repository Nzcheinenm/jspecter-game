package nzch.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import nzch.Jspectergame;
import nzch.systems.DialogueSystem;

import java.util.ArrayList;
import java.util.List;


public class DialogueUI {
    private final Node guiNode;
    private final BitmapFont guiFont;
    private final Jspectergame game;
    private final AppSettings settings;

    private BitmapText dialogueText;
    private final List<BitmapText> optionTexts;
    private BitmapText continueText;
    private boolean visible;

    private DialogueSystem.DialogueNode currentNode;

    public DialogueUI(Node guiNode, BitmapFont guiFont, Jspectergame game) {
        this.guiNode = guiNode;
        this.guiFont = guiFont;
        this.game = game;
        this.settings = game.getContext().getSettings();
        this.optionTexts = new ArrayList<>();

        createUI();
        hide();
    }

    private void createUI() {
        int screenHeight = settings.getHeight();

        // Фон диалога (можно добавить позже)
        dialogueText = new BitmapText(guiFont, false);
        dialogueText.setSize(guiFont.getCharSet().getRenderedSize());
        dialogueText.setColor(ColorRGBA.White);
        dialogueText.setText("");
        dialogueText.setLocalTranslation(100, screenHeight - 100, 0);

        // Текст для продолжения
        continueText = new BitmapText(guiFont, false);
        continueText.setSize(guiFont.getCharSet().getRenderedSize() * 0.8f);
        continueText.setColor(ColorRGBA.Yellow);
        continueText.setText("[ПРОБЕЛ] чтобы продолжить");
        continueText.setLocalTranslation(100, screenHeight - 150, 0);

        guiNode.attachChild(dialogueText);
        guiNode.attachChild(continueText);
    }

    private void updateDialogueDisplay() {
        if (currentNode == null) {
            hide();
            return;
        }

        int screenHeight = settings.getHeight();
        dialogueText.setText(currentNode.getText());

        // Очищаем старые варианты ответов
        for (BitmapText optionText : optionTexts) {
            guiNode.detachChild(optionText);
        }
        optionTexts.clear();

        // Показываем варианты ответов если они есть
        if (currentNode.hasOptions()) {
            String[] options = currentNode.getOptions();
            float yPos = screenHeight - 200;

            for (int i = 0; i < options.length; i++) {
                BitmapText optionText = new BitmapText(guiFont, false);
                optionText.setSize(guiFont.getCharSet().getRenderedSize() * 0.7f);
                optionText.setColor(ColorRGBA.Cyan);
                optionText.setText((i + 1) + ". " + options[i]);
                optionText.setLocalTranslation(120, yPos - (i * 30), 0);

                optionTexts.add(optionText);
                guiNode.attachChild(optionText);
            }

            continueText.setText("[1-" + options.length + "] выбрать ответ");
        } else {
            continueText.setText("[ПРОБЕЛ] чтобы продолжить");
        }
    }

    // Остальные методы остаются без изменений
    public void showDialogue(String dialogueKey) {
        DialogueSystem.DialogueNode startNode = game.dialogueSystem.getDialogue(dialogueKey);
        if (startNode != null) {
            currentNode = startNode;
            updateDialogueDisplay();
            visible = true;
        }
    }

    public void nextDialogue() {
        if (currentNode == null) {
            hide();
            return;
        }

        if (!currentNode.hasOptions()) {
            // Если нет вариантов ответа, заканчиваем диалог
            game.endDialogue();
        }
    }

    public void selectOption(int optionIndex) {
        if (currentNode != null && currentNode.hasOptions()) {
            if (optionIndex >= 0 && optionIndex < currentNode.getOptions().length) {
                currentNode = currentNode.getNextNode(optionIndex);
                updateDialogueDisplay();
            }
        }
    }

    public void hide() {
        dialogueText.setText("");
        continueText.setText("");

        for (BitmapText optionText : optionTexts) {
            guiNode.detachChild(optionText);
        }
        optionTexts.clear();

        visible = false;
        currentNode = null;
    }

    public boolean isVisible() {
        return visible;
    }
}