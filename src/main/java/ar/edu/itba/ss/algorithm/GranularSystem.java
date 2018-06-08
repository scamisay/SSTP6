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
    private int particleNumbers;

    private Silo silo;
    private Printer printer;

    private boolean updateStatisticalValues;

    //(tiempo de egreso, particula)
    private List<Vector2D> egresos = new ArrayList<>();

    private static final double SLIDING_WINDOW = .2;

    public GranularSystem(double dt, long dt2, Silo silo, int particleNumbers) {
        this.dt = dt;
        this.dt2 = dt2;
        this.silo = silo;
        this.particleNumbers = particleNumbers;
    }

    public void recordStatistics(){
        updateStatisticalValues = true;
    }

    public void setPrintable(){
        printer = new Printer(silo);
    }

    public void simulate(){
        silo.fillSilo(particleNumbers);

        double t = 0;
        long i = 0;

        for (; silo.isSomeoneLeftToEscape() ; t+=dt, i++ ){
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
            if(updateStatisticalValues ){
                updateEscapes(t);
            }
            silo.evolveLeapFrog(dt);

        }
    }

    public List<Vector2D> getEgresos() {
        return egresos;
    }

    private void updateEscapes(double t) {
        List<Vector2D> escaped = silo.getParticlesHaveJustEscaped(t);
        if(!escaped.isEmpty()){
            egresos.addAll(escaped);
        }
    }


}
