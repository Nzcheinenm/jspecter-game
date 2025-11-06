package nzch.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import nzch.Jspectergame;
import nzch.manager.NiftyUIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для главного меню игры
 */
public class MainMenuController implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Jspectergame game;
    private NiftyUIManager uiManager;

    // Элементы настроек
    private DropDown<String> resolutionDropdown;
    private DropDown<String> qualityDropdown;
    private CheckBox fullscreenCheckbox;
    private Slider musicSlider;
    private Slider sfxSlider;

    public MainMenuController() {
    }

    public void setGame(Jspectergame game) {
        this.game = game;
    }

    public void setUIManager(NiftyUIManager uiManager) {
        this.uiManager = uiManager;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        if (screen.getScreenId().equals("settings_screen")) {
            initializeSettingsScreen();
        }
    }

    @Override
    public void onStartScreen() {
        System.out.println("Запущен экран: " + screen.getScreenId());
    }

    @Override
    public void onEndScreen() {
    }

    // === ОСНОВНЫЕ КНОПКИ ГЛАВНОГО МЕНЮ ===
    public void startGame() {
        System.out.println("Запуск новой игры...");

        if (game != null) {
            // Скрываем главное меню
            nifty.gotoScreen("main_screen"); // Переходим к игровому экрану

            // Инициализируем игру если нужно
            uiManager.hideMainMenu();

            // Можно добавить загрузочный экран здесь
//            showLoadingScreen();
        }
    }

    public void loadGame() {
        System.out.println("Загрузка игры...");
        // TODO: Реализовать загрузку сохраненной игры
        showMessage("Система сохранений будет добавлена в будущем обновлении");
    }

    public void showSettings() {
        System.out.println("Открытие настроек...");
        nifty.gotoScreen("settings_screen");
    }

    public void exitGame() {
        System.out.println("Выход из игры...");
        if (game != null) {
            game.stop(); // Останавливаем игру
        }
    }

    // === НАСТРОЙКИ ===

    private void initializeSettingsScreen() {
        // Получаем элементы управления настройками
        resolutionDropdown = screen.findNiftyControl("resolution_dropdown", DropDown.class);
        qualityDropdown = screen.findNiftyControl("quality_dropdown", DropDown.class);
        fullscreenCheckbox = screen.findNiftyControl("fullscreen_checkbox", CheckBox.class);
        musicSlider = screen.findNiftyControl("music_slider", Slider.class);
        sfxSlider = screen.findNiftyControl("sfx_slider", Slider.class);

        // Заполняем выпадающие списки
        initializeResolutionDropdown();
        initializeQualityDropdown();

        // Загружаем текущие настройки
        loadCurrentSettings();
    }

    private void initializeResolutionDropdown() {
        if (resolutionDropdown != null) {
            resolutionDropdown.clear();

            List<String> resolutions = new ArrayList<>();
            resolutions.add("1280x720");
            resolutions.add("1366x768");
            resolutions.add("1920x1080");
            resolutions.add("2560x1440");

            for (String resolution : resolutions) {
                resolutionDropdown.addItem(resolution);
            }

            // Устанавливаем текущее разрешение по умолчанию
            resolutionDropdown.selectItem("1280x720");
        }
    }

    private void initializeQualityDropdown() {
        if (qualityDropdown != null) {
            qualityDropdown.clear();

            List<String> qualities = new ArrayList<>();
            qualities.add("Низкое");
            qualities.add("Среднее");
            qualities.add("Высокое");
            qualities.add("Ультра");

            for (String quality : qualities) {
                qualityDropdown.addItem(quality);
            }

            // Устанавливаем текущее качество по умолчанию
            qualityDropdown.selectItem("Высокое");
        }
    }

    private void loadCurrentSettings() {
        // Здесь можно загрузить сохраненные настройки
        if (fullscreenCheckbox != null) {
            fullscreenCheckbox.setChecked(false);
        }

        if (musicSlider != null) {
            musicSlider.setValue(80.0f); // 80% громкость
        }

        if (sfxSlider != null) {
            sfxSlider.setValue(70.0f); // 70% громкость
        }
    }

    public void saveSettings() {
        System.out.println("Сохранение настроек...");

        // Получаем выбранные значения
        String resolution = resolutionDropdown.getSelection();
        String quality = qualityDropdown.getSelection();
        boolean fullscreen = fullscreenCheckbox.isChecked();
        float musicVolume = musicSlider.getValue();
        float sfxVolume = sfxSlider.getValue();

        // Применяем настройки
        applySettings(resolution, quality, fullscreen, musicVolume, sfxVolume);

        showMessage("Настройки сохранены!");
        backToMainMenu();
    }

    public void backToMainMenu() {
        nifty.gotoScreen("main_menu");
    }

    private void applySettings(String resolution, String quality, boolean fullscreen,
                               float musicVolume, float sfxVolume) {
        System.out.println("Применение настроек:");
        System.out.println("Разрешение: " + resolution);
        System.out.println("Качество: " + quality);
        System.out.println("Полный экран: " + fullscreen);
        System.out.println("Громкость музыки: " + musicVolume);
        System.out.println("Громкость эффектов: " + sfxVolume);

        // TODO: Реализовать применение настроек к игре
        if (game != null) {
            // game.applyGraphicsSettings(resolution, quality, fullscreen);
            // game.applyAudioSettings(musicVolume, sfxVolume);
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private void showLoadingScreen() {
        // Можно добавить красивый загрузочный экран
        System.out.println("Загрузка игрового мира...");

        // Имитация загрузки
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 секунды загрузки

                // После загрузки скрываем меню и показываем игру
                if (uiManager != null) {
                    uiManager.hideMainMenu();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showMessage(String message) {
        System.out.println("Сообщение: " + message);
        // TODO: Реализовать красивый popup для сообщений
    }

    public boolean isMainMenuVisible() {
        return nifty.getCurrentScreen().getScreenId().equals("main_menu");
    }
}