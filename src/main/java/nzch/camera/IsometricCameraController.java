package nzch.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class IsometricCameraController {

    private Camera camera;
    private Vector3f target;
    private float distance;
    private float height;
    private float angle;

    // Параметры зума
    private float minDistance = 10f;
    private float maxDistance = 40f;
    private float zoomSpeed = 2f;

    public IsometricCameraController(Camera camera) {
        this.camera = camera;
        this.target = Vector3f.ZERO;
        this.distance = 25f;
        this.height = 20f;
        this.angle = (float) Math.toRadians(45);
        updateCamera();
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public void updateCamera() {
        // Вычисляем позицию камеры на основе угла и расстояния
        float offsetX = (float) (distance * Math.cos(angle));
        float offsetZ = (float) (distance * Math.sin(angle));

        // Высота камеры пропорциональна расстоянию
        float currentHeight = height * (distance / maxDistance);

        Vector3f cameraPos = new Vector3f(
                target.x + offsetX,
                target.y + currentHeight,
                target.z + offsetZ
        );

        camera.setLocation(cameraPos);
        camera.lookAt(target, Vector3f.UNIT_Y);
    }

    // Методы для зума
    public void zoom(float amount) {
        distance -= amount * zoomSpeed;

        // Ограничиваем расстояние зума
        if (distance < minDistance) {
            distance = minDistance;
        }
        if (distance > maxDistance) {
            distance = maxDistance;
        }

        updateCamera();
    }

    public void setDistance(float distance) {
        this.distance = Math.max(minDistance, Math.min(maxDistance, distance));
        updateCamera();
    }

    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    public void setZoomLimits(float min, float max) {
        this.minDistance = min;
        this.maxDistance = max;
        // Корректируем текущее расстояние
        this.distance = Math.max(min, Math.min(max, distance));
        updateCamera();
    }

    public float getDistance() {
        return distance;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public float getMaxDistance() {
        return maxDistance;
    }
}