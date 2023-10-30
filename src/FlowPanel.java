//package FlowSkeleton;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javax.swing.JPanel;

public class FlowPanel extends JPanel implements Runnable 
{
	Terrain land;
	int[] loc;//Array storing the co-ordnates of the next point in the getPermute() method
	int lo;//Stores the lower bounds of the threads for loop
	int hi;//Stores the upper bounds of the threads for loop
	private Object locker; //Store the lock that was created in the Flow main method
	
	//Basic constructor that was used by the original skeleton code to create the GUI
	FlowPanel(Terrain terrain)
	{
		land = terrain;
		loc = new int[2];
		lo = 0;
		hi = 0;
	}
	
	//Second constructor that I used to devide up the workload amongst the 4 threads
	FlowPanel(Terrain terrain, int low, int high, Object key)
	{
        	land = terrain;
        	loc = new int[2];
        	lo = low;
        	hi = high;
        	locker = key;
        }

		
	// responsible for painting the terrain and water
	// as images
	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		  
		super.paintComponent(g);
		
		// All of this is to overlay the water onto the terrain image... i dont really understand it tbh but it seems to work
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(land.getImage(),0,0,null);
		float alpha = 0.25f;
		g2.drawImage(land.getWater(),0,0,null);
	}
	
	//This is the method which is called by the GUI when the reset button is clicked
	public void reset()
	{
		land.resetWtr();
		repaint();
	}
	
	//This is the method called by GUI when play button is clicked
	public void play()
	{
		land.setRunning(true);				
	}
	
	//This is the method called by GUI when exit button is clicked
	public void shutdown()
	{
		System.exit(0);
	}
	
	//This is method called by GUI when pause is clicked
	public void pause()
	{
		land.setRunning(false);
	}
	
	//This is the method called by the GUI on mouseclick to place water
	public void wtrClick(int x, int y)
	{
		land.placeWater(x, y);
		repaint();
	}
	
	//This is a method I used to check try to synchronise the threads at the end of each timestep.
	//At the end of each timestep each thread will increment an atomic integer stored in the Terrain class
	//If after this incrementation, the count is not equal to 4, then it will mean some thread is still running and the current thread will wait
	//until all threads have completed.
	//The final thread to complete will wake up the other threads                          
        public  void checkThreads()
        {
        	land.incThreadCount();
        	if(land.getThreadCount() < 4)
        	{
        		synchronized(locker)
        		{
        			try
        			{
        				locker.wait();
				}
				catch (InterruptedException e)
				{
        				e.printStackTrace();
				}
			}	
		}
		else if (land.getThreadCount() == 4)
                {
                	synchronized(locker)
                	{
                        	land.incCount();
                        	land.setThreadCount(0);
                        	
                        //This was code i used to output the number of water on the grid in order to test fluid conservation	
                        /*	
                        	float ans = 0;
                        	for (int g=0; g<land.dimx;g++)
                        	{
                        		for (int e=0; e<land.dimy; e++)
                        		{
                        			ans= ans+ land.wtrGrid[g][e].getDepth();	
                        		}
                        	}	
                        	
                        	System.out.println(ans);*/
                        	
                        	locker.notifyAll();
			}
		}                
	}
			
	public void run(){
		//play();	
		// display loop here
		// to do: this should be controlled by the GUI
		// to allow stopping and starting
	
		while(true )
		{
			//This is checking that the program is meant to be playing
			//This if statement indicates the start of a new timestep
			if (land.getRunning())
			{
				//Water is cleared from the edges of the terrain
				land.clearY();
				land.clearX();	
				
				//This is the beginning of each threads traversal over thier respective parts of the terrain grid
				for (int i = lo; i<(hi*land.dimy); i++)
				{
					//Using getPermute to set the loc array to the co-ordinates of the current index
					land.getPermute(i,loc);
					
					//Im using a min variable to determine which surrounding point is the lowest.
					//It is initialised to 10000000 as Im assuming this will be bigger than any point on the terrain grid
					float min = 10000000;
					
					//The co-ordinated of the lowest point are stored in this two number array
					int[] minPoint = new int[2]; 
			
			
					//The code below is super ugly, Im really sorry to whoever has to mark this.
					//Im going to try to explain whats going on but tbh I wrote this like 5 days ago and right now 
					//I cant quite remember what I was thinking either
					
					//This set of two loops is checking each point surrounding the current index "i", in order to check which point is lowest
					for (int x = -1; x<2; x++)
					{
						for (int y = -1; y<2; y++)
						{
							//This if statement is checking that any boundary points that may come up are set to -100 so that they can be ignored 
							if ((loc[0]==0) || (loc[0]== land.dimx-1) || (loc[1]==0) || (loc[1]== land.dimy-1 ))
							{
								min = -100;	
							}	
						
							//This statement is checking if the current position is the lowest one, and if so, is assigning its height to the min value, 
							//while storing its co-ordinates in the minPoint array
							else if ( ((land.wtrGrid[loc[0]+x][loc[1]+y].getDepth())/100+ land.height[loc[0]+x][loc[1]+y]) < min) 
							{
								min = (land.wtrGrid[loc[0]+x][loc[1]+y].getDepth())/100+ land.height[loc[0]+x][loc[1]+y];
								minPoint[0] = loc[0]+x;
								minPoint[1] = loc[1]+y;
							}
						}
					}
			
					synchronized(locker)
					{	
						//This if statement checks that the current position on the grid has a height higher than the minimum point, as well as that it actually has water store on it
						//, and then also whether or not its been labelled as a boundary point.			
						if ((((land.wtrGrid[loc[0]][loc[1]].getDepth())/100+land.height[loc[0]][loc[1]])> min)  && (min!= -100)&& (land.wtrGrid[loc[0]][loc[1]].getDepth()>0))
						{	
							//This part spreads the water from the current index in the array to its smallest neighbour
						
							land.spreadWater(loc[0], loc[1], minPoint[0], minPoint[1]);
						}
					}
			
			
				}
				//Calling the method i created earlier to make sure the threads wait for each other before moving on
				checkThreads();
			}
			
			//This restarts the loop
			else
			{
				continue;
			}
		
	    }
	}
}
