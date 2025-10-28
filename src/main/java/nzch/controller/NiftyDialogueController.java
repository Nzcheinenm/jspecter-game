package nzch.controller;


import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import nzch.Jspectergame;
import nzch.character.CombatCharacter;
import nzch.character.Enemy;
import nzch.system.DialogueSystem;

/**
 * Контроллер для управления диалогами через Nifty GUI
 */
public class NiftyDialogueController implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Jspectergame game;

    // Диалоговые элементы
    private Element dialoguePanel;
    private Element npcNameText;
    private Element dialogueText;
    private Element optionsContainer;
    private Element hintText;

    // Боевые элементы
    private Element combatLayer;
    private Element combatInfoText;
    private Element turnInfoText;
    private Element actionHintText;
    private Element enemyCountText;
    private Element roundInfoText;

    // Панели персонажей
    private Element playerInfoPanel;
    private Element playerNameText;
    private Element playerHpText;
    private Element playerStatusText;
    private Element enemyListPanel;

    // Кнопки действий
    private Button attackButton;
    private Button moveButton;
    private Button skillButton;
    private Button itemButton;
    private Button endTurnButton;

    // Лог боя
    private Element[] logLines;

    // Элементы врагов (заранее созданные)
    private Element[] enemyPanels;
    private Element[] enemyNameTexts;
    private Element[] enemyHpTexts;

    private DialogueSystem.DialogueNode currentNode;
    private String currentNPCName;

    // Боевые переменные
    private int currentRound = 1;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        initializeUIElements();
        hideDialogue();
        hideCombatUI();
    }

    private void initializeUIElements() {
        // Диалоговые элементы
        dialoguePanel = screen.findElementByName("dialogue_panel");
        npcNameText = screen.findElementByName("npc_name");
        dialogueText = screen.findElementByName("dialogue_text");
        optionsContainer = screen.findElementByName("options_container");
        hintText = screen.findElementByName("hint_text");

        // Боевые элементы
        combatLayer = screen.findElementByName("combat_layer");
        combatInfoText = screen.findElementByName("combat_info");
        turnInfoText = screen.findElementByName("turn_info");
        actionHintText = screen.findElementByName("action_hint");
        enemyCountText = screen.findElementByName("enemy_count");
        roundInfoText = screen.findElementByName("round_info");

        // Панели персонажей
        playerInfoPanel = screen.findElementByName("player_info");
        playerNameText = screen.findElementByName("player_name");
        playerHpText = screen.findElementByName("player_hp");
        playerStatusText = screen.findElementByName("player_status");
        enemyListPanel = screen.findElementByName("enemy_list");

        // Кнопки действий
        attackButton = screen.findNiftyControl("attack_button", Button.class);
        moveButton = screen.findNiftyControl("move_button", Button.class);
        skillButton = screen.findNiftyControl("skill_button", Button.class);
        itemButton = screen.findNiftyControl("item_button", Button.class);
        endTurnButton = screen.findNiftyControl("end_turn_button", Button.class);

        // Инициализируем лог боя
        logLines = new Element[5];
        for (int i = 0; i < 5; i++) {
            logLines[i] = screen.findElementByName("log_line_" + (i + 1));
        }

        // Заранее создаем элементы для врагов (максимум 5)
        initializeEnemyElements();

        clearCombatLog();
    }

    private void initializeEnemyElements() {
        enemyPanels = new Element[5];
        enemyNameTexts = new Element[5];
        enemyHpTexts = new Element[5];

        for (int i = 0; i < 5; i++) {
            // Создаем панель для врага
            enemyPanels[i] = screen.findElementByName("enemy_panel_" + (i + 1));
            enemyNameTexts[i] = screen.findElementByName("enemy_name_" + (i + 1));
            enemyHpTexts[i] = screen.findElementByName("enemy_hp_" + (i + 1));

            // Скрываем по умолчанию
            if (enemyPanels[i] != null) {
                enemyPanels[i].hide();
            }
        }
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    // === ДИАЛОГИ ===

    public void option1() {
        onOptionSelected(0);
    }

    public void option2() {
        onOptionSelected(1);
    }

    public void option3() {
        onOptionSelected(2);
    }

    public void setGame(Jspectergame game) {
        this.game = game;
    }

    public void showDialogue(String npcName, String dialogueKey) {
        if (dialoguePanel == null || game == null) return;

        DialogueSystem.DialogueNode startNode = game.getDialogueSystem().getDialogue(dialogueKey);
        if (startNode != null) {
            this.currentNPCName = npcName;
            this.currentNode = startNode;
            updateDialogueDisplay();
            dialoguePanel.show();
        }
    }

    private void updateDialogueDisplay() {
        if (currentNode == null) return;

        setText(npcNameText, currentNPCName);
        setText(dialogueText, extractDialogueText(currentNode.getText()));

        if (currentNode.hasOptions()) {
            optionsContainer.show();
            setText(hintText, "Выберите вариант ответа");

            String[] options = currentNode.getOptions();
            for (int i = 0; i < Math.min(options.length, 3); i++) {
                Element optionElement = screen.findElementByName("option_" + (i + 1));
                if (optionElement != null) {
                    setText(optionElement, (i + 1) + ". " + options[i]);
                    optionElement.show();
                }
            }

            for (int i = options.length; i < 3; i++) {
                Element optionElement = screen.findElementByName("option_" + (i + 1));
                if (optionElement != null) {
                    optionElement.hide();
                }
            }
        } else {
            optionsContainer.hide();
            setText(hintText, "[ПРОБЕЛ] продолжить");
        }
    }

    public void onOptionSelected(int optionIndex) {
        if (currentNode != null && currentNode.hasOptions()) {
            if (optionIndex >= 0 && optionIndex < currentNode.getOptions().length) {
                currentNode = currentNode.getNextNode(optionIndex);
                if (currentNode != null) {
                    updateDialogueDisplay();
                } else {
                    hideDialogue();
                    if (game != null) game.endDialogue();
                }
            }
        }
    }

    public void nextDialogue() {
        if (currentNode == null || currentNode.hasOptions()) return;
        hideDialogue();
        if (game != null) game.endDialogue();
    }

    public void hideDialogue() {
        if (dialoguePanel != null) dialoguePanel.hide();
        currentNode = null;
        currentNPCName = null;
    }

    // === БОЕВОЙ ИНТЕРФЕЙС ===

    public void showCombatUI() {
        if (combatLayer != null) {
            combatLayer.show();
            updateCombatInfo();
        }
    }

    public void hideCombatUI() {
        if (combatLayer != null) combatLayer.hide();
    }

    public void updateCombatInfo(String combatInfo, String turnInfo, String actionHint) {
        setText(combatInfoText, combatInfo);
        setText(turnInfoText, turnInfo);
        setText(actionHintText, actionHint);
        updateCombatInfo();
    }

    public void updateCombatInfo() {
        if (game != null && game.inCombat) {
            // Обновляем счетчик врагов и раунд
            int enemyCount = (int) game.combatCharacters.stream()
                    .filter(c -> c instanceof Enemy && c.isAlive())
                    .count();
            setText(enemyCountText, "Врагов: " + enemyCount);
            setText(roundInfoText, "Раунд: " + currentRound);

            // Обновляем информацию об игроке
            CombatCharacter player = game.getPlayerCombatCharacter();
            if (player != null) {
                setText(playerNameText, player.getName());
                setText(playerHpText, "HP: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
                setText(playerStatusText, player.isAlive() ? "✅ Готов" : "💀 Убит");
            }

            // Обновляем список врагов
            updateEnemyList();
        }
    }

    private void updateEnemyList() {
        if (enemyPanels == null) return;

        // Сначала скрываем все панели врагов
        for (Element panel : enemyPanels) {
            if (panel != null) {
                panel.hide();
            }
        }

        // Показываем только нужных врагов
        int enemyIndex = 0;
        for (CombatCharacter character : game.combatCharacters) {
            if (character instanceof Enemy enemy && character.isAlive() && enemyIndex < enemyPanels.length) {

                if (enemyPanels[enemyIndex] != null) {
                    // Показываем панель
                    enemyPanels[enemyIndex].show();

                    // Обновляем информацию
                    if (enemyNameTexts[enemyIndex] != null) {
                        String statusIcon = enemy.getCurrentHealth() < enemy.getMaxHealth() * 0.5 ? " 💢" : "";
                        setText(enemyNameTexts[enemyIndex], enemy.getName() + statusIcon);
                    }

                    if (enemyHpTexts[enemyIndex] != null) {
                        setText(enemyHpTexts[enemyIndex], "HP: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
                    }

                    // Меняем цвет фона в зависимости от HP
                    if (enemy.getCurrentHealth() < enemy.getMaxHealth() * 0.3) {
                        enemyPanels[enemyIndex].setStyle("enemy_critical");
                    } else if (enemy.getCurrentHealth() < enemy.getMaxHealth() * 0.6) {
                        enemyPanels[enemyIndex].setStyle("enemy_wounded");
                    } else {
                        enemyPanels[enemyIndex].setStyle("enemy_healthy");
                    }
                }

                enemyIndex++;
            }
        }
    }

    // === ДЕЙСТВИЯ В БОЮ ===

    public void attackAction() {
        if (game != null && game.inCombat) {
            addCombatLog("Вы готовитесь атаковать...");
            setText(actionHintText, "Выберите цель для атаки");
            // Здесь будет логика выбора цели для атаки
        }
    }

    public void moveAction() {
        if (game != null && game.inCombat) {
            addCombatLog("Вы готовитесь переместиться...");
            setText(actionHintText, "Выберите клетку для перемещения");
            if (game.gridSystem != null && game.currentTurnCharacter != null) {
                game.gridSystem.highlightMovementRange(
                        game.currentTurnCharacter.getPosition(),
                        game.currentTurnCharacter.getMovementRange()
                );
            }
        }
    }

    public void skillAction() {
        addCombatLog("Умения пока недоступны");
        setText(actionHintText, "Умения будут добавлены позже");
    }

    public void itemAction() {
        addCombatLog("Предметы пока недоступны");
        setText(actionHintText, "Инвентарь будет добавлен позже");
    }

    public void endTurnAction() {
        if (game != null && game.inCombat) {
            addCombatLog("Вы завершаете свой ход");
            if (game.gridSystem != null) {
                game.gridSystem.hideMovementRange();
            }
            game.endCurrentTurn();
        }
    }

    // === ЛОГ БОЯ ===

    public void addCombatLog(String message) {
        if (logLines == null) return;

        // Сдвигаем старые сообщения вверх
        for (int i = logLines.length - 2; i >= 0; i--) {
            if (logLines[i] != null && logLines[i + 1] != null) {
                TextRenderer currentRenderer = logLines[i].getRenderer(TextRenderer.class);
                TextRenderer nextRenderer = logLines[i + 1].getRenderer(TextRenderer.class);
                if (currentRenderer != null && nextRenderer != null) {
                    nextRenderer.setText(currentRenderer.getOriginalText());
                }
            }
        }

        // Добавляем новое сообщение
        if (logLines[0] != null) {
            setText(logLines[0], "> " + message);
        }
    }

    public void clearCombatLog() {
        if (logLines != null) {
            for (Element line : logLines) {
                if (line != null) {
                    setText(line, "");
                }
            }
        }
    }

    public void setRound(int round) {
        this.currentRound = round;
        updateCombatInfo();
    }

    public void updateTurnInfo(String characterName, int health, String status) {
        String turnText = "Ход: " + characterName + " (HP: " + health + ") - " + status;
        setText(turnInfoText, turnText);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private void setText(Element element, String text) {
        if (element != null) {
            TextRenderer renderer = element.getRenderer(TextRenderer.class);
            if (renderer != null) renderer.setText(text);
        }
    }

    private String extractDialogueText(String fullText) {
        return fullText.contains(":") ? fullText.split(":", 2)[1].trim() : fullText;
    }

    public boolean isDialogueVisible() {
        return dialoguePanel != null && dialoguePanel.isVisible();
    }
}