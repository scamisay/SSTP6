package ar.edu.itba.ss.helper;

import ar.edu.itba.ss.domain.Particle;
import ar.edu.itba.ss.domain.Silo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class Printer {

    private double height;
    private double width;
    private Silo room;

    private static final String FILE_NAME_OVITO = "ovito.xyz";

    public Printer(Silo room) {
        this.room = room;
        this.height = room.getScenarioHeight();
        this.width = room.getWidth();
        try{
            File file = new File(FILE_NAME_OVITO);

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        }catch (Exception e){
            System.out.println("problemas creando el archivo "+FILE_NAME_OVITO);
        }
    }

    public void printState(double time, List<Particle> particles){
        printForOvito(time, particles);
    }

    private void printForOvito(double time, List<Particle> particles) {
        printStringToFile(FILE_NAME_OVITO, printParticles(time, particles));
    }

    private String printParticles(double time, List<Particle> particles) {
        String printedBorders = printSiloBorders();
        int particlesInBorder = printedBorders.split("\n").length;
        return (particles.size()+particlesInBorder+2)+"\n"+
                time + "\n" +
                "0 0 0 0 0 0 0.0001 0 0 0\n"+
                width +" "+height+" 0 0 0 0 0.0001 0 0 0\n"+
                printedBorders +
                particles.stream()
                        .map(Particle::toString)
                        .collect(Collectors.joining("\n")) +"\n";
    }

    private String printSiloBorders() {
        StringBuffer sb = new StringBuffer();
        String format = "%.6f %.6f 0 0 0 0 %.6f 1 0 0";
        double radius = .25;
        //walls
        for(double y = room.getBottomPadding(); y <= (room.getHeight()+ room.getBottomPadding()); y+=radius){
            sb.append(String.format(format, room.getLeftWall(), y, radius)+"\n");
            sb.append(String.format(format, room.getRightWall(), y, radius)+"\n");
        }

        //bottom
        for(double x = room.getLeftWall(); x <= room.getExitStart(); x+=radius){
            sb.append(String.format(format,x, room.getBottomPadding(), radius)+"\n");
        }
        for(double x = room.getExitEnd(); x <= room.getRightWall(); x+=radius){
            sb.append(String.format(format,x, room.getBottomPadding(), radius)+"\n");
        }

        //top
        for(double x = room.getLeftWall(); x <= room.getWidth(); x+=radius){
            sb.append(String.format(format,x, room.getBottomPadding()+ room.getHeight(), radius)+"\n");
        }
        return sb.toString();
    }

    private void printStringToFile(String filename, String content){
        try {
            Files.write(Paths.get(filename), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            try {
                new File(filename).createNewFile();
                Files.write(Paths.get(filename), content.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e1) {
                System.out.println("No se pudo crear el archivo "+filename);
            }
        }
    }

}
