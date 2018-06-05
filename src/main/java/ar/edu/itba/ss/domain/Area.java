package ar.edu.itba.ss.domain;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Area {
    private Vector2D upLeftPoint;
    private Vector2D downRightPoint;

    private Area(){}

    Area(double x1, double y1, double x2, double y2){
        upLeftPoint = new Vector2D(x1,y1);
        downRightPoint = new Vector2D(x2,y2);
    }

    public double getMinX() {
        return upLeftPoint.getX();
    }

    public double getMinY() {
        return downRightPoint.getY();
    }

    public double getWidth(){
        return downRightPoint.getX() - upLeftPoint.getX();
    }

    public double getHeight(){
        return upLeftPoint.getY() - downRightPoint.getY();
    }

    public boolean containsParticle(Particle particle) {
        boolean isInX = (getMinX() <= particle.getPosition().getX()) && (particle.getPosition().getX() <= getWidth());
        boolean isInY = (getMinY() <= particle.getPosition().getY()) && (particle.getPosition().getY() <= getHeight());
        return isInX && isInY;
    }
}
