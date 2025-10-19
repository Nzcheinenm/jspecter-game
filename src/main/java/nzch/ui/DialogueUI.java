package nzch.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import nzch.Jspectergame;
import nzch.system.DialogueSystem;

import java.util.ArrayList;
import java.util.List;

public class DialogueUI {
    private final Node guiNode;
    private final BitmapFont guiFont;
    private final Jspectergame game;
    private AppSettings settings;

    // Элементы UI
    private BitmapText dialogueText;
    private final List<BitmapText> optionTexts;
    private BitmapText continueText;

    // Фоны
    private Geometry dialogueBackground;
    private Geometry optionsBackground;

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
        int screenWidth = settings.getWidth();

        // Создаем фоны
        createDialogueBackground(screenWidth, screenHeight);
        createOptionsBackground(screenWidth, screenHeight);

        // Текст диалога
        dialogueText = new BitmapText(guiFont, false);
        dialogueText.setSize(guiFont.getCharSet().getRenderedSize());
        dialogueText.setColor(ColorRGBA.White);
        dialogueText.setText("");
        dialogueText.setLocalTranslation(150, screenHeight - 120, 0);

        // Текст для продолжения
        continueText = new BitmapText(guiFont, false);
        continueText.setSize(guiFont.getCharSet().getRenderedSize() * 0.8f);
        continueText.setColor(ColorRGBA.Yellow);
        continueText.setText("[ПРОБЕЛ] чтобы продолжить");
        continueText.setLocalTranslation(150, screenHeight - 170, 0);

