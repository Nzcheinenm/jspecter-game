package nzch.system;

import java.util.HashMap;
import java.util.Map;

public class DialogueSystem {
    private final Map<String, DialogueNode> dialogues;

    public DialogueSystem() {
        dialogues = new HashMap<>();
        initializeDialogues();
    }

    private void initializeDialogues() {
        // Диалог стражника
        DialogueNode guard1 = new DialogueNode("Стражник: Стой! Кто идет?",
                new String[]{"Я дружелюбный путник", "Пропусти меня, стражник", "Мне нужна помощь"});

        DialogueNode guard2a = new DialogueNode("Стражник: Рад слышать. Будь осторожен в этих землях.", null);
        DialogueNode guard2b = new DialogueNode("Стражник: Сначала скажи пароль!",
                new String[]{"Какой пароль?", "Я забыл пароль"});
        DialogueNode guard2c = new DialogueNode("Стражник: В чем дело, путник?",
                new String[]{"Я ищу древний артефакт", "Мне нужны припасы"});

        DialogueNode guard3b1 = new DialogueNode("Стражник: Пароль - 'дракон'. Запомни это!", null);
        DialogueNode guard3b2 = new DialogueNode("Стражник: Тогда возвращайся, когда вспомнишь!", null);
        DialogueNode guard3c1 = new DialogueNode("Стражник: Артефакт? Попробуй спросить у волшебника.", null);
        DialogueNode guard3c2 = new DialogueNode("Стражник: У торговца есть всё необходимое.", null);

        guard1.setNextNode(0, guard2a);
        guard1.setNextNode(1, guard2b);
        guard1.setNextNode(2, guard2c);

        guard2b.setNextNode(0, guard3b1);
        guard2b.setNextNode(1, guard3b2);
        guard2c.setNextNode(0, guard3c1);
        guard2c.setNextNode(1, guard3c2);

        dialogues.put("guard_dialogue", guard1);

        // Диалог торговца
        DialogueNode merchant1 = new DialogueNode("Торговец: Добро пожаловать! Что желаете?",
                new String[]{"Покажи свой товар", "У меня нет денег", "Как дела?"});

        DialogueNode merchant2a = new DialogueNode("Торговец: У меня есть зелья здоровья и маны. Выбирай!", null);
        DialogueNode merchant2b = new DialogueNode("Торговец: Тогда возвращайся, когда разбогатеешь!", null);
        DialogueNode merchant2c = new DialogueNode("Торговец: Дела идут хорошо! Город процветает.", null);

        merchant1.setNextNode(0, merchant2a);
        merchant1.setNextNode(1, merchant2b);
        merchant1.setNextNode(2, merchant2c);

        dialogues.put("merchant_dialogue", merchant1);
    }

    public DialogueNode getDialogue(String key) {
        return dialogues.get(key);
    }

    public static class DialogueNode {
        private final String text;
        private final String[] options;
        private DialogueNode[] nextNodes;

        public DialogueNode(String text, String[] options) {
            this.text = text;
            this.options = options;
            if (options != null) {
                this.nextNodes = new DialogueNode[options.length];
            }
        }

        public void setNextNode(int index, DialogueNode node) {
            if (nextNodes != null && index < nextNodes.length) {
                nextNodes[index] = node;
            }
        }

        public String getText() {
            return text;
        }

        public String[] getOptions() {
            return options;
        }

        public DialogueNode getNextNode(int index) {
            return (nextNodes != null && index < nextNodes.length) ? nextNodes[index] : null;
        }

        public boolean hasOptions() {
            return options != null && options.length > 0;
        }
    }
}