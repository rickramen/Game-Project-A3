package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 
import org.joml.*; 

public class RightAction extends AbstractInputAction{
    private MyGame game;
    private GameObject av; 
    private ProtocolClient protClient;
   
    public RightAction(MyGame g) 
    { 
        game = g; 
        //protClient = p;
    } 

    @Override 
    public void performAction(float time, Event e) 
    {
        av = game.getAvatar();
        av.yaw(-0.01f);
       // protClient.sendMoveMessage(av.getWorldLocation());
    }
   
}
