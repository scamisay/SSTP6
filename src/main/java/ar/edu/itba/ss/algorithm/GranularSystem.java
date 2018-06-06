package ar.edu.itba.ss.algorithm;

import ar.edu.itba.ss.domain.Particle;
import ar.edu.itba.ss.domain.Silo;
import ar.edu.itba.ss.helper.Printer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static ar.edu.itba.ss.algorithm.ParticlesCreator.MAX_RADIUS;
import static ar.edu.itba.ss.algorithm.ParticlesCreator.MIN_RADIUS;
import static ar.edu.itba.ss.domain.Particle.G;

public class GranularSystem {

    private double dt;
    private long dt2;
    private double simulationTime;
    private int particleNumbers;

    private Silo silo;
    private Printer printer;

    private boolean updateStatisticalValues;
    private List<Vector2D> kineticEnergy = new ArrayList<>();
    private List<Vector2D> caudal = new ArrayList<>();

    private static final double SLIDING_WINDOW = .2;

    public GranularSystem(double dt, long dt2, double simulationTime, Silo silo, int particleNumbers) {
        this.dt = dt;
        this.dt2 = dt2;
        this.simulationTime = simulationTime;
        this.silo = silo;
        this.particleNumbers = particleNumbers;
    }

    private List<Double> timesForStatics = new ArrayList<>();
    private List<Double> npList = new ArrayList<>();

    public void updateStatisticalValues(List<Double> times){
        timesForStatics = times;
    }

    public void setPrintable(){
        printer = new Printer(silo);
    }

    public void simulate(){
        silo.fillSilo(particleNumbers);

        double t = 0;
        long i = 0;

        double nextStatPoint = simulationTime;
        Iterator<Double> itStatPoints = timesForStatics.iterator();
        if(itStatPoints.hasNext()){
            nextStatPoint = itStatPoints.next();
        }

        for (; t < simulationTime ; t+=dt, i++ ){
            if (i % dt2 == 0 ) {
                if(printer != null){
                    printer.printState(t, silo.getParticles());
                    System.out.println(t);
                }

               /* if(t > nextStatPoint){
                    updateKineticEnergy(t);
                    updateCaudal(t);

                    if(itStatPoints.hasNext()){
                        nextStatPoint = itStatPoints.next();
                        npList.add(calculateNp());
                    }else {
                        nextStatPoint = simulationTime;
                    }
                }*/

                /*if(updateStatisticalValues && (t > simulationTime*SLIDING_WINDOW)){
                    updateKineticEnergy(t);
                    updateCaudal(t);
                }*/
            }
            silo.evolveLeapFrog(dt);

        }
    }

    private List<Particle> particlesInPressureArea(){
        return silo.getParticles().stream()
                .filter( p -> p.getPosition().getY() >= silo.getBottomPadding())
                .filter( p -> p.getPosition().getY() <= (silo.getBottomPadding() + silo.getHeight()))
                .filter( p -> p.getForce() .getX() > 0).collect(Collectors.toList());
    }

    private double calculateNp() {
        long n =  particlesInPressureArea().size();
        double areaOfPressure = calculateAreaOfPressure();
        return n/areaOfPressure;
    }

    private double calculateAreaOfPressure() {
        /*double height = particlesInPressureArea().stream().mapToDouble( p -> p.getPosition().getY()).max().getAsDouble() - silo.getBottomPadding();
        return height * silo.getWidth();*/
        return silo.getWidth() * silo.getWidth();
    }

    private void updateCaudal(double t) {
        caudal.add(new Vector2D(t, silo.numberOfparticlesHaveEscaped()));
    }

    private void updateKineticEnergy(double t) {
        kineticEnergy.add(new Vector2D(t, silo.getKineticEnergy()));
    }


    public List<Vector2D> getKineticEnergy() {
        return kineticEnergy;
    }

    public List<Vector2D> getCaudal() {
        return caudal;
    }

    public double getAverageCaudal(){
        return caudal.stream().mapToDouble(v->v.getY()).average().getAsDouble();
    }

    public double getStandardDeviation(){
        double average = getAverageCaudal();
        return Math.sqrt(
                caudal.stream()
                .mapToDouble(v->Math.pow( v.getY() - average ,2))
                .sum() / (caudal.size() - 1)
        );
    }

    public List<Double> getNpList() {
        return npList;
    }

    public double getBeverlooCaudal(double c){
        double r = (MAX_RADIUS+MIN_RADIUS)/2;
        double d = silo.getExitOpeningSize();
        double np = npList.stream().mapToDouble( cnp -> cnp).average().getAsDouble();
        return np*Math.sqrt(G)*Math.pow(d-(c*r), 1.5);
    }
}
