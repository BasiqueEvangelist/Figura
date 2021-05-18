package net.blancworks.figura;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec2f;

public class CameraData {
    public Vector3f position;
    public Vector3f pivot;
    public Vector3f fpPosition;
    public Vector3f fpPivot;
    public Vec2f rotation;


    public CameraData() {
        this.position = new Vector3f(0, 0, 0);
        this.pivot = new Vector3f(0, 0, 0);
        this.fpPosition = new Vector3f(0, 0, 0);
        this.fpPivot = new Vector3f(0, 0, 0);
        this.rotation = new Vec2f(0, 0);
    }
}
