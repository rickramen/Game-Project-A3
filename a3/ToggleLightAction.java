package a3;

import tage.*;
import tage.input.action.AbstractInputAction; 
import net.java.games.input.Event; 

public class ToggleLightAction extends AbstractInputAction{
    private MyGame game;

    public ToggleLightAction(MyGame g) 
    { 
        game = g; 
    }

    @Override 
    public void performAction(float time, Event e){
        if (game.isLightOn()) {
            game.toggleLightOff();

        } else {
            game.toggleLightOn();
        }
    }

}