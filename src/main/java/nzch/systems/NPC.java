package nzch.systems;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

public class NPC {
    private Node npcNode;
    private final String name;
    private final String dialogueKey;
    private Geometry interactionIndicator;
    private final Vector3f position;

    public NPC(String name, String dialogueKey, Vector3f position, AssetManager assetManager) {
        this.name = name;
        this.dialogueKey = dialogueKey;
        this.position = position;

        createNPC(assetManager);
        createInteractionIndicator(assetManager);
    }

    private void createNPC(AssetManager assetManager) {
        npcNode = new Node("NPC_" + name);

        // Тело NPC с освещаемым материалом
        Box body = new Box(0.4f, 0.8f, 0.4f);
        Geometry bodyGeo = new Geometry("Body", body);
        Material bodyMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        bodyMat.setBoolean("UseMaterialColors", true);
        bodyMat.setColor("Ambient", ColorRGBA.Red);
        bodyMat.setColor("Diffuse", ColorRGBA.Red);
        bodyMat.setColor("Specular", ColorRGBA.White);
        bodyMat.setFloat("Shininess", 16f);
        bodyGeo.setMaterial(bodyMat);
        bodyGeo.setLocalTranslation(0, 0.8f, 0);
        bodyGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Голова NPC
        Sphere head = new Sphere(16, 16, 0.3f);
        Geometry headGeo = new Geometry("Head", head);
        Material headMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        headMat.setBoolean("UseMaterialColors", true);
        headMat.setColor("Ambient", ColorRGBA.Pink);
        headMat.setColor("Diffuse", ColorRGBA.Pink);
        headMat.setColor("Specular", ColorRGBA.White);
        headMat.setFloat("Shininess", 32f);
        headGeo.setMaterial(headMat);
        headGeo.setLocalTranslation(0, 1.8f, 0);
        headGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        npcNode.attachChild(bodyGeo);
        npcNode.attachChild(headGeo);
        npcNode.setLocalTranslation(position);
    }

    private void createInteractionIndicator(AssetManager assetManager) {
        Sphere indicator = new Sphere(16, 16, 0.1f);
        interactionIndicator = new Geometry("InteractionIndicator", indicator);
        Material indicatorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        indicatorMat.setColor("Color", ColorRGBA.Yellow);
        indicatorMat.getAdditionalRenderState().setWireframe(true);
        interactionIndicator.setMaterial(indicatorMat);
        interactionIndicator.setLocalTranslation(position.add(0, 3f, 0));
        interactionIndicator.setCullHint(Node.CullHint.Always); // Скрываем по умолчанию
    }

    public Node getNode() {
        return npcNode;
    }

    public Geometry getInteractionIndicator() {
        return interactionIndicator;
    }

    public String getName() {
        return name;
    }

    public String getDialogue() {
        return dialogueKey;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void showInteractionIndicator() {
        interactionIndicator.setCullHint(Node.CullHint.Never);
    }

    public void hideInteractionIndicator() {
        interactionIndicator.setCullHint(Node.CullHint.Always);
    }
}