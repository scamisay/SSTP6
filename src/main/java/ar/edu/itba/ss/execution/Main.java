package ar.edu.itba.ss.execution;

import ar.edu.itba.ss.algorithm.GranularSystem;
import ar.edu.itba.ss.domain.Silo;

import java.util.ArrayList;

public class Main {

    //Dimensiones de la habitacion
    private static final double WIDTH = 20;
    private static final double HEIGHT = 20;
    private static final double EXIT_WIDTH = 1.2;

    //tiempos de simulacion
    private static final double DT = 1e-5;
    private static final long DT2 = (long)2e2;

    private static final int PARTICLE_NUMBER = 100;

    public static void main(String[] args) {

        double topPadding = 0;
        double bottomPadding = 5;
        double simulationTime = .2;
        Silo room = new Silo(WIDTH, HEIGHT, EXIT_WIDTH, topPadding,bottomPadding);
        GranularSystem system = new GranularSystem(DT, DT2, simulationTime, room, PARTICLE_NUMBER);
        system.setPrintable();
        system.simulate();
        System.out.println(1);
    }

    /*public static void main(String[] args) {
        double width = 20;
        double exitOpeningSize = width / 5;
        Silo silo = new Silo(width, 20, exitOpeningSize, 0, 5);
        double dt = 1e-5;
        long dt2 = (long) 1e2;
        int particleNumbers = 300;
        GranularSystem system = new GranularSystem(dt, dt2, .2, silo, particleNumbers);
        system.setPrintable();
        system.simulate();

    }*/
}
