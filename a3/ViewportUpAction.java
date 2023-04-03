package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ViewportUpAction extends AbstractInputAction{
    private MyGame game; 
    private Camera cam;
    private Vector3f oldPosition, newPosition, FwdDirection; 

    public ViewportUpAction(MyGame g){
        game = g;
    }

    @Override
    public void performAction(float time, Event e){
        cam = game.getViewportCamera();
        oldPosition = cam.getLocation();
        FwdDirection = cam.getV();
        FwdDirection.mul(0.05f);  
        newPosition = oldPosition.add(FwdDirection.x(), FwdDirection.y(), FwdDirection.z());
        cam.setLocation(newPosition);  
    }
}

