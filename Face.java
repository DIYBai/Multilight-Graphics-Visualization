//represents a face of a polyhedra with size-three array of double arrays (3 vertices which are 3 points)
//currently, all faces are assumed triangles but class should generalize without much issue
class Face{
    double[][] verts;
    double[] norm;
    double[] center = new double[3];

    //constructor just stores given 2D array then calculates/initalizes norm vec and center pt
    public Face(double[][] vertices){
        verts = vertices;
        calcNorm();
        calcCenter();
    }

    //sets norm field to be the cross product of (first) two edges, assuming points are in CCW order
    private void calcNorm() {
        double[] faceVec1 = util.pointsToVec(verts[0], verts[1]);
        double[] faceVec2 = util.pointsToVec(verts[1], verts[2]);
        norm = util.cross3D(faceVec1, faceVec2);
    }

    //for each center coordinate, average vertices' corresponding coordinate vals
    private void calcCenter(){
        int numVerts = verts.length;
        for(int i = 0; i < numVerts; i++){
            center[i] = 0.0; //re-setting so each center calculation is independent
            for(int j = 0; j < numVerts; j++){
                center[i] += verts[j][i];
            }
            center[i] /= numVerts;
        }
    }

    //actually changes the points according to the transform matrix and
    //re-calculates normal and center pt
    public void transform(double[][] transMtx){
        for(int i = 0; i < verts.length; i++){
            verts[i] = util.transVec(transMtx, verts[i]);
        }
        calcNorm();  //update normal and center pt
        calcCenter();
    }

    //creates a new face constructed with this face's transformed vertices
    public Face getTransform(double[][] transMtx){
        double[][] newVerts = new double[verts.length][];
        for(int i = 0; i < newVerts.length; i++){
            newVerts[i] = util.transVec(transMtx, verts[i]);
        }
        return new Face(newVerts); //automatically will calculate the normal and center pt
    }
}
