package tage;

import tage.*;
import org.joml.*;
import java.lang.Math;
import java.awt.event.*;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

/**
 * CameraOrbit3D targets and follows the avatar.
 * This camera allows to control the camera azimuth, elevation, and distance
 * from avatar
 * 
 * @author Rick Ammann
 */

public class CameraOrbit3D {
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target

    public CameraOrbit3D(Camera cam, GameObject av, Engine e) {
        engine = e;
        camera = cam;
        this.avatar = av;
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 2.0f; // distance from camera to avatar
        setupInputs();
        updateCameraPosition();
    }

    /** Assigns Orbit Camera actions to keyboard / controller using input manager */
    private void setupInputs() {
        InputManager im = engine.getInputManager();
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        OrbitAzimuthLeftAction azmLeftAction = new OrbitAzimuthLeftAction();
        OrbitAzimuthRightAction azmRightAction = new OrbitAzimuthRightAction();
        OrbitElevationAction elevAction = new OrbitElevationAction();
        OrbitElevationUpAction elevUpAction = new OrbitElevationUpAction();
        OrbitElevationDownAction elevDownAction = new OrbitElevationDownAction();
        OrbitZoomAction zoomAction = new OrbitZoomAction();
        OrbitZoomUpAction zoomUpAction = new OrbitZoomUpAction();
        OrbitZoomDownAction zoomDownAction = new OrbitZoomDownAction();

        // Keyboard
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.LEFT, azmLeftAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.RIGHT, azmRightAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.UP, elevUpAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.DOWN, elevDownAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.Q, zoomUpAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
                net.java.games.input.Component.Identifier.Key.E, zoomDownAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // Controller
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.RX, azmAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.RY, elevAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(
                net.java.games.input.Component.Identifier.Axis.Z, zoomAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    /**
     * Compute the camera’s azimuth, elevation, and distance, relative to
     * the target in spherical coordinates, then convert to world Cartesian
     * coordinates and set the camera position from that.
     */
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math
                .toDegrees((double) avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(cameraAzimuth);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    /** Updates the orbit camera azimuth based on controller X axis value */
    private class OrbitAzimuthAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float rotAmount;
            if (e.getValue() < -0.2) {
                rotAmount = 0.3f;
            } else if (e.getValue() > 0.2) {
                rotAmount = -0.3f;
            } else {
                rotAmount = 0.0f;
            }

            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    /** Updates orbit camera's azimuth to the left */
    private class OrbitAzimuthLeftAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float rotAmount;
            rotAmount = 0.3f;
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    /** Updates orbit camera's azimuth to the right */
    private class OrbitAzimuthRightAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float rotAmount;
            rotAmount = -0.3f;
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    /** Updates the orbit camera elevation based on controller Y axis value */
    private class OrbitElevationAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float pitchAmount;
            if (e.getValue() < -0.2) {
                pitchAmount = 0.3f;
            } else if (e.getValue() > 0.2) {
                pitchAmount = -0.3f;
            } else {
                pitchAmount = 0.0f;
            }

            // Restrict camera from going under
            if (cameraElevation + pitchAmount < 10.0f || cameraElevation + pitchAmount > 90.0f) {
                pitchAmount = 0.0f;
            }
            cameraElevation += pitchAmount;
            updateCameraPosition();
        }
    }

    /** Increases the orbit camera elevation */
    private class OrbitElevationUpAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float pitchAmount;
            pitchAmount = 0.3f;
            if (cameraElevation + pitchAmount < 10.0f || cameraElevation + pitchAmount > 90.0f) {
                pitchAmount = 0.0f;
            }
            cameraElevation += pitchAmount;
            updateCameraPosition();
        }
    }

    /** decreases the orbit camera elevation */
    private class OrbitElevationDownAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float pitchAmount;
            pitchAmount = -0.3f;
            if (cameraElevation + pitchAmount < 10.0f || cameraElevation + pitchAmount > 90.0f) {
                pitchAmount = 0.0f;
            }
            cameraElevation += pitchAmount;
            updateCameraPosition();
        }
    }

    /** Controls the zoom distance based on Z axis value of controller */
    private class OrbitZoomAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float zoomAmount;
            if (e.getValue() < -0.2) {
                zoomAmount = -0.1f;
            } else if (e.getValue() > 0.2) {
                zoomAmount = 0.1f;
            } else {
                zoomAmount = 0.0f;
            }

            if (cameraRadius + zoomAmount < 1.0f || cameraRadius + zoomAmount > 15.0f) {
                zoomAmount = 0.0f;
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }

    /** Increases the orbit camera zoom value */
    private class OrbitZoomUpAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float zoomAmount;
            zoomAmount = 0.1f;
            if (cameraRadius + zoomAmount < 1.0f || cameraRadius + zoomAmount > 15.0f) {
                zoomAmount = 0.0f;
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }

    /** Decreases the orbit camera zoom value */
    private class OrbitZoomDownAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            float zoomAmount;
            zoomAmount = -0.1f;
            if (cameraRadius + zoomAmount < 1.0f || cameraRadius + zoomAmount > 15.0f) {
                zoomAmount = 0.0f;
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }
}