        guiNode.attachChild(dialogueText);
        guiNode.attachChild(continueText);
    }

    private void createDialogueBackground(int screenWidth, int screenHeight) {
        // Создаем фон с закругленными краями (простой прямоугольник)
        Quad bgQuad = new Quad(screenWidth - 100, 180);
        dialogueBackground = new Geometry("DialogueBG", bgQuad);

        Material bgMat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        // Темный полупрозрачный фон
        bgMat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.2f, 0.85f));
        dialogueBackground.setMaterial(bgMat);
        dialogueBackground.setLocalTranslation(50, screenHeight - 230, -1);

        // Добавляем рамку
        createBorder(dialogueBackground, 2, ColorRGBA.Cyan, screenWidth - 100, 180, 50, screenHeight - 230);

        guiNode.attachChild(dialogueBackground);
        dialogueBackground.setCullHint(Node.CullHint.Always); // Скрываем по умолчанию
    }

    private void createOptionsBackground(int screenWidth, int screenHeight) {
        Quad optionsQuad = new Quad(screenWidth - 150, 150);
        optionsBackground = new Geometry("OptionsBG", optionsQuad);

        Material optionsMat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        optionsMat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.3f, 0.9f));
        optionsBackground.setMaterial(optionsMat);
        optionsBackground.setLocalTranslation(75, screenHeight - 400, -1);
        optionsBackground.setCullHint(Node.CullHint.Always); // Скрываем по умолчанию

        // Рамка для options
        createBorder(optionsBackground, 1, ColorRGBA.Cyan, screenWidth - 150, 150, 75, screenHeight - 400);

        guiNode.attachChild(optionsBackground);
    }

    private void createBorder(Geometry background, int borderWidth, ColorRGBA color, float width, float height, float posX, float posY) {
        Material borderMat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        borderMat.setColor("Color", color);

        // Верхняя рамка
        Quad topBorder = new Quad(width, borderWidth);
        Geometry topBorderGeo = new Geometry("TopBorder", topBorder);
        topBorderGeo.setMaterial(borderMat);
        topBorderGeo.setLocalTranslation(posX, posY + height, -0.5f);
        guiNode.attachChild(topBorderGeo);
        topBorderGeo.setCullHint(Node.CullHint.Always);

        // Нижняя рамка
        Quad bottomBorder = new Quad(width, borderWidth);
        Geometry bottomBorderGeo = new Geometry("BottomBorder", bottomBorder);
        bottomBorderGeo.setMaterial(borderMat);
        bottomBorderGeo.setLocalTranslation(posX, posY, -0.5f);
        guiNode.attachChild(bottomBorderGeo);
        bottomBorderGeo.setCullHint(Node.CullHint.Always);

        // Левая рамка
        Quad leftBorder = new Quad(borderWidth, height);
        Geometry leftBorderGeo = new Geometry("LeftBorder", leftBorder);
        leftBorderGeo.setMaterial(borderMat);
        leftBorderGeo.setLocalTranslation(posX, posY, -0.5f);
        guiNode.attachChild(leftBorderGeo);
        leftBorderGeo.setCullHint(Node.CullHint.Always);

        // Правая рамка
        Quad rightBorder = new Quad(borderWidth, height);
        Geometry rightBorderGeo = new Geometry("RightBorder", rightBorder);
        rightBorderGeo.setMaterial(borderMat);
        rightBorderGeo.setLocalTranslation(posX + width - borderWidth, posY, -0.5f);
        guiNode.attachChild(rightBorderGeo);
        rightBorderGeo.setCullHint(Node.CullHint.Always);
    }

    private void showBorders(boolean show) {
        Node.CullHint hint = show ? Node.CullHint.Never : Node.CullHint.Always;

        // Показываем/скрываем все рамки
        Geometry topBorder = (Geometry) guiNode.getChild("TopBorder");
        Geometry bottomBorder = (Geometry) guiNode.getChild("BottomBorder");
        Geometry leftBorder = (Geometry) guiNode.getChild("LeftBorder");
        Geometry rightBorder = (Geometry) guiNode.getChild("RightBorder");

        if (topBorder != null) topBorder.setCullHint(hint);
        if (bottomBorder != null) bottomBorder.setCullHint(hint);
        if (leftBorder != null) leftBorder.setCullHint(hint);
        if (rightBorder != null) rightBorder.setCullHint(hint);
    }

    public void showDialogue(String dialogueKey) {
        DialogueSystem.DialogueNode startNode = game.getDialogueSystem().getDialogue(dialogueKey);
        if (startNode != null) {
            currentNode = startNode;

            // Показываем фон диалога
            if (dialogueBackground != null) {
                dialogueBackground.setCullHint(Node.CullHint.Never);
            }

            // Показываем текст
            dialogueText.setCullHint(Node.CullHint.Never);
            continueText.setCullHint(Node.CullHint.Never);

            updateDialogueDisplay();
            visible = true;
        }
    }

    private void updateDialogueDisplay() {
        if (currentNode == null) {
            hide();
            return;
        }

        dialogueText.setText(currentNode.getText());

        // Очищаем старые варианты ответов
        for (BitmapText optionText : optionTexts) {
            guiNode.detachChild(optionText);
        }
        optionTexts.clear();

        // Показываем варианты ответов если они есть
        if (currentNode.hasOptions()) {
            String[] options = currentNode.getOptions();
            float yPos = settings.getHeight() - 220;

            // Показываем фон для вариантов
            if (optionsBackground != null) {
                optionsBackground.setCullHint(Node.CullHint.Never);
            }

            for (int i = 0; i < options.length; i++) {
                BitmapText optionText = new BitmapText(guiFont, false);
                optionText.setSize(guiFont.getCharSet().getRenderedSize() * 0.7f);
                optionText.setColor(ColorRGBA.Cyan);
                optionText.setText((i + 1) + ". " + options[i]);
                optionText.setLocalTranslation(140, yPos - (i * 30), 0);

                optionTexts.add(optionText);
                guiNode.attachChild(optionText);
            }

            continueText.setText("[1-" + options.length + "] выбрать ответ");
        } else {
            // Скрываем фон для вариантов
            if (optionsBackground != null) {
                optionsBackground.setCullHint(Node.CullHint.Always);
            }
            continueText.setText("[ПРОБЕЛ] чтобы продолжить");
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
        // Скрываем текст
        dialogueText.setText("");
        continueText.setText("");
        dialogueText.setCullHint(Node.CullHint.Always);
        continueText.setCullHint(Node.CullHint.Always);

        // Скрываем фоны
        if (dialogueBackground != null) {
            dialogueBackground.setCullHint(Node.CullHint.Always);
        }
        if (optionsBackground != null) {
            optionsBackground.setCullHint(Node.CullHint.Always);
        }

        // Скрываем рамки
        showBorders(false);

        // Удаляем варианты ответов
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

    // Метод для обновления размеров при изменении размера окна
    public void updateScreenSize(int width, int height) {
        // Сначала скрываем все
        hide();

        // Удаляем старые элементы
        if (dialogueBackground != null) {
            guiNode.detachChild(dialogueBackground);
        }
        if (optionsBackground != null) {
            guiNode.detachChild(optionsBackground);
        }

        // Удаляем старые рамки
        Geometry topBorder = (Geometry) guiNode.getChild("TopBorder");
        Geometry bottomBorder = (Geometry) guiNode.getChild("BottomBorder");
        Geometry leftBorder = (Geometry) guiNode.getChild("LeftBorder");
        Geometry rightBorder = (Geometry) guiNode.getChild("RightBorder");

        if (topBorder != null) guiNode.detachChild(topBorder);
        if (bottomBorder != null) guiNode.detachChild(bottomBorder);
        if (leftBorder != null) guiNode.detachChild(leftBorder);
        if (rightBorder != null) guiNode.detachChild(rightBorder);

        // Обновляем настройки
        this.settings = game.getContext().getSettings();

        // Пересоздаем UI с новыми размерами
        createUI();
    }
}