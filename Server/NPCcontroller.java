package server;

import java.util.Random;

import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;

public class NPCcontroller {
    private NPC npc;
    Random rn = new Random();
    BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
    boolean nearFlag = false;
    long thinkStartTime, tickStarttime;
    long lastThinkUpdateTime, lastTickUpdateTime;
    GameServerUDP server;
    double criteria = 2.0;

    public void updateNPCs() {
        npc.updateLocation();
    }

    public void start(GameServerUDP s) {
        thinkStartTime = System.nanoTime();
        tickStarttime = System.nanoTime();
        lastThinkUpdateTime = thinkStartTime;
        lastTickUpdateTime = tickStarttime;
        server = s;
        setupNPCs();
        setupBehaviorTree();
        npcLoop();
    }

    private void setupNPCs() {
        npc = new NPC();
        npc.randomizeLocation(rn.nextInt(40), rn.nextInt(40));
    }

    private void npcLoop() {
        while(true) {
            long currentTime = System.nanoTime();
            float elapsedThinkMiliSecs = (currentTime = lastThinkUpdateTime) / (1000000.0f);
            float elapsedTickMiliSecs = (currentTime = lastTickUpdateTime) / (1000000.0f);

            if (elapsedTickMiliSecs >=25.0f) {
                lastTickUpdateTime = currentTime;
                npc.updateLocation();
                server.sendNPCinfo();
            }

            if(elapsedThinkMiliSecs >=250.0f) {
                lastThinkUpdateTime = currentTime;
                bt.update(elapsedThinkMiliSecs);
            }
            Thread.yield();
        }
    }

    private void setupBehaviorTree() {
        bt.insertAtRoot(new BTSequence(10));
  
        bt.insert(10, new AvatarNear(server, this, npc, false));
        bt.insert(10, new GetBig(npc));
    }

    public boolean getNearFlag() {
        return false;
    }

    public boolean getBigFlag() {
        return false;
    }

    public NPC getNPC() {
        return npc;
    }

    public String getCriteria() {
        return null;
    }

    public void setNearFlag(boolean b) {
    }
}
