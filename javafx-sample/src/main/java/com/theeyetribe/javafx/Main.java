package com.theeyetribe.javafx;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import com.theeyetribe.javafx.scenes.SceneController;
import com.theeyetribe.javafx.utils.GazeFrameCache;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

/**
 * Main application class of program
 */
public class Main extends Application implements IGazeListener
{
    private SceneController mCurrentController;

    private Stage mStage;
    private Scene mScene;
    private Screen mScreen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        mStage = primaryStage;
        mStage.initStyle(StageStyle.UNDECORATED);

        mScreen = Screen.getPrimary();

        loadMainScene();

        //initial placement of window, center bottom. Ensures best calibration for large screens
        javafx.geometry.Rectangle2D bounds = mScreen.getVisualBounds();
        if(mScene.getWidth() < bounds.getWidth() && mScene.getHeight() < bounds.getHeight())
        {
            mStage.setX((bounds.getWidth() - mScene.getWidth()) * .5f);
            mStage.setY(bounds.getHeight() - mScene.getHeight());
        }
        else
        {
            //if screen bounds smaller than stage, shrink stage
            if(mScene.getWidth() > bounds.getWidth())
            {
                mStage.setWidth(bounds.getWidth());
            }
            if(mScene.getHeight() > bounds.getHeight())
            {
                mStage.setHeight(bounds.getHeight());
            }
        }

        GazeManager.getInstance().addGazeListener(this);
        GazeManager.getInstance().activateAsync();
    }

    @Override
    public void stop() throws Exception
    {
        if(null != mCurrentController)
            mCurrentController.onStop();

        // deactivating, listener removed at part of this call
        GazeManager.getInstance().deactivate();
    }

    public void loadMainScene() throws Exception
    {
        mScene = loadScene(mCurrentController, "scene_main.fxml");
    }

    public void loadCalibrationScene() throws Exception
    {
        mScene = loadScene(mCurrentController, "scene_calibrate.fxml");
    }

    public void loadEvaluationScene() throws Exception
    {
        mScene = loadScene(mCurrentController, "scene_evaluate.fxml");
    }

    public void closeProgram()
    {
        mStage.close();
    }

    private Scene loadScene(SceneController current, String newScene) throws Exception
    {
        //stop old scene if any
        if(null != current)
            current.onStop();

        //load main scene and bind controller
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("Bundle"));
        loader.setLocation(getClass().getClassLoader().getResource(newScene));
        Pane root = loader.load();

        //create new scene
        Scene scene = new Scene(root);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                mStage.close();
            }
        });

        //Since we use undecorated Stage, we need to handle window drag n drop manually
        final Point2D dragDelta = new Point2D();
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragDelta.x = event.getSceneX();
                dragDelta.y = event.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mStage.setX(event.getScreenX() - dragDelta.x);
                mStage.setY(event.getScreenY() - dragDelta.y);
            }
        });

        //show new scene
        mStage.setTitle("EyeTribe JavaFX");
        mStage.setScene(scene);
        mStage.show();

        //start scene once shown
        mCurrentController = loader.getController();
        mCurrentController.setMain(this);
        mCurrentController.onStart();

        return scene;
    }

    @Override
    public void onGazeUpdate(GazeData gazeData)
    {
        // Update gaze cache as long as program runs
        GazeFrameCache.getInstance().update(gazeData);
    }
}
