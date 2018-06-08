package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.GranularSystem;

public class CommonScenario {

    //Dimensiones de la habitacion
    private static final double WIDTH = 20;
    private static final double HEIGHT = 20;
    private static final double EXIT_WIDTH = 1.2;

    //tiempos de simulacion
    private static final double DT = 5e-5;
    private static final long DT2 = (long)1e2;

    private static final double topPadding = 0;
    private static final double bottomPadding = 5;

    public static GranularSystem simulate(int numberOfParticles, double drivenVelocity ){
        Room room = new Room(WIDTH, HEIGHT, EXIT_WIDTH, topPadding,bottomPadding,drivenVelocity);
        GranularSystem system = new GranularSystem(DT, DT2, room, numberOfParticles);
        system.setPrintable();
        system.recordStatistics();
        system.simulate();
        return system;
    }
}
