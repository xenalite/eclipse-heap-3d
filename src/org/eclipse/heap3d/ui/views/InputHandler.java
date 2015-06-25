package org.eclipse.heap3d.ui.views;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class InputHandler {

	private static final float FREQUENCY = 1000F / 60F;
	private static float SPEED_UP_FACTOR = 2;
	private static float NORMAL_SPEED = 50;
	
	private Configuration configuration;
	private BasicTransforms cameraTransforms = new BasicTransforms();
	private List<BasicTransforms> labels = new ArrayList<>();

	private double mousePosX;
	private double mousePosY;
	private double mouseOldX;
	private double mouseOldY;

	private float movement = 50;
	private boolean forward = false;
	private boolean backward = false;
	private boolean left = false;
	private boolean right = false;
	private boolean up = false;
	private boolean down = false;

	public InputHandler(Scene scene, Configuration configuration) {
		this.configuration = configuration;
		initialiseTickEvents();
		initialiseCamera(scene);
		initialiseMouseEventHandlers(scene);
		initialiseKeyboardEventHandlers(scene);
	}

	public List<BasicTransforms> getLabels() {
		return labels;
	}

	private void initialiseCamera(Scene scene) {
		Camera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll(new Rotate(180, Rotate.Z_AXIS));
		camera.setNearClip(0.1);
		camera.setFarClip(Double.MAX_VALUE);
		cameraTransforms.getChildren().add(camera);

		resetCamera();
		scene.setCamera(camera);
	}

	private void resetCamera() {
		cameraTransforms.setRy(-135);
		cameraTransforms.setTranslate(new Point3D(2000, 500, 2000));
	}

	private void initialiseTickEvents() {
		KeyFrame keyFrame = new KeyFrame(Duration.millis(FREQUENCY), event -> {
			if (forward)
				handleMovementForwardBackward(true);
			if (backward)
				handleMovementForwardBackward(false);
			if (left)
				handleMovementLeftRight(true);
			if (right)
				handleMovementLeftRight(false);
			if (up)
				handleMovementUpDown(true);
			if (down)
				handleMovementUpDown(false);

			for (BasicTransforms label : labels)
				rotateTextTowardsCamera(label);
		});

		Timeline timeline = new Timeline(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void rotateTextTowardsCamera(BasicTransforms labelTransforms) {
		Point3D to = labelTransforms.getTranslate();
		Point3D from = cameraTransforms.getTranslate();

		if (to.distance(from) > 20000) {
			labelTransforms.setVisible(false);
			return;
		} else
			labelTransforms.setVisible(true);

		Point3D direction2D = new Point3D(to.getX(), 0, to.getZ()).subtract(new Point3D(from.getX(), 0, from.getZ()));

		labelTransforms.setRy(direction2D.equals(Point3D.ZERO) ? 0
				: (from.getX() < to.getX() ? 1 : -1) * direction2D.angle(Rotate.Z_AXIS));

		labelTransforms.setRx((from.getY() > to.getY() ? 1 : -1)
				* (direction2D.equals(Point3D.ZERO) ? 90 : to.subtract(from).angle(direction2D)));
	}

	private void initialiseMouseEventHandlers(Scene scene) {
		scene.setOnMousePressed(event -> {
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();
			mouseOldX = event.getSceneX();
			mouseOldY = event.getSceneY();
		});

		scene.setOnMouseDragged(event -> {
			mouseOldX = mousePosX;
			mouseOldY = mousePosY;
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();

			if (event.isPrimaryButtonDown()) {
				cameraTransforms.setRy(cameraTransforms.getRyAngle() - (mousePosX - mouseOldX) * 0.2);
				double angle = cameraTransforms.getRxAngle() + (mousePosY - mouseOldY) * 0.2;
				angle = Math.max(angle, -90);
				angle = Math.min(angle, 90);
				cameraTransforms.setRx(angle);
			}
		});
	}

	private void initialiseKeyboardEventHandlers(Scene scene) {
		scene.setOnKeyPressed(event -> {
			KeyCode key = event.getCode();
			if(key.equals(configuration.forwardsKey))
				forward = true;
			if(key.equals(configuration.backwardsKey))
				backward = true;
			if(key.equals(configuration.leftKey))
				left = true;
			if(key.equals(configuration.rightKey))
				right = true;
			if(key.equals(configuration.upKey))
				up = true;
			if(key.equals(configuration.downKey))
				down = true;
			if(key.equals(configuration.resetCameraKey))
				resetCamera();
			if(key.equals(configuration.speedUpKey))
				movement = NORMAL_SPEED * SPEED_UP_FACTOR;
		});

		scene.setOnKeyReleased(event -> {
			KeyCode key = event.getCode();
			if(key.equals(configuration.forwardsKey))
				forward = false;
			if(key.equals(configuration.backwardsKey))
				backward = false;
			if(key.equals(configuration.leftKey))
				left = false;
			if(key.equals(configuration.rightKey))
				right = false;
			if(key.equals(configuration.upKey))
				up = false;
			if(key.equals(configuration.downKey))
				down = false;
			if(key.equals(configuration.speedUpKey))
				movement = NORMAL_SPEED;
		});
	}

	private void handleMovementForwardBackward(boolean isForward) {
		int coefficient = isForward ? 1 : -1;
		double l = movement * cos(toRadians(cameraTransforms.getRxAngle()));
		cameraTransforms.offsetTranslate(coefficient * l * sin(toRadians(cameraTransforms.getRyAngle())),
				-coefficient * movement * sin(toRadians(cameraTransforms.getRxAngle())),
				coefficient * l * cos(toRadians(cameraTransforms.getRyAngle())));
	}

	private void handleMovementLeftRight(boolean isLeft) {
		int coefficient = isLeft ? 1 : -1;
		cameraTransforms.offsetTranslate(
				movement * sin(toRadians((90 * coefficient) + cameraTransforms.getRyAngle())), 0,
				movement * cos(toRadians((90 * coefficient) + cameraTransforms.getRyAngle())));
	}

	private void handleMovementUpDown(boolean isUp) {
		int coefficient = isUp ? 1 : -1;
		cameraTransforms.offsetTranslate(0, coefficient * movement, 0);
	}
}