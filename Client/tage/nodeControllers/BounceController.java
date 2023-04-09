package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;

/**
 * A BounceController is a node controller when called causes any object 
 * it is attatched to to bounce in place it is located.
 * @author Rick Ammann
 */

public class BounceController extends NodeController 
{
    private Vector3f oldPosition, newPosition;
    private float height = 0.0f;
    private float bounceRate = .01f;
    private float cycleTime = 1.0f;
    private float totalTime = 0.0f;
    private float direction = 1.0f;
    private Engine engine;

    /** Creates bounce controller with specified cycletime */
    public BounceController(Engine e) { 
        super(); 
        engine = e;
    }

    /** Called by SceneGraph one per frame during display() */
    public void apply(GameObject go){
        float elapsedTime = super.getElapsedTime();
        totalTime += elapsedTime / 1000.0f;

        if(totalTime > cycleTime){
            totalTime = 0.0f;
        }

        /** Restricts bounce to specified ranges */
        if(go.getWorldLocation().y() > 2){
            height = 0.0f;
            height = -(direction * bounceRate * totalTime);
        }
        else if(go.getWorldLocation().y() <= 1){
            height = 0.0f;
            height = direction * bounceRate * totalTime;
        }
   
        oldPosition = go.getLocalLocation();
        newPosition = new Vector3f(oldPosition.x(), oldPosition.y() + height , oldPosition.z());
        go.setLocalLocation(newPosition);

    }
}
