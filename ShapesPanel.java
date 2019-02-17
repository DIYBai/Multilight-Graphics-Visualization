import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
// import java.util.Arrays; //useful for debugging

//actually projects arrayList of 3D polyhedra objects and transforms them as needed
class ShapesPanel extends JPanel implements ActionListener{
    double[] eyeCoords = {0, 0, 500};
    ArrayList<Polyhedra> shapes;
    ArrayList<Light> lights;
    ArrayList<Path2D.Double> visShapes;
    ArrayList<Polyhedra> polyList;
    ArrayList<Face> polyFaceList;
    double xTheta, yTheta, zTheta, arbTheta;
    int arbX, arbY, arbZ;
    BufferedImage buff;

    //initializes panel, default vals, one shape (cube), and starts the timer to animate
    public ShapesPanel(){
        setPreferredSize(new Dimension(500,500));
        setBackground(Color.red); //shouldn't see red
        lights = new ArrayList<Light>();
        lights.add( new Light( 80, 30, 250, 0.1, 1.0, 0.1, 10) );
        lights.add( new Light(-50, 0, 50, 0.1, 0.1, 1.0, 10) );
        xTheta = yTheta = zTheta = arbTheta = 0;
        arbX = arbY = arbZ = 1; //initial arbitrary axis is about a diagonal of the cube
        shapes = new ArrayList<Polyhedra>();
        initRoom();

        //next chunk ceates 2 cubes
        double dim = 30;  //changes size of cube
        double[] ptA = { dim,  dim,  dim};
        double[] ptB = { dim,  dim, -dim};
        double[] ptC = {-dim,  dim, -dim};
        double[] ptD = {-dim,  dim,  dim};
        double[] ptE = { dim, -dim,  dim};
        double[] ptF = { dim, -dim, -dim};
        double[] ptG = {-dim, -dim, -dim};
        double[] ptH = {-dim, -dim,  dim};

        Face topF1    = new Face(new double[][] {ptA, ptB, ptC});
        Face topF2    = new Face(new double[][] {ptC, ptD, ptA});
        Face bottomF1 = new Face(new double[][] {ptG, ptF, ptE});
        Face bottomF2 = new Face(new double[][] {ptE, ptH, ptG});
        Face frontF1  = new Face(new double[][] {ptE, ptA, ptD});
        Face frontF2  = new Face(new double[][] {ptD, ptH, ptE});
        Face backF1   = new Face(new double[][] {ptG, ptC, ptB});
        Face backF2   = new Face(new double[][] {ptB, ptF, ptG});
        Face leftF1   = new Face(new double[][] {ptD, ptC, ptG});
        Face leftF2   = new Face(new double[][] {ptG, ptH, ptD});
        Face rightF1  = new Face(new double[][] {ptE, ptF, ptB});
        Face rightF2  = new Face(new double[][] {ptB, ptA, ptE});

        Face[] cubeFaces = { topF1, topF2, bottomF1, bottomF2, frontF1, frontF2, backF1, backF2, leftF1, leftF2, rightF1, rightF2 }; //right face
        Polyhedra cube = new Polyhedra(cubeFaces);
        cube.shiftPoly(0, -(100-dim), 0); //shift it towards bottom of face
        cube.setLightFacs(0.3, 0.6, 0.1);
        shapes.add(cube);


        dim = 20;  //changes size of cube
        ptA = new double[] { dim,  dim,  dim};
        ptB = new double[] { dim,  dim, -dim};
        ptC = new double[] {-dim,  dim, -dim};
        ptD = new double[] {-dim,  dim,  dim};
        ptE = new double[] { dim, -dim,  dim};
        ptF = new double[] { dim, -dim, -dim};
        ptG = new double[] {-dim, -dim, -dim};
        ptH = new double[] {-dim, -dim,  dim};

        topF1    = new Face(new double[][] {ptA, ptB, ptC});
        topF2    = new Face(new double[][] {ptC, ptD, ptA});
        bottomF1 = new Face(new double[][] {ptG, ptF, ptE});
        bottomF2 = new Face(new double[][] {ptE, ptH, ptG});
        frontF1  = new Face(new double[][] {ptE, ptA, ptD});
        frontF2  = new Face(new double[][] {ptD, ptH, ptE});
        backF1   = new Face(new double[][] {ptG, ptC, ptB});
        backF2   = new Face(new double[][] {ptB, ptF, ptG});
        leftF1   = new Face(new double[][] {ptD, ptC, ptG});
        leftF2   = new Face(new double[][] {ptG, ptH, ptD});
        rightF1  = new Face(new double[][] {ptE, ptF, ptB});
        rightF2  = new Face(new double[][] {ptB, ptA, ptE});

        cubeFaces = new Face[] { topF1, topF2, bottomF1, bottomF2, frontF1, frontF2, backF1, backF2, leftF1, leftF2, rightF1, rightF2 }; //right face
        cube = new Polyhedra(cubeFaces);
        cube.shiftPoly(50, 100-dim, 0); //shift away from origin
        cube.setLightFacs(0.1, 0.4, 0.5);
        shapes.add(cube);

        visShapes = visSurfaces();

        Timer timer = new Timer(42, this); //tries to animate ~24 frames/second, in reality does as fast as it can
        timer.setInitialDelay(100); //animation starts after .1 seconds
        timer.start();
    } //end of constructor


