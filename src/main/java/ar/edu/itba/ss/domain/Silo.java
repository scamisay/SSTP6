package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.ParticlesCreator;
import ar.edu.itba.ss.algorithm.cim.CellIndexMethod;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Silo{

    private static final int MAX_CREATION_TRIES = 1000;

    private final double width;
    private final double height;
    private final double exitOpeningSize;
    private double falldownLimit;
    private Area insideSiloArea;
    private final double topPadding;
    private final double bottomPadding;
    private List<Particle> particles;
    private double kN = 5e7;//N/m.
    private double kT = 1e3;//N/m.
    private double gamma = 5e6;
    private double A = 2000;
    private double B = 0.08;
    private double TAU = 0.5;//s

    private double drivenVelocity;
    public Vector2D target;


    //Cota superior para M: L/(2 * rMax)/4 > M
    private static final int M =4;

    // L > W > D
    public Silo(double width, double height, double exitOpeningSize, double topPadding, double bottomPadding, double drivenVelocity) {
        if(exitOpeningSize > width || width > height){
            throw new IllegalArgumentException("height > width > exitOpeningSize");
        }
        this.width = width;
        this.height = height;
        this.exitOpeningSize = exitOpeningSize;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        particles = new ArrayList<>();
        insideSiloArea = new Area(0,bottomPadding+height,width,bottomPadding);
        this.drivenVelocity = drivenVelocity;
        target = new Vector2D(width/2, 0);
    }

    public void fillSilo(int particlesToAdd) {
        ParticlesCreator filler = new ParticlesCreator(insideSiloArea);
        for(int i = 0; i < particlesToAdd; i++){
            if(!addOne(filler)){
                break;
            }
        }
    }

    public double getExitStart(){
        return (width / 2) - (exitOpeningSize / 2);
    }

    public double getExitOpeningSize() {
        return exitOpeningSize;
    }

    public double getExitEnd(){
        return getExitStart() + exitOpeningSize;
    }

    public boolean isInExitArea(double x){
        double exitStart = getExitStart();
        double exitEnd = getExitEnd();
        return (exitStart <= x) && (x <= exitEnd);
    }

    private CellIndexMethod instantiateCIM(List<Particle> particles){
        return new CellIndexMethod(M, insideSiloArea.getHeight(),
                ParticlesCreator.MAX_RADIUS*2., particles, false);
    }

    private boolean addOne(ParticlesCreator filler){
        for(int intent = 1 ; intent <= MAX_CREATION_TRIES; intent++){

            List<Particle> pAux = new ArrayList<>(this.particles);
            Particle particle = filler.create();
            pAux.add(particle);

            CellIndexMethod cim = instantiateCIM(pAux);
            cim.calculate();

            if(isThereRoomForParticle(particle)){
                addParticle(particle);
                return true;
            }
        }
        return false;
    }

    private boolean isThereRoomForParticle(Particle particle) {
        return particle.getNeighbours().stream()
                .noneMatch( p ->  particle.isOverlapped(p));
    }

    private void addParticle(Particle particle) {
        particles.add(particle);
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public double getHeight() {
        return height;
    }

    public double getScenarioHeight(){
        return height + bottomPadding + topPadding;
    }

    public double getWidth() {
        return width;
    }

    public double getBottomPadding() {
        return bottomPadding;
    }

    public void setkN(double kN) {
        this.kN = kN;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /*public void evolve(double dt) {
        CellIndexMethod cim = instantiateCIM(particles);
        cim.calculate();
        particles.forEach( p -> p.updatePosition(dt, this));
        particles.forEach(Particle::updateForce);
        particles.forEach( p -> p.calculateForce(kN, kT, this));
        particles.forEach( p -> p.updateVelocity(dt));
    }*/

    public void evolveLeapFrog(double dt) {
        CellIndexMethod cim = instantiateCIM(particles);
        cim.calculate();
        particles.forEach( p -> p.updateVelocityLF(dt));
        particles.forEach( p -> p.updatePositionLF(dt, this));
        particles.forEach( p -> p.calculateForceLF(kN, gamma, this,A,B, drivenVelocity, TAU, target, dt));
    }

    public boolean containsParticle(Vector2D aPosition) {
        return insideSiloArea.containsParticle(aPosition);
    }

    public double getLeftWall() {
        return insideSiloArea.getMinX();
    }

    public double getRightWall() {
        return insideSiloArea.getWidth();
    }

    public boolean wentOutside(Particle particle) {
        return (particle.getPosition().getY() <= 0 )||
                (getLeftWall() > particle.getPosition().getX()-particle.getRadius()) ||
                (getRightWall() < particle.getPosition().getX()+particle.getRadius());
    }

    //todo: ponerlo dentro del silo sin superposiciones
    public Vector2D chooseAvailablePositionInSilo(double radius) {
        ParticlesCreator creator = new ParticlesCreator(insideSiloArea);
        Vector2D relocatedPosition = creator.createRandomPosition(radius);
        return relocatedPosition;
    }

    public double getKineticEnergy() {
        return particles.stream().mapToDouble( p -> p.getKineticEnergy()).sum();
    }

    private List<Particle> particlesRecentlyFallen = new ArrayList<>();

    public long numberOfparticlesHaveEscaped() {
        Iterator<Particle> it = particlesRecentlyFallen.iterator();
        while(it.hasNext()){
            Particle particle = it.next();
            if(particle.getPosition().getY() > getBottomPadding()){
                it.remove();
            }
        }
        List<Particle> newFallen = particles.stream()
                .filter( p -> !particlesRecentlyFallen.contains(p))
                .filter( p -> p.getPosition().getY() < getBottomPadding())
                .collect(Collectors.toList());
        particlesRecentlyFallen.addAll(newFallen);
        return newFallen.size();
    }
}
