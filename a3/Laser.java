package a3;

import tage.*;
import org.joml.*;

public class Laser {
    //private Vector3f direction;
    private Vector2f direction;
    private GameObject gameObject, av;
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

    public void initialize(Vector2f direction, Vector2f pos, float speed) {
        av = game.getAvatar();
        av.getWorldRotation();
        direction = direction.normalize();
        this.direction = direction;
        gameObject.setLocalLocation(new Vector3f(pos.x, av.getWorldLocation().y() + .65f , pos.y));
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
        //Vector3f deltaPosition = new Vector3f(direction.x, direction.y, direction.z);
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