    //sets up painted component and creates a new buffered image to draw
    //goes through every pixel, gets the color that pixel should be, and sets
    //that color for the bufferedImage, which gets drawn at the end
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        int width = getWidth();
        int height = getHeight();
        Color bgColor = Color.black;
        g2d.translate(width/2, height/2);

        buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // omp parallel for
        for(int xpixel = 0; xpixel < getWidth(); xpixel++){
            for(int ypixel = 0; ypixel < getHeight(); ypixel++) {
                int pixRGB = getPixelColor(xpixel, ypixel, bgColor).getRGB();
                buff.setRGB(xpixel, ypixel, pixRGB);
            }
        }
        g2d.drawImage(buff, -width/2, -height/2, this);
    } //end of paintComponent() function


    //given the pixel and the background color, finds the color/intensities for the pixel
    //does this by checking if it's in any of the Path2Ds that represent the
    //projection of a visible face. Checks all of them and 'chooses' the one with highest
    //z-coord for now, because higher z means closer to camera if camera is on +z-axis
    //Does similar thing with lights, but checks to see if it's in a radius
    private Color getPixelColor(int xpixel, int ypixel, Color bgColor){
        int xCoord = xpixel - getHeight()/2;
        int yCoord = getHeight()/2 - ypixel;
        Color pixelColor = bgColor; //set default
        double zCoord = -100000000; //initialize to be further back that the room's back face
        Boolean inShape = false;
        Polyhedra containShape = null;
        Face containFace = null;

        //goes through all faces to see which face contains that point and is furthest in front
        for(int i = 0; i < visShapes.size(); i++){
            Path2D.Double shape = visShapes.get(i);
            if( shape.contains(xCoord, yCoord) ){
                Face posFace = polyFaceList.get(i);
                double posZ = util.getZ(xCoord, yCoord, posFace.norm, posFace.center);
                if(!inShape || posZ > zCoord){
                    inShape = true;
                    containShape = polyList.get(i);
                    containFace = posFace;
                    zCoord = posZ;
                }
            }
        }

        // for(int i = 0; i < lights.size(); i++){
        for(Light l : lights){
            double[] lightCoords2D = util.getProjectedCoords(l.pos, eyeCoords);
            double xDist = xCoord - lightCoords2D[0];
            double yDist = yCoord - lightCoords2D[1];
            double totalDist = Math.sqrt(xDist*xDist + yDist*yDist);
            double projectedRad =  l.radiusPix/(1.0 - l.pos[2]/eyeCoords[2]);
            if( totalDist <= projectedRad && l.pos[2] > zCoord){ //need to be within radius of light center pt, and in front of front-most (found!) thing
                inShape = false; //furthest forward thing has to be a point on a light, not a point on face
                zCoord = l.pos[2]; //update zCoord
                double ratio = l.radiusPix/totalDist;
                float newR = (float)Math.pow(l.ir, 0.4/ratio);  //this math means that the middle is white while the edge is based on light's intensity
                float newG = (float)Math.pow(l.ig, 0.4/ratio);  //This creates a glowing effect
                float newB = (float)Math.pow(l.ib, 0.4/ratio);
                pixelColor = new Color( newR, newG, newB );
            }
        }

        if (inShape) { //in this case, pixel tp be colored is a point on some face/needs shading
            float[] intens = { (float)0.0, (float)0.0, (float)0.0 };
            for(Light light : lights){ //gets intensites due to every light source and adds them together
                float[] temp = getIntensity(xCoord, yCoord, containShape, containFace, light);
                for(int i = 0; i < 3; i++){
                    float newVal = intens[i] + temp[i];
                    if(newVal > 1.0){ //make sure that no intensity goes above 1
                        newVal = (float)1.0;
                    }
                    intens[i] = newVal;
                } //end of for-loop that does each color
            } //end of for-loop that goes through all lights
            pixelColor = new Color(intens[0], intens[1], intens[2]);
        }

        return pixelColor;
    } //end of getPixelColor() function


    //returns list of path2D's (triangles) which are 2D projections of 3D faces that are visible to given camera
    private ArrayList<Path2D.Double> visSurfaces(){
        ArrayList<Path2D.Double> surfaceList = new ArrayList<Path2D.Double>();
        polyList = new ArrayList<Polyhedra>();
        polyFaceList = new ArrayList<Face>();

        for(int i = 0; i < shapes.size(); i++){ //iterates through all polyhedra
            Polyhedra currPoly = shapes.get(i);
            Face[] faceArray = currPoly.faces;
            if(i != 0){ //rotates every poly except background/bounding box
                faceArray = currPoly.getRotatedFaces(xTheta, yTheta, zTheta, arbX, arbY, arbZ, arbTheta);
            }
            for( Face face : faceArray ) {
                //checks angle between face's normal vector and vector
                //from face to eye - if > 90*, does not draw that face/store any relevant information
                double[] faceToEyeVec = util.pointsToVec(face.center, eyeCoords);
                if( util.getAngle(faceToEyeVec, face.norm) >= Math.PI/2 ){
                    continue; //does not add to any arraylist, just keeps checking the next face
                }

                double[][] newVerts = face.verts;
                double[] newPt1 = util.getProjectedCoords( newVerts[0], eyeCoords );
                double[] newPt2 = util.getProjectedCoords( newVerts[1], eyeCoords );
                double[] newPt3 = util.getProjectedCoords( newVerts[2], eyeCoords );

                Path2D.Double drawTriangle = new Path2D.Double();
                drawTriangle.moveTo(newPt1[0], newPt1[1]);
                drawTriangle.lineTo(newPt2[0], newPt2[1]);
                drawTriangle.lineTo(newPt3[0], newPt3[1]);

                //store everything for quicker access later
                surfaceList.add(drawTriangle);
                polyList.add(currPoly);
                polyFaceList.add(face);
            } //end of loop that goes through all faes
        } //end of loop that goes through all polyhedra
        return surfaceList;
    } //end of visSurfaces() function


    //gets the 3 intensities for a given x-y pair on a given plane/face due to a
    //given source of lights
    //NOTE: may break if z component of face's normal is 0
    private float[] getIntensity(double xCoord, double yCoord, Polyhedra poly, Face face, Light l){
        double[] normV = face.norm;
        double zCoord = util.getZ(xCoord, yCoord, normV, face.center);
        double[] lightCoords = l.pos;

        double[] rayV  = { lightCoords[0] - xCoord, lightCoords[1] - yCoord, lightCoords[2] - zCoord }; //vector representing vector from sphere-pt to light
        double[] wVec = util.project(rayV, normV);
        double[] reflectV = util.subVecs(util.scaleVec(wVec, 2), rayV); //used for specular calculations

        //normalize all vectors
        normV = util.norm(normV);
        double[] newRayV = util.norm(rayV);
        reflectV = util.norm(reflectV);
        double[] eyeV = util.norm(eyeCoords);

        double cosTheta = util.dot(normV, newRayV);
        if (cosTheta < 0) { cosTheta = 0; }
        double cosAlpha = util.dot(reflectV, eyeV);
        if (cosAlpha < 0) { cosAlpha = 0; }
        double specFactor = Math.pow(cosAlpha, 3);

        //based on coefficients and above results, actually calculate
        //the intensities then color of current pixel and color it on bufferedImage
        double diffInten = poly.kd * cosTheta;
        double specInten = poly.ks * specFactor;
        double inten  = diffInten + specInten + poly.ka;
        double dist = util.getVecLength(rayV) - l.radiusPix; //radius of light has small effect on intensities
        inten /= Math.pow(dist/100 + 1, 0.5); //light intensity falls off with distance but not too quickly (thus /100)

        //find specific intensities for the 3 components taking into account lights'
        //emission and the polyhedra's absorption of certain wavelengths
        float intenR = (float)(inten * Math.sqrt(l.ir) * poly.colors[0]); //downplay effect of light's intensity vals by sqrting
        float intenG = (float)(inten * Math.sqrt(l.ig) * poly.colors[1]);
        float intenB = (float)(inten * Math.sqrt(l.ib) * poly.colors[2]);
        return new float[] {intenR, intenG, intenB};
    } //end of getIntensity() function


    //creates an almost rectangular prism with intentionally opposite normals to
    //act as a bounding box/location for scene.
    public void initRoom(){
        double roomX = 200;
        double roomY = 100;
        double roomZ = 200;

        //having issues with shading for surfaces with normals having z-component of 0
        //instead of rectangular viewing box, have *almost* rectangular box
        double[] ptA = { 1.1*roomX,  1.1*roomY,  roomZ};
        double[] ptB = {     roomX,      roomY, -roomZ};
        double[] ptC = {    -roomX,      roomY, -roomZ};
        double[] ptD = {-1.1*roomX,  1.1*roomY,  roomZ};
        double[] ptE = { 1.1*roomX, -1.1*roomY,  roomZ};
        double[] ptF = {     roomX,     -roomY, -roomZ};
        double[] ptG = {    -roomX,     -roomY, -roomZ};
        double[] ptH = {-1.1*roomX, -1.1*roomY,  roomZ};

        Face topF1    = new Face(new double[][] {ptC, ptB, ptA});
        Face topF2    = new Face(new double[][] {ptA, ptD, ptC});
        Face bottomF1 = new Face(new double[][] {ptE, ptF, ptG});
        Face bottomF2 = new Face(new double[][] {ptG, ptH, ptE});
        Face frontF1  = new Face(new double[][] {ptD, ptA, ptE});
        Face frontF2  = new Face(new double[][] {ptE, ptH, ptD});
        Face backF1   = new Face(new double[][] {ptB, ptC, ptG});
        Face backF2   = new Face(new double[][] {ptG, ptF, ptB});
        Face leftF1   = new Face(new double[][] {ptG, ptC, ptD});
        Face leftF2   = new Face(new double[][] {ptD, ptH, ptG});
        Face rightF1  = new Face(new double[][] {ptB, ptF, ptE});
        Face rightF2  = new Face(new double[][] {ptE, ptA, ptB});

        Face[] roomFaces = { topF1, topF2, bottomF1, bottomF2, frontF1, frontF2, backF1, backF2, leftF1, leftF2, rightF1, rightF2 };
        Polyhedra room = new Polyhedra(roomFaces);
        room.setColors(0.9, 0.9, 0.9); //pretty much white room that reflects all colors
        room.setLightFacs(0.3, 0.6, 0.1);
        shapes.add(room);
    } //end of initRoom() function


    //updates theta values and repaints
    public void setThetas(double xTh, double yTh, double zTh, double arbTh){
        xTheta = xTh;
        yTheta = yTh;
        zTheta = zTh;
        arbTheta = arbTh;
        visShapes = visSurfaces(); //visShapes only needs to get updated if objects rotate
                                   //(thus some new faces may be visible or invisible from cam perspective)
        repaint();
    }


    //updates arb axis and repaints if arbTheta isn't 0, otherwise shouldn't change
    public void setAxis(int x, int y, int z){
        arbX = x;
        arbY = y;
        arbZ = z;
        if (arbTheta != 0) { repaint(); }
    } //end of setAxis() function


    //this method is responsible for animating the scene - this function runs every
    //couple of milliseconds continuously. For now, just moves lights in z-axis
    public void actionPerformed(ActionEvent e){
        //currently, simple moves lights further away until a hardcoded value then resets
        for(Light l : lights){
            double[] lightCoords = l.pos;
            if (lightCoords[2] < -100){
                lightCoords[2] = 200;
            }
            else {
                lightCoords[2] -= 5;
            }
        }
        repaint();
    } //end of actionPerformed() function


    //allows other classes to add a light given initial values for the light
    //and then repaints the scene
    public void addLight(double x, double y, double z, double r, double g, double b, int rad){
        Light newLight = new Light( x, y, z, r, g, b, rad);
        lights.add(newLight);
        repaint();
    } //end of addLight() function


    //resets the lights arraylist so it's empty
    public void removeAllLights(){
        lights = new ArrayList<Light>();
        repaint();
    } //end of removeAllLights() function
}
