package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 
import org.joml.*; 

public class RightAction extends AbstractInputAction{
    private MyGame game; 
    private GameObject av; 
    private Camera cam;
 
    public RightAction(MyGame g) 
    { 
        game = g; 
    } 

    @Override 
    public void performAction(float time, Event e) 
    {
        if(game.isOnDolphin()){
            av = game.getAvatar();
            av.yaw(-0.01f);
        }
        else{
            cam = game.getCamera();
            cam.yaw(-0.01f);
        }
    }
}
