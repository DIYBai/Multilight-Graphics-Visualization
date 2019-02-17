import java.util.ArrayList;
import java.awt.geom.*;
// import java.util.Arrays; //useful for printing arrays (for debugging purposes mainly)

//essentially a data struct that stores the polyhedra data as a 3D array
//(array of faces which are arrays of points which are arrays of coords which are doubles)
class Polyhedra{
    Face[] faces;
    double[] centerPt = {0, 0, 0}; //initialized to be at origin - used for transforms
    double[] colors = {1.0, 1.0, 1.0}; //each should be double from 0 - 1
    double ka = 0.3;
    double kd = 0.4;
    double ks = 0.3; //ka + kd + ks should equal 1.0

    //not responsible for creating face array
    public Polyhedra( Face[] faceArray ){
        faces = faceArray;
    }

    //sets center point somewhere else - used for rotation/translation purposes
    public Polyhedra( Face[] faceArray, int x, int y, int z){
        faces = faceArray;
        centerPt[0] = x;
        centerPt[1] = y;
        centerPt[2] = z;
    }

    //sets intensities (i.e. how well this polyhedra reflects certain light)
    //automatically constrains values to be between 1 and 0 (incusive)
    public void setColors(double r, double g, double b){
        if     (r > 1.0){ r = 1.0; }
        else if(r < 0.0){ r = 0.0; }
        colors[0] = r;

        if     (g > 1.0){ g = 1.0; }
        else if(g < 0.0){ g = 0.0; }
        colors[1] = g;

        if     (b > 1.0){ b = 1.0; }
        else if(b < 0.0){ b = 0.0; }
        colors[2] = b;
    }

    //alternate method for convenience if colors are in a list
    public void setColors(double[] colorList){
        setColors(colorList[0], colorList[1], colorList[2]);
    }

    //attempts to normalize parameters in case they don't actually add up to 1
    //Warning: could crash if ka + kd + ks add up to slightly more than 1 due to rounding
    public void setLightFacs(double a, double d, double s){
        double sum = a + d + s;
        ka = a / sum;
        kd = d / sum;
        ks = s / sum;
    }

    //returns array of faces which are this polyhedra's faces only rotated
    //finds a bunch of transformations and combines them by multiplying, then
    //applies the transformation to each face
    public Face[] getRotatedFaces(double xTheta, double yTheta, double zTheta, double arbX, double arbY, double arbZ, double arbTheta, Face[] faceArray){
        double[][] shiftTrans = util.transMtx(-centerPt[0], -centerPt[1], -centerPt[2]); //translates to origin
        double[][] shiftBackTrans = util.transMtx(centerPt[0], centerPt[1], centerPt[2]); //translate back

        double[][] xRotateTrans   = util.xRotateMtx(    xTheta, true);
        double[][] yRotateTrans   = util.yRotateMtx(    yTheta, true);
        double[][] zRotateTrans   = util.zRotateMtx(    zTheta, true);
        double[] normAxis = util.norm( new double[] {arbX, arbY, arbZ} );
        double[][] arbRotateTrans = util.arbRotateMtx(arbTheta, true, normAxis[0], normAxis[1], normAxis[2]);

        double[][]  xyRotateTrans = util.mtxMult(xRotateTrans, yRotateTrans);
        double[][] xyzRotateTrans = util.mtxMult(xyRotateTrans, zRotateTrans);
        double[][] allRotateTrans = util.mtxMult(xyzRotateTrans, arbRotateTrans);

        double[][] compositeTrans = util.mtxMult(shiftBackTrans, allRotateTrans);
        compositeTrans = util.mtxMult(compositeTrans, shiftTrans);

        Face[] rotFaces = new Face[faces.length];
        // omp parallel for
        for(int i = 0; i < faces.length; i++){
            Face f = faces[i];
            rotFaces[i] = f.getTransform(compositeTrans);
        }
        return rotFaces;
    }

    //shortcut that calls more genereal getRotatedFaces method above, passing default
    //param of this polyhedra's faces array
    public Face[] getRotatedFaces(double xTheta, double yTheta, double zTheta, double arbX, double arbY, double arbZ, double arbTheta){
        return getRotatedFaces(xTheta, yTheta, zTheta, arbX, arbY, arbZ, arbTheta, this.faces);
    }

    //actually rotates polyhedra by updating faces arry to the rotated faces
    public void rotatePoly(double xTheta, double yTheta, double zTheta){
        this.faces = getRotatedFaces(xTheta, yTheta, zTheta, 1.0, 1.0, 1.0, 0.0);
    }

    //returns array of faces which are this polyhedra's faces only shifted
    //applies shift transform to every face and saves new transformed faces to array to be returned
    public Face[] getShiftedFaces(double xShift, double yShift, double zShift, Face[] faceArray){
        Face[] shiftFaces = new Face[faceArray.length];

        //omp parallel for
        for(int i = 0; i < faceArray.length; i++){
            Face f = faceArray[i];
            shiftFaces[i] = f.getTransform( util.transMtx(xShift, yShift, zShift) );
        }
        return shiftFaces;
    }

    //shortcut method that calls more general metho above
    public Face[] getShiftedFaces(double xShift, double yShift, double zShift){
        return getShiftedFaces(xShift, yShift, zShift, this.faces);
    }

    //actually shifts this polyhedra's faces
    public void shiftPoly(double xShift, double yShift, double zShift){
        this.faces = getShiftedFaces(xShift, yShift, zShift);
        centerPt[0] += xShift;
        centerPt[1] += yShift;
        centerPt[2] += zShift;
    }
}
