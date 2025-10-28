package nzch.manager;


import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import nzch.Jspectergame;
import nzch.controller.NiftyDialogueController;

public class NiftyUIManager {

    private final Jspectergame game;
    private NiftyJmeDisplay niftyDisplay;
    private NiftyDialogueController dialogueController;

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

            dialogueController = new NiftyDialogueController();
            dialogueController.setGame(game);

            Nifty nifty = niftyDisplay.getNifty();
            nifty.fromXml("nifty/dialogue.xml", "main_screen", dialogueController);

            game.getGuiViewPort().addProcessor(niftyDisplay);

            System.out.println("Nifty GUI успешно инициализирован");

        } catch (Exception e) {
            System.err.println("Ошибка инициализации Nifty GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === ДИАЛОГИ ===

    public void showDialogue(String npcName, String dialogueKey) {
        if (dialogueController != null) {
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
        if (dialogueController != null) {
            dialogueController.showCombatUI();
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

    // === ОЧИСТКА ===

    public void cleanup() {
        if (niftyDisplay != null) {
            game.getGuiViewPort().removeProcessor(niftyDisplay);
            niftyDisplay = null;
        }
    }
}