//Represents a single light object with different light intensities, positions, and radii
class Light{
    float ir;
    float ig;
    float ib;
    double[] pos = new double[3];
    double[] vel = new double[3];
    int radiusPix;// = 1;

    //constructor initalizes all field values
    public Light(double xPos, double yPos, double zPos, double r, double g, double b, int radPix){
        pos[0] = xPos;
        pos[1] = yPos;
        pos[2] = zPos;
        ir = (float)r;
        ig = (float)g;
        ib = (float)b;
        radiusPix = radPix;
    }

    //constructor calls more general constructor with specific hardcoded field vals
    public Light(double xPos, double yPos, double zPos){
        this(xPos, yPos, zPos, 1.0, 1.0, 1.0, 1);
    }

    //constructor calls constructor which calls the more general constructor
    public Light(){
        this(0.0, 0.0, 0.0);
    }

    //allows setting the color emission intensities
    //params should be between 1.0 and 0.0, but currently not checked
    public void recolor(double nr, double ng, double nb){
        ir = (float)nr;
        ig = (float)ng;
        ib = (float)nb;
    }

    //changes the light's coords to whatever is input
    public void moveTo(double newX, double newY, double newZ){
        pos[0] = newX;
        pos[1] = newY;
        pos[2] = newZ;
    }

    //instead of moving the light exactly to a spot, just shifts it
    public void shift(double shiftX, double shiftY, double shiftZ){
        pos[0] += shiftX;
        pos[1] += shiftY;
        pos[2] += shiftZ;
    }

    //sets light's velocity to specific vector
    public void setV(double vx, double vy, double vz){
        vel[0] = vx;
        vel[0] = vy;
        vel[0] = vz;
    }

    //changes light's velocity by adding vector specified by params
    public void addV(double vx, double vy, double vz){
        vel[0] += vx;
        vel[0] += vy;
        vel[0] += vz;
    }

    //returns the vector from given pt (double array of 3 coords) to this light's center pt
    public double[] vectorTo(double[] pt){
        double[] vecToLight = new double[3];
        vecToLight[0] = pos[0] - pt[0];
        vecToLight[1] = pos[1] - pt[1];
        vecToLight[2] = pos[2] - pt[2];
        return vecToLight;
    }
}
