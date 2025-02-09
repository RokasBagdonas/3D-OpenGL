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
  public boolean animateSlide = false;
  public boolean animateRock = false;
  public boolean animateRoll = false;
  public boolean animateLight = true;
  private float snowmanXPos = 0.0f;
  private float snowmanZPos = 0.0f;


  public void startAnimation() {
    animateSlide = true;
    animateRock = true;
    animateRoll = true;
    animateLight = true;
    startTime = getSeconds()-savedTime;
  }
   
  public void stopAnimation() {
    animateSlide = false;
    animateRock = false;
    animateRoll = false;
    animateLight = false;
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
  private Mat4 perspective;
  private Model floor, wall, cube, sphere, button, nose;
  private Model hatBottom, hatTop1, feather;
  private Model metalBox;
  private Model pole, lamp, platform;
  private Light spotLight, worldLight;
  private SGNode sceneGraphRoot;
  //transforms for animations
  private TransformNode snowmanSlideTranslate = new TransformNode("translate(x,0,z);", Mat4Transform.translate(0.0f,0.0f,0.0f));

  private TransformNode snowmanBaseRotateZ = new TransformNode("base rotate(0,0,rotateZ)", Mat4Transform.rotateAroundZ(0));
  private TransformNode snowmanBaseRotateX = new TransformNode("base rotate(0,0,rotateX)", Mat4Transform.rotateAroundX(0));
  
  private TransformNode snowmanHeadRotateZ = new TransformNode("head rotate(0,0,rotateZ)", Mat4Transform.rotateAroundZ(0));
  private TransformNode snowmanHeadRotateX = new TransformNode("head rotate(0,0,rotateX)", Mat4Transform.rotateAroundX(0));

  private void disposeModels(GL3 gl) {
    floor.dispose(gl);
    //cube.dispose(gl);
    sphere.dispose(gl);
    spotLight.dispose(gl);
    worldLight.dispose(gl);
    hatBottom.dispose(gl);
    hatTop1.dispose(gl);
    feather.dispose(gl);
    metalBox.dispose(gl);
    wall.dispose(gl);
  }

  public void initialise(GL3 gl) {
    //Load textures
    //snow textures from: https://opengameart.org/content/tomeks-seamless-snow-textures
    int[] textureFloor1 = TextureLibrary.loadTexture(gl, "textures/tomek/snow10_d.jpg");
    
    //snow forest texture from: https://www.flickr.com/photos/mesec/44765934870 
    int[] textureWall1 = TextureLibrary.loadTexture(gl, "textures/snowy_forest.jpg");

    //snow textures from: https://opengameart.org/content/tomeks-seamless-snow-textures 
    int[] textureSnow1 = TextureLibrary.loadTexture(gl, "textures/tomek/snow7_d.jpg");
    int[] textureSnow1Specular = TextureLibrary.loadTexture(gl, "textures/tomek/snow7_s.jpg");
    //Coal1 texture: https://images.app.goo.gl/EbwyCAxWsHt1kpx16
    int[] textureCoal1 = TextureLibrary.loadTexture(gl, "textures/coal1.jpg");
    //Wood1 texture: https://opengameart.org/content/wood-texture-tiles
    int[] textureWood1 = TextureLibrary.loadTexture(gl, "textures/wood1.jpg");
    //"mud" texture: https://images.app.goo.gl/CwZs72esA97AFNiv8 
    int[] textureMud1 = TextureLibrary.loadTexture(gl, "textures/coffeeStains1.jpg");

    // https://opengameart.org/content/metalstone-textures 
    int[] textureMetal1 = TextureLibrary.loadTexture(gl, "textures/mtl_wall01_c.jpg");
    int[] textureMetal1Specular = TextureLibrary.loadTexture(gl, "textures/mtl_wall01_s.jpg");

    //hat texture from https://opengameart.org/content/fabric-green-towel-seamless-texture-with-normalmap 
    int[] textureHat1 = TextureLibrary.loadTexture(gl, "textures/GreenTowel_S.jpg"); 
    //setup spotlight (turing light around the snowman, taken from tutorials)
    spotLight = new Light(gl);
    spotLight.setCamera(camera);

    //world light setup 
    worldLight = new Light(gl);
    worldLight.setCamera(camera);
    worldLight.setPosition(new Vec3(5.6f, 9f, 8f));
    



    // MODELS ***************************************************

    //create floor
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "vs_tt_05.txt", "fs_tt_05.txt");
    Material material = new Material(new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.8f, 0.8f, 0.8f), new Vec3(0.1f, 0.1f, 0.1f), 60.0f);
    Mat4 modelMatrix = Mat4Transform.scale(16,1f,16);
    floor = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureFloor1, textureMud1);

    //create background wall
    Mat4 wallMatrix = Mat4Transform.scale(16,1f,16);
    wallMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), wallMatrix);
    wallMatrix = Mat4.multiply(Mat4Transform.translate(0.0f,8.0f, -8f), wallMatrix);
    wall = new Model(gl, camera, spotLight, worldLight, shader, material, wallMatrix, mesh, textureWall1);
    

    //create snowman model
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");

    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.8f, 0.8f, 0.8f), 15.0f);


    button = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureCoal1);
    nose = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureWood1);
    //use mud texture that is mixded with the snow
    sphere = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureSnow1, textureMud1, textureSnow1Specular);

    
    //create hat
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.3f, 0.3f, 0.3f), 60.0f);
    hatBottom = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureHat1);
    hatTop1 = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureHat1);
    feather = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureHat1);

    //create metal box
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1f, 1f, 1f), 4.0f);
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    metalBox = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureMetal1, textureMetal1Specular);
    
    //create spotlight objects 
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 50.0f);

    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    platform = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureMetal1, textureMetal1Specular);

    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    pole = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureMetal1, textureMetal1Specular);
    lamp = new Model(gl, camera, spotLight, worldLight, shader, material, modelMatrix, mesh, textureMetal1, textureMetal1Specular);
    

    //create snowman scene graph. Translation node descriptions are read from right to left
    sceneGraphRoot = new NameNode("Scene graph: snowman");
    //snowman body -----------------------------------
    NameNode base = new NameNode("base");
      Mat4 m = Mat4Transform.scale(3,3,3);
      m = Mat4.multiply(Mat4Transform.translate(0,1.5f,0.0f), m);
      TransformNode baseTransform = new TransformNode("translate(0,1.5f,0.0f); scale(3,3,3); ", m);

      ModelNode baseShape = new ModelNode("base (sphere)", sphere);

    NameNode head = new NameNode("head");
      m = Mat4Transform.scale(2.5f,2.5f,2.5f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f,3.8f,0.0f), m);
      TransformNode headTransform = new TransformNode("translate(0.0f,3.8f,0.0f); scale(2.5f,2.5f,2.5f);", m);

      ModelNode headShape = new ModelNode("head (sphere)", sphere);

    //buttons -----------------------------------
    NameNode buttons = new NameNode("buttons");

    NameNode bottomButton = new NameNode("bottom button");
    float scale = 0.25f;
      //1. scale and move all buttons to the bottom button position
      m = Mat4Transform.scale(scale,scale,scale);
      m = Mat4.multiply(Mat4Transform.translate(0.0f,1.2f,1.5f), m);
      TransformNode initialButtonTransfom = new TransformNode("translate(0.0f,1.8f,0.9f); scale(0.25f, 0.25f, 0.25f);", m);

      ModelNode bottomButtonShape = new ModelNode("bottom button (sphere)", button);

    NameNode middleButton = new NameNode("middle button");
      //2. move middle button relative to the bottom one
      m = Mat4Transform.translate(0.0f, 2f, -0.01f);
      TransformNode middleButtonTransform = new TransformNode("translate(0.0f, 2f, -0.01f);", m);

      ModelNode middleButtonShape = new ModelNode("middle button (sphere)", button);
    
    NameNode topButton = new NameNode("top button");
      //3. move top button relative to the middle one
      m = Mat4Transform.translate(0.0f, 1.8f, -0.2f);
      TransformNode topButtonTransform = new TransformNode("translate(0.0f, 1.8f, -0.2f);", m);

      ModelNode topButtonShape = new ModelNode("top button (sphere)", button);

    //eyes -----------------------------------
    //common variables for both eyes
    NameNode eyes = new NameNode("eyes");
    float scaleEye = 0.3f;
    float eyeDistance = 0.35f;
    float eyeY = 4.3f;
    float eyeZ = 1.1f;

    NameNode leftEye = new NameNode("left eye");
      m =  Mat4Transform.scale(scaleEye,scaleEye,scaleEye);
      m = Mat4.multiply(Mat4Transform.translate(-eyeDistance, eyeY, eyeZ), m);
      TransformNode leftEyeTransform = new TransformNode("scale(" + scaleEye + ","  + scaleEye + "," + scaleEye + "); translate(0.3f, 4.3f, 1.1f);", m);

      ModelNode leftEyeShape = new ModelNode("left eye (sphere)", button);


    NameNode rightEye = new NameNode("right eye");
      m =  Mat4Transform.scale(scaleEye,scaleEye,scaleEye);
      m = Mat4.multiply(Mat4Transform.translate(eyeDistance, eyeY, eyeZ), m);
      TransformNode rightEyeTransform = new TransformNode("scale(" + scaleEye + ","  + scaleEye + "," + scaleEye + ");  translate(-0.3f, 4.3f, 1.1f);", m);

      ModelNode rightEyeShape = new ModelNode("right eye (sphere)", button);

    //nose -----------------------------------
    NameNode nose0 = new NameNode("nose0");
      m = Mat4Transform.scale(0.2f, 0.2f, 0.6f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 4f, 1.35f), m);

      TransformNode nose0Transform = new TransformNode("translate(0.0f, 4f, 1.35f); scale(0.2f, 0.2f, 0.6f);", m);
      ModelNode nose0Shape = new ModelNode("nose0 (sphere)", nose);

    //mouth - same shape model as nose
    NameNode mouth = new NameNode("mouth");
      m = Mat4Transform.scale(0.6f, 0.2f, 0.2f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 3.5f, 1.2f), m);

      TransformNode mouthTransform = new TransformNode("translate(0.0f, 3.5f, 1.2f); scale(0.6f, 0.2f, 0.2f);", m);
      ModelNode mouthShape = new ModelNode("mouth (sphere)", nose);

    //hat Nodes -----------------------------------
    TransformNode hatInitialTransfrom = new TransformNode("translate(0.0f,5f, 0.0f)", Mat4Transform.translate(0.0f, 5f, 0.0f));

    NameNode hatBottomName = new NameNode("hat bottom");
      m = Mat4Transform.scale(2f,0.5f,2f);
      TransformNode hatCircle1Transform = new TransformNode("scale(2f,0.5f,2f);", m);
      ModelNode hatCircle1Shape = new ModelNode("base hat (sphere)", hatBottom);

    NameNode hatTop = new NameNode("hatTop");
      m = Mat4Transform.scale(1.4f,1.4f,1.4f);
      m = Mat4.multiply(Mat4Transform.translate(0.0f, 0.2f, 0.0f), m);
      TransformNode hatTopTransform = new TransformNode("translate(0.0f, 0.2f, 0.0f); scale(1.4f,1.4f,1.4f);", m);
      ModelNode hatTopShape = new ModelNode("hat top (sphere)", hatTop1);

      //feathers
      //transform for both feahters ---------------------------------
      Mat4 featherM = Mat4Transform.scale(0.3f,0.8f,0.1f);
      featherM = Mat4.multiply(Mat4Transform.rotateAroundY(90), featherM);
      featherM = Mat4.multiply(Mat4Transform.rotateAroundX(-45), featherM);
      NameNode feathers = new NameNode("feathers");
    
    NameNode featherRight = new NameNode("featherRight");
      m = Mat4.multiply(Mat4Transform.translate(0.7f, 0.4f, 0.0f), featherM);
      TransformNode featherRightTransform = new TransformNode("translate(0.7f, 0.4f, 0.0f); rotateAroundX(-45); rotateAroundY(90);  scale(0.3f,0.8f,0.1f);", m);
      ModelNode featherRightShape = new ModelNode("feahter right (sphere)", feather);

    NameNode featherLeft = new NameNode("featherLeft");
      m = Mat4.multiply(Mat4Transform.translate(-0.7f, 0.4f, 0.0f), featherM);
      TransformNode featherLeftTransform = new TransformNode("translate(-0.7f, 0.4f, 0.0f); rotateAroundX(-45); rotateAroundY(90);  scale(0.3f,0.8f,0.1f);", m);
      ModelNode featherLeftShape = new ModelNode("feahter left (sphere)", feather);

    //metal object ----------------------------------
    NameNode box = new NameNode("metal box");
      m = Mat4Transform.scale(1f, 3.5f, 1f);
      m = Mat4.multiply(Mat4Transform.translate(2.5f, 1.75f, 3.5f), m);
      TransformNode boxTransform = new TransformNode("scale(1f, 3.5f, 1f); translate(2.5f, 1.75f, 3.5f);", m );
      ModelNode boxShape = new ModelNode("metal box (cube)", metalBox);

    
    //spotlight structure ---------------------------------------
    //intial translate matrix to position both elements at an initial spot
    NameNode spotlight = new NameNode("spotlight");
    m = Mat4Transform.translate(-3.5f, 0.0f, 3.5f);
    TransformNode spotlightTranslate = new TransformNode("translate(-3.5f, 0.0f, 3.5f);", m);

    //pole platform 

    NameNode spotlightPlatform = new NameNode("spotlight platform");
      m = Mat4Transform.scale(1.4f, 0.3f, 1.4f);
      TransformNode platformScale = new TransformNode("scale(1.4f, 0.3f, 1.4f);", m);
      ModelNode platformShape = new ModelNode("spotlight platform (cube)", platform);

    //pole
    TransformNode poleTranslate = new TransformNode("name", Mat4Transform.translate(0.0f, 4f, 0.0f));

    NameNode spotlightPole = new NameNode("spotlight pole");
      m = Mat4Transform.scale(0.5f, 10f, 0.5f);
      TransformNode poleScale = new TransformNode("scale(0.5f, 10f, 0.5f);", m);
      ModelNode poleShape = new ModelNode("spotlight pole (sphere)", pole);

    //lamp
    NameNode spotlightLamp = new NameNode("spotlight lamp");
      m = Mat4Transform.scale(0.4f, 1.7f, 0.4f);
      m = Mat4.multiply(Mat4Transform.rotateAroundZ(-120), m);
      m = Mat4.multiply(Mat4Transform.translate(0.3f, 4.8f, 0.0f), m);
      TransformNode lampTransform = new TransformNode("translate(0.3f, 4.8f, 0.0f); rotateAroundZ(-120); scale(0.4f, 1.7f, 0.4f);", m);
      ModelNode lampShape = new ModelNode("spotlight lamp (sphere)", lamp);

    



    sceneGraphRoot.addChild(snowmanSlideTranslate);
      //slide
      snowmanSlideTranslate.addChild(snowmanBaseRotateZ);
      //rock
      snowmanBaseRotateZ.addChild(snowmanBaseRotateX);
      snowmanBaseRotateX.addChild(base);

      //snowman
      base.addChild(baseTransform);
        baseTransform.addChild(baseShape);

        //roll
        base.addChild(snowmanHeadRotateZ);
          snowmanHeadRotateZ.addChild(snowmanHeadRotateX);
          snowmanHeadRotateX.addChild(head);
        head.addChild(headTransform);
          headTransform.addChild(headShape);
        head.addChild(eyes);

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

        head.addChild(hatInitialTransfrom); //used to set same starting position for all the elements
        hatInitialTransfrom.addChild(hatBottomName );
          hatBottomName .addChild(hatCircle1Transform);
            hatCircle1Transform.addChild(hatCircle1Shape);
          hatBottomName .addChild(hatTop);
            hatTop.addChild(hatTopTransform);
              hatTopTransform.addChild(hatTopShape);

            hatTop.addChild(feathers);
              feathers.addChild(featherRight);
                featherRight.addChild(featherRightTransform);
                featherRightTransform.addChild(featherRightShape);
              feathers.addChild(featherLeft);
                featherLeft.addChild(featherLeftTransform);
                featherLeftTransform.addChild(featherLeftShape);

      base.addChild(buttons);
      buttons.addChild(initialButtonTransfom); //set all buttons at the same starting point
        initialButtonTransfom.addChild(bottomButton);
          bottomButton.addChild(bottomButtonShape);
        initialButtonTransfom.addChild(middleButtonTransform); //move buttons up
          middleButtonTransform.addChild(middleButton);
            middleButton.addChild(middleButtonShape);
          middleButtonTransform.addChild(topButtonTransform); //final position for the top button
            topButtonTransform.addChild(topButton);
              topButton.addChild(topButtonShape);

      sceneGraphRoot.addChild(box);
        box.addChild(boxTransform);
          boxTransform.addChild(boxShape);

    sceneGraphRoot.addChild(spotlight);
    spotlight.addChild(spotlightTranslate);
      spotlightTranslate.addChild(spotlightPlatform);
        spotlightPlatform.addChild(platformScale);
          platformScale.addChild(platformShape);

      
      spotlightTranslate.addChild(poleTranslate);
        poleTranslate.addChild(spotlightPole);
          spotlightPole.addChild(poleScale);
            poleScale.addChild(poleShape);

        poleTranslate.addChild(spotlightLamp);
        spotlightLamp.addChild(lampTransform);
          lampTransform.addChild(lampShape);


    sceneGraphRoot.update();

    sceneGraphRoot.print(0,false);

  }


  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    if(animateLight) spotLight.setPosition(getLightPosition());  // changing spotLight position each frame
    spotLight.render(gl);
    worldLight.render(gl);
    floor.render(gl);
    wall.render(gl);

    if (animateSlide) slideSnowman();
    if(animateRock) rockSnowman();
    if (animateRoll) rollSnowman();
      
    // cube.render(gl);
    sceneGraphRoot.draw(gl);
  }

  // The spotLight's postion is continually being changed, so needs to be calculated for each frame.
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
  private float SLIDE_DISTANCE = 2.8f;
  private void slideSnowman() {
    if(incX){
      if(snowmanXPos > SLIDE_DISTANCE) incX = false;
      else snowmanXPos += 0.02f;
    }
    else if(!incX){
      if(snowmanXPos < -SLIDE_DISTANCE) incX = true;
      else snowmanXPos -= 0.02f;  
    }

    snowmanSlideTranslate.setTransform(Mat4Transform.translate(snowmanXPos, 0.0f, 0.0f));
    snowmanSlideTranslate.update();
  }

  //variables that change the snowman's base (body) position
  private float rotateZ = 0;
  private float rotateX = 0;

  private final static int ROTATE_BASE_MAX = 25;
  //for toggling sin function sign when rotateX/Z >= ROTATE_BASE_MAX
  private int signZ = 1;
  private int signX = 1;
  private void rockSnowman(){
    double elapsedTime = getSeconds()-startTime;
    
    if(rotateZ >= ROTATE_BASE_MAX) signZ = -1;
    else if (rotateZ <= -ROTATE_BASE_MAX) signZ = 1;

    if(rotateX >= ROTATE_BASE_MAX) signX = -1;
    else if (rotateX <= -ROTATE_BASE_MAX) signX = 1;

    rotateZ += signZ * Math.abs(0.6f*(float)(Math.sin(Math.toRadians(elapsedTime*8))));
    rotateX += signX * Math.abs(0.6f*(float)(Math.sin(Math.toRadians(elapsedTime*12))));

    snowmanBaseRotateZ.setTransform(Mat4Transform.rotateAroundZ(rotateZ));
    snowmanBaseRotateX.setTransform(Mat4Transform.rotateAroundX(rotateX));
    snowmanBaseRotateX.update();
    snowmanBaseRotateZ.update();

  }

  
  //variables that change the snowman's head (body) position
  private float rotateHeadZ = 0;
  private float rotateHeadX = 0;
  private final static int ROTATE_HEAD_MAX = 10;
    //for toggling sin function sign when rotateX/Z >= ROTATE_HEAD_MAX
  private int signHeadZ = 1;
  private int signHeadX = 1;

  private void rollSnowman(){
    double elapsedTime = getSeconds()-startTime;
    
    if(rotateHeadZ >= ROTATE_HEAD_MAX) signHeadZ = -1;
    else if (rotateHeadZ <= -ROTATE_HEAD_MAX) signHeadZ = 1;

    if(rotateHeadX >= ROTATE_HEAD_MAX) signHeadX = -1;
    else if (rotateHeadX <= -ROTATE_HEAD_MAX) signHeadX = 1;

    rotateHeadZ += signHeadZ * Math.abs(0.4f*(float)(Math.sin(Math.toRadians(elapsedTime*12))));
    rotateHeadX += signHeadX * Math.abs(0.4f*(float)(Math.sin(Math.toRadians(elapsedTime*8))));

    snowmanHeadRotateZ.setTransform(Mat4Transform.rotateAroundZ(rotateHeadZ));
    snowmanHeadRotateX.setTransform(Mat4Transform.rotateAroundX(rotateHeadX));
    snowmanHeadRotateX.update();
    snowmanHeadRotateZ.update();
    
  }

  public void resetSnowman(){
    animateSlide = false; animateRoll = false; animateRock = false;
  snowmanSlideTranslate.setTransform(Mat4Transform.translate(0.0f,0.0f,0.0f));
  snowmanSlideTranslate.update();

  snowmanBaseRotateZ.setTransform(Mat4Transform.rotateAroundZ(0));
  snowmanBaseRotateX.setTransform(Mat4Transform.rotateAroundX(0));
  snowmanBaseRotateX.update();
  snowmanBaseRotateZ.update();
  snowmanHeadRotateZ.setTransform(Mat4Transform.rotateAroundZ(0));
  snowmanHeadRotateX.setTransform(Mat4Transform.rotateAroundX(0));
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