package a3;


import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event;
import org.joml.*; 

public class FireAction extends AbstractInputAction{
    private MyGame game;
    private ProtocolClient protClient;
    private Vector3f localLocation;
    private Matrix4f rot;
    private GameObject av;



    public FireAction(MyGame g, ProtocolClient p) 
    {
        game = g;
        protClient = p;
    }

    @Override
    public void performAction(float time, Event e) {
        av = game.getAvatar();
        localLocation = av.getLocalLocation();
        rot = av.getLocalRotation();
        game.createLaser(new Vector2f(av.getLocalForwardVector().x, av.getLocalForwardVector().z), new Vector2f(localLocation.x, localLocation.z), 30f);
        game.playFireSound();
    }


}
