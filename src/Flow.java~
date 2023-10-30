//package FlowSkeleton;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicLong;
public class Flow {
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static FlowPanel fp;
	static Water wGrid[][];
	
	
	// start timer
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	
	// stop timer, return time elapsed in seconds
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	

	
	public static void setupGUI(int frameX,int frameY,Terrain landdata) 
	{
	
		Dimension fsize = new Dimension(800, 800);
		JFrame frame = new JFrame("Waterflow"); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
    	
		JPanel g = new JPanel();
		g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
   
		fp = new FlowPanel(landdata);
		fp.setPreferredSize(new Dimension(frameX,frameY));
		fp.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int x = e.getX();
				int y = e.getY();
				//wGrid[x][y].setDepth(3);
				fp.wtrClick(x, y);
			}
			
			public void mouseExited(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mousePressed(MouseEvent e){}
		});
		
		g.add(fp);
	    
		// to do: add a MouseListener, buttons and ActionListeners on those buttons
	   	
		JPanel b = new JPanel();
		b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
		
	
		
		JButton endB = new JButton("End");;
		// add the listener to the jbutton to handle the "pressed" event
		endB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// to do ask threads to stop
				fp.shutdown();
				frame.dispose();
			}
		});
		
		//Create reset button
		JButton resetB = new JButton("Reset");;
		resetB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fp.reset();
			}
		});
		
		//Create pause button	
		JButton pauseB = new JButton("Pause");;
                pauseB.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e)
                        {
                                fp.pause();
                        }
		});
		
		//Create play button
		JButton playB = new JButton("Play");;
                playB.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e)
                        {
                                fp.play();
                                //fp.repaint();
                        }
                });
		
		//Create a label to display the timestamp
		JLabel counterL = new JLabel("   Timestamp: "+landdata.getCount());
		
		//Below im creating a timer to refresh the Jframe ever 33 miliseconds
		int delay = 33;
		ActionListener taskPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				fp.repaint();
				counterL.setText("   Timestamp: "+landdata.getCount());
			}
		};
		
	
		//Add all the above components to the frame		
		b.add(resetB);
		b.add(pauseB);
		b.add(playB);
		b.add(endB);
		b.add(counterL);
		g.add(b);
    	
		frame.setSize(frameX, frameY+50);	// a little extra space at the bottom for buttons
		frame.setLocationRelativeTo(null);  // center window on screen
		frame.add(g); //add contents to window
		frame.setContentPane(g);
		frame.setVisible(true);
		Thread fpt = new Thread(fp);
		 new Timer(delay, taskPerformer).start(); //This might not be the most effecient way to go about updating the frame
		 

		
	
}
	
		
	public static void main(String[] args) throws java.lang.InterruptedException {
		Terrain landdata = new Terrain();
	
		// check that number of command line arguments is correct
		if(args.length != 1)
		{
			System.out.println("Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}
				
		// landscape information from file supplied as argument
		// 
		landdata.readData(args[0]);
		
		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
	
			SwingUtilities.invokeLater(()->setupGUI(frameX, frameY, landdata));

		// to do: initialise and start simulation
		
		//Create the lock which will be used to synchronize threads
		Object lock = new Object();
		
		//Create the 4 threads
		Thread[] threads = new Thread[4];
		
		for(int i = 0; i<4; i++)
		{
			threads[i] = new Thread(new FlowPanel(landdata,(i*frameX)/4, ((1+i)*frameX)/4, lock));
			threads[i].start();
		}
		
		//Im not sure if this join is necessary as the threads loop pretty much indefinitely 
		for(int i = 0; i< 4; i++)
		{
			threads[i].join();

		}
			
	}
}
