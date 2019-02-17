//collection of useful functions, mostly dealing with vectors and/or matrices
//a few functions to get transform matrices can return 3x3 (non-homogenous) instead of 4x4 (homogenous) matrices
//mostly doesn't check for valid input
class util{

    //multiplies two matrices (must have matching inner dimensions) and returns
    //resulting matrix
    //Assumes matrices are able to be multiplied (i.e. same inner dims)
    public static double[][] mtxMult (double[][] mtx1, double[][] mtx2){
        double[][] retMtx = new double[ mtx1.length ][ mtx2[0].length ];

        // if( mtx1[0].length == mtx2.length ){ //check if dimensions match
        // omp parallel for
        for( int i = 0; i < mtx1.length; i++ ) { //which row of 1st mtx
            for( int j = 0; j < mtx2[0].length; j++ ){ //which column of 2nd mtx
                for( int k = 0; k < mtx2.length; k++ ) {
                    retMtx[i][j] += mtx1[i][k] * mtx2[k][j];
                }
            }
        }
        // }
        return retMtx;
    }

    //shortcut for specifically multiplying a matrix and a vector (so don't need 2D matrix for vector)
    public static double[] mtxVecMult (double[][] mtx, double[] vec){
        double[] retVec = new double[ mtx.length ];

        // if( mtx1[0].length == vec.length ){ //check if dimensions match
        // omp parallel for
        for( int i = 0; i < mtx.length; i++ ) { //which row of 1st mtx
            for( int j = 0; j < vec.length; j++ ) {
                retVec[i] += mtx[i][j] * vec[j];
            }
        }
        // }
        return retVec;
    }

    //transforms a vector given a (4x4) transformation matrix and original vector
    //first makes vector homogenous so shifts can be applied
    public static double[] transVec (double[][] mtx, double[] vec){
        double[] retVec = new double[ mtx.length ];
        vec = makeHomog(vec);

        // omp parallel for
        for( int i = 0; i < mtx.length; i++ ) { //which row of 1st mtx
            for( int j = 0; j < vec.length; j++ ) {
                retVec[i] += mtx[i][j] * vec[j];
            }
        }
        // }
        return unmakeHomog(retVec);
    }

    //given a vector, returns length of the vector by summing squares of each
    //component then squaring the result
    public static double getVecLength(double[] vec){
        double vecLength = 0.0;
        for(int i = 0; i < vec.length; i++){
            vecLength += Math.pow(vec[i], 2);
        }
        return Math.pow(vecLength, 0.5);
    }

    //given a vector, returns same vector but scaled so length is 1
    //possible rounding errors can result in vector with length that is not *quite* 1
    public static double[] norm(double[] vec){
        double vecLength = getVecLength(vec);
        double[] normVec = new double[vec.length];
        for(int i = 0; i < vec.length; i++){
            normVec[i] = vec[i] / vecLength;
        }
        return normVec;
    }

    //given a vector and scale factor, simply returns the same vector only each
    //component is scaled by the scale factor
    public static double[] scaleVec(double[] vec, double scaleFac){
        double[] scaledV = new double[vec.length];
        for(int i = 0; i < vec.length; i++){
            scaledV[i] = vec[i] * scaleFac;
        }
        return scaledV;
    }

    //following equation derived from equation of plane s.t. normV dot ( pt - (xCoord, yCoord, zCoord) ) = 0
    //where everything except zCoord is given from above
    public static double getZ(double xCoord, double yCoord, double[] normV, double[] pt){
        double normZ = normV[2];
        if(normZ == 0){
            normZ = 0.00000001;
        }
        return pt[2] + ( normV[0]*(pt[0]-xCoord) + normV[1]*(pt[1]-yCoord) )/normZ;
    }

    //given a vector, adds an additional homgenous coord of 1
    public static double[] makeHomog(double[] nonHVec){
        double[] homogVec = new double[nonHVec.length + 1];
        for(int i = 0; i < nonHVec.length; i++){
            homogVec[i] = nonHVec[i];
        }
        homogVec[nonHVec.length] = 1;
        return homogVec;
    }

