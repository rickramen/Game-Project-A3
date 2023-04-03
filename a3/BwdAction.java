package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event;

import javax.swing.text.DefaultStyledDocument.ElementSpec;

import org.joml.*; 

public class BwdAction extends AbstractInputAction{
    private MyGame game; 
    private GameObject av; 
    private Camera cam;
    private Vector3f oldPosition, newPosition, camFwdDirection; 
    private Vector4f fwdDirection; 

    public BwdAction(MyGame g) 
    { 
        game = g; 
    } 

    @Override 
    public void performAction(float time, Event e) 
    { 
        if(game.isOnDolphin()){
            av = game.getAvatar();
            oldPosition = av.getWorldLocation(); 
            fwdDirection = new Vector4f(0f,0f,1f,1f); 
            fwdDirection.mul(av.getWorldRotation()); 
            fwdDirection.mul(-0.02f);
            newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z()); 
            av.setLocalLocation(newPosition); 
        }
        else{
            cam = game.getCamera();
            oldPosition = cam.getLocation();
            camFwdDirection = cam.getN();
            camFwdDirection.mul(-0.02f);   
            newPosition = oldPosition.add(camFwdDirection.x(), camFwdDirection.y(), camFwdDirection.z());
            cam.setLocation(newPosition); 
        }
    } 
}
