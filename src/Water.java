import java.util.concurrent.atomic.AtomicInteger;
public class Water
{
    //Depth variable
    private AtomicInteger depth;
    
    //constructor
    public Water()
    {
        depth = new AtomicInteger(0);
    }
    
    //Gets depth, outputs it in float so that it can be divided by 100
    public float getDepth()
    {
        return depth.get();
    }
    
    //Gives one unit of water to another Water object
    //Is synchronized in hopes that it will avert bad interleaving between the two calls to give and take water
    //Spoiler alert: I dont think it works  
    synchronized public void giveWater(Water other, Terrain land, int x, int y, int sx, int sy)
    {
        if (this.getDepth()>0)
        {
         this.depWater(land, x, y);
         other.incDepth(land, sx, sy);
        }
        else//This was used to test for data races
        {
         System.out.println("glitch here");
        }
    }
    //Adds a given value to the current depth
     public void setDepth(int d)
    {
        depth.getAndAdd(d);
    }
    
    //Depletes water by 1 unit and updates the overlay
     public void depWater(Terrain land, int x, int y)
    {
        depth.getAndDecrement();
        if (depth.get() == 0)
        {
         land.removeWater(x, y);
        }
        
    }
    
    //Increments water by 1 unit and updates the overlay
     public void incDepth(Terrain land, int x, int y)
    {
        if (depth.get() ==0)
        {
         land.updateWaterLight(x, y);
        }
        else  if ((depth.get() ==1))
        {
         land.updateWaterMid(x, y);
        }
        else  if (depth.get() >2)
        {
         land.updateWaterDark(x, y);
        }


        
        depth.getAndIncrement();
    }
    
    //Sets water to 0.
    //This is used to clear boundary variables
    public void clearWater()
    {
        depth.set(0);
    }
}