    //simply returns the same vector only without the last (theoretically
    //homgenous) coordinate, and each component is divided by homogenous coord (1 in trivial case)
    public static double[] unmakeHomog(double[] hVec){
        double[] nonHVec = new double[hVec.length - 1];
        double homogCoord = hVec[hVec.length-1];
        for(int i = 0; i < hVec.length - 1; i++){
            nonHVec[i] = hVec[i] / homogCoord;
        }
        return nonHVec;
    }

    //given an initial point and final point, returns vector between two
    public static double[] pointsToVec(double[] initPoint, double[] finalPoint){
        double[] vector = new double[initPoint.length];
        for(int i = 0; i < initPoint.length; i++){
            vector[i] = finalPoint[i] - initPoint[i];
        }
        return vector;
    }

    //given two vectors, returns a vector whose components are the sum of the
    //corresponding components of the input vectors
    public static double[] addVecs(double[] v1, double[] v2){
        double[] vector = new double[v1.length];
        for(int i = 0; i < v1.length; i++){
            vector[i] = v1[i] + v2[i];
        }
        return vector;
    }

    //given two vectors, returns a vector whose components are the differences of the
    //corresponding components of the input vectors
    //for this one, order matters. -v2 is added to v1
    public static double[] subVecs(double[] v1, double[] v2){
        double[] vector = new double[v1.length];
        for(int i = 0; i < v1.length; i++){
            vector[i] = v1[i] - v2[i];
        }
        return vector;
    }

    //returns the dot product of two vectors by summing the product of corresponding
    //components
    public static double dot(double[] vec1, double[] vec2){
        //if(vec1.length == vec2.length){
        double result = 0.0;
        for(int i = 0; i < vec1.length; i++){
            result += (vec1[i] * vec2[i]);
        }
        return result;
        // }
    }

    //crosses vectors - assumes vectors are 3D but will just ignore higher dims
    //so could work
    public static double[] cross3D(double[] vec1, double[] vec2){
        //if(vec1.length == vec2.length == 3){
        double crossVec[] = new double[3];
        crossVec[0] = ( vec1[1] * vec2[2] ) - ( vec1[2] * vec2[1] );
        crossVec[1] = ( vec1[2] * vec2[0] ) - ( vec1[0] * vec2[2] );
        crossVec[2] = ( vec1[0] * vec2[1] ) - ( vec1[1] * vec2[0] );
        return crossVec;
        // }
    }

    //gets angle between two vectors by dividing the dot product by the product of
    //the vectors' lengths then inputting that result to the arccos function
    //possible NaN errors can occur if either vector is the 0 vector
    public static double getAngle(double[] vec1, double[] vec2){
        double cosTheta = dot(vec1, vec2) / ( getVecLength(vec1) * getVecLength(vec2) );
        return Math.acos(cosTheta);
    }

    //returns the vector resulting from projecting vec1 onto vec2
    public static double[] project(double[] vec1, double[] vec2){
        double scale = dot(vec1, vec2) / getVecLength(vec2);
        return scaleVec( norm(vec2), scale );
    }

    //given point in 3D cartesian coords and an eyeCoord (assumed to be at 0, 0, z)
    //gets 2D coordinates of projected point
    public static double[] getProjectedCoords(double[] pt3d, double[] eyeCoords){
        double eyeHeight = eyeCoords[2];
        double newX = pt3d[0] / (1 - pt3d[2]/eyeHeight);
        double newY = pt3d[1] / (1 - pt3d[2]/eyeHeight);
        return new double[] {newX, newY};
    }

    //returns 4x4 matrix to shift by parameters given (assumes 3D coords)
    public static double[][] transMtx(double x, double y, double z){ //, Boolean homogenous){
        return new double[][] { {1, 0, 0, x},
                                {0, 1, 0, y},
                                {0, 0, 1, z},
                                {0, 0, 0, 1} };
    }

