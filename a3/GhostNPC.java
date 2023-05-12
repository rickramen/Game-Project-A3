package a3;

import java.util.UUID;
import tage.*;
import org.joml.*;


public class GhostNPC extends GameObject{
    private int id;
    public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f pos){
        super(GameObject.root(), s, t);
        this.id = id; 
        setPosition(pos);
    }

    void setPosition(Vector3f pos) {
        this.setLocalLocation(pos);
    }

    public void setSize(boolean big) { 
        if (!big) { 
            this.setLocalScale((new Matrix4f()).scaling(0.5f)); 
        }
        else { 
            this.setLocalScale((new Matrix4f()).scaling(1.0f)); 
        }
    }
}