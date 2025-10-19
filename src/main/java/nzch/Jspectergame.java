package nzch;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import nzch.camera.IsometricCameraController;
import nzch.character.CombatCharacter;
import nzch.character.Enemy;
import nzch.character.NPC;
import nzch.character.PlayerCombatCharacter;
import nzch.manager.NPCManager;
import nzch.system.BattleSystem;
import nzch.system.DialogueSystem;
import nzch.system.GridSystem;
import nzch.ui.DialogueUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Jspectergame extends SimpleApplication {

    public static final int HEALTH = 50;
    public static final int ATTACK = 25;
    public static final int ARMOR = 16;
    public DialogueSystem dialogueSystem;
    private Node player;
    private Vector3f targetPosition;
    private final float playerSpeed = 8.0f;
    private IsometricCameraController cameraController;
    private Geometry targetMarker;
    private NPCManager npcManager;
    private DialogueUI dialogueUI;
    private NPC currentInteractingNPC;
    private DirectionalLightShadowRenderer shadowRenderer;
    private Timer gameTimer;
    private BattleSystem battleSystem;
    private boolean inCombat = false;
    private List<CombatCharacter> combatCharacters;
    private CombatCharacter currentTurnCharacter;
    private GridSystem gridSystem;
    private AmbientLight ambientLight;
    private DirectionalLight sunLight;
    private PointLight torchLight;
    private boolean lightingEnabled = true;

    public static void main(String[] args) {
        Jspectergame app = new Jspectergame();
        // Настройки приложения
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Изометрическая RPG"); // заголовок окна
        settings.setResolution(1280, 720);// разрешение окна
        settings.setFullscreen(false);           // оконный режим
        settings.setVSync(true);                 // вертикальная синхронизация
        settings.setSamples(4);                  // сглаживание (антиалиасинг)

        app.setSettings(settings);
        app.setShowSettings(false); // не показывать диалог настроек при запуске
        app.start();
    }

    @Override
    public void simpleInitApp() {
        combatCharacters = new ArrayList<>();
        gameTimer = new java.util.Timer();

        setupIsometricCamera();
        setupBasicLighting();
        setupLighting();
        createWorld();
        createPlayer();
        createTargetMarker();
        createSpecialLights();

        // Инициализация систем
        dialogueSystem = new DialogueSystem();
        dialogueUI = new DialogueUI(guiNode, guiFont, this);
        npcManager = new NPCManager(rootNode, assetManager, this);

        // Инициализация боевой системы
        gridSystem = new GridSystem(rootNode, assetManager, 40, 40); // Увеличиваем сетку
        battleSystem = new BattleSystem(this, gridSystem);

        // Создание NPC и врагов
        npcManager.createNPCs();
        createEnemies();

        setupMouseInput();
        setupKeyboardInput();
        setupCombatControls();

        // Включаем сетку по умолчанию для отладки
        gridSystem.toggleGrid();
    }

    public BitmapFont getGuiFont() {
        return guiFont;
    }

    public Node getGuiNode() {
        return guiNode;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public DialogueSystem getDialogueSystem() {
        return dialogueSystem;
    }

    public Timer getTimerJdk() {
        return this.gameTimer;
    }

    private void startNextTurn() {
        System.out.println("=== ЗАПРОС СЛЕДУЮЩЕГО ХОДА ===");

        currentTurnCharacter = battleSystem.getNextTurnCharacter();
        if (currentTurnCharacter == null) {
            System.out.println("Нет активных персонажей, завершаю бой");
            endCombat();
            return;
        }

        System.out.println("Текущий ход: " + currentTurnCharacter.getName() +
                " (HP: " + currentTurnCharacter.getCurrentHealth() + ")");

        // Проверяем, не мертв ли персонаж
        if (!currentTurnCharacter.isAlive()) {
            System.out.println("Персонаж мертв, ищу следующего...");
            battleSystem.endTurn(currentTurnCharacter);
            startNextTurn(); // Рекурсивно ищем следующего
            return;
        }

        battleSystem.showAvailableActions(currentTurnCharacter);

        if (currentTurnCharacter instanceof PlayerCombatCharacter) {
            System.out.println(">>> ОЖИДАНИЕ ДЕЙСТВИЙ ИГРОКА <<<");
            enablePlayerTurn();
        } else {
            System.out.println(">>> ВЫПОЛНЕНИЕ ХОДА ВРАГА <<<");
            executeEnemyTurn((Enemy) currentTurnCharacter);
        }
    }

    public void endCurrentTurn() {
        System.out.println("--- ЗАВЕРШЕНИЕ ТЕКУЩЕГО ХОДА ---");
        if (currentTurnCharacter != null) {
            battleSystem.endTurn(currentTurnCharacter);
            startNextTurn();
        }
    }

    private void setupBasicLighting() {
        // Яркое освещение чтобы избежать черного экрана
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f)); // Увеличиваем яркость
        rootNode.addLight(ambientLight);

        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        sunLight.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sunLight);

        // Убираем точечный свет если он вызывает проблемы
        torchLight = new PointLight();
        torchLight.setColor(new ColorRGBA(1.0f, 0.9f, 0.7f, 1.0f));
        torchLight.setRadius(10f);
        rootNode.addLight(torchLight);
    }

    private void createPlayer() {
        // Загружаем модель игрока
        Spatial playerModel = assetManager.loadModel("Models/character.obj");

        // Создаем материал для модели (если нужно)
        Material playerMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        playerMat.setBoolean("UseMaterialColors", true);
        playerMat.setColor("Ambient", ColorRGBA.White);
        playerMat.setColor("Diffuse", ColorRGBA.Black);
        playerModel.setMaterial(playerMat);

        // Настраиваем масштаб модели (если она слишком большая/маленькая)
        playerModel.setLocalScale(3f); // Подберите подходящий масштаб

        // Затем поднимаем по оси Y
        playerModel.setLocalTranslation(0, 1.0f, 0); // Поднимаем на 1 единицу вверх

        // Разворачиваем модель если она лежит или повернута неправильно
        setupModelOrientation(playerModel);

        // Создаем корневой узел для игрока
        player = new Node("Player");
        player.attachChild(playerModel);
        player.setLocalTranslation(0, 0, 0);

        rootNode.attachChild(player);
        targetPosition = player.getLocalTranslation();

    }

    private void setupModelOrientation(Spatial model) {
        // 1. Поворот вокруг оси X (если модель "лежит")
        model.rotate(FastMath.DEG_TO_RAD * 90, 0, 0);

        // 2. Поворот вокруг оси Y (если модель смотрит в другую сторону)
        // model.rotate(0, FastMath.DEG_TO_RAD * 180, 0);

        // 3. Комбинированный поворот
        // model.rotate(FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * 180, 0);

        // Наиболее частый случай - модель лежит на боку:
//        model.rotate(FastMath.DEG_TO_RAD * -90, 0, 0);

        // Можно также использовать кватернионы для точного поворота:
        // Quaternion rot = new Quaternion();
        // rot.fromAngles(FastMath.DEG_TO_RAD * -90, 0, 0);
        // model.setLocalRotation(rot);
    }

    // Обновляем обработку кликов для начала боя при клике на врага
    private void handleMouseClick() {
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f);
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f);
        dir.subtractLocal(click3d).normalizeLocal();

        Ray ray = new Ray(click3d, dir);
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);

        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            Geometry clickedGeo = closest.getGeometry();
            Vector3f contactPoint = closest.getContactPoint();

            // ПРОВЕРЯЕМ КЛИК НА ВРАГА ДЛЯ НАЧАЛА БОЯ
            Enemy clickedEnemy = npcManager.getEnemyAtGeometry(clickedGeo);
            if (clickedEnemy != null && !inCombat) {
                // Начинаем бой при клике на врага
                startCombatWithEnemy(clickedEnemy);
                return;
            }

            if (inCombat && currentTurnCharacter instanceof PlayerCombatCharacter) {
                handleCombatClick(clickedGeo, contactPoint);
            } else {
                handleMovementClick();
            }
        }
    }

    private void startCombatWithEnemy(Enemy clickedEnemy) {
        System.out.println("Начинаем бой с " + clickedEnemy.getName());
        startCombat();
    }

    private void handleCombatClick(Geometry clickedGeo, Vector3f contactPoint) {
        // Проверяем, кликнули ли на врага
        Enemy clickedEnemy = npcManager.getEnemyAtGeometry(clickedGeo);
        if (clickedEnemy != null && battleSystem.canAttack(currentTurnCharacter, clickedEnemy)) {
            // Атакуем врага
            battleSystem.attack(currentTurnCharacter, clickedEnemy);
            return;
        }

        // Проверяем, кликнули ли на доступную клетку для движения
        Vector3f gridPos = gridSystem.worldToGridPosition(contactPoint);
        if (gridSystem.isPositionInMovementRange(gridPos, currentTurnCharacter.getPosition(),
                currentTurnCharacter.getMovementRange())) {
            // Двигаем персонажа в ближайшую клетку
            gridPos.setX(gridPos.x - 1);
            gridPos.setZ(gridPos.z - 1);
            battleSystem.moveCharacter(currentTurnCharacter, gridPos);
            gridSystem.hideMovementRange();
        }
    }

    // Метод для получения игрока как CombatCharacter
    public PlayerCombatCharacter getPlayerCombatCharacter() {
        for (CombatCharacter character : combatCharacters) {
            if (character instanceof PlayerCombatCharacter) {
                return (PlayerCombatCharacter) character;
            }
        }
        return null;
    }

    private void setupLighting() {
        // 1. Ambient Light (рассеянное освещение)
        ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
        rootNode.addLight(ambientLight);

        // 2. Directional Light (солнечный свет)
        sunLight = new DirectionalLight();
        sunLight.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        sunLight.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sunLight);

        // 3. Point Light (источник света у игрока - как факел)
        torchLight = new PointLight();
        torchLight.setColor(new ColorRGBA(1.0f, 0.9f, 0.7f, 1.0f));
        torchLight.setRadius(10f);
        rootNode.addLight(torchLight);

        // Настраиваем тени (опционально)
        setupAdvancedLighting();
    }

    private void setupAdvancedLighting() {
        // Базовая настройка света (как ранее)
        ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
        rootNode.addLight(ambientLight);

        sunLight = new DirectionalLight();
        sunLight.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        sunLight.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sunLight);

        // Настройка рендерера теней
        shadowRenderer = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
        shadowRenderer.setLight(sunLight);
        shadowRenderer.setShadowIntensity(0.5f);
        viewPort.addProcessor(shadowRenderer);

        // Для объектов, которые должны отбрасывать тени:
        // geometry.setShadowMode(ShadowMode.Cast);
        // Для объектов, которые должны принимать тени:
        // geometry.setShadowMode(ShadowMode.Receive);
    }

    private void createSpecialLights() {
        // Добавляем огонь у костра
        createCampfire(-10, 0, 8);

        // Добавляем свет в зданиях
        createBuildingLight(-15, 2, -12);
        createBuildingLight(20, 2, 18);
    }

    private void createCampfire(float x, float y, float z) {
        // Создаем костер
        Sphere fire = new Sphere(16, 16, 0.5f);
        Geometry fireGeo = new Geometry("Campfire", fire);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        fireMat.setBoolean("UseMaterialColors", true);
        fireMat.setColor("Ambient", ColorRGBA.Orange);
        fireMat.setColor("Diffuse", ColorRGBA.Orange);
        fireMat.setColor("Specular", ColorRGBA.Yellow);
        fireMat.setFloat("Shininess", 128f);
        fireGeo.setMaterial(fireMat);
        fireGeo.setLocalTranslation(x, y + 0.5f, z);
        fireGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(fireGeo);

        // Добавляем свет от костра
        PointLight fireLight = new PointLight();
        fireLight.setColor(new ColorRGBA(1.0f, 0.5f, 0.2f, 1.0f));
        fireLight.setRadius(8f);
        fireLight.setPosition(new Vector3f(x, y + 1f, z));
        rootNode.addLight(fireLight);
    }

    private void createBuildingLight(float x, float y, float z) {
        // Свет внутри здания
        PointLight buildingLight = new PointLight();
        buildingLight.setColor(new ColorRGBA(1.0f, 0.9f, 0.7f, 1.0f));
        buildingLight.setRadius(6f);
        buildingLight.setPosition(new Vector3f(x, y + 3f, z));
        rootNode.addLight(buildingLight);
    }

    // Обновляем метод создания земли с использованием освещаемых материалов
    private void createGround() {
        // Создаем землю с освещаемым материалом
        Box ground = new Box(50, 0.1f, 50);
        Geometry groundGeo = new Geometry("Ground", ground);

        // Используем Lighting material вместо Unshaded
        Material groundMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        groundMat.setBoolean("UseMaterialColors", true);
        groundMat.setColor("Ambient", new ColorRGBA(0.2f, 0.6f, 0.2f, 1.0f));
        groundMat.setColor("Diffuse", new ColorRGBA(0.2f, 0.6f, 0.2f, 1.0f));
        groundMat.setColor("Specular", new ColorRGBA(0.2f, 0.6f, 0.2f, 1.0f));
        groundMat.setFloat("Shininess", 8f);

        groundGeo.setMaterial(groundMat);
        groundGeo.setLocalTranslation(0, -1, 0);

        // Устанавливаем отбрасывание теней
        groundGeo.setShadowMode(RenderQueue.ShadowMode.Receive);
        rootNode.attachChild(groundGeo);

        createGrid();
    }

    // Обновляем метод создания деревьев
    private void createTree(float x, float y, float z) {
        Node tree = new Node("Tree");

        // Ствол дерева
        Box trunk = new Box(0.3f, 1.5f, 0.3f);
        Geometry trunkGeo = new Geometry("Trunk", trunk);
        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        trunkMat.setColor("Ambient", new ColorRGBA(0.4f, 0.2f, 0.1f, 1.0f));
        trunkMat.setColor("Diffuse", new ColorRGBA(0.4f, 0.2f, 0.1f, 1.0f));
        trunkMat.setColor("Specular", new ColorRGBA(0.2f, 0.1f, 0.05f, 1.0f));
        trunkMat.setFloat("Shininess", 4f);
        trunkGeo.setMaterial(trunkMat);
        trunkGeo.setLocalTranslation(0, 1.5f, 0);
        trunkGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Крона дерева
        Sphere leaves = new Sphere(16, 16, 1.8f);
        Geometry leavesGeo = new Geometry("Leaves", leaves);
        Material leavesMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        leavesMat.setBoolean("UseMaterialColors", true);
        leavesMat.setColor("Ambient", new ColorRGBA(0.1f, 0.8f, 0.1f, 1.0f));
        leavesMat.setColor("Diffuse", new ColorRGBA(0.1f, 0.8f, 0.1f, 1.0f));
        leavesMat.setColor("Specular", new ColorRGBA(0.3f, 1.0f, 0.3f, 1.0f));
        leavesMat.setFloat("Shininess", 8f);
        leavesGeo.setMaterial(leavesMat);
        leavesGeo.setLocalTranslation(0, 3.5f, 0);
        leavesGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        tree.attachChild(trunkGeo);
        tree.attachChild(leavesGeo);
        tree.setLocalTranslation(x, y, z);
        rootNode.attachChild(tree);
    }

    // Обновляем метод создания зданий
    private void createBuilding(float x, float y, float z) {
        Box building = new Box(3f, 4f, 3f);
        Geometry buildingGeo = new Geometry("Building", building);
        Material buildingMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        buildingMat.setBoolean("UseMaterialColors", true);
        buildingMat.setColor("Ambient", new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        buildingMat.setColor("Diffuse", new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        buildingMat.setColor("Specular", new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        buildingMat.setFloat("Shininess", 32f);
        buildingGeo.setMaterial(buildingMat);
        buildingGeo.setLocalTranslation(x, 2f, z);
        buildingGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(buildingGeo);
    }


    // Добавляем управление освещением
    private void setupLightingControls() {
        inputManager.addMapping("ToggleLighting", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("CycleTime", new KeyTrigger(KeyInput.KEY_T));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                switch (name) {
                    case "ToggleLighting":
                        toggleLighting();
                        break;
                    case "CycleTime":
                        cycleTimeOfDay();
                        break;
                }
            }
        }, "ToggleLighting", "CycleTime");
    }

    private void toggleLighting() {
        lightingEnabled = !lightingEnabled;

        if (lightingEnabled) {
            rootNode.addLight(ambientLight);
            rootNode.addLight(sunLight);
            rootNode.addLight(torchLight);
        } else {
            rootNode.removeLight(ambientLight);
            rootNode.removeLight(sunLight);
            rootNode.removeLight(torchLight);
        }
    }

    private void cycleTimeOfDay() {
        // Меняем время суток - вращаем направленный свет
        float currentTime = (float) Math.random(); // Простая реализация
        float sunAngle = currentTime * FastMath.TWO_PI;

        sunLight.setDirection(new Vector3f(
                FastMath.cos(sunAngle) * 2f,
                -1f + FastMath.sin(sunAngle) * 0.5f,
                FastMath.sin(sunAngle) * 2f
        ).normalizeLocal());

        // Меняем цвет в зависимости от времени
        if (currentTime < 0.25f || currentTime > 0.75f) {
            // Ночь
            sunLight.setColor(new ColorRGBA(0.2f, 0.2f, 0.5f, 1.0f));
            ambientLight.setColor(new ColorRGBA(0.1f, 0.1f, 0.2f, 1.0f));
        } else if (currentTime < 0.3f || currentTime > 0.7f) {
            // Утро/Вечер
            sunLight.setColor(new ColorRGBA(1.0f, 0.6f, 0.3f, 1.0f));
            ambientLight.setColor(new ColorRGBA(0.4f, 0.3f, 0.2f, 1.0f));
        } else {
            // День
            sunLight.setColor(new ColorRGBA(1.0f, 1.0f, 0.9f, 1.0f));
            ambientLight.setColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        }
    }

    private void setupIsometricCamera() {
        cameraController = new IsometricCameraController(cam);
        flyCam.setEnabled(false);
    }

    private void createWorld() {
        createGround();
        createEnvironment();
    }

    private void createGrid() {
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.setColor("Color", new ColorRGBA(0.5f, 0.5f, 0.5f, 0.3f));

        for (int i = -50; i <= 50; i += 2) {
            Box lineX = new Box(50, 0.01f, 0.02f);
            Geometry lineXGeo = new Geometry("LineX", lineX);
            lineXGeo.setMaterial(gridMat);
            lineXGeo.setLocalTranslation(0, -0.9f, i);
            rootNode.attachChild(lineXGeo);

            Box lineZ = new Box(0.02f, 0.01f, 50);
            Geometry lineZGeo = new Geometry("LineZ", lineZ);
            lineZGeo.setMaterial(gridMat);
            lineZGeo.setLocalTranslation(i, -0.9f, 0);
            rootNode.attachChild(lineZGeo);
        }
    }

    private void createEnvironment() {
        for (int i = 0; i < 10; i++) {
            createTree(
                    (float) (Math.random() * 60 - 30),
                    0,
                    (float) (Math.random() * 60 - 30)
            );
        }

        createBuilding(-15, 0, -12);
        createBuilding(20, 0, 18);
    }

    private void createTargetMarker() {
        Sphere marker = new Sphere(16, 16, 0.2f);
        targetMarker = new Geometry("TargetMarker", marker);
        Material markerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        markerMat.setColor("Color", ColorRGBA.Red);
        markerMat.getAdditionalRenderState().setWireframe(true);
        targetMarker.setMaterial(markerMat);
        targetMarker.setLocalTranslation(0, -10, 0);
        rootNode.attachChild(targetMarker);
    }

    private void setupMouseInput() {
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Interact", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (name.equals("Click") && isPressed && !dialogueUI.isVisible()) {
                handleMouseClick();
            }
            if (name.equals("Interact") && isPressed && !dialogueUI.isVisible()) {
                handleInteraction();
            }
        }, "Click", "Interact");
    }

    private void setupKeyboardInput() {
        inputManager.addMapping("Space", new com.jme3.input.controls.KeyTrigger(
                com.jme3.input.KeyInput.KEY_SPACE));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (name.equals("Space") && isPressed) {
                if (dialogueUI.isVisible()) {
                    dialogueUI.nextDialogue();
                } else {
                    handleInteraction();
                }
            }
        }, "Space");

        // Добавляем выбор вариантов диалога цифрами
        for (int i = 1; i <= 9; i++) {
            final int optionIndex = i - 1;
            inputManager.addMapping("Option" + i, new com.jme3.input.controls.KeyTrigger(
                    com.jme3.input.KeyInput.KEY_1 + optionIndex));

            int finalI = i;
            inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
                if (name.equals("Option" + finalI) && isPressed && dialogueUI.isVisible()) {
                    dialogueUI.selectOption(optionIndex);
                }
            }, "Option" + i);
        }
    }

    private void handleInteraction() {
        // Проверяем NPC в радиусе взаимодействия
        NPC nearestNPC = npcManager.findNearestNPC(player.getLocalTranslation(), 3.0f);

        if (nearestNPC != null) {
            startDialogue(nearestNPC);
        }
    }

    public void startDialogue(NPC npc) {
        currentInteractingNPC = npc;
        dialogueUI.showDialogue(npc.getDialogue());
        targetPosition = player.getLocalTranslation(); // Останавливаем движение
    }

    public void endDialogue() {
        currentInteractingNPC = null;
        dialogueUI.hide();
    }

    private void movePlayerToTarget(float tpf) {
        Vector3f currentPos = player.getLocalTranslation();
        Vector3f direction = targetPosition.subtract(currentPos);

        if (direction.length() > 0.1f) {
            direction.setY(0);
            direction.normalizeLocal();

            player.lookAt(targetPosition, Vector3f.UNIT_Y);

            Vector3f movement = direction.mult(playerSpeed * tpf);

            if (movement.length() > targetPosition.subtract(currentPos).length()) {
                player.setLocalTranslation(targetPosition);
            } else {
                player.move(movement);
            }
        }
    }

    private void createEnemies() {
        // Создаем несколько врагов
        createEnemy("Гоблин", new Vector3f(8, 0, 8), 30, 12, 15);
        createEnemy("Орк", new Vector3f(-6, 0, 10), 45, 16, 12);
        createEnemy("Скелет", new Vector3f(12, 0, -4), 25, 14, 18);
    }

    private void createEnemy(String name, Vector3f position, int health, int attack, int armor) {
        Enemy enemy = new Enemy(name, position, assetManager);
        enemy.setStats(health, attack, armor);
        combatCharacters.add(enemy);

        // Добавляем врага в менеджер NPC для отображения
        npcManager.addEnemy(enemy);
    }

    private void setupCombatControls() {
        inputManager.addMapping("StartCombat", new com.jme3.input.controls.KeyTrigger(
                com.jme3.input.KeyInput.KEY_C));
        inputManager.addMapping("EndTurn", new com.jme3.input.controls.KeyTrigger(
                KeyInput.KEY_SPACE));
        inputManager.addMapping("ShowGrid", new com.jme3.input.controls.KeyTrigger(
                com.jme3.input.KeyInput.KEY_G));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                switch (name) {
                    case "StartCombat":
                        if (!inCombat) {
                            startCombat();
                        }
                        break;
                    case "EndTurn":
                        if (inCombat) {
                            endCurrentTurn();
                        }
                        break;
                    case "ShowGrid":
                        gridSystem.toggleGrid();
                        break;
                }
            }
        }, "StartCombat", "EndTurn", "ShowGrid");
    }

    public void startCombat() {
        inCombat = true;
        // Создаем боевую группу
        List<CombatCharacter> playerTeam = new ArrayList<>();
        List<CombatCharacter> enemyTeam = new ArrayList<>();
        // Добавляем игрока в команду
        PlayerCombatCharacter playerCombat = new PlayerCombatCharacter(player, "Игрок", HEALTH, ATTACK, ARMOR);
        playerTeam.add(playerCombat);
        combatCharacters.add(playerCombat);
        // Добавляем врагов в команду
        for (CombatCharacter character : combatCharacters) {
            if (character instanceof Enemy) {
                enemyTeam.add(character);
            }
        }
        // Убедимся что есть враги для боя
        if (enemyTeam.isEmpty()) {
            System.out.println("Нет врагов для боя!");
            inCombat = false;
            return;
        }
        battleSystem.startBattle(playerTeam, enemyTeam);
        battleSystem.showBattleUI();
        // Начинаем первый ход
        startNextTurn();
    }

    private void enablePlayerTurn() {
        // Активируем управление для игрока
        gridSystem.highlightMovementRange(currentTurnCharacter.getPosition(),
                currentTurnCharacter.getMovementRange());
    }

    private void endCombat() {
        inCombat = false;
        battleSystem.hideBattleUI();
        gridSystem.hideAllHighlights();

        // Очищаем мертвых персонажей
        combatCharacters.removeIf(character -> character.getCurrentHealth() <= 0);

        // Удаляем мертвых врагов из сцены
        for (CombatCharacter character : new ArrayList<>(combatCharacters)) {
            if (character.getCurrentHealth() <= 0 && character instanceof Enemy) {
                npcManager.removeEnemy((Enemy) character);
                combatCharacters.remove(character);
            }
        }
    }

    private void handleMovementClick() {
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f);
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f);
        dir.subtractLocal(click3d).normalizeLocal();

        Ray ray = new Ray(click3d, dir);
        CollisionResults results = new CollisionResults();
        rootNode.collideWith(ray, results);

        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            Vector3f contactPoint = closest.getContactPoint();

            targetPosition = new Vector3f(contactPoint.x, 0, contactPoint.z);
            targetMarker.setLocalTranslation(targetPosition.add(0, 0.1f, 0));
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Обновляем боевую систему каждый кадр
        if (inCombat) {
            battleSystem.update(tpf);
        } else if (!dialogueUI.isVisible()) {
            movePlayerToTarget(tpf);
        }

        cameraController.setTarget(player.getLocalTranslation());
        cameraController.updateCamera();

        npcManager.updateInteractionIndicators(player.getLocalTranslation());
    }

    private void executeEnemyTurn(Enemy enemy) {
        System.out.println("Выполнение хода врага: " + enemy.getName());
        // Выполняем ход немедленно в основном потоке
        enemy.executeAITurn(battleSystem);
    }
}