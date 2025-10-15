package nzch.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class IsometricCameraController {

    private final Camera camera;
    private Vector3f target;
    private float distance;
    private float height;
    private float angle; // угол обзора в радианах

    public IsometricCameraController(Camera camera) {
        this.camera = camera;
        this.target = Vector3f.ZERO;
        this.distance = 25f;
        this.height = 20f;
        this.angle = (float) Math.toRadians(45); // 45 градусов
        updateCamera();
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public void updateCamera() {
        // Вычисляем позицию камеры на основе угла и расстояния
        float offsetX = (float) (distance * Math.cos(angle));
        float offsetZ = (float) (distance * Math.sin(angle));

        Vector3f cameraPos = new Vector3f(
                target.x + offsetX,
                target.y + height,
                target.z + offsetZ
        );

        camera.setLocation(cameraPos);
        camera.lookAt(target, Vector3f.UNIT_Y);
    }

    // Методы для настройки камеры
    public void setDistance(float distance) {
        this.distance = distance;
        updateCamera();
    }

    public void setHeight(float height) {
        this.height = height;
        updateCamera();
    }

    public void setAngle(float angleDegrees) {
        this.angle = (float) Math.toRadians(angleDegrees);
        updateCamera();
    }
}