    //returnx 4x4 matrix to scale x, y, and z by parameter vals
    //can also return a 3x3 matrix to scale if homogenous bool is false
    public static double[][] scaleMtx(double xFac, double yFac, double zFac, Boolean homogenous){
        if(homogenous){
            return new double[][] { {xFac, 0, 0, 0},
                                    {0, yFac, 0, 0},
                                    {0, 0, zFac, 0},
                                    {0, 0,    0, 1} };
        } else {
            return new double[][] { {xFac,    0,    0},
                                    {   0, yFac,    0},
                                    {   0,    0, zFac} };
        }
    }

    //returns 4x4 matrix by calling/returning more general scaleMtx function
    public static double[][] scaleMtx(double fac, Boolean homogenous){
        return scaleMtx(fac, fac, fac, homogenous);
    }

    //returns 4x4 matrix to rotate about x axis in CCW direction
    public static double[][] xRotateMtx(double theta, Boolean homogenous) {
        if(homogenous){
            return new double[][] { {1,               0,                0, 0},
                                    {0, Math.cos(theta), -Math.sin(theta), 0},
                                    {0, Math.sin(theta),  Math.cos(theta), 0},
                                    {0,                0,               0, 1} };
        } else {
            return new double[][] { {1,               0,                0},
                                    {0, Math.cos(theta), -Math.sin(theta)},
                                    {0, Math.sin(theta),  Math.cos(theta)} };
        }
    }

    //returns 4x4 matrix to rotate about y axis in CCW direction
    public static double[][] yRotateMtx(double theta, Boolean homogenous) {
        if(homogenous){
            return new double[][] { { Math.cos(theta), 0, Math.sin(theta), 0},
                                    {               0, 1,               0, 0},
                                    {-Math.sin(theta), 0, Math.cos(theta), 0},
                                    {               0, 0,               0, 1} };
        } else {
            return new double[][] { { Math.cos(theta), 0, Math.sin(theta)},
                                    {               0, 1,               0},
                                    {-Math.sin(theta), 0, Math.cos(theta)} };
        }
    }

    //returns 4x4 matrix to rotate about z axis in CCW direction
    public static double[][] zRotateMtx(double theta, Boolean homogenous) {
        if(homogenous){
            return new double[][] { {Math.cos(theta), -Math.sin(theta), 0, 0},
                                    {Math.sin(theta),  Math.cos(theta), 0, 0},
                                    {              0,                0, 1, 0},
                                    {              0,                0, 0, 1} };
        } else {
            return new double[][] { {Math.cos(theta), -Math.sin(theta), 0},
                                    {Math.sin(theta),  Math.cos(theta), 0},
                                    {              0,                0, 1} };
        }
    }

    //returns 4x4 matrix to rotate about arbitrary axis in CCW direction
    public static double[][] arbRotateMtx(double theta, Boolean homogenous, double ax, double ay, double az) {
        double ct = Math.cos(theta);
        double st = Math.sin(theta);
        double opp = 1 - ct; //renaming for convenience
        if(homogenous){
            return new double[][] { {ct + opp*Math.pow(ax, 2),        opp*ax*ay - st*az,        opp*ax*az + st*ay, 0},
                                    {       opp*ax*ay + st*az, ct + opp*Math.pow(ay, 2),        opp*ay*az - st*ax, 0},
                                    {       opp*ax*az - st*ay,        opp*ay*az + st*ax, ct + opp*Math.pow(az, 2), 0},
                                    {                       0,                        0,                        0, 1} };
        } else {
            return new double[][] { {ct + opp*Math.pow(ax, 2),        opp*ax*ay - st*az,        opp*ax*az + st*ay},
                                    {       opp*ax*ay + st*az, ct + opp*Math.pow(ay, 2),        opp*ay*az - st*ax},
                                    {       opp*ax*az - st*ay,        opp*ay*az + st*ax, ct + opp*Math.pow(az, 2)} };
        }
    }
}
