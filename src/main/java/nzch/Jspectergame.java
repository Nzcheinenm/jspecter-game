package nzch;

import com.jme3.app.SimpleApplication;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 */
public class Jspectergame extends SimpleApplication {

    public static void main(String[] args) {
        Jspectergame app = new Jspectergame();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Vector3f center = new Vector3f(2,2,2);
        Vector3f up = new Vector3f(1,1,1);
        Box b2 = new Box(up,center);

        Vector3f position = new Vector3f(3,3,3);
        Light light = new PointLight(position);
        light.setColor(ColorRGBA.Cyan);
        light.setEnabled(true);

        Geometry geom = new Geometry("Box", b);
        Geometry geom2 = new Geometry("Box2", b2);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);

        rootNode.addLight(light);

        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        geom2.setMaterial(mat2);

        rootNode.attachChild(geom2);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //this method will be called every game tick and can be used to make updates
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }
}
