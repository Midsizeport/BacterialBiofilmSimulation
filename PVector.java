import java.io.Serializable;
import java.util.Objects;

/**
 * A Vector
 *
 * @author Seth McNevin, Brian Mbawa, Gladwin Ngobeni
 */

public class PVector implements Serializable {
    private double x;
    private double y;
    private double z;

    /**
     *
     * @param x the first entry of this vector
     * @param y the second entry of this vector
     */
    public PVector(double x, double y){
        this(x, y, 0); // Default z to 0
    }

    /**
     * Vector defined in 3 dimensions
     *
     * @param x the first entry of this vector
     * @param y the second entry of this vector
     * @param z the third entry of this vector
     */
    public PVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a copy of this vector object at time of simulation - no Seth, a copy constructor
     * does not do the same thing. We need the coordinates right at this instance, not when the vector
     * was initialized, dummy.
     *
     * @return copy of this vector
     */
    public PVector copy() {
        return new PVector(this.x, this.y, this.z);
    }

    /**
     * the zero vector
     */
    public static final PVector ZERO = new PVector(0, 0, 0);

    /**
     * Adds another vector to this vector
     *
     * @param v the other vector to be added
     */
    public void add(PVector v){
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    /**
     * Adds another vector to this vector
     *
     * @param v the other vector to be added
     * @return the sum vector
     */
    public PVector addVector(PVector v) {
        return new PVector(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    /**
     * Subtracts another vector from this vector
     *
     * @param v the other vector to be subtracted
     */
    public void sub(PVector v){
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public static PVector sub(PVector v1, PVector v2) {
        return new PVector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public void mult(double scalar){
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public PVector multiply(double scalar){
        return new PVector(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public PVector divide(double scalar){
        return new PVector(this.x / scalar, this.y / scalar ,this.z / scalar);
    }

    /**
     * Computes the magnitude of this vector
     *
     * @return the computed magnitude as a double
     */
    public double magnitude(){
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Normalizes this vector
     *
     * @return a unit vector
     */
    public PVector normalize(){
        double mag = magnitude();
        if (mag == 0) return new PVector(0, 0, 0);
        return new PVector(x / mag, y / mag, z / mag);
    }

    /**
     * Creates a new unit vector with a given angle
     *
     * @param radians the angle in radians
     * @return a new angle with approximate length 1.0 and the given angle
     */
    public static PVector unit(double radians) {
        double x = Math.cos(radians);
        double y = Math.sin(radians);
        return new PVector(x,y);
    }

    /**
     * Limits the magnitude of this vector to a maximum
     *
     * @param limit limiting scalar
     */
    public void speedLimit(double limit){
        if (magnitude() > limit){
            normalize();
            mult(limit);
        }
    }

    /**
     * Calculates the angle between the origin point (0,0,0) to this vector's (x,y,z)
     *
     * @return angle between origin and (x,y,z) in radians
     */
    public double direction() {
        return Math.atan2(y, x);
    }

    /**
     * Obtains the straight line distance between two vector
     *
     * @param v the other vector with which the distance will be computed
     * @return returns a difference vector
     */
    public double distance(PVector v){
        double xdist = x - v.x;
        double ydist = y - v.y;
        double zdist = z - v.z;
        return Math.sqrt(xdist * xdist + ydist * ydist + zdist * zdist);
    }

    /**
     * Computes the dot product between this vector and another vector
     *
     * @param v the other vector with which the dot product will be calculated
     * @return the dot product as a double
     */
    public double dot(PVector v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    /**
     * Computes the cross product between this vector and another vector
     *
     * @param v the other vector with which the cross product will be calculated
     * @return the cross product as a new PVector
     */
    public PVector cross(PVector v) {
        double x = this.y * v.z - this.z * v.y;
        double y = this.z * v.x - this.x * v.z;
        double z = this.x * v.y - this.y * v.x;
        return new PVector(x, y, z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if both references point to the same object
        if (obj == null || getClass() != obj.getClass()) return false; // Check for null and class type
        PVector pVector = (PVector) obj; // Typecast the object to PVector
        return Double.compare(pVector.x, x) == 0 &&
                Double.compare(pVector.y, y) == 0 &&
                Double.compare(pVector.z, z) == 0; // Compare the coordinates
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z); // Use Objects.hash() to generate a hash code based on coordinates
    }

    @Override
    public String toString() {
        return this.x + " " +  this.y + " " + this.z;
    }
}


