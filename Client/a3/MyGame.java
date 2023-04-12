package a3;

import tage.*;
import tage.nodeControllers.*;
import tage.shapes.*;
import tage.input.*; 
import tage.input.action.*; 

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


import org.joml.*;

import java.util.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.java.games.input.*; 
import net.java.games.input.Component.Identifier.*; 
import tage.networking.IGameConnection.ProtocolType;

import javax.swing.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;


public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;
	private NodeController rc, bc;

	// ----- Scripts -----
	private File scriptFile1;
	private long fileLastModifiedTime = 0;
	ScriptEngine jsEngine;

	// ---- Camera ----
	private Camera cam;
	private Camera vpCam;
	private CameraOrbit3D orbitController;

	private double startTime, prevTime, elapsedTime, deltaTime;
	private int scoreCounter = 0;

	private boolean isAxesOn = true;
	private boolean isCubeAlive = true;
	private boolean isSphereAlive = true;
	private boolean isTorusAlive = true;
	private boolean isResetToggled = false;

	private int lakeIslands;
	private Light light1;

	// ---- GameObject Declarations ----
	private GameObject x, y, z;
	private ObjShape linxS, linyS, linzS;

	private GameObject terr;
	private ObjShape terrS;
	private TextureImage terrtx;
	private TextureImage hills;

	private GameObject avatar;
	private ObjShape dolS, ghostS, zombieS, robotS;
	private TextureImage doltx, ghostT, zombietx;
	private TextureImage robottx;

	private GameObject cub, sphere, tor, manHg, robot;
	private ObjShape cubS, sphereS, torS, manHgS;
	private TextureImage cubtx, spheretx, tortx, manHgtx;


	// Server
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	

	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
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

		terrS = new TerrainPlane(1500);

		dolS = new ImportedModel("dolphinHighPoly.obj");
		zombieS = new ImportedModel("zombie.obj");
		ghostS = new Sphere();

		manHgS = new ManualHourglass();

		cubS = new Cube();
		sphereS = new Sphere();
	
		robotS = new ImportedModel("robot2.obj");

	}

	@Override
	public void loadTextures()
	{	
		manHgtx = new TextureImage("hg.png");
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		zombietx = new TextureImage("zombie.png");
		ghostT = new TextureImage("sob.png");
		cubtx = new TextureImage("treasure.png");
		spheretx = new TextureImage("sob.png");
		tortx = new TextureImage("donut.png");
		terrtx = new TextureImage("sob.png");
		hills = new TextureImage("heightmap.png");
		robottx = new TextureImage("robotunwraped.png");
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

		// build the world X, Y, Z axes to show origin
		x = new GameObject(GameObject.root(), linxS); 
 		y = new GameObject(GameObject.root(), linyS); 
  		z = new GameObject(GameObject.root(), linzS); 
  		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f)); 
  		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f)); 
  		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f)); 
		
		// build terrain
		terr = new GameObject(GameObject.root(), terrS, terrtx);
		initialTranslation = (new Matrix4f().translation(0f,0f,0f));
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f().scaling(30f,1f,30f));
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);

		// build avatar
		avatar = new GameObject(GameObject.root(), zombieS, zombietx);
		initialTranslation = (new Matrix4f()).translation(0,1,0);
		initialScale = (new Matrix4f()).scaling(3.0f);	
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);

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

		// build cube 
		cub = new GameObject(GameObject.root(), cubS, cubtx); 
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
  		cub.setLocalTranslation(initialTranslation); 
		initialScale = (new Matrix4f()).scaling(0.5f); 
  		cub.setLocalScale(initialScale); 

		// build sphere 
		sphere = new GameObject(GameObject.root(), sphereS, spheretx);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
		sphere.setLocalTranslation(initialTranslation); 
		initialScale = (new Matrix4f()).scaling(0.5f); 
  		sphere.setLocalScale(initialScale); 

		// build torus 
		robot = new GameObject(GameObject.root(), robotS, robottx); 
		robot.getRenderStates().setTiling(1);
		initialTranslation = (new Matrix4f()).translation(
			setRandomLocation(), 
			1, 
			setRandomLocation());
  		robot.setLocalTranslation(initialTranslation); 
  		initialScale = (new Matrix4f()).scaling(0.75f); 
  		robot.setLocalScale(initialScale); 

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
		prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();

		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize parameters in InitParams.js
		scriptFile1 = new File ("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		System.out.println(
			((Integer)jsEngine.get("test")).intValue()
		);
	

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		//---------------- Camera ----------------------
		cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		//---------------- Orbital Controller ------------
		orbitController = new CameraOrbit3D(cam, avatar, engine);

		//----------------- Node Controllers -------------------
		rc = new RotationController(engine, new Vector3f(0,1,0), 0.001f);
		(engine.getSceneGraph()).addNodeController(rc); 
		bc = new BounceController(engine);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.toggle();
		bc.toggle();

		rc.addTarget(manHg);
		bc.addTarget(manHg);


		//--------------View Port---------------------
		(engine.getRenderSystem()).addViewport("VIEWPORT", 0.75f, 0f, 0.25f, 0.25f);	
		Viewport vp = (engine.getRenderSystem()).getViewport("VIEWPORT");
		vpCam = vp.getCamera();

		vp.setHasBorder(true);
		vp.setBorderWidth(4);
		vp.setBorderColor(0.0f, 1.0f, 0.0f);

		// Set viewport camera above avatar avatar
		vpCam.setLocation(new Vector3f(avatar.getWorldLocation().x(),
									avatar.getWorldLocation().y() + 10,
									avatar.getWorldLocation().z()));
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
		
		setupNetworking();
	}

	@Override
	public void update()
	{	
		// Calculate elapsed time
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		deltaTime = elapsedTime * 0.03;
		

		//  Build Main HUD
		float mainRelativeLeft = engine.getRenderSystem().getViewport("MAIN").getRelativeLeft();
        float mainRelativeBottom = engine.getRenderSystem().getViewport("MAIN").getRelativeBottom();
        float mainActualWidth = engine.getRenderSystem().getViewport("MAIN").getActualWidth();
        float mainActualHeight = engine.getRenderSystem().getViewport("MAIN").getActualHeight();

		String scoreCounterStr = Integer.toString(scoreCounter);
		String dispStr1 = "Score = " + scoreCounterStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		Vector3f hud3Color = new Vector3f(0,1,0);	
		(engine.getHUDmanager()).setHUD1(dispStr1, 
										hud1Color, 
										(int)(mainRelativeLeft * mainActualWidth + 10), 
										(int)(mainRelativeBottom * mainActualHeight + 10));

		// Build Viewport HUD
		float vpRelativeLeft = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeLeft();
        float vpRelativeBottom = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeBottom();
        float vpActualWidth = engine.getRenderSystem().getViewport("VIEWPORT").getActualWidth();
        float vpActualHeight = engine.getRenderSystem().getViewport("VIEWPORT").getActualHeight();

		String avatarPos = (int)avatar.getWorldLocation().x() + ", " +
						(int)avatar.getWorldLocation().y() + ", " +
						(int)avatar.getWorldLocation().z();				
		(engine.getHUDmanager()).setHUD2(avatarPos, 
										hud2Color,
										(int)(vpRelativeLeft * mainActualWidth + 10),
										(int)(vpRelativeBottom * vpActualHeight + 10));

		// Collect item and increase score when camera is within range
		if(avatar.getLocalLocation().distance(cub.getLocalLocation()) < 1.2 && isCubeAlive){
			isCubeAlive = false;
			rc.addTarget(cub);
			scoreCounter++;
		}else if(avatar.getLocalLocation().distance(sphere.getLocalLocation()) < 1.2 && isSphereAlive){
			isSphereAlive = false;
			rc.addTarget(sphere);
			scoreCounter++;
		}else if(avatar.getLocalLocation().distance(manHg.getLocalLocation()) < 1.2){
			isResetToggled = true;
			resetPrizes();
			resetController();
		}
		
		// Update altitude of avatar based on height map
		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

		im.update((float)elapsedTime);
		orbitController.updateCameraPosition();
		processNetworking((float)elapsedTime);
	}


	// Generates random number between -10 and 10 for loc coords
	private int setRandomLocation(){
		Random r = new Random();
		int randomNum = r.nextInt(10 + 10) - 10;
		return randomNum;
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

	// Reset controller by removing targets
	public void resetController(){
		rc.removeTarget(cub);
		rc.removeTarget(sphere);
		rc.removeTarget(tor);
	}
	
	// Resets gameObjects status and positions
	private void resetPrizes(){	

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

	// --------- SCRIPTING -----------
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

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	//--------------------- GETTERS -------------

	public GameObject getAvatar(){return avatar;}

	public Camera getCamera(){
		return engine.getRenderSystem().getViewport("MAIN").getCamera();
	}
		
	public Camera getViewportCamera(){
		return engine.getRenderSystem().getViewport("VIEWPORT").getCamera();
	}
	
	public Vector3f getPlayerPosition() { 
		return avatar.getWorldLocation();
	}

	public double getDeltaTime(){
		return this.deltaTime;
	}
}


				



