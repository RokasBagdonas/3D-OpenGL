import gmaths.*;

import java.nio.*;

import javax.naming.NameAlreadyBoundException;
import javax.xml.crypto.dsig.Transform;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;


public class M01_GLEventListener implements GLEventListener {

  private static final boolean DISPLAY_SHADERS = false;

  public M01_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(4f,6f,15f));
    this.camera.setTarget(new Vec3(0f,5f,0f));
  }

  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }

  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    disposeModels(gl);
  }

  // ***************************************************
  /* THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */

  private Camera camera;
  private Mat4 perspective; // ?
  private Model floor, cube, sphere, button;
  private Light light;
  private SGNode sceneGraphRoot;


  private void disposeModels(GL3 gl) {
    floor.dispose(gl);
    //cube.dispose(gl);
    sphere.dispose(gl);
    light.dispose(gl);
  }

  public void initialise(GL3 gl) {
    createRandomNumbers();
    //Load textures
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/chequerboard.jpg");
    // int[] textureId1 = TextureLibrary.loadTexture(gl, "textures/container2.jpg");
    // int[] textureId2 = TextureLibrary.loadTexture(gl, "textures/container2_specular.jpg");
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/jade.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/jade_specular.jpg");
    int[] textureId5 = TextureLibrary.loadTexture(gl, "textures/snow1.jpg");
    //setup light
    light = new Light(gl);
    light.setCamera(camera);



    // MODELS ***************************************************

    //create floor
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "vs_tt_05.txt", "fs_tt_05.txt");
    Material material = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
    Mat4 modelMatrix = Mat4Transform.scale(16,1f,16);
    floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId0);

    //create snowman model
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");

    // no texture version
    // shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04_notex.txt");

    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
      //Model matricies are irrelevant when using scene graph.
      //Scene graph supplies the transformations.
    // modelMatrix = Mat4.multiply(Mat4Transform.scale(3,3,3), Mat4Transform.translate(0,0.5f,0));
    // modelMatrix = Mat4.multiply(Mat4Transform.translate(0,4,0), modelMatrix);

    sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId5, textureId4);

    button = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3, textureId4);


    // no texture version
    // sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh);

    //create snowman scene graph
    sceneGraphRoot = new NameNode("Scene graph: snowman");

    NameNode base = new NameNode("base");
      Mat4 m = Mat4Transform.scale(3,3,3);
      m = Mat4.multiply(m, Mat4Transform.translate(0,0.5f,0.0f));
      TransformNode baseTransform = new TransformNode("scale(3,3,3); translate(0,0.5,0)", m);

      ModelNode baseShape = new ModelNode("base (sphere)", sphere);

    NameNode head = new NameNode("head");
      m = Mat4Transform.scale(2.5f,2.5f,2.5f);
      m = Mat4.multiply(m, Mat4Transform.translate(0.0f,1.5f,0.0f));
      TransformNode headTransform = new TransformNode("scale(2.62f,2.62f,2.62f); translate(0.0f,1.5f,0.0f);", m);

      ModelNode headShape = new ModelNode("head (sphere)", sphere);

    
    NameNode buttons = new NameNode("buttons");
    //1. - adjust the size. Move to the bottom spot. Translate the 2nd button and third button. translate third button
    //TODO: make separate translations for each, except scalling
    NameNode bottomButton = new NameNode("bottom button");
    float scale = 0.3f;
      m = Mat4Transform.scale(scale,scale,scale);
      m = Mat4.multiply(m, Mat4Transform.translate(0.0f,4f,6f));
      TransformNode initialButtonTransfom = new TransformNode("scale(1.1f, 1.1f, 1.1f); translate(0.0f,1.8f,0.9f);", m);

      ModelNode bottomButtonShape = new ModelNode("bottom button (sphere)", button);

    NameNode middleButton = new NameNode("middle button");
      m = Mat4Transform.translate(0.0f, 2f, 0.0f);
      TransformNode middleButtonTransform = new TransformNode("translate(0.0f, 0.3f, 0.0f);", m);

      ModelNode middleButtonShape = new ModelNode("middle button (sphere - button)", button);

    NameNode topButton = new NameNode("top button");
      m = Mat4Transform.translate(0.0f, 2f, 0.0f);
      TransformNode topButtonTransform = new TransformNode("translate(0.0f, 0.3f, 0.0f);", m);

      ModelNode topButtonShape = new ModelNode("top button (sphere - button)", button);

    NameNode eyes = new NameNode("eyes");
    float scaleEye = 0.3f;
    float eyeDistance = 0.35f;
    float eyeY = 4.3f;
    float eyeZ = 1.1f;

    NameNode leftEye = new NameNode("left eye");
      m =  Mat4Transform.scale(scaleEye,scaleEye,scaleEye);
      m = Mat4.multiply(Mat4Transform.translate(-eyeDistance, eyeY, eyeZ), m);
      // mLeftEye = Mat4.multiply(mLeftEye, Mat4Transform.translate(0.0f, 8.5f, 12f));

      TransformNode leftEyeTransform = new TransformNode("scale(" + scaleEye + ","  + scaleEye + "," + scaleEye + "); translate(-0.6f, 14f, 6f);", m);

      ModelNode leftEyeShape = new ModelNode("left eye (sphere - button)", button);


    NameNode rightEye = new NameNode("right eye");
    m =  Mat4Transform.scale(scaleEye,scaleEye,scaleEye);
    m = Mat4.multiply(Mat4Transform.translate(eyeDistance, eyeY, eyeZ), m);
    // mLeftEye = Mat4.multiply(mLeftEye, Mat4Transform.translate(0.0f, 8.5f, 12f));

    TransformNode rightEyeTransform = new TransformNode("scale(" + scaleEye + ","  + scaleEye + "," + scaleEye + "); translate(0.6f, 14f, 6f);", m);

    ModelNode rightEyeShape = new ModelNode("right eye (sphere - button)", button);

    NameNode nose = new NameNode("nose");
      m = Mat4Transform.scale(0.2f, 0.2f, 0.6f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 4f, 1.35f), m);

      TransformNode noseTransform = new TransformNode("scale(0.3f, 0.3f, 0.5f); translate(0.0f, 4f, 1f);", m);
      ModelNode noseShape = new ModelNode("nose (sphere - nose)", button);



    
    sceneGraphRoot.addChild(base);
      base.addChild(baseTransform);
        baseTransform.addChild(baseShape);
        
      base.addChild(head);
        head.addChild(headTransform);
          headTransform.addChild(headShape);
        head.addChild(eyes);

          eyes.addChild(leftEye);
            leftEye.addChild(leftEyeTransform);
              leftEyeTransform.addChild(leftEyeShape);
            eyes.addChild(rightEye);
            rightEye.addChild(rightEyeTransform);
              rightEyeTransform.addChild(rightEyeShape);

        head.addChild(nose);
          nose.addChild(noseTransform);
            noseTransform.addChild(noseShape);

      base.addChild(buttons);
      buttons.addChild(initialButtonTransfom);
        initialButtonTransfom.addChild(bottomButton);
          bottomButton.addChild(bottomButtonShape);
        initialButtonTransfom.addChild(middleButtonTransform);
          middleButtonTransform.addChild(middleButton);
            middleButton.addChild(middleButtonShape);
          middleButtonTransform.addChild(topButtonTransform);
            topButtonTransform.addChild(topButton);
              topButton.addChild(topButtonShape);

    
    
    










    sceneGraphRoot.update();

    sceneGraphRoot.print(0,false);


  }



  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    light.setPosition(getLightPosition());  // changing light position each frame
    light.render(gl);

    floor.render(gl);
    // cube.render(gl);
    sceneGraphRoot.draw(gl);
  }

  // The light's postion is continually being changed, so needs to be calculated for each frame.
  private Vec3 getLightPosition() {
    double elapsedTime = getSeconds()-startTime;
    float x = 5.0f*(float)(Math.sin(Math.toRadians(elapsedTime*50)));
    float y = 2.7f;
    float z = 5.0f*(float)(Math.cos(Math.toRadians(elapsedTime*50)));
    return new Vec3(x,y,z);

    //return new Vec3(5f,3.4f,5f);  // use to set in a specific position for testing
  }

    // ***************************************************
  /* TIME
   */

  private double startTime;

  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

  // ***************************************************
  /* An array of random numbers
   */

  private int NUM_RANDOMS = 1000;
  private float[] randoms;

  private void createRandomNumbers() {
    randoms = new float[NUM_RANDOMS];
    for (int i=0; i<NUM_RANDOMS; ++i) {
      randoms[i] = (float)Math.random();
    }
  }


}