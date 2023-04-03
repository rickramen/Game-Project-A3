package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 
import org.joml.*; 

public class TurnAction extends AbstractInputAction{
    private MyGame game; 
    private GameObject av; 
    private Camera cam;
 
    public TurnAction(MyGame g) 
    { 
        game = g; 
    } 

    @Override 
    public void performAction(float time, Event e) 
    {
        float keyValue = e.getValue(); 
        if (keyValue > -.2 && keyValue < .2) {return;}  // deadzone 

        // X-axis: joystick = -1 pushed left, 1 pushed right
        if(game.isOnDolphin()){
            av = game.getAvatar();
            
            if(keyValue < -0.2){
                av.yaw(0.01f);
            }
            else {
                av.yaw(-0.01f);
            }   
        } 
        else{
            cam = game.getCamera();

            if(keyValue < -0.2){
                cam.yaw(0.01f);
            }
            else{
                cam.yaw(-0.01f);
            }
        }
    }
}
