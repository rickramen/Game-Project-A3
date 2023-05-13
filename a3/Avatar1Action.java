package a3;


import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event;

public class Avatar1Action extends AbstractInputAction{
    private MyGame game;
    private GameObject av;



    public Avatar1Action(MyGame g) 
    {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        av = game.getAvatar();
        TextureImage texture = game.getAvatar1texture();
        av.setTextureImage(texture);
    }


}
