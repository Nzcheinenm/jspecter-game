package nzch.system;


import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.ArrayList;
import java.util.List;

public class GridSystem {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final int width;
    private final int height;
    private final float cellSize = 1.0f;
    private final List<Geometry> gridCells;
    private final List<Geometry> highlightedCells;
    private boolean gridVisible = false;

    public GridSystem(Node rootNode, AssetManager assetManager, int width, int height) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.width = width;
        this.height = height;
        this.gridCells = new ArrayList<>();
        this.highlightedCells = new ArrayList<>();
        createGrid();
    }

    private void createGrid() {
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.setColor("Color", new ColorRGBA(1, 1, 1, 0.3f));
        gridMat.getAdditionalRenderState().setWireframe(true);

        for (int x = -width / 2; x <= width / 2; x++) {
            for (int z = -height / 2; z <= height / 2; z++) {
                Quad cell = new Quad(cellSize, cellSize);
                Geometry cellGeo = new Geometry("GridCell_" + x + "_" + z, cell);
                cellGeo.setMaterial(gridMat);
                cellGeo.setLocalTranslation(x * cellSize, 0.01f, z * cellSize);
                cellGeo.rotate(-FastMath.HALF_PI, 0, 0); // Поворачиваем чтобы лежал горизонтально
                cellGeo.setCullHint(Node.CullHint.Always); // Скрываем по умолчанию

                gridCells.add(cellGeo);
                rootNode.attachChild(cellGeo);
            }
        }
    }

    public void toggleGrid() {
        gridVisible = !gridVisible;
        for (Geometry cell : gridCells) {
            cell.setCullHint(gridVisible ? Node.CullHint.Never : Node.CullHint.Always);
        }
    }

    public void highlightMovementRange(Vector3f center, int range) {
        clearHighlights();

        Material highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMat.setColor("Color", new ColorRGBA(0, 1, 0, 0.5f));

        Vector3f gridCenter = worldToGridPosition(center);

        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= range) { // Ромбовидная область
                    Vector3f gridPos = new Vector3f(gridCenter.x + dx, 0, gridCenter.z + dz);
                    if (isValidGridPosition(gridPos)) {
                        highlightCell(gridPos, highlightMat);
                    }
                }
            }
        }
    }

    private void highlightCell(Vector3f gridPos, Material material) {
        Quad cell = new Quad(cellSize, cellSize);
        Geometry cellGeo = new Geometry("Highlight_" + gridPos.x + "_" + gridPos.z, cell);
        cellGeo.setMaterial(material);
        cellGeo.setLocalTranslation(gridPos.x * cellSize, 0.02f, gridPos.z * cellSize);
        cellGeo.rotate(-FastMath.HALF_PI, 0, 0);

        highlightedCells.add(cellGeo);
        rootNode.attachChild(cellGeo);
    }

    public void hideMovementRange() {
        clearHighlights();
    }

    public void hideAllHighlights() {
        clearHighlights();
    }

    private void clearHighlights() {
        for (Geometry cell : highlightedCells) {
            rootNode.detachChild(cell);
        }
        highlightedCells.clear();
    }

    public Vector3f worldToGridPosition(Vector3f worldPos) {
        int gridX = Math.round(worldPos.x / cellSize);
        int gridZ = Math.round(worldPos.z / cellSize);
        return new Vector3f(gridX, 0, gridZ);
    }

    public Vector3f gridToWorldPosition(Vector3f gridPos) {
        return new Vector3f(gridPos.x * cellSize, 0, gridPos.z * cellSize);
    }

    public boolean isValidGridPosition(Vector3f gridPos) {
        return gridPos.x >= -width / 2 && gridPos.x <= width / 2 &&
                gridPos.z >= -height / 2 && gridPos.z <= height / 2;
    }

    public boolean isPositionInMovementRange(Vector3f targetPos, Vector3f startPos, int range) {
        Vector3f startGrid = worldToGridPosition(startPos);
        Vector3f targetGrid = worldToGridPosition(targetPos);

        int distance = (int) (Math.abs(targetGrid.x - startGrid.x) + Math.abs(targetGrid.z - startGrid.z));
        boolean inRange = distance <= range && isValidGridPosition(targetGrid);

        System.out.println("Проверка движения: из " + startGrid + " в " + targetGrid +
                " (дистанция: " + distance + ", в диапазоне: " + inRange + ")");
        return inRange;
    }
}