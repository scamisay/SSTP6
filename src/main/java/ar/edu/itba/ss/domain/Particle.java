package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.cim.Cell;
import ar.edu.itba.ss.algorithm.cim.Range;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

public class Particle {
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D force;
    private double mass;
    private double radius;
    private Cell cell;
    private List<Particle> neighbours = new ArrayList<>();
    private Vector2D lastForce;
    private Vector2D lastPosition;

    public static final double G = 9.80665;// 9.80665 m/s2

    public Particle(Vector2D position, double mass, double radius) {
        this.position = position;
        this.mass = mass;
        this.radius = radius;
        initParticle();
    }

    public Particle(Vector2D position, Particle particle) {
        this.position = position;
        this.velocity = particle.getVelocity();
        this.force = particle.getForce();
        this.mass = particle.getMass();
        this.radius = particle.getRadius();
    }

    private void initParticle(){
        this.velocity = new Vector2D(0,0);
        this.force = new Vector2D(0,0);
        lastForce = force;
        lastPosition = position;
    }

    /***
     * getters and setters start
     */
    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public double getRadius() {
        return radius;
    }

    public Vector2D getLastPosition() {
        return lastPosition;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getForce() {
        return force;
    }

    public double getMass() {
        return mass;
    }

    public List<Particle> getNeighbours() {
        return neighbours;
    }

    public void setForce(Vector2D force) {
        this.force = force;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public void setLastPosition(Vector2D lastPosition) {
        this.lastPosition = lastPosition;
    }

    /***
     * getters and setters end
     */



    public boolean isCloseEnough(Particle particle, double maxDistance) {
        return distanceBorderToBorder(particle) <= maxDistance;
    }

    public Double distanceBorderToBorder(Particle particle){
        return distanceCenterToCenter(particle) - (getRadius() + particle.getRadius());
    }

    private double distanceCenterToCenter(Particle particle) {
        double difx = getPosition().getX() - particle.getPosition().getX();
        double dify = getPosition().getY() - particle.getPosition().getY();
        return Math.sqrt(Math.pow(difx, 2) + Math.pow(dify, 2));
    }

    public boolean isNeighbourCloseEnough(Particle particle, double maxDistance, boolean periodicContourCondition) {
        if (periodicContourCondition) {
            List<Cell> calculated = getCell().calculateNeighbourCells();
            if (!calculated.contains(particle.getCell())) {
                //debo dar la vuelta

                //defino las direcciones en cada una de las componentes
                double newX = getNewX(calculated,particle);
                double newY = getNewY(calculated,particle);



                Particle newParticle = new Particle(new Vector2D(newX, newY), particle);
                return distanceCenterToCenter(newParticle) <= maxDistance;
            }
        }
        return isCloseEnough(particle, maxDistance);
    }

    private double getNewX(List<Cell> calculated, Particle particle) {
        if (!hasRangeX(calculated, particle.getCell().getRangeX())) {
            if (getPosition().getX() - particle.getPosition().getX() > 0) {
                return particle.getPosition().getX() + getCell().getRangeX().getHighest();
            } else {
                return particle.getPosition().getX() - getCell().getRangeX().getHighest();
            }
        }
        return particle.getPosition().getX();
    }
    private double getNewY(List<Cell> calculated, Particle particle) {
        if (!hasRangeY(calculated, particle.getCell().getRangeY())) {
            if (getPosition().getY() - particle.getPosition().getY() > 0) {
                return particle.getPosition().getY() + getCell().getRangeY().getHighest();
            } else {
                return particle.getPosition().getY() - getCell().getRangeY().getHighest();
            }
        }
        return particle.getPosition().getY();

    }

    private boolean hasRangeX(List<Cell> calculated, Range rangex) {
        for (Cell c : calculated) {
            if (c != null && c.getRangeX().equals(rangex)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRangeY(List<Cell> calculated, Range rangey) {
        for (Cell c : calculated) {
            try {
                if (c != null && c.getRangeY().equals(rangey)) {
                    return true;
                }
            } catch (Exception e) {
                System.out.print(1);
            }

        }
        return false;
    }

    public List<Particle> getOtherParticlesInCell() {
        return getCell().getParticles().stream().filter(p -> !p.equals(this)).collect(toList());
    }

    public void addNeighbour(Particle particle) {
        if (particle == null) {
            throw new IllegalArgumentException("La particula no puede ser nula");
        }
        neighbours.add(particle);
    }

    public double overlap(Particle other) {
        double overlap = getRadius() + other.getRadius() - getPosition().distance(other.getPosition());
        return overlap > 0 ? overlap : 0;
        //return overlap;
    }

    public double previusOverlap(Particle other) {
        double overlap = getRadius() + other.getRadius() - getLastPosition().distance(other.getLastPosition());
        return overlap > 0 ? overlap : 0;//todo: probar si se permiten valores negativos
    }

    private double overlapDerivate(Particle p) {
        return overlap(p) - previusOverlap(p);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"%.6f %.6f %.6f %.6f %.6f %.6f %.6f 1 1 1",
                position.getX(), position.getY(),
                velocity.getX(), velocity.getY(),
                force.getX(),force.getY(),
                radius);
    }

    public void updatePosition(double dt, Silo silo) {
        if(silo.wentOutside(this)){
            position = silo.chooseAvailablePositionInSilo(radius);
            initParticle();
        }else{
            double lastXPosition = lastPosition.getX();
            double lastYPosition = lastPosition.getY();
            lastPosition = position;
            double newPosX = position.getX() + dt*velocity.getX() +(FastMath.pow(dt,2)/mass) *force.getX();
            double newPosY = position.getY() + dt*velocity.getY() +(FastMath.pow(dt,2)/mass) *force.getY();
            position = new Vector2D(newPosX,newPosY);

            if(brokeThroughBottom(silo,lastYPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(position.getX(), Math.max(silo.getBottomPadding(), lastYPosition));
            }else if(brokeThroughTop(silo,lastYPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(position.getX(), Math.min(silo.getBottomPadding(), lastYPosition));
            }else if(brokeThroughLefttWall(silo,lastXPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(Math.max(silo.getLeftWall(),lastXPosition), position.getY());
            }else if(brokeThroughRighttWall(silo,lastXPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(Math.min(silo.getRightWall(),lastXPosition), position.getY());
            }
        }
    }

    private boolean brokeThroughRighttWall(Silo silo, double lastXPosition) {
        return (lastXPosition < silo.getLeftWall() ) && ( silo.getRightWall() <= position.getX());
    }

    private boolean brokeThroughLefttWall(Silo silo, double lastXPosition) {
        return (lastXPosition > silo.getLeftWall() ) && ( silo.getLeftWall() >= position.getX());
    }


    private boolean brokeThroughBottom(Silo silo, double lastYPosition) {
        return !silo.isInExitArea(position.getX()) &&
                position.getX() >= silo.getLeftWall() &&
                position.getX() <= silo.getRightWall() &&
                (lastYPosition > silo.getBottomPadding() ) && ( silo.getBottomPadding() >= position.getY());
    }

    private boolean brokeThroughTop(Silo silo, double lastYPosition) {
        double top = silo.getBottomPadding() + silo.getHeight();
        return !silo.isInExitArea(position.getX()) &&
                position.getX() >= silo.getLeftWall() &&
                position.getX() <= silo.getRightWall() &&
                (lastYPosition < top ) && ( top <= position.getY());
    }

    void updateVelocity(double dt) {
        double newVx = velocity.getX() + (dt/(2*mass))*(lastForce.getX()+force.getX());
        double newVy = velocity.getY() + (dt/(2*mass))*(lastForce.getY()+force.getY());
        velocity = new Vector2D(newVx,newVy);
    }

    void updateForce(){
        lastForce=force;
        //force = new Vector2D(0,0);
    }

    public void calculateForce(double kN, double gamma, Silo silo, double dt) {
        //me quedo con los vecinos que colisionan conmigo
        List<Particle> collisionsWithParticles =
                getNeighbours().stream()
                .filter(p -> this.overlap(p)>0)
                .collect(toList());

        double overlapWithAWall = overlapWithAWall(silo);
        if(overlapWithAWall > 0){
            Particle opositeParticle = createMirroredParticle(overlapWithAWall);
            collisionsWithParticles.add(opositeParticle);
        }

        double totalForceInX = calculateTotalForceInX(collisionsWithParticles, kN, gamma, dt);
        double totalForceInY = calculateTotalForceInY(collisionsWithParticles, kN, gamma, dt);
        force = new Vector2D(totalForceInX, totalForceInY);

        if(overlapWithAWall > 0){
            //velocity = velocity.negate();
            double bottomOverlap = overlapWithABottomWall(silo);
            Vector2D newVelocidy = null;
            if(overlapWithAWall != bottomOverlap){
                //fue con una pared
                newVelocidy = new Vector2D(velocity.getX()*-1, velocity.getY());
            }else {
                //fue con el fondo o con el techo
                newVelocidy = new Vector2D(velocity.getX(), velocity.getY()*-1);
            }
            //se modifica una de las componentes de la velocidad dependiendo de q pared toco
            velocity = newVelocidy;
        }
    }

    private Particle createMirroredParticle(double overlapWithAWall) {
        Vector2D influence = force.equals(force.getZero())? force: force.normalize();
        Vector2D mirroredPos = position.add(influence.scalarMultiply(overlapWithAWall));
        //Vector2D mirroredPosPrev = lastPosition.add(influence.scalarMultiply(overlapWithAWall));

        Particle mirrored = new Particle(mirroredPos, this);

        //cambio el sentido de las fuerzas para la particula espejada
        mirrored.setForce(force.negate());
        mirrored.setVelocity(velocity.negate());
        mirrored.setLastPosition(mirroredPos);
        return mirrored;
    }

    private double calculateTotalForceInX(List<Particle> collisionsWithParticles, double kN, double gamma, double dt) {
        return collisionsWithParticles.stream()
                .mapToDouble(p ->
                        getNormalForce(p,kN,gamma, dt)*getNormalVersor(p).getX()
                )
                .sum();
    }

    private double overlapWithABottomWall(Silo silo) {
        if(!silo.containsParticle(this) || silo.isInExitArea(getPosition().getX())){
            return 0;
        }

        List<Vector2D> walls = Arrays.asList(
                new Vector2D(getPosition().getX(), silo.getBottomPadding())
        );

        return walls.stream()
                .mapToDouble( w -> getRadius() - getPosition().distance(w) )
                .max().getAsDouble();
    }


    private double overlapWithAWall(Silo silo) {
        //si esta afuera del silo o a la altura de la apertura no considero el overlap
        if(!silo.containsParticle(this) || silo.isInExitArea(getPosition().getX())){
            return 0;
        }

        List<Vector2D> walls = Arrays.asList(
                new Vector2D(silo.getLeftWall(), getPosition().getY()),
                new Vector2D(silo.getRightWall(), getPosition().getY()),
                new Vector2D(getPosition().getX(), silo.getBottomPadding()),
                new Vector2D(getPosition().getX(), silo.getHeight()+silo.getBottomPadding())
        );

        double ret = walls.stream()
                .mapToDouble( w -> getRadius() - getPosition().distance(w) )
                .max().getAsDouble();
        return ret>0?ret:0;
    }

    private double calculateTotalForceInY(List<Particle> collisionsWithParticles, double kN, double gamma, double dt) {
        return collisionsWithParticles.stream()
                .mapToDouble(p ->
                        this.getNormalForce(p,kN,gamma,dt)*this.getNormalVersor(p).getY()
                )
                .sum() - getMass()*G;
    }

    private double getNormalForce(Particle p, double kN, double gamma, double dt) {
        return getNormalVersor(p).scalarMultiply(-1*kN*overlap(p)-gamma*overlapDerivate(p)).getNorm();
    }

    private Vector2D getNormalVersor(Particle p) {
        double e_n_x = ( this.getPosition().getX() - p.getPosition().getX())/(p.getPosition().distance(this.getPosition()));
        double e_n_y = ( this.getPosition().getY() - p.getPosition().getY())/(p.getPosition().distance(this.getPosition()));
        return new Vector2D(e_n_x, e_n_y);
    }


    /*private Vector2D getTangentVersor(Particle p) {
        Vector2D normalVersor = getNormalVersor(p);
        double x = -1*normalVersor.getY();
        double y = normalVersor.getY();
        return new Vector2D(x, y);
    }*/

    public void resetNeighbours() {
        neighbours = new ArrayList<>();
    }

    public double getKineticEnergy(){
        if(Double.isNaN(velocity.getY()) || Double.isNaN(velocity.getX() )){
            return 0;
        }
        return 0.5*mass*velocity.getNormSq();
    }

    public void updateVelocityLF(double dt) {
        double newVx = velocity.getX() + (dt/(mass))*(force.getX());
        double newVy = velocity.getY() + (dt/(mass))*(force.getY());
        velocity = new Vector2D(newVx,newVy);
    }

    public void updatePositionLF(double dt, Silo silo) {
        if(silo.wentOutside(this)){
            position = silo.chooseAvailablePositionInSilo(radius);
            initParticle();
        }else{
            double lastXPosition = lastPosition.getX();
            double lastYPosition = lastPosition.getY();

            double newPosX = lastPosition.getX() + (dt/mass) *force.getX();
            double newPosY = lastPosition.getY() + (dt/mass) *force.getY();

            if(!silo.isInExitArea(newPosX) && (newPosY-getRadius())<=silo.getBottomPadding() ){
                newPosY = silo.getBottomPadding() + getRadius();
            }

            if((newPosX-getRadius())<=silo.getLeftWall() ){
                newPosX = silo.getLeftWall() + getRadius();
            }

            if((newPosX+getRadius())>=silo.getRightWall() ){
                newPosX = silo.getRightWall() - getRadius();
            }

            position = new Vector2D(newPosX,newPosY);
            lastPosition = position;

            /*if(brokeThroughBottom(silo,lastYPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(position.getX(), Math.max(silo.getBottomPadding(), lastYPosition));
            }else if(brokeThroughTop(silo,lastYPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(position.getX(), Math.min(silo.getBottomPadding(), lastYPosition));
            }else if(brokeThroughLefttWall(silo,lastXPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(Math.max(silo.getLeftWall(),lastXPosition), position.getY());
            }else if(brokeThroughRighttWall(silo,lastXPosition)){
                lastPosition = new Vector2D(lastXPosition, lastYPosition);
                position = new Vector2D(Math.min(silo.getRightWall(),lastXPosition), position.getY());
            }else {
                lastPosition = position;
            }*/
        }
    }

    public void calculateForceLF(double kN, double gamma, Silo silo, double dt) {
        List<Particle> collisionsWithParticles =
                getNeighbours().stream()
                        .filter(p -> this.overlap(p)>0)
                        .collect(toList());

        double overlapWithAWall = overlapWithAWall(silo);
        if(overlapWithAWall > 0){
            Particle opositeParticle = createMirroredParticle(overlapWithAWall);
            collisionsWithParticles.add(opositeParticle);
        }

        double totalForceInX = calculateTotalForceInX(collisionsWithParticles, kN, gamma, dt);
        double totalForceInY = calculateTotalForceInY(collisionsWithParticles, kN, gamma, dt);
        force = new Vector2D(totalForceInX, totalForceInY);
    }
}

