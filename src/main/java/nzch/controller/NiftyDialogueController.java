package nzch.controller;


import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import nzch.Jspectergame;
import nzch.system.DialogueSystem;

/**
 * Контроллер для управления диалогами через Nifty GUI
 */
public class NiftyDialogueController implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Jspectergame game;

    private Element dialoguePanel;
    private Element npcNameText;
    private Element dialogueText;
    private Element optionsContainer;
    private Element hintText;

    private DialogueSystem.DialogueNode currentNode;
    private String currentNPCName;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("=== BIND СALLED ===");
        this.nifty = nifty;
        this.screen = screen;

        // Находим элементы
        findUIElements();

        if (dialoguePanel != null) {
            System.out.println("UI элементы успешно найдены!");
            hideDialogue();
        } else {
            System.out.println("ОШИБКА: Элементы UI не найдены после bind");
        }
    }

    private void findUIElements() {
        dialoguePanel = screen.findElementByName("dialogue_panel");
        npcNameText = screen.findElementByName("npc_name");
        dialogueText = screen.findElementByName("dialogue_text");
        optionsContainer = screen.findElementByName("options_container");
        hintText = screen.findElementByName("hint_text");

        System.out.println("Результаты поиска элементов:");
        System.out.println("- dialoguePanel: " + (dialoguePanel != null));
        System.out.println("- npcNameText: " + (npcNameText != null));
        System.out.println("- dialogueText: " + (dialogueText != null));
        System.out.println("- optionsContainer: " + (optionsContainer != null));
        System.out.println("- hintText: " + (hintText != null));

        // Дополнительная проверка - выведем все элементы экрана
        System.out.println("Все элементы на экране:");
        printAllElements(screen.getRootElement(), 0);
    }

    private void printAllElements(Element element, int depth) {
        if (element == null) return;

        String indent = "  ".repeat(depth);
        System.out.println(indent + "- " + element.getId() + " (" + element.getId() + ")");

        for (Element child : element.getChildren()) {
            printAllElements(child, depth + 1);
        }
    }

    @Override
    public void onStartScreen() {
        System.out.println("Экран main_screen активирован");
    }

    @Override
    public void onEndScreen() {
        System.out.println("Экран main_screen деактивирован");
    }

    // Обработчики для кнопок
    public void option1() {
        System.out.println("Кнопка 1 нажата");
        onOptionSelected(0);
    }

    public void option2() {
        System.out.println("Кнопка 2 нажата");
        onOptionSelected(1);
    }

    public void option3() {
        System.out.println("Кнопка 3 нажата");
        onOptionSelected(2);
    }

    public void setGame(Jspectergame game) {
        this.game = game;
    }

    public void showDialogue(String npcName, String dialogueKey) {
        System.out.println("showDialogue вызван: " + npcName + ", " + dialogueKey);

        if (dialoguePanel == null) {
            System.out.println("ОШИБКА: dialoguePanel is null - переинициализируем поиск");
            findUIElements();

            if (dialoguePanel == null) {
                System.out.println("КРИТИЧЕСКАЯ ОШИБКА: dialoguePanel все еще null после перепоиска");
                return;
            }
        }

        if (game == null) {
            System.out.println("ОШИБКА: game is null");
            return;
        }

        DialogueSystem.DialogueNode startNode = game.getDialogueSystem().getDialogue(dialogueKey);
        if (startNode != null) {
            this.currentNPCName = npcName;
            this.currentNode = startNode;
            updateDialogueDisplay();
            dialoguePanel.show();
            System.out.println("Диалог показан успешно!");
        } else {
            System.out.println("Диалог не найден для ключа: " + dialogueKey);
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

            // Скрываем неиспользуемые кнопки
            for (int i = options.length; i < 3; i++) {
                Element optionElement = screen.findElementByName("option_" + (i + 1));
                if (optionElement != null) {
                    optionElement.hide();
                }
            }
        } else {
            if (optionsContainer != null) {
                optionsContainer.hide();
            }
            setText(hintText, "[ПРОБЕЛ] продолжить");
        }
    }

    public void onOptionSelected(int optionIndex) {
        System.out.println("Выбран вариант: " + optionIndex);

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
        if (dialoguePanel != null) {
            dialoguePanel.hide();
            System.out.println("Диалог скрыт");
        }
        currentNode = null;
        currentNPCName = null;
    }

    private void setText(Element element, String text) {
        if (element != null) {
            TextRenderer renderer = element.getRenderer(TextRenderer.class);
            if (renderer != null) {
                renderer.setText(text);
            } else {
                System.out.println("TextRenderer не найден для элемента: " + element.getId());
            }
        } else {
            System.out.println("Элемент для установки текста не найден");
        }
    }

    private String extractDialogueText(String fullText) {
        return fullText.contains(":") ? fullText.split(":", 2)[1].trim() : fullText;
    }

    public boolean isDialogueVisible() {
        return dialoguePanel != null && dialoguePanel.isVisible();
    }
}