package nzch.manager;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.*;
import nzch.Jspectergame;
import nzch.controller.NiftyDialogueController;

/**
 * Менеджер для управления Nifty GUI
 */
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
            System.out.println("=== ИНИЦИАЛИЗАЦИЯ NIFTY GUI (ПРОГРАММНО) ===");

            // Создаем Nifty display
            niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                    game.getAssetManager(),
                    game.getInputManager(),
                    game.getAudioRenderer(),
                    game.getGuiViewPort()
            );

            Nifty nifty = niftyDisplay.getNifty();

            // Создаем контроллер
            dialogueController = new NiftyDialogueController();
            dialogueController.setGame(game);

            // Регистрируем контроллер
            nifty.registerScreenController(dialogueController);

            // Создаем экран программно
            createScreenProgrammatically(nifty);

            // Переходим на созданный экран
            nifty.gotoScreen("main_screen");

            // Добавляем Nifty в viewport
            game.getGuiViewPort().addProcessor(niftyDisplay);

            System.out.println("Nifty GUI успешно инициализирован программно");

        } catch (Exception e) {
            System.err.println("ОШИБКА инициализации Nifty GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createScreenProgrammatically(Nifty nifty) {
        System.out.println("Создаем экран программно...");

        new ScreenBuilder("main_screen") {{
            controller(dialogueController);

            layer(new LayerBuilder("dialogue_layer") {{
                childLayoutCenter();

                panel(new PanelBuilder("dialogue_panel") {{
                    width("70%");
                    height("35%");
                    backgroundColor("#1a1a2ecc");
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    visible(false);

                    // Имя NPC
                    text(new TextBuilder("npc_name") {{
                        text("NPC");
                        color("#ffff00ff");
                        height("15%");
                        font("Interface/Fonts/GoodHeadPro-Medium.ttf");
                        textHAlignLeft();
                    }});

                    // Текст диалога
                    panel(new PanelBuilder() {{
                        height("45%");
                        width("100%");
                        childLayoutCenter();
                        marginBottom("10px");

                        text(new TextBuilder("dialogue_text") {{
                            text("Текст диалога...");
                            color("#ffffffff");
                            width("95%");
                            wrap(true);
                            font("Interface/Fonts/GoodHeadPro-Medium.ttf");
                            textHAlignLeft();
                        }});
                    }});

                    // Контейнер опций
                    panel(new PanelBuilder("options_container") {{
                        height("30%");
                        width("100%");
                        childLayoutVertical();
                        visible(false);

                        // Кнопки опций
                        control(new ControlBuilder("option_1", "button") {{
                            width("100%");
                            height("25%");
                            interactOnClick("option1()");
                        }});

                        control(new ControlBuilder("option_2", "button") {{
                            width("100%");
                            height("25%");
                            interactOnClick("option2()");
                        }});

                        control(new ControlBuilder("option_3", "button") {{
                            width("100%");
                            height("25%");
                            interactOnClick("option3()");
                        }});
                    }});

                    // Подсказка
                    panel(new PanelBuilder() {{
                        height("10%");
                        width("100%");
                        childLayoutCenter();

                        text(new TextBuilder("hint_text") {{
                            text("[ПРОБЕЛ] продолжить");
                            color("#00ff00ff");
                            font("Interface/Fonts/GoodHeadPro-Medium.ttf");
                            textHAlignCenter();
                        }});
                    }});
                }});
            }});

        }}.build(nifty);

        System.out.println("Экран создан программно");
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

    public void cleanup() {
        if (niftyDisplay != null) {
            game.getGuiViewPort().removeProcessor(niftyDisplay);
            niftyDisplay = null;
        }
    }
}