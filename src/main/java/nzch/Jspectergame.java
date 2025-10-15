package nzch;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
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
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import nzch.camera.IsometricCameraController;
import nzch.manager.NPCManager;
import nzch.systems.DialogueSystem;
import nzch.systems.NPC;
import nzch.ui.DialogueUI;

public class Jspectergame extends SimpleApplication {

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

    public static void main(String[] args) {
        Jspectergame app = new Jspectergame();
        app.start();
    }

    // Добавляем поля для освещения
    private AmbientLight ambientLight;
    private DirectionalLight sunLight;
    private PointLight torchLight;
    private boolean lightingEnabled = true;

    @Override
    public void simpleInitApp() {
        // Настройка изометрической камеры
        setupIsometricCamera();

        // Настройка освещения ДО создания объектов
        setupLighting();

        // Создание игрового мира
        createWorld();

        // Создание игрока
        createPlayer();

        // Создание маркера цели
        createTargetMarker();

        // Создаем специальные источники света
        createSpecialLights();

        // Инициализация систем
        dialogueSystem = new DialogueSystem();
        dialogueUI = new DialogueUI(guiNode, guiFont, this);
        npcManager = new NPCManager(rootNode, assetManager, this);

        // Создание NPC
        npcManager.createNPCs();

        // Настройка управления
        setupMouseInput();
        setupKeyboardInput();
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

    // Обновляем метод создания игрока
    private void createPlayer() {
        player = new Node("Player");

        // Тело игрока с освещаемым материалом
        Box body = new Box(0.4f, 0.8f, 0.4f);
        Geometry bodyGeo = new Geometry("Body", body);
        Material bodyMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        bodyMat.setBoolean("UseMaterialColors", true);
        bodyMat.setColor("Ambient", ColorRGBA.Blue);
        bodyMat.setColor("Diffuse", ColorRGBA.Blue);
        bodyMat.setColor("Specular", ColorRGBA.White);
        bodyMat.setFloat("Shininess", 16f);
        bodyGeo.setMaterial(bodyMat);
        bodyGeo.setLocalTranslation(0, 0.8f, 0);
        bodyGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Голова игрока
        Sphere head = new Sphere(16, 16, 0.3f);
        Geometry headGeo = new Geometry("Head", head);
        Material headMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        headMat.setBoolean("UseMaterialColors", true);
        headMat.setColor("Ambient", ColorRGBA.Yellow);
        headMat.setColor("Diffuse", ColorRGBA.Yellow);
        headMat.setColor("Specular", ColorRGBA.White);
        headMat.setFloat("Shininess", 32f);
        headGeo.setMaterial(headMat);
        headGeo.setLocalTranslation(0, 1.8f, 0);
        headGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        player.attachChild(bodyGeo);
        player.attachChild(headGeo);
        player.setLocalTranslation(0, 0, 0);
        rootNode.attachChild(player);

        targetPosition = player.getLocalTranslation();
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

    // Обновляем метод update для движения света с игроком
    @Override
    public void simpleUpdate(float tpf) {
        if (!dialogueUI.isVisible()) {
            movePlayerToTarget(tpf);
        }

        cameraController.setTarget(player.getLocalTranslation());
        cameraController.updateCamera();

        // Обновляем позицию источника света (факела) с игроком
        if (torchLight != null) {
            Vector3f playerPos = player.getLocalTranslation();
            torchLight.setPosition(new Vector3f(playerPos.x, playerPos.y + 2f, playerPos.z));
        }

        // Обновляем индикаторы взаимодействия
        npcManager.updateInteractionIndicators(player.getLocalTranslation());
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
            Vector3f contactPoint = closest.getContactPoint();

            targetPosition = new Vector3f(contactPoint.x, 0, contactPoint.z);
            targetMarker.setLocalTranslation(targetPosition.add(0, 0.1f, 0));
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
}