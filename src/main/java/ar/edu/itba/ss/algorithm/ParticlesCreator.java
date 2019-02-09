package ar.edu.itba.ss.algorithm;

import ar.edu.itba.ss.domain.Area;
import ar.edu.itba.ss.domain.Particle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static ar.edu.itba.ss.helper.Numeric.randomBetween;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class ParticlesCreator {

    public static final double MASS = 70;//kg
    public static final double MIN_RADIUS = .5/2;//m
    public static final double MAX_RADIUS = .58/2;//m

    private Area area;

    public ParticlesCreator(Area area) {
        this.area = area;
    }

    //todo: definir generador aleatorios con seed
    public Particle create() {
        double radius = createRadius();
        Vector2D position = createPosition(radius);
        return new Particle(position, MASS, radius,true);
    }

    private double createRadius() {
        return randomBetween(MIN_RADIUS, MAX_RADIUS);
    }

    private Vector2D createPosition(double radius) {
        return createRandomPosition(radius);
    }

    public Vector2D createRandomPosition(double radius){
        double crowdRadius = area.getHeight()/2;
        double minHeight = area.getMinY();
        double x = randomBetween(area.getMinX()+radius, area.getWidth()-radius);
        double y = randomBetween(minHeight+radius, heightInCircle(crowdRadius, x, crowdRadius, minHeight)-radius);
        return new Vector2D(x,y);
    }

    private double heightInCircle(double r, double x, double x_0, double y_0) {
        return sqrt(pow(r,2)-pow(x-x_0,2))+y_0;
    }

}
