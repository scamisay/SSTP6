package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.ParticlesCreator;
import ar.edu.itba.ss.algorithm.cim.CellIndexMethod;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Room {

    private static final int MAX_CREATION_TRIES = 1000;

    private final double width;
    private final double height;
    private final double exitOpeningSize;
    private Area insideSiloArea;
    private final double topPadding;
    private final double bottomPadding;
    private List<Particle> particles;
    private double kN = 1e5;//N/m.
    private double kT = 1e3;//N/m.
    private double gamma = 1e3;
    private double A = 2000;
    private double B = 0.08;
    private double TAU = 0.5;//s

    private double drivenVelocity;
    public Vector2D target;


    //Cota superior para M: L/(2 * rMax)/4 > M
    private static final int M =4;

    // L > W > D
    public Room(double width, double height, double exitOpeningSize, double topPadding, double bottomPadding, double drivenVelocity) {
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

    public void evolveLeapFrog(double dt) {
        CellIndexMethod cim = instantiateCIM(particles);
        cim.calculate();
        particles.forEach( p -> p.updatePositionLF(dt));
        particles.forEach( p -> p.predictVelocity(dt));
        particles.forEach( p -> p.calculateForceLF(kN, gamma, this,A,B, drivenVelocity, TAU, target, dt));
        particles.forEach( p -> p.updateVelocityLF(dt));
    }

    public double getLeftWall() {
        return insideSiloArea.getMinX();
    }

    public double getRightWall() {
        return insideSiloArea.getWidth();
    }

    public boolean hasEscaped(Particle particle) {
        return (particle.getPosition().getY() < getBottomPadding())
                &&
                (particle.getLastPosition().getY() >= getBottomPadding());
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

    public List<Vector2D> getParticlesHaveJustEscaped(double dt) {
        return particles.stream()
                .filter(p->hasEscaped(p))
                .map(p-> new Vector2D(dt,p.getId()))
                .collect(Collectors.toList());
    }

    public boolean isSomeoneLeftToEscape() {
        return particles.stream().filter( p -> (p.getPosition().getY() + 4*p.getRadius()) >= getBottomPadding()).count() > 0;
    }
}
