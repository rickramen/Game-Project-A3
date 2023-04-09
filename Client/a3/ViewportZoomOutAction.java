package a3;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ViewportZoomOutAction extends AbstractInputAction{
    private MyGame game; 
    private Camera cam;
    private Vector3f oldPosition, newPosition, FwdDirection; 

    public ViewportZoomOutAction(MyGame g){
        game = g;
    }

    @Override
    public void performAction(float time, Event e){
        cam = game.getViewportCamera();
        oldPosition = cam.getLocation();
        FwdDirection = cam.getN();
        FwdDirection.mul(-0.05f);  
        newPosition = oldPosition.add(FwdDirection.x(), FwdDirection.y(), FwdDirection.z());

        // Set max limit to zoom out
        if(newPosition.y() > 25.0f){
            newPosition = cam.getLocation();
        }

        cam.setLocation(newPosition);  
    }
}

