import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
/**
 * Rokas Bagdonas rbagdonas1@sheffield.ac.uk
 * I have used the code form M0X tutorials and built on top.
 */

public class Main extends JFrame implements ActionListener {
  
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
  private GLCanvas canvas;
  private M01_GLEventListener glEventListener;
  private final FPSAnimator animator; 


  public static void main(String[] args) {
    Main b1 = new Main("Main");
    b1.getContentPane().setPreferredSize(dimension);
    b1.pack();
    b1.setVisible(true);
  }

  public Main(String textForTitleBar) {
    super(textForTitleBar);
    GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
    canvas = new GLCanvas(glcapabilities);
    Camera camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
    glEventListener = new M01_GLEventListener(camera);
    canvas.addGLEventListener(glEventListener);
    canvas.addMouseMotionListener(new MyMouseInput(camera));
    canvas.addKeyListener(new MyKeyboardInput(camera));

    getContentPane().add(canvas, BorderLayout.CENTER);
    JMenuBar menuBar=new JMenuBar();
    this.setJMenuBar(menuBar);
      JMenu fileMenu = new JMenu("File");
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
    menuBar.add(fileMenu);
    
    JPanel p = new JPanel();
      
      JButton b = new JButton("slide");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("rock");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("roll");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("slide, rock & roll");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("reset snowman");
      b.addActionListener(this);
      p.add(b);
      
    this.add(p, BorderLayout.SOUTH);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animator.stop();
        remove(canvas);
        dispose();
        System.exit(0);
      }
    });
    animator = new FPSAnimator(canvas, 60);
    animator.start();
  }



  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("slide")) {
      this.glEventListener.animateSlide = !this.glEventListener.animateSlide;
    }
    else if (e.getActionCommand().equalsIgnoreCase("rock")) {
      this.glEventListener.animateRock = !this.glEventListener.animateRock;
    }
    else if (e.getActionCommand().equalsIgnoreCase("roll")) {
      this.glEventListener.animateRoll = !this.glEventListener.animateRoll;
    }
    else if (e.getActionCommand().equalsIgnoreCase("slide, rock & roll")) {
      this.glEventListener.animateSlide = !this.glEventListener.animateSlide;
      this.glEventListener.animateRock = !this.glEventListener.animateRock;
      this.glEventListener.animateRoll = !this.glEventListener.animateRoll;
    }
    else if (e.getActionCommand().equalsIgnoreCase("reset snowman")) {
      this.glEventListener.resetSnowman();
    }
    else if(e.getActionCommand().equalsIgnoreCase("quit"))
      System.exit(0);
  }
}

class MyKeyboardInput extends KeyAdapter  {
  private Camera camera;
  
  public MyKeyboardInput(Camera camera) {
    this.camera = camera;
  }
  
  public void keyPressed(KeyEvent e) {
    Camera.Movement m = Camera.Movement.NO_MOVEMENT;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
      case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
      case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
      case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
      case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
      case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;
    }
    camera.keyboardInput(m);
  }
}

class MyMouseInput extends MouseMotionAdapter {
  private Point lastpoint;
  private Camera camera;
  
  public MyMouseInput(Camera camera) {
    this.camera = camera;
  }
  
    /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */    
  public void mouseDragged(MouseEvent e) {
    Point ms = e.getPoint();
    float sensitivity = 0.001f;
    float dx=(float) (ms.x-lastpoint.x)*sensitivity;
    float dy=(float) (ms.y-lastpoint.y)*sensitivity;
    //System.out.println("dy,dy: "+dx+","+dy);
    if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
      camera.updateYawPitch(dx, -dy);
    lastpoint = ms;
  }

  /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */  
  public void mouseMoved(MouseEvent e) {   
    lastpoint = e.getPoint(); 
  }
}