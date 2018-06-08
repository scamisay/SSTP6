package ar.edu.itba.ss.execution;

import ar.edu.itba.ss.algorithm.GranularSystem;
import ar.edu.itba.ss.domain.Room;

public class Main {

    //Dimensiones de la habitacion
    private static final double WIDTH = 20;
    private static final double HEIGHT = 20;
    private static final double EXIT_WIDTH = 1.2;

    //tiempos de simulacion
    private static final double DT = 5e-5;
    private static final long DT2 = (long)1e2;

    private static final int PARTICLE_NUMBER = 200;

    public static void main(String[] args) {

        double topPadding = 0;
        double bottomPadding = 5;
        double drivenVelocity = 5;
        Room room = new Room(WIDTH, HEIGHT, EXIT_WIDTH, topPadding,bottomPadding,drivenVelocity);
        GranularSystem system = new GranularSystem(DT, DT2, room, PARTICLE_NUMBER);
        system.setPrintable();
        system.recordStatistics();
        system.simulate();
        System.out.println(1);
    }

}
