package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 
import org.joml.*; 

public class ToggleAxesAction extends AbstractInputAction{
    private MyGame game;

    public ToggleAxesAction(MyGame g) 
    { 
        game = g; 
    }

    @Override 
    public void performAction(float time, Event e){
        game.toggleAxes();
    }

}