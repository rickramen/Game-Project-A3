package a3;

import tage.*;
import org.joml.*;

public class Laser {
    private Vector2f direction;
    private GameObject gameObject;
    private int layer = 0xffffffff;
    private MyGame game;
    private float speed;
    private float deactivateTimer;

    public Laser (GameObject gameObject, MyGame g) {
        this.gameObject = gameObject;
        this.game = g;
    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public void initialize(Vector2f direction, int layer, Vector2f pos, float speed) {
        direction = direction.normalize();
        this.direction = direction;
        this.layer = layer;
        gameObject.setLocalLocation(new Vector3f(pos.x, .65f, pos.y));
        this.speed = speed;
        syncToPhysics();
        deactivateTimer = 0f;
    }

    public void update(float deltaTime) {
        deactivateTimer +=deltaTime;
        if(deactivateTimer > 10f) {
            game.deactivateProjectile(this);
            return;
        }
        Vector2f deltaPosition = new Vector2f(direction.x, direction.y);
        deltaPosition.mul(speed).mul(deltaTime);
        Vector3f localLocation = gameObject.getLocalLocation();
        gameObject.setLocalLocation(new Vector3f(localLocation.x + deltaPosition.x, localLocation.y, localLocation.z + deltaPosition.y));
        syncToPhysics();
        
    }

    private void syncToPhysics() {
        float values[] = new float[16];
        double transform[] = MyGame.toDoubleArray(gameObject.getLocalTranslation().get(values));
        gameObject.getPhysicsObject().setTransform(transform);
    }
}