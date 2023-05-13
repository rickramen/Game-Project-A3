package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event;
import org.joml.*; 

public class BwdAction extends AbstractInputAction{
    private MyGame game; 
    private GameObject av; 
    private Vector3f oldPosition, newPosition; 
    private Vector4f fwdDirection; 
    private ProtocolClient protClient;

    public BwdAction(MyGame g, ProtocolClient p) 
    { 
        game = g; 
        protClient = p;
    } 

    @Override 
    public void performAction(float time, Event e) 
    { 
        double speedMul = game.getSpeed();
        av = game.getAvatar();
        oldPosition = av.getWorldLocation(); 
        fwdDirection = new Vector4f(0f,0f,1f,1f); 
        fwdDirection.mul(av.getWorldRotation()); 
        fwdDirection.mul((float)-speedMul);
        newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z()); 
        av.setLocalLocation(newPosition);  
        protClient.sendMoveMessage(av.getWorldLocation());
    } 
}
