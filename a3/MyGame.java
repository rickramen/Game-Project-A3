package a3;

import tage.*;
import tage.nodeControllers.*;
import tage.shapes.*;
import tage.input.*; 
import tage.input.action.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;

import net.java.games.input.*; 
import net.java.games.input.Component.Identifier.*; 

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;


public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private NodeController rc, bc;

	// ----- Scripts -----
	private File scriptFile1;
	private long fileLastModifiedTime = 0;
	ScriptEngine jsEngine;

	// ---- Camera ----
	private Camera cam;
	private Camera vpCam;
	private CameraOrbit3D orbitController;

	private Vector3f right;
	private boolean onDolphin = true;
	private double lastFrameTime, currFrameTime, elapsTime;

	private boolean isAxesOn = true;
	private boolean isCubeAlive = true;
	private boolean isSphereAlive = true;
	private boolean isTorusAlive = true;
	private boolean isResetToggled = false;

	private int lakeIslands;

	private int scoreCounter = 0;
	private Light light1;

	// ---- GameObject Declarations ----
	private GameObject x, y, z;
	private ObjShape linxS, linyS, linzS;

	private GameObject ground;
	private ObjShape groundS;
	private TextureImage groundtx;

	private GameObject dol;
	private ObjShape dolS;
	private TextureImage doltx;

	// Prizes
	private GameObject cub, sphere, tor, manHg;
	private ObjShape cubS, sphereS, torS, manHgS;
	private TextureImage cubtx, spheretx, tortx, manHgtx;

	// Mini Prizes
	ArrayList<GameObject> miniPrizeList;
	private GameObject miniCub, miniSphere, miniTor;
	private ObjShape miniCubS, miniSphereS, miniTorS;
	private TextureImage miniCubtx, miniSpheretx, miniTortx;
	

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f)); 
  		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f)); 
  		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));

		groundS = new Plane();

		dolS = new ImportedModel("dolphinHighPoly.obj");

		manHgS = new ManualHourglass();

		cubS = new Cube();
		sphereS = new Sphere();
		torS = new Torus();

		miniCubS = new Cube();
		miniSphereS = new Sphere();
		miniTorS = new Torus();
	}

	@Override
	public void loadTextures()
	{	
		groundtx = new TextureImage("ground.png");
		manHgtx = new TextureImage("hg.png");
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		cubtx = new TextureImage("treasure.png");
		spheretx = new TextureImage("sob.png");
		tortx = new TextureImage("donut.png");
		
		miniCubtx = new TextureImage("treasure.png");
		miniSpheretx = new TextureImage("drink.png");
		miniTortx = new TextureImage("donut.png");
	}

	@Override
	public void loadSkyBoxes(){
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(lakeIslands);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialScale;
		Random rand = new Random();

		
		// build ground plane
		ground = new GameObject(GameObject.root(), groundS, groundtx);
		ground.setLocalTranslation((new Matrix4f()).translation(0,0,0));
		ground.setLocalScale((new Matrix4f()).scaling(100.0f));

		// build the world X, Y, Z axes to show origin
		x = new GameObject(GameObject.root(), linxS); 
 		y = new GameObject(GameObject.root(), linyS); 
  		z = new GameObject(GameObject.root(), linzS); 
  		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f)); 
  		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f)); 
  		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f)); 
	
		// build dolphin avatar
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,1,0);
		initialScale = (new Matrix4f()).scaling(3.0f);
			
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);

		// build manual hourglass shape
		manHg = new GameObject(GameObject.root(), manHgS, manHgtx);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
		manHg.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.35f);
		manHg.setLocalScale(initialScale);
		manHg.getRenderStates().hasLighting(true);

		// build cube prize
		cub = new GameObject(GameObject.root(), cubS, cubtx); 
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
  		cub.setLocalTranslation(initialTranslation); 
		initialScale = (new Matrix4f()).scaling(0.5f); 
  		cub.setLocalScale(initialScale); 

		// build sphere prize
		sphere = new GameObject(GameObject.root(), sphereS, spheretx);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
		sphere.setLocalTranslation(initialTranslation); 
		initialScale = (new Matrix4f()).scaling(0.5f); 
  		sphere.setLocalScale(initialScale); 

		// build torus prize
		tor = new GameObject(GameObject.root(), torS, tortx); 
		tor.getRenderStates().setTiling(1);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
  		tor.setLocalTranslation(initialTranslation); 
  		initialScale = (new Matrix4f()).scaling(0.75f); 
  		tor.setLocalScale(initialScale); 

		// build mini prizes
		miniCub = new GameObject(GameObject.root(), miniCubS, miniCubtx);
		initialTranslation = (new Matrix4f()).translation(0,0,-1);
		initialScale = (new Matrix4f()).scaling(0.05f); 
		miniCub.setLocalTranslation(initialTranslation);
		miniCub.setLocalScale(initialScale);

		miniSphere = new GameObject(GameObject.root(), miniSphereS, miniSpheretx);
		initialTranslation = (new Matrix4f()).translation(-1,0,0);
		initialScale = (new Matrix4f()).scaling(0.05f); 
		miniSphere.setLocalTranslation(initialTranslation);
		miniSphere.setLocalScale(initialScale);

		miniTor = new GameObject(GameObject.root(), miniTorS, miniTortx);
		initialTranslation = (new Matrix4f()).translation(1,0,0);
		initialScale = (new Matrix4f()).scaling(0.1f); 
		miniTor.setLocalTranslation(initialTranslation);
		miniTor.setLocalScale(initialScale);

		miniPrizeList = new ArrayList<GameObject>();
		miniPrizeList.add(miniCub);
		miniPrizeList.add(miniSphere);
		miniPrizeList.add(miniTor);

		// Set hierarchical relationship to dolphin avatar
		for(GameObject prize : miniPrizeList){
			prize.setParent(dol);
			prize.propagateTranslation(true);
			prize.getRenderStates().disableRendering();
		}
	}

	@Override
	public void initializeLights()
	{	
		// Ambient light
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		// Positional light
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame()
	{	
		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize paramters in InitParams.js
		scriptFile1 = new File ("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		System.out.println(
			((Integer)jsEngine.get("test")).intValue()
		);
	

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		//---------------- Camera ----------------------
		cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		//---------------- Orbital Controller ------------
		orbitController = new CameraOrbit3D(cam, dol, engine);

		
		//----------------- Node Controllers -------------------
		rc = new RotationController(engine, new Vector3f(0,1,0), 0.001f);
		(engine.getSceneGraph()).addNodeController(rc); 
		bc = new BounceController(engine);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.toggle();
		bc.toggle();

		rc.addTarget(manHg);
		bc.addTarget(manHg);

		for(GameObject prize : miniPrizeList){
			rc.addTarget(prize);
		}

		//--------------View Port---------------------
		(engine.getRenderSystem()).addViewport("VIEWPORT", 0.75f, 0f, 0.25f, 0.25f);	
		Viewport vp = (engine.getRenderSystem()).getViewport("VIEWPORT");
		vpCam = vp.getCamera();

		vp.setHasBorder(true);
		vp.setBorderWidth(4);
		vp.setBorderColor(0.0f, 1.0f, 0.0f);

		// Set viewport camera above dol avatar
		vpCam.setLocation(new Vector3f(dol.getWorldLocation().x(),
									dol.getWorldLocation().y() + 10,
									dol.getWorldLocation().z()));
		vpCam.setU(new Vector3f(1, 0, 0));
		vpCam.setV(new Vector3f(0, 0, -1));
		vpCam.setN(new Vector3f(0, -1, 0));
	
		//--------- INPUTS SECTION------------
		im = engine.getInputManager(); 
		
		FwdAction fwdAction = new FwdAction(this);
		BwdAction bwdAction = new BwdAction(this);
		LeftAction leftAction = new LeftAction(this);
		RightAction rightAction = new RightAction(this);
		FwdBwdAction fwdBwdAction = new FwdBwdAction(this);
		TurnAction turnAction = new TurnAction(this);

		ToggleAxesAction toggleAxesAction = new ToggleAxesAction(this); 

		ViewportUpAction viewportUpAction = new ViewportUpAction(this);
		ViewportDownAction viewportDownAction = new ViewportDownAction(this);
		ViewportLeftAction viewportLeftAction = new ViewportLeftAction(this);
		ViewportRightAction viewportRightAction = new ViewportRightAction(this);
		ViewportZoomInAction viewportZoomInAction = new ViewportZoomInAction(this);
		ViewportZoomOutAction viewportZoomOutAction = new ViewportZoomOutAction(this);

		// Keyboard
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key._1, toggleAxesAction, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.W, fwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.S, bwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.A, leftAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.D, rightAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.I, viewportUpAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.K, viewportDownAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.J, viewportLeftAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.L, viewportRightAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key._0, viewportZoomInAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key._9, viewportZoomOutAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	

		// Gamepad
		im.associateActionWithAllGamepads(
			net.java.games.input.Component.Identifier.Button._7, toggleAxesAction, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);	
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Y, fwdBwdAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.X, turnAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._3, viewportUpAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._0, viewportDownAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._1, viewportRightAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._2, viewportLeftAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._4, viewportZoomOutAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Button._5, viewportZoomInAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
	}

	@Override
	public void update()
	{	
		// Calculate elapsed time
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime += (currFrameTime - lastFrameTime) / 1000.0;
		int elapsTimeSec = Math.round((float)elapsTime);

		im.update((float)elapsTime);
		
		//  Build Main HUD
		float mainRelativeLeft = engine.getRenderSystem().getViewport("MAIN").getRelativeLeft();
        float mainRelativeBottom = engine.getRenderSystem().getViewport("MAIN").getRelativeBottom();
        float mainActualWidth = engine.getRenderSystem().getViewport("MAIN").getActualWidth();
        float mainActualHeight = engine.getRenderSystem().getViewport("MAIN").getActualHeight();

		String scoreCounterStr = Integer.toString(scoreCounter);
		String dispStr1 = "Score = " + scoreCounterStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, 
										hud1Color, 
										(int)(mainRelativeLeft * mainActualWidth + 10), 
										(int)(mainRelativeBottom * mainActualHeight + 10));

		// Build Viewport HUD
		float vpRelativeLeft = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeLeft();
        float vpRelativeBottom = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeBottom();
        float vpActualWidth = engine.getRenderSystem().getViewport("VIEWPORT").getActualWidth();
        float vpActualHeight = engine.getRenderSystem().getViewport("VIEWPORT").getActualHeight();

		String avatarPos = (int)dol.getWorldLocation().x() + ", " +
						(int)dol.getWorldLocation().y() + ", " +
						(int)dol.getWorldLocation().z();
		Vector3f hud3Color = new Vector3f(0,1,0);					
		(engine.getHUDmanager()).setHUD2(avatarPos, 
										hud3Color,
										(int)(vpRelativeLeft * mainActualWidth + 10),
										(int)(vpRelativeBottom * vpActualHeight + 10));

		// Collect item and increase score when camera is within range
		if(isOnDolphin()){
			if(dol.getLocalLocation().distance(cub.getLocalLocation()) < 1.2 && isCubeAlive){
				isCubeAlive = false;
				rc.addTarget(cub);
				miniCub.getRenderStates().enableRendering();
				scoreCounter++;
			}else if(dol.getLocalLocation().distance(sphere.getLocalLocation()) < 1.2 && isSphereAlive){
				isSphereAlive = false;
				rc.addTarget(sphere);
				miniSphere.getRenderStates().enableRendering();
				scoreCounter++;
			}else if(dol.getLocalLocation().distance(tor.getLocalLocation()) < 1.2 && isTorusAlive){
				isTorusAlive = false;
				rc.addTarget(tor);
				miniTor.getRenderStates().enableRendering();
				scoreCounter++;
			}else if(dol.getLocalLocation().distance(manHg.getLocalLocation()) < 1.2){
				isResetToggled = true;
				resetPrizes();
				resetController();
			}
		}	

		//positionCameraBehindAvatar();
		orbitController.updateCameraPosition();
	}

	private void runScript(File scriptFile)
	{
		try
		{
			FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader);
			fileReader.close();
		}
		catch(FileNotFoundException e1)
		{
			System.out.println(scriptFile + " not found " + e1); }
	    catch (IOException e2)
		{
           	System.out.println("IO problem with " + scriptFile + e2); 
		}
	    catch (ScriptException e3)
		{ 
           	System.out.println("ScriptException in " + scriptFile + e3); 
		}
	    catch (NullPointerException e4)
		{ 
            System.out.println ("Null ptr exception reading " + scriptFile + e4);
		}
	}

	// Toggles render for Line axes
	public void toggleAxes(){
		if(isAxesOn){
			x.getRenderStates().disableRendering();
			y.getRenderStates().disableRendering();
			z.getRenderStates().disableRendering();
			isAxesOn = false;
		}
		else{
			x.getRenderStates().enableRendering();
			y.getRenderStates().enableRendering();
			z.getRenderStates().enableRendering();
			isAxesOn = true;
		}
	}

	// Getter for dolphin avatar
	public GameObject getAvatar(){return dol;}

	// Getter for game camera
	public Camera getCamera(){
		return engine.getRenderSystem().getViewport("MAIN").getCamera();
	}
	
	// Getter Viewport camera
	public Camera getViewportCamera(){
		return engine.getRenderSystem().getViewport("VIEWPORT").getCamera();
	}

	// Getter to check whether player is ON/OFF dolphin (A1)
	public boolean isOnDolphin(){
		return onDolphin;
	}

	// Updates value of ride ON/OFF state (A1)
	public void updateOnDolphin(){
		if(onDolphin){onDolphin = false;}
		else{onDolphin = true;}
	}

	// Reset controller by removing targets
	public void resetController(){
		rc.removeTarget(cub);
		rc.removeTarget(sphere);
		rc.removeTarget(tor);
	}
	
	// Generates random number between -10 and 10 for loc coords
	private int setRandomLocation(){
		Random r = new Random();
		int randomNum = r.nextInt(10 + 10) - 10;
		return randomNum;
	}

	// Resets gameObjects status and positions
	private void resetPrizes(){
		for(GameObject prize : miniPrizeList){
			prize.getRenderStates().disableRendering();
		}

		manHg.setLocalTranslation(new Matrix4f().translation(
			setRandomLocation(), 
			1, 
			setRandomLocation()));

		isCubeAlive = true;
		cub.setLocalTranslation(new Matrix4f().translation(
			setRandomLocation(), 
			1, 
			setRandomLocation()));
	
		isSphereAlive = true;
		sphere.setLocalTranslation(new Matrix4f().translation(
			setRandomLocation(), 
			1, 
			setRandomLocation()));
		
		isTorusAlive = true;
		tor.setLocalTranslation(new Matrix4f().translation(
			setRandomLocation(), 
			1, 
			setRandomLocation()));
	}
}


				



