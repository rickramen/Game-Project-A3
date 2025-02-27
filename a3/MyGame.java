package a3;

import tage.*;
import tage.Light.LightType;
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
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.networking.IGameConnection.ProtocolType;

import javax.swing.*;
import javax.swing.plaf.metal.OceanTheme;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.PhysicsEngineFactory;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.jogamp.opengl.util.texture.Texture;
import com.bulletphysics.collision.dispatch.CollisionObject;

import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.audio.joal.JOALAudioManager;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;
	private NodeController rc;

	// ----- Scripts -----
	private File scriptFile1;
	private long fileLastModifiedTime = 0;
	ScriptEngine jsEngine;

	// ---- Camera ----
	private Camera cam;
	private Camera vpCam;
	private CameraOrbit3D orbitController;

	private double lastFrameTime, currFrameTime, elapsTime;

	private double startTime, prevTime, elapsedTime, deltaTime;
	private int timeLeft, timerLength;

	private boolean isAxesOn, toggleLights, isAvatarAlive;
	private int sunset;
	private Light light1, light2, light3;

	private double avatarPosX, avatarPosY, avatarPosZ, avatarScale;
	private double alienScale, buffScale, miniBuffScale;
	private double rotationSpeed, alienWalkAniSpeed;
	private double terrainLocX, terrainLocY, terrainLocZ, terrainScaleX, terrainScaleY, terrainScaleZ;

	// ---- GameObject Declarations ----
	private GameObject x, y, z;
	private ObjShape linxS, linyS, linzS;

	private GameObject terr;
	private ObjShape terrS;
	private TextureImage terrtx;
	private TextureImage hills;

	private GameObject avatar, alien;
	private AnimatedShape alienS;
	private ObjShape ghostS, robotS;
	private TextureImage ghostT, alientx, robottx, robottx2;

	private GameObject lightning, miniLightning, power, minipower;
	private ObjShape lightningS;
	private TextureImage lightningtx, powertx;

	private GameObject laserBeam;
	private ObjShape laserBeamS;
	private TextureImage lasertx;
	private HashMap<Integer, GameObject> physicsObjects;

	// Server
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// Physics Engine
	private PhysicsEngine physicsEngine;
	private PhysicsObject alienBoxP;

	// Sound
	private IAudioManager audioMgr;
	private Sound ambientSound, alienSound, laserSound;

	private boolean running = true;
	private float vals[] = new float[16];

	// Update Variables
	private int health;
	private float powerUpTime;
	private double speed;
	private float strengthUpTime;
	private int strength;

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));

		terrS = new TerrainPlane(1500);

		alienS = new AnimatedShape("alien.rkm", "alien.rks");
		alienS.loadAnimation("WALK", "alien_walk.rka");
		robotS = new ImportedModel("robot2.obj");
		ghostS = new ImportedModel("robot2.obj");

		lightningS = new ImportedModel("speed.obj");

		laserBeamS = new Sphere();

	}

	@Override
	public void loadTextures() {
		terrtx = new TextureImage("foliage.png");
		hills = new TextureImage("heightmap.png");

		alientx = new TextureImage("alien.png");
		robottx = new TextureImage("robotunwraped.png");
		robottx2 = new TextureImage("robotunwraped2.png");
		ghostT = new TextureImage("robotunwraped.png");
		lightningtx = new TextureImage("speed.png");
		powertx = new TextureImage("power.png");

		lasertx = new TextureImage("energy.png");

	}

	@Override
	public void loadSkyBoxes() {
		sunset = (engine.getSceneGraph()).loadCubeMap("sunset");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(sunset);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale;

		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize parameters in InitParams.js
		scriptFile1 = new File("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		// ScriptEngine variables
		avatarPosX = ((double) (jsEngine.get("avatarPosX")));
		avatarPosY = ((double) (jsEngine.get("avatarPosY")));
		avatarPosZ = ((double) (jsEngine.get("avatarPosZ")));
		avatarScale = ((double) (jsEngine.get("avatarScale")));
		alienScale = ((double) (jsEngine.get("alienScale")));
		terrainLocX = ((double) (jsEngine.get("terrainLocX")));
		terrainLocY = ((double) (jsEngine.get("terrainLocY")));
		terrainLocZ = ((double) (jsEngine.get("terrainLocZ")));
		terrainScaleX = ((double) (jsEngine.get("terrainScaleX")));
		terrainScaleY = ((double) (jsEngine.get("terrainScaleY")));
		terrainScaleZ = ((double) (jsEngine.get("terrainScaleZ")));
		buffScale = ((double) (jsEngine.get("buffScale")));
		miniBuffScale = ((double) (jsEngine.get("miniBuffScale")));
		alienWalkAniSpeed = ((double) (jsEngine.get("alienWalkAniSpeed")));

		// build the world X, Y, Z axes to show origin
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		x.getRenderStates().disableRendering();
		y.getRenderStates().disableRendering();
		z.getRenderStates().disableRendering();

		// build terrain
		terr = new GameObject(GameObject.root(), terrS, terrtx);
		initialTranslation = (new Matrix4f().translation((float) terrainLocX, (float) (terrainLocY),
				(float) (terrainLocZ)));
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f().scaling((float) terrainScaleX, (float) terrainScaleY, (float) terrainScaleZ));
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);

		// build avatar
		avatar = new GameObject(GameObject.root(), robotS, robottx);
		initialTranslation = (new Matrix4f()).translation((float) avatarPosX, (float) avatarPosY, (float) avatarPosZ);
		initialScale = (new Matrix4f()).scaling((float) avatarScale);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(180.0f)));

		// build speed buff
		lightning = new GameObject(GameObject.root(), lightningS, lightningtx);
		initialTranslation = (new Matrix4f()).translation(
				setRandomLocation(),
				1,
				setRandomLocation());
		lightning.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling((float) buffScale);
		lightning.setLocalScale(initialScale);

		miniLightning = new GameObject(GameObject.root(), lightningS, lightningtx);
		miniLightning.setLocalLocation(avatar.getLocalLocation().add(new Vector3f(0.0f, 0.0f, 1.5f)));
		miniLightning.setLocalScale(new Matrix4f().scaling((float) miniBuffScale));
		miniLightning.setParent(avatar);
		miniLightning.propagateTranslation(true);
		miniLightning.propagateRotation(true);
		miniLightning.getRenderStates().disableRendering();

		// build power buff
		power = new GameObject(GameObject.root(), lightningS, powertx);
		initialTranslation = (new Matrix4f()).translation(
				setRandomLocation(),
				1,
				setRandomLocation());
		power.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f().scaling((float) buffScale));
		power.setLocalScale(initialScale);

		minipower = new GameObject(GameObject.root(), lightningS, powertx);
		minipower.setLocalLocation(avatar.getLocalLocation().add(new Vector3f(1.5f, 0.0f, 0.0f)));
		minipower.setLocalScale(new Matrix4f().scaling((float) miniBuffScale));
		minipower.setParent(avatar);
		minipower.propagateTranslation(true);
		minipower.propagateRotation(true);
		minipower.getRenderStates().disableRendering();

		// build alien
		alien = new GameObject(GameObject.root(), alienS, alientx);
		initialTranslation = (new Matrix4f()).translation(
				setRandomLocation(),
				0,
				setRandomLocation());
		alien.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling((float) alienScale);
		alien.setLocalScale(initialScale);
		alienS.playAnimation("WALK", (float) alienWalkAniSpeed, AnimatedShape.EndType.LOOP, 0);

	}

	@Override
	public void initializeLights() {
		// Ambient light
		Light.setGlobalAmbient(0.2f, 0.2f, 0.2f);
		// Positional light
		light1 = new Light();
		light1.setType(LightType.SPOTLIGHT);
		light1.setAmbient(1.0f, 1.0f, 1.0f);
		light1.setSpecular(0, 0, 0);
		light1.setLocation(new Vector3f((float) avatarPosX, (float) avatarPosY, (float) avatarPosZ));
		(engine.getSceneGraph()).addLight(light1);

		light2 = new Light();
		light2.setSpecular(0, 0, 0);

		light2.setType(LightType.POSITIONAL);
		light2.setLocation(new Vector3f(lightning.getWorldLocation()));
		(engine.getSceneGraph()).addLight(light2);

		light3 = new Light();
		light3.setSpecular(0, 0, 0);

		light3.setType(LightType.POSITIONAL);
		light3.setLocation(new Vector3f(power.getWorldLocation()));
		(engine.getSceneGraph()).addLight(light3);
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();

		// Initialize JavaScript engine
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// Initialize parameters in InitParams.js
		scriptFile1 = new File("assets/scripts/InitParams.js");
		this.runScript(scriptFile1);

		health = ((int) (jsEngine.get("health")));
		timerLength = ((int) (jsEngine.get("timerLength")));
		isAxesOn = ((boolean) (jsEngine.get("isAxesOn")));
		rotationSpeed = ((double) (jsEngine.get("rotationSpeed")));
		speed = ((double) (jsEngine.get("speed")));
		strength = ((int) (jsEngine.get("strength")));
		toggleLights = ((boolean) (jsEngine.get("toggleLights")));
		isAvatarAlive = ((boolean) (jsEngine.get("isAvatarAlive")));

		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ---------------- Camera ----------------------
		cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		// ---------------- Orbital Controller ------------
		orbitController = new CameraOrbit3D(cam, avatar, engine);

		// ----------------- Node Controllers -------------------
		rc = new RotationController(engine, new Vector3f(0, 1, 0), (float) rotationSpeed);
		(engine.getSceneGraph()).addNodeController(rc);

		rc.toggle();

		rc.addTarget(lightning);
		rc.addTarget(miniLightning);
		rc.addTarget(power);
		rc.addTarget(minipower);

		// --------------View Port---------------------
		(engine.getRenderSystem()).addViewport("VIEWPORT", 0.75f, 0f, 0.25f, 0.25f);
		Viewport vp = (engine.getRenderSystem()).getViewport("VIEWPORT");
		vpCam = vp.getCamera();

		vp.setHasBorder(true);
		vp.setBorderWidth(4);
		vp.setBorderColor(0.0f, 1.0f, 0.0f);

		// Set viewport camera above avatar
		vpCam.setLocation(new Vector3f(avatar.getWorldLocation().x(),
				avatar.getWorldLocation().y() + 10,
				avatar.getWorldLocation().z()));
		vpCam.setU(new Vector3f(1, 0, 0));
		vpCam.setV(new Vector3f(0, 0, -1));
		vpCam.setN(new Vector3f(0, -1, 0));

		// --------- INPUTS SECTION------------
		im = engine.getInputManager();

		setupNetworking();

		FwdAction fwdAction = new FwdAction(this, protClient);
		BwdAction bwdAction = new BwdAction(this, protClient);
		LeftAction leftAction = new LeftAction(this, protClient);
		RightAction rightAction = new RightAction(this, protClient);
		FwdBwdAction fwdBwdAction = new FwdBwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
		FireAction fireAction = new FireAction(this, protClient);

		ToggleAxesAction toggleAxesAction = new ToggleAxesAction(this);

		Avatar1Action avatar1Action = new Avatar1Action(this);
		Avatar2Action avatar2Action = new Avatar2Action(this);

		ToggleLightAction toggleLightAction = new ToggleLightAction(this);

		// Keyboard

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

		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, fireAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._1, avatar1Action,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._2, avatar2Action,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._3, toggleAxesAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.C, toggleLightAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Gamepad

		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._1, fireAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._2, toggleLightAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._3, toggleAxesAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Axis.Y, fwdBwdAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Axis.X, turnAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._4, avatar1Action,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(
				net.java.games.input.Component.Identifier.Button._5, avatar2Action,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// --Initialize Physics System--
		physicsObjects = new HashMap<Integer, GameObject>();

		String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0f, 0f, 0f };
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);

		// --Create Physics World--
		float mass = 5.0f;
		float up[] = { 0, 1, 0 };
		double[] tempTransform;

		float alienBoxSize[] = { 4f, 5f, 4f };
		float tempTValues[] = new float[16];
		tempTransform = toDoubleArray(alien.getLocalTranslation().get(tempTValues));
		alienBoxP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass, tempTransform, alienBoxSize);
		alien.setPhysicsObject(alienBoxP);

		updateProjectile();

		// Initialize Sound
		initAudio();
	}

	@Override
	public void update() {
		// Calculate elapsed time
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime += (currFrameTime - lastFrameTime) / 1000.0;

		int elapsTimeSec = Math.round((float) elapsTime);

		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		deltaTime = elapsedTime * 0.03;

		updateProjectile();

		// Track viewport camera with avatar
		vpCam.setLocation(new Vector3f(avatar.getWorldLocation().x(),
				avatar.getWorldLocation().y + 15,
				avatar.getWorldLocation().z()));

		// Update Sound
		alienSound.setLocation(alien.getWorldLocation());
		ambientSound.setLocation(terr.getWorldLocation());
		laserSound.setLocation(avatar.getWorldLocation());

		setEarParameters();

		// Update Lights
		if (toggleLights) {
			light1.setLocation(avatar.getWorldLocation());
			light1.setDirection(avatar.getWorldForwardVector());
			light1.setDiffuse(1, 1, 1);
			toggleLights = true;
		} else {
			light1.setDiffuse(0, 0, 0);
			toggleLights = false;
		}

		// Update alien physics
		updateAlienBoxLoc();

		// Update Physics
		if (running = true) {
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float) elapsedTime);
			for (GameObject go : engine.getSceneGraph().getGameObjects()) {
				if (go.getPhysicsObject() != null) {
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3, 0, mat.m30());
					mat2.set(3, 1, mat.m31());
					mat2.set(3, 2, mat.m32());
					go.setLocalTranslation(mat2);

					mat2.set(2, 0, mat.m20());
					mat2.set(2, 1, mat.m21());
					mat2.set(2, 2, mat.m22());
					mat2.set(1, 0, mat.m10());
					mat2.set(1, 1, mat.m11());
					mat2.set(1, 2, mat.m12());
					AxisAngle4f aa = new AxisAngle4f();
					mat2.getRotation(aa);
					Matrix4f rotMatrix = new Matrix4f();
					rotMatrix.rotation(aa);
					go.setLocalRotation(rotMatrix);
				}
			}
		}

		// Build Main HUD
		float mainRelativeLeft = engine.getRenderSystem().getViewport("MAIN").getRelativeLeft();
		float mainRelativeBottom = engine.getRenderSystem().getViewport("MAIN").getRelativeBottom();
		float mainActualWidth = engine.getRenderSystem().getViewport("MAIN").getActualWidth();
		float mainActualHeight = engine.getRenderSystem().getViewport("MAIN").getActualHeight();

		String healthCounterStr = Integer.toString(health);
		String dispStr1 = "Enemy Health = " + healthCounterStr;
		Vector3f redColor = new Vector3f(1, 0, 0); // red
		Vector3f greenColor = new Vector3f(0, 1, 0); // green
		Vector3f blueColor = new Vector3f(0, 0, 1); // blue

		(engine.getHUDmanager()).setHUD1(dispStr1,
				redColor,
				(int) (mainActualWidth / 2 + 10),
				(int) (mainActualHeight - 25));

		timeLeft = timerLength - elapsTimeSec;
		if (timeLeft < 0) {
			timeLeft = 0;
		}

		String timerStr = Integer.toString(timeLeft);
		String dispStr2 = "Time Left = " + timerStr;

		(engine.getHUDmanager()).setHUD2(dispStr2,
				blueColor,
				(int) (mainRelativeLeft * mainActualWidth + 10),
				(int) (mainRelativeBottom * mainActualHeight + 10));

		// Build Viewport HUD
		float vpRelativeLeft = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeLeft();
		float vpRelativeBottom = engine.getRenderSystem().getViewport("VIEWPORT").getRelativeBottom();
		float vpActualWidth = engine.getRenderSystem().getViewport("VIEWPORT").getActualWidth();
		float vpActualHeight = engine.getRenderSystem().getViewport("VIEWPORT").getActualHeight();

		// Update altitude of avatar based on height map
		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

		Vector3f alienLoc = alien.getWorldLocation();
		float alienHeight = terr.getHeight(alienLoc.x(), alienLoc.z());

		alien.setLocalLocation(new Vector3f(alienLoc.x(), alienHeight, alienLoc.z()));

		// Update Lights
		light1.setLocation(avatar.getWorldLocation());
		light1.setDirection(avatar.getWorldForwardVector());

		// Chase
		chaseAvatar();

		// Power Up
		updateBuffs();

		// Lose Condition
		if (alien.getWorldLocation().distance(avatar.getLocalLocation()) <= 1 || timeLeft == 0) {
			avatar.getRenderStates().disableRendering();
			toggleLightOff();
			alienS.stopAnimation();
			isAvatarAlive = false;
		}

		// Win Condition
		if (health <= 0) {
			health = 0;
			alien.getRenderStates().disableRendering();
			alienSound.stop();
		}

		im.update((float) elapsedTime);
		orbitController.updateCameraPosition();
		alienS.updateAnimation();
		processNetworking((float) elapsedTime);
	}

	// Update Lights

	// Physics Engine Collision Check
	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		dynamicsWorld = ((JBulletPhysicsEngine) physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i = 0; i < manifoldCount; i++) {
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++) {
				contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f) {
					updateEnemyHP();
					// System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				}
			}
		}
	}

	// Lasers
	private LinkedList<Laser> activeLasers = new LinkedList<Laser>();
	private LinkedList<Laser> inactiveLasers = new LinkedList<Laser>();

	public Laser createLaser(Vector2f direction, Vector2f pos, float speed) {
		Laser laser = null;

		if (inactiveLasers.size() > 0) {
			laser = inactiveLasers.getFirst();
			laser.getGameObject().getRenderStates().enableRendering();
			activeLasers.addLast(laser);
			inactiveLasers.removeFirst();
		} else {
			GameObject gameObject = new GameObject(GameObject.root(), laserBeamS, lasertx);
			gameObject.setLocalScale(new Matrix4f().scaling(.05f));
			float vals[] = new float[16];
			int uid = physicsEngine.nextUID();
			double[] transform = toDoubleArray(gameObject.getLocalTranslation().get(vals));
			PhysicsObject physicsObject = physicsEngine.addSphereObject(uid, 1f, transform, .05f);
			physicsObject.setBounciness(1f);
			physicsObject.setFriction(0f);
			gameObject.setPhysicsObject(physicsObject);
			physicsObjects.put(uid, gameObject);

			laser = new Laser(gameObject, this);
			activeLasers.addLast(laser);
		}
		laser.initialize(direction, pos, speed);

		return laser;
	}

	public void deactivateProjectile(Laser laser) {
		for (int i = 0; i < activeLasers.size(); i++) {
			if (activeLasers.get(i) == laser) {
				inactiveLasers.addLast(activeLasers.remove(i));
				laser.getGameObject().getRenderStates().disableRendering();
				return;
			}
		}
	}

	public void updateProjectile() {
		Laser activeLasers[] = new Laser[this.activeLasers.size()];

		this.activeLasers.toArray(activeLasers);
		for (Laser activeLaser : activeLasers) {
			activeLaser.update((float) elapsedTime / 1000);

		}
	}

	// Generates random number between -20 and 20 for loc coords
	private int setRandomLocation() {
		Random r = new Random();
		int randomNum = r.nextInt(20 + 20) - 20;
		return randomNum;
	}

	// Toggles render for Line axes
	public void toggleAxes() {
		if (isAxesOn) {
			x.getRenderStates().disableRendering();
			y.getRenderStates().disableRendering();
			z.getRenderStates().disableRendering();
			isAxesOn = false;
		} else {
			x.getRenderStates().enableRendering();
			y.getRenderStates().enableRendering();
			z.getRenderStates().enableRendering();
			isAxesOn = true;
		}
	}

	// --------- SCRIPTING -----------
	private void runScript(File scriptFile) {
		try {
			FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader);
			fileReader.close();
		} catch (FileNotFoundException e1) {
			System.out.println(scriptFile + " not found " + e1);
		} catch (IOException e2) {
			System.out.println("IO problem with " + scriptFile + e2);
		} catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFile + e3);
		} catch (NullPointerException e4) {
			System.out.println("Null ptr exception reading " + scriptFile + e4);
		}
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public GhostManager getGhostManager() {
		return gm;
	}

	public Engine getEngine() {
		return engine;
	}

	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (protClient == null) {
			System.out.println("missing protocol host");
		} else { // Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}

	// ---------------------- AUDIO --------------

	public void initAudio() {
		AudioResource resource1, resource2, resource3;
		audioMgr = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}

		resource1 = audioMgr.createAudioResource("assets/sounds/alien.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/sounds/ambient.wav", AudioResourceType.AUDIO_STREAM);
		resource3 = audioMgr.createAudioResource("assets/sounds/laser.wav", AudioResourceType.AUDIO_SAMPLE);
		alienSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		ambientSound = new Sound(resource2, SoundType.SOUND_EFFECT, 50, true);
		laserSound = new Sound(resource3, SoundType.SOUND_EFFECT, 50, false);

		alienSound.initialize(audioMgr);
		ambientSound.initialize(audioMgr);
		laserSound.initialize(audioMgr);

		alienSound.setMaxDistance(10.0f);
		alienSound.setMinDistance(0.5f);
		alienSound.setRollOff(5.0f);

		ambientSound.setMaxDistance(10.0f);
		ambientSound.setMinDistance(0.5f);
		ambientSound.setRollOff(5.0f);

		laserSound.setMaxDistance(10.0f);
		laserSound.setMinDistance(0.5f);
		laserSound.setRollOff(5.0f);

		alienSound.setLocation(alien.getWorldLocation());
		ambientSound.setLocation(terr.getWorldLocation());
		laserSound.setLocation(avatar.getWorldLocation());

		setEarParameters();

		alienSound.play();
		ambientSound.play();

	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	public void playFireSound() {
		laserSound.play();

	}

	// -------------- TOGGLE LIGHTS ----------------
	public void toggleLightOn() {
		toggleLights = true;
	}

	public void toggleLightOff() {
		toggleLights = false;
	}

	// --------------------- CHASE BEHAVIOR --------------
	public void chaseAvatar() {
		if (alien.getWorldLocation().distance(avatar.getLocalLocation()) > 1 && isAvatarAlive) {
			float time = (float) elapsedTime % 10;
			alien.lookAt(avatar);
			Vector3f oldPosition = alien.getWorldLocation();
			Vector3f targetLocation = avatar.getLocalLocation();
			Vector4f chaseDirection = new Vector4f(1f, 1f, 1f, 1f);

			chaseDirection.mul(targetLocation.x() - oldPosition.x(), targetLocation.y() - oldPosition.y(),
					targetLocation.z() - oldPosition.z(), 0f);
			chaseDirection.mul(.0008f * time); // increases float increases speed
			Vector3f newPosition = oldPosition.add(chaseDirection.x(), chaseDirection.y(), chaseDirection.z());
			alien.setLocalLocation(newPosition);
		}
	}

	// --------------UPDATE ALIENBOX POSITION -------------
	public void updateAlienBoxLoc() {
		double[] tempTransform;
		float tempTValues[] = new float[16];
		tempTransform = toDoubleArray(alien.getLocalTranslation().get(tempTValues));
		alienBoxP.setTransform(tempTransform);
	}

	// ---------------- DAMAGE CALCULATION ---------------
	public void updateEnemyHP() {
		health -= strength;
	}

	// ------------- BUFF HANDLER ---------------------
	public void updateBuffs() {
		if (isAvatarAlive) {
			Matrix4f newTranslation;
			// Speed buff
			if (powerUpTime > 0) {
				powerUpTime -= 0.1f * elapsedTime;
			} else {
				powerUpTime = 0;
				speed = 0.02;
				miniLightning.getRenderStates().disableRendering();

			}

			if (avatar.getLocalLocation().distance(lightning.getLocalLocation()) < 1.2) {
				miniLightning.getRenderStates().enableRendering();
				powerUpTime = 1000;
				speed = 0.05;

				newTranslation = (new Matrix4f()).translation(
						setRandomLocation(),
						1,
						setRandomLocation());
				lightning.setLocalTranslation(newTranslation);
				light2.setLocation(lightning.getLocalLocation());

			}

			// Power buff
			if (strengthUpTime > 0) {
				strengthUpTime -= 0.1f * elapsedTime;
			} else {
				strengthUpTime = 0;
				strength = 1;
				minipower.getRenderStates().disableRendering();
			}

			if (avatar.getLocalLocation().distance(power.getLocalLocation()) < 1.2) {
				minipower.getRenderStates().enableRendering();
				strengthUpTime = 1000;
				strength = 3;

				newTranslation = (new Matrix4f()).translation(
						setRandomLocation(),
						1,
						setRandomLocation());
				power.setLocalTranslation(newTranslation);
				light3.setLocation(power.getLocalLocation());
			}
		}
	}

	// --------------------- GETTERS -------------

	public GameObject getAvatar() {
		return avatar;
	}

	public Camera getCamera() {
		return engine.getRenderSystem().getViewport("MAIN").getCamera();
	}

	public Camera getViewportCamera() {
		return engine.getRenderSystem().getViewport("VIEWPORT").getCamera();
	}

	public Vector3f getPlayerPosition() {
		return avatar.getWorldLocation();
	}

	public double getDeltaTime() {
		return this.deltaTime;
	}

	public ObjShape getNPCshape() {
		return alienS;
	}

	public TextureImage getNPCtexture() {
		return alientx;
	}

	public double getSpeed() {
		return speed;
	}

	public TextureImage getAvatar1texture() {
		return robottx;
	}

	public TextureImage getAvatar2texture() {
		return robottx2;
	}

	public boolean isLightOn() {
		return toggleLights;
	}

	// -------------PHYSICS UTILITY -------------
	public static float[] toFloatArray(double[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}

	public static double[] toDoubleArray(float[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}
}
