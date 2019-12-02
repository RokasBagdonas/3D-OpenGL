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

  //Interaction **************************************
  private boolean animation = false;
  private double savedTime = 0;
  private boolean animateSlide = false;
  private boolean animateRock = false;
  private boolean animateRoll = false;
  private float snowmanXPos = 0.0f;
  private float snowmanZPos = 0.0f;


  public void startAnimation() {
    //animateSlide = true;
    animateRock = true;
    animateRoll = true;
    startTime = getSeconds()-savedTime;
  }
   
  public void stopAnimation() {
    animateSlide = false;
    animateRock = false;
    animateRoll = false;
    double elapsedTime = getSeconds()-startTime;
    savedTime = elapsedTime;
  }

  public void test() {
    System.out.println("Test M01 GL EVENT LISTENER");
  }
  // ***************************************************
  /* THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */

  private Camera camera;
  private Mat4 perspective; // ?
  private Model floor, cube, sphere, button, nose;
  private Light light, worldLight;
  private SGNode sceneGraphRoot;
  private TransformNode snowmanSlideTranslate = new TransformNode("translate(x,0,z);", Mat4Transform.translate(0.0f,0.0f,0.0f));
  private TransformNode snowmanBaseRotateZ = new TransformNode("base rotate(0,0,rotateZ)", Mat4Transform.rotateAroundZ(0));
  private TransformNode snowmanBaseRotateX = new TransformNode("base rotate(0,0,rotateX)", Mat4Transform.rotateAroundX(0));
  
  private TransformNode snowmanHeadRotateZ = new TransformNode("head rotate(0,0,rotateZ)", Mat4Transform.rotateAroundZ(0));
  private TransformNode snowmanHeadRotateX = new TransformNode("head rotate(0,0,rotateX)", Mat4Transform.rotateAroundX(0));

  private void disposeModels(GL3 gl) {
    floor.dispose(gl);
    //cube.dispose(gl);
    sphere.dispose(gl);
    light.dispose(gl);
    worldLight.dispose(gl);
  }

  public void initialise(GL3 gl) {
    createRandomNumbers();
    //Load textures
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/chequerboard.jpg");
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/jade.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/jade_specular.jpg");
    //snow textures from: https://opengameart.org/content/tomeks-seamless-snow-textures 
    int[] textureSnow1 = TextureLibrary.loadTexture(gl, "textures/tomek/snow7_d.jpg");
    int[] textureSnow1Specular = TextureLibrary.loadTexture(gl, "textures/tomek/snow7_s.jpg");
    //Coal1 texture: https://images.app.goo.gl/EbwyCAxWsHt1kpx16
    int[] textureCoal1 = TextureLibrary.loadTexture(gl, "textures/coal1.jpg");
    //Wood1 texture: https://opengameart.org/content/wood-texture-tiles
    int[] textureWood1 = TextureLibrary.loadTexture(gl, "textures/wood1.jpg");
    //"mud" texture: https://images.app.goo.gl/CwZs72esA97AFNiv8 
    int[] textureMud1 = TextureLibrary.loadTexture(gl, "textures/coffeeStains1.jpg");
    //setup light
    light = new Light(gl);
    light.setCamera(camera);
    
    worldLight = new Light(gl);
    worldLight.setCamera(camera);
    worldLight.setPosition(new Vec3(2f, 5.9f, 3f));
    



    // MODELS ***************************************************

    //create floor
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "vs_tt_05.txt", "fs_tt_05.txt");
    Material material = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
    Mat4 modelMatrix = Mat4Transform.scale(16,1f,16);
    floor = new Model(gl, camera, light, worldLight, shader, material, modelMatrix, mesh, textureId0, textureMud1, textureSnow1Specular);

    //create snowman model
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");

    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.8f, 0.8f, 0.8f), 15.0f);

    sphere = new Model(gl, camera, light, worldLight, shader, material, modelMatrix, mesh, textureSnow1, textureMud1, textureSnow1Specular);

    button = new Model(gl, camera, light, worldLight, shader, material, modelMatrix, mesh, textureCoal1, textureSnow1Specular);
    nose = new Model(gl, camera, light, worldLight, shader, material, modelMatrix, mesh, textureWood1, textureSnow1Specular);
    

    // no texture version
    // sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh);

    //create snowman scene graph
    sceneGraphRoot = new NameNode("Scene graph: snowman");

    NameNode base = new NameNode("base");
      Mat4 m = Mat4Transform.scale(3,3,3);
      m = Mat4.multiply(Mat4Transform.translate(0,1.5f,0.0f), m);
      TransformNode baseTransform = new TransformNode("scale(3,3,3); translate(0,0.5,0)", m);

      ModelNode baseShape = new ModelNode("base (sphere)", sphere);

    NameNode head = new NameNode("head");
      m = Mat4Transform.scale(2.5f,2.5f,2.5f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f,3.8f,0.0f), m);
      TransformNode headTransform = new TransformNode("scale(2.62f,2.62f,2.62f); translate(0.0f,1.5f,0.0f);", m);

      ModelNode headShape = new ModelNode("head (sphere)", sphere);

    
    NameNode buttons = new NameNode("buttons");

    NameNode bottomButton = new NameNode("bottom button");
    float scale = 0.25f;
      //1. scale and move all buttons to the bottom button position
      m = Mat4Transform.scale(scale,scale,scale);
      m = Mat4.multiply(Mat4Transform.translate(0.0f,1.2f,1.5f), m);
      TransformNode initialButtonTransfom = new TransformNode("scale(1.1f, 1.1f, 1.1f); translate(0.0f,1.8f,0.9f);", m);

      ModelNode bottomButtonShape = new ModelNode("bottom button (sphere)", button);

    NameNode middleButton = new NameNode("middle button");
      //2. move reamining button relative to the bottom one
      m = Mat4Transform.translate(0.0f, 2f, -0.01f);
      TransformNode middleButtonTransform = new TransformNode("translate(0.0f, 0.3f, 0.0f);", m);

      ModelNode middleButtonShape = new ModelNode("middle button (sphere - button)", button);
    
    NameNode topButton = new NameNode("top button");
      //3. move top button relative to the middle one
      m = Mat4Transform.translate(0.0f, 1.8f, -0.2f);
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

    NameNode nose0 = new NameNode("nose0");
      m = Mat4Transform.scale(0.2f, 0.2f, 0.6f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 4f, 1.35f), m);

      TransformNode nose0Transform = new TransformNode("scale(0.3f, 0.3f, 0.5f); translate(0.0f, 4f, 1f);", m);
      ModelNode nose0Shape = new ModelNode("nose0 (sphere - nose)", nose);

    NameNode mouth = new NameNode("mouth");
      m = Mat4Transform.scale(0.6f, 0.2f, 0.2f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 3.5f, 1.2f), m);

      TransformNode mouthTransform = new TransformNode("scale(0.3f, 0.3f, 0.5f); translate(0.0f, 4f, 1f);", m);
      ModelNode mouthShape = new ModelNode("mouth (sphere - mouth)", nose);


    
    sceneGraphRoot.addChild(snowmanSlideTranslate);
      //slide
      snowmanSlideTranslate.addChild(snowmanBaseRotateZ);
      //rock
      snowmanBaseRotateZ.addChild(snowmanBaseRotateX);
      snowmanBaseRotateX.addChild(base);

      //snowman
      base.addChild(baseTransform);
        baseTransform.addChild(baseShape);
        
        base.addChild(snowmanHeadRotateZ);
          snowmanHeadRotateZ.addChild(snowmanHeadRotateX);
          snowmanHeadRotateX.addChild(head);
        head.addChild(headTransform);
          headTransform.addChild(headShape);
        head.addChild(eyes);

        // base.addChild(head);
        // head.addChild(headTransform);
        //   headTransform.addChild(headShape);
        // head.addChild(eyes);

          eyes.addChild(leftEye);
            leftEye.addChild(leftEyeTransform);
              leftEyeTransform.addChild(leftEyeShape);
            eyes.addChild(rightEye);
            rightEye.addChild(rightEyeTransform);
              rightEyeTransform.addChild(rightEyeShape);

        head.addChild(nose0);
          nose0.addChild(nose0Transform);
            nose0Transform.addChild(nose0Shape);

        head.addChild(mouth);
          mouth.addChild(mouthTransform);
            mouthTransform.addChild(mouthShape);

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
    worldLight.render(gl);
    floor.render(gl);
    if (animateSlide){
      slideSnowman();
    }
    if(animateRock){
      rockSnowman();
    }

    if (animateRoll){
      rollSnowman();
    }
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

  private boolean incX = true;
  private boolean incZ = true;
  private void slideSnowman() {
    if(incX){
      if(snowmanXPos > 4.0f) incX = false;
      else snowmanXPos += 0.05f;
    }
    else if(!incX){
      if(snowmanXPos < -4.0f) incX = true;
      else snowmanXPos -= 0.05f;  
    }

    snowmanSlideTranslate.setTransform(Mat4Transform.translate(snowmanXPos, 0.0f, 0.0f));
    snowmanSlideTranslate.update();
  }
  private float rotateZ = 0;
  private float rotateX = 0;
  private final static int ROTATE_BASE_MAX = 45;
  private int signZ = 1;
  private int signX = 1;
  private void rockSnowman(){
    double elapsedTime = getSeconds()-startTime;
    
    if(rotateZ >= ROTATE_BASE_MAX) signZ = -1;
    else if (rotateZ <= -ROTATE_BASE_MAX) signZ = 1;

    if(rotateX >= ROTATE_BASE_MAX) signX = -1;
    else if (rotateX <= -ROTATE_BASE_MAX) signX = 1;

    rotateZ += signZ * Math.abs(0.5f*(float)(Math.sin(Math.toRadians(elapsedTime*10))));
    rotateX += signX * Math.abs(0.5f*(float)(Math.sin(Math.toRadians(elapsedTime*30))));

    snowmanBaseRotateZ.setTransform(Mat4Transform.rotateAroundZ(rotateZ));
    snowmanBaseRotateX.setTransform(Mat4Transform.rotateAroundX(rotateX));
    snowmanBaseRotateX.update();
    snowmanBaseRotateZ.update();

  }
  private float rotateHeadZ = 0;
  private float rotateHeadX = 0;
  private final static int ROTATE_HEAD_MAX = 20;
  private int signHeadZ = 1;
  private int signHeadX = 1;

  private void rollSnowman(){
    double elapsedTime = getSeconds()-startTime;
    
    if(rotateHeadZ >= ROTATE_HEAD_MAX) signHeadZ = -1;
    else if (rotateHeadZ <= -ROTATE_HEAD_MAX) signHeadZ = 1;

    if(rotateHeadX >= ROTATE_HEAD_MAX) signHeadX = -1;
    else if (rotateHeadX <= -ROTATE_HEAD_MAX) signHeadX = 1;

    rotateHeadZ += signHeadZ * Math.abs(0.2f*(float)(Math.sin(Math.toRadians(elapsedTime*40))));
    rotateHeadX += signHeadX * Math.abs(0.2f*(float)(Math.sin(Math.toRadians(elapsedTime*40))));

    snowmanHeadRotateZ.setTransform(Mat4Transform.rotateAroundZ(rotateHeadZ));
    snowmanHeadRotateX.setTransform(Mat4Transform.rotateAroundX(rotateHeadX));
    snowmanHeadRotateX.update();
    snowmanHeadRotateZ.update();
    
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