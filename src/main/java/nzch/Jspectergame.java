package nzch;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
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

    public static void main(String[] args) {
        Jspectergame app = new Jspectergame();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Настройка изометрической камеры
        setupIsometricCamera();

        // Создание игрового мира
        createWorld();

        // Создание игрока
        createPlayer();

        // Создание маркера цели
        createTargetMarker();

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

    private void setupIsometricCamera() {
        cameraController = new IsometricCameraController(cam);
        flyCam.setEnabled(false);
    }

    private void createWorld() {
        createGround();
        createEnvironment();
    }

    private void createGround() {
        Box ground = new Box(50, 0.1f, 50);
        Geometry groundGeo = new Geometry("Ground", ground);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        groundMat.setColor("Color", new ColorRGBA(0.2f, 0.6f, 0.2f, 1.0f));
        groundGeo.setMaterial(groundMat);
        groundGeo.setLocalTranslation(0, -1, 0);
        rootNode.attachChild(groundGeo);

        createGrid();
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

    private void createTree(float x, float y, float z) {
        Node tree = new Node("Tree");

        Box trunk = new Box(0.3f, 1.5f, 0.3f);
        Geometry trunkGeo = new Geometry("Trunk", trunk);
        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color", new ColorRGBA(0.4f, 0.2f, 0.1f, 1.0f));
        trunkGeo.setMaterial(trunkMat);
        trunkGeo.setLocalTranslation(0, 1.5f, 0);

        Sphere leaves = new Sphere(16, 16, 1.8f);
        Geometry leavesGeo = new Geometry("Leaves", leaves);
        Material leavesMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        leavesMat.setColor("Color", new ColorRGBA(0.1f, 0.8f, 0.1f, 1.0f));
        leavesGeo.setMaterial(leavesMat);
        leavesGeo.setLocalTranslation(0, 3.5f, 0);

        tree.attachChild(trunkGeo);
        tree.attachChild(leavesGeo);
        tree.setLocalTranslation(x, y, z);
        rootNode.attachChild(tree);
    }

    private void createBuilding(float x, float y, float z) {
        Box building = new Box(3f, 4f, 3f);
        Geometry buildingGeo = new Geometry("Building", building);
        Material buildingMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        buildingMat.setColor("Color", new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        buildingGeo.setMaterial(buildingMat);
        buildingGeo.setLocalTranslation(x, 2f, z);
        rootNode.attachChild(buildingGeo);
    }

    private void createPlayer() {
        player = new Node("Player");

        Box body = new Box(0.4f, 0.8f, 0.4f);
        Geometry bodyGeo = new Geometry("Body", body);
        Material bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bodyMat.setColor("Color", ColorRGBA.Blue);
        bodyGeo.setMaterial(bodyMat);
        bodyGeo.setLocalTranslation(0, 0.8f, 0);

        Sphere head = new Sphere(16, 16, 0.3f);
        Geometry headGeo = new Geometry("Head", head);
        Material headMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        headMat.setColor("Color", ColorRGBA.Yellow);
        headGeo.setMaterial(headMat);
        headGeo.setLocalTranslation(0, 1.8f, 0);

        player.attachChild(bodyGeo);
        player.attachChild(headGeo);
        player.setLocalTranslation(0, 0, 0);
        rootNode.attachChild(player);

        targetPosition = player.getLocalTranslation();
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

    @Override
    public void simpleUpdate(float tpf) {
        if (!dialogueUI.isVisible()) {
            movePlayerToTarget(tpf);
        }

        cameraController.setTarget(player.getLocalTranslation());
        cameraController.updateCamera();

        // Обновляем индикаторы взаимодействия
        npcManager.updateInteractionIndicators(player.getLocalTranslation());
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