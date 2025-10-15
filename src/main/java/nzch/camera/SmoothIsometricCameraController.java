package nzch.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class SmoothIsometricCameraController {

    private final Camera camera;
    private Vector3f target;
    private Vector3f currentCameraPos;
    private final float distance;
    private final float height;
    private final float angle;
    private float smoothness = 5.0f; // коэффициент плавности

    public SmoothIsometricCameraController(Camera camera) {
        this.camera = camera;
        this.target = Vector3f.ZERO;
        this.currentCameraPos = Vector3f.ZERO;
        this.distance = 25f;
        this.height = 20f;
        this.angle = (float) Math.toRadians(45);
        updateCameraInstant(); // мгновенная установка начальной позиции
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public void update(float tpf) {
        // Плавное перемещение камеры к целевой позиции
        Vector3f desiredPosition = calculateDesiredCameraPosition();
        currentCameraPos = currentCameraPos.interpolateLocal(desiredPosition, tpf * smoothness);

        camera.setLocation(currentCameraPos);
        camera.lookAt(target, Vector3f.UNIT_Y);
    }

    private Vector3f calculateDesiredCameraPosition() {
        float offsetX = (float) (distance * Math.cos(angle));
        float offsetZ = (float) (distance * Math.sin(angle));

        return new Vector3f(
                target.x + offsetX,
                target.y + height,
                target.z + offsetZ
        );
    }

    private void updateCameraInstant() {
        currentCameraPos = calculateDesiredCameraPosition();
        camera.setLocation(currentCameraPos);
        camera.lookAt(target, Vector3f.UNIT_Y);
    }

    public void setSmoothness(float smoothness) {
        this.smoothness = smoothness;
    }
}