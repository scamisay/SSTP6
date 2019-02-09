package ar.edu.itba.ss.algorithm;

import ar.edu.itba.ss.domain.Room;
import ar.edu.itba.ss.helper.Printer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class GranularSystem {

    private double dt;
    private long dt2;
    private int particleNumbers;

    private Room room;
    private Printer printer;

    private boolean updateStatisticalValues;

    //(tiempo de egreso, particula)
    private List<Vector2D> egresos = new ArrayList<>();

    private static final double SLIDING_WINDOW = .2;

    public GranularSystem(double dt, long dt2, Room room, int particleNumbers) {
        this.dt = dt;
        this.dt2 = dt2;
        this.room = room;
        this.particleNumbers = particleNumbers;
    }

    public void recordStatistics(){
        updateStatisticalValues = true;
    }

    public void setPrintable(){
        printer = new Printer(room);
    }

    public void simulate(){
        room.fillSilo(particleNumbers);

        double t = 0;
        long i = 0;

        for (; i<=dt2*100; t+=dt, i++ ){
            if (i % dt2 == 0 ) {
                if(printer != null){
                    printer.printState(t, room.getParticles());
                    if((i*1e5)%555 == 0){
                        //System.out.println(particleNumbers - egresos.size() +" t="+t);
                    }
                    System.out.println(i/dt2);
                }

            }
            if(updateStatisticalValues ){
                updateEscapes(t);
            }
            room.evolveLeapFrog(dt);

        }
        printer.printAll();
    }

    public List<Vector2D> getEgresos() {
        return egresos;
    }

    private void updateEscapes(double t) {
        List<Vector2D> escaped = room.getParticlesHaveJustEscaped(t);
        if(!escaped.isEmpty()){
            egresos.addAll(escaped);
        }
    }


}
