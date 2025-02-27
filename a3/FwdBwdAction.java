package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 
import org.joml.*; 

public class FwdBwdAction extends AbstractInputAction{
    private MyGame game; 
    private GameObject av; 
    private Vector3f oldPosition, newPosition; 
    private Vector4f fwdDirection; 
    private ProtocolClient protClient;

    public FwdBwdAction(MyGame g, ProtocolClient p) 
    { 
        game = g; 
        protClient = p;
    } 

    @Override 
    public void performAction(float time, Event e) 
    {   
        float keyValue = e.getValue(); 
        if (keyValue > -.2 && keyValue < .2) {return;}  // deadzone 

    
        av = game.getAvatar(); 
        oldPosition = av.getWorldLocation(); 
        fwdDirection = new Vector4f(0f,0f,1f,1f); 
        fwdDirection.mul(av.getWorldRotation()); 

        // Y-axis: joystick keyValue = -1 pushed up, 1 pushed down
        if(keyValue < -0.2){
            fwdDirection.mul(0.02f); 
        }
        else{
            fwdDirection.mul(-0.02f); 
        }
        
        newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z()); 
        av.setLocalLocation(newPosition);       
        protClient.sendMoveMessage(av.getWorldLocation());
    }
}
