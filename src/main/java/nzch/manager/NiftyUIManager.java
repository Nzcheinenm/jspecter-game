package nzch.manager;


import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import nzch.Jspectergame;
import nzch.controller.NiftyDialogueController;
import nzch.ui.MainMenuController;

public class NiftyUIManager {

    private Jspectergame game;
    private NiftyJmeDisplay niftyDisplay;
    private NiftyDialogueController dialogueController;
    private MainMenuController mainMenuController;
    private Nifty nifty;

    // Состояния UI
    private boolean mainMenuVisible = true;
    private boolean gameUIEnabled = false;

    public NiftyUIManager(Jspectergame game) {
        this.game = game;
        initializeNifty();
    }

    private void initializeNifty() {
        try {
            niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                    game.getAssetManager(),
                    game.getInputManager(),
                    game.getAudioRenderer(),
                    game.getGuiViewPort()
            );

            nifty = niftyDisplay.getNifty();

            // Инициализируем контроллер главного меню
            mainMenuController = new MainMenuController();
            mainMenuController.setGame(game);
            mainMenuController.setUIManager(this);

            // Загружаем главное меню ПЕРВЫМ
            nifty.fromXml("nifty/main_menu.xml", "main_menu", mainMenuController);

            // Затем загружаем игровой UI
            dialogueController = new NiftyDialogueController();
            dialogueController.setGame(game);
            nifty.fromXml("nifty/dialogue.xml", "main_screen", dialogueController);

            // Добавляем Nifty в viewport
            game.getGuiViewPort().addProcessor(niftyDisplay);

            // Показываем главное меню при запуске
            showMainMenu();

            System.out.println("Nifty GUI успешно инициализирован");

        } catch (Exception e) {
            System.err.println("Ошибка инициализации Nifty GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === ГЛАВНОЕ МЕНЮ ===
    public void showMainMenu() {
        if (nifty != null) {
            nifty.gotoScreen("main_menu");
            mainMenuVisible = true;
            gameUIEnabled = false;

            // Отключаем игровое управление когда меню активно
            if (game != null) {
                game.setEnableGameInput(false);
            }
        }
    }

    public void hideMainMenu() {
        if (nifty != null) {
            nifty.gotoScreen("main_screen");
            mainMenuVisible = false;
            gameUIEnabled = true;

            // Включаем игровое управление
            game.setEnableGameInput(true);
        }
    }

    public boolean isMainMenuVisible() {
        return mainMenuVisible;
    }

    public boolean isGameUIEnabled() {
        return gameUIEnabled;
    }

    // === ДИАЛОГИ ===

    public void showDialogue(String npcName, String dialogueKey) {
        if (dialogueController != null && gameUIEnabled) {
            dialogueController.showDialogue(npcName, dialogueKey);
        }
    }

    public void hideDialogue() {
        if (dialogueController != null) {
            dialogueController.hideDialogue();
        }
    }

    public void nextDialogue() {
        if (dialogueController != null) {
            dialogueController.nextDialogue();
        }
    }

    public void selectOption(int optionIndex) {
        if (dialogueController != null) {
            dialogueController.onOptionSelected(optionIndex);
        }
    }

    public boolean isDialogueVisible() {
        return dialogueController != null && dialogueController.isDialogueVisible();
    }

    // === БОЕВОЙ UI ===

    public void showCombatUI() {
        if (dialogueController != null && gameUIEnabled) {
            dialogueController.showCombatUI();
            updateCombatUI();
        }
    }

    public void hideCombatUI() {
        if (dialogueController != null) {
            dialogueController.hideCombatUI();
        }
    }

    public void updateCombatInfo(String combatInfo, String turnInfo, String actionHint) {
        if (dialogueController != null) {
            dialogueController.updateCombatInfo(combatInfo, turnInfo, actionHint);
        }
    }

    public void updateTurnInfo(String characterName, int health, String status) {
        if (dialogueController != null) {
            dialogueController.updateTurnInfo(characterName, health, status);
        }
    }

    public void addCombatLog(String message) {
        if (dialogueController != null) {
            dialogueController.addCombatLog(message);
        }
    }

    public void setCombatRound(int round) {
        if (dialogueController != null) {
            dialogueController.setRound(round);
        }
    }

    public void updateCombatUI() {
        if (dialogueController != null) {
            dialogueController.updateCombatInfo();
        }
    }

    public void forceUpdateCombatUI() {
        if (dialogueController != null && game != null && game.inCombat) {
            dialogueController.updateCombatInfo();

            if (game.currentTurnCharacter != null) {
                String status = game.currentTurnCharacter instanceof nzch.character.PlayerCombatCharacter ?
                        "Ваш ход" : "Ход противника";
                dialogueController.updateTurnInfo(
                        game.currentTurnCharacter.getName(),
                        game.currentTurnCharacter.getCurrentHealth(),
                        status
                );
            }
        }
    }

    // === ОЧИСТКА ===

    public void cleanup() {
        if (niftyDisplay != null) {
            game.getGuiViewPort().removeProcessor(niftyDisplay);
            niftyDisplay = null;
        }
    }
}