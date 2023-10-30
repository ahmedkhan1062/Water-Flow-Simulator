//package FlowSkeleton;

import java.io.File;
import java.awt.image.*;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Locale;
import java.util.concurrent.atomic.*;
public class Terrain {

	float [][] height; // regular grid of height values
	int count = 0;//This is used to count the current timestep
	AtomicInteger threadCount = new AtomicInteger(0);// counter to count the number of active threads, this is used in the run() method of FlowPanel to synchronize the threads
	Water [][] wtrGrid;// Array containing the water objects
	volatile boolean running;// boolean to be controlled for stopping and starting the simulation
	int dimx, dimy; // data dimensions
	BufferedImage img; // greyscale image for displaying the terrain top-down
	BufferedImage waterImg; // Overlayed image for water movement
	ArrayList<Integer> permute;	// permuted list of integers in range [0, dimx*dimy)
	
	//These are my get and set methods for the atomic variables defined above
	public int getCount()
	{
		return count;
	}
	
	public void incCount()
	{
		count++;
	}
	
	public int getThreadCount()
        {
                return threadCount.get();
        }
        
        public void setThreadCount(int val)
        {
        	threadCount.set(val);
        }
        public void incThreadCount()
        {
                threadCount.getAndIncrement();
        }
	
	
        //get method for the running boolean
	public boolean getRunning()
	{
		return running;
	}
	
	// Set method for running
	public void setRunning(boolean run)
	{
		running = run;
	}
	
	// overall number of elements in the height grid
	int dim(){
		return dimx*dimy;
	}
	
	// get x-dimensions (number of columns)
	int getDimX(){
		return dimx;
	}
	
	// get y-dimensions (number of rows)
	int getDimY(){
		return dimy;
	}
	
	// get greyscale image
	public BufferedImage getImage() {
		  return img;
	}
	
	//get water overlay image
	public BufferedImage getWater()
	{
		return waterImg;
	}
	
	// convert linear position into 2D location in grid
	void locate(int pos, int [] ind)
	{
		ind[0] = (int) pos / dimy; // x
		ind[1] = pos % dimy; // y	
	}
	
	// convert height values to greyscale colour and populate an image
	void deriveImage()
	{
		img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
		float maxh = -10000.0f, minh = 10000.0f;
		
		// determine range of heights
		for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
				float h = height[x][y];
				if(h > maxh)
					maxh = h;
				if(h < minh)
					minh = h;
			}
		
		for(int x=0; x < dimx; x++)
			for(int y=0; y < dimy; y++) {
				 // find normalized height value in range
				 float val = (height[x][y] - minh) / (maxh - minh);
				 Color col = new Color(val, val, val, 1.0f);
				 img.setRGB(x, y, col.getRGB());
			}
	}
	
	//Creates a transparent image to use for water overlay
	void deriveWaterImg()
	{
		waterImg = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
		Color Trans = new Color(0, 0, 0, 0);
		for(int x = 0; x<dimx; x++)
		{
			for(int y = 0; y<dimy; y++)
			{
				waterImg.setRGB(x, y, Trans.getRGB());
			}
		}
	}
	
	//Spreads water from a first set of co-ordinates to a second set of co-ordinates
	 public void spreadWater(int fx, int fy, int sx, int sy)
	{
		wtrGrid[fx][fy].giveWater(wtrGrid[sx][sy], this, fx, fy, sx, sy);
		//updateWater(sx, sy);
		//if (wtrGrid[fx][fy].getDepth()==0)
                //{
                 //       removeWater(fx, fy);
                //}

	}
	
	//Removes water from the upper and lower boundaries
	public void clearX()
	{
		for (int x = 1; x <dimx; x++)
		{
			wtrGrid[x][0].clearWater();
			removeWater(x, 0);
			wtrGrid[x][dimy-1].clearWater();
			removeWater(x, dimy-1);
		}
	}
        //Removes water from the left and right boundaries
        public void clearY()
        {
                for (int y = 1; y <dimy; y++)
                {
                        wtrGrid[0][y].clearWater();
                        removeWater(0, y);
                        wtrGrid[dimx-1][y].clearWater();
                        removeWater(dimx -1, y);
                }
	}
	
	//Clears the grid of water and sets all water depths to 0
	public void resetWtr()
	{
		for(int x = 0; x<dimx; x++)
                {
                        for(int y = 0; y<dimy; y++)
                        {
                               	wtrGrid[x][y].clearWater();
                                removeWater(x, y);
                        }
                }
	}
	
	//Places 3 units of water in every position within a 3x3 block
	public  void placeWater(int x, int y)
	{
		
		for (int r = -3; r <4; r++)
		{
			for  (int c =-3; c<4; c++)
			{
				wtrGrid[x+r][y+c].setDepth(3);
				updateWaterDark(x+r, y+c);
			}
		}
		
	}
	
	//This method causes new water to be visually updated on the overlay image in dark blue
	 void updateWaterDark(int x, int y)
	{
		int blue = new Color(17,30,108).getRGB();
		waterImg.setRGB(x, y, blue);
	}
	
	 //This method causes new water to be visually updated on the overlay image in medium blue
         void updateWaterMid(int x, int y)
        {
                int blue = new Color(0,0,255).getRGB();
                waterImg.setRGB(x, y, blue);
        }

         //This method causes new water to be visually updated on the overlay image in light blue
         void updateWaterLight(int x, int y)
        {
                int blue = new Color(0,128,255).getRGB();
                waterImg.setRGB(x, y, blue);
        }

	
	//This method causes no-longer existing water to be visually removed from the overlay image
	void removeWater(int x, int y)
	{
		int Trans = new Color(0, 0, 0, 0).getRGB();
		waterImg.setRGB(x, y, Trans);
	}
	
	
	// generate a permuted list of linear index positions to allow a random
	// traversal over the terrain


	void genPermute() {
		permute = new ArrayList<Integer>();
		for(int idx = 0; idx < dim(); idx++)
			permute.add(idx);
		java.util.Collections.shuffle(permute);
	}
	
	// find permuted 2D location from a linear index in the
	// range [0, dimx*dimy)
	void getPermute(int i, int [] loc) {
		locate(permute.get(i), loc);
	}
	
	// read in terrain from file
	void readData(String fileName){ 
		try{ 
		
			Scanner sc = new Scanner(new File(fileName));
			sc.useLocale(Locale.ENGLISH); //I changed the locale to prevent mismatch error
			// read grid dimensions
			// x and y correpond to columns and rows, respectively.
			// Using image coordinate system where top left is (0, 0).
			dimy = sc.nextInt(); 
			dimx = sc.nextInt();
			running = false;
			// populate height grid
			height = new float[dimx][dimy];
			wtrGrid = new Water[dimx][dimy];
			
			for(int y = 0; y < dimy; y++)
			{
				for(int x = 0; x < dimx; x++)	
				{
					height[x][y] = sc.nextFloat();
					wtrGrid[x][y] = new Water();
				}
			}
		
			
		
			
				
			sc.close(); 
			
			// create randomly permuted list of indices for traversal 
			genPermute(); 
			
			// generate greyscale heightfield image
			deriveImage();
			//generate water image
			deriveWaterImg();
			
		
		} 
		catch (IOException e){ 
			System.out.println("Unable to open input file "+fileName);
			e.printStackTrace();
		}
		catch (java.util.InputMismatchException e){ 
			System.out.println("Malformed input file "+fileName);
			e.printStackTrace();
		}
	}
}
