package ar.edu.itba.ss.execution;

import ar.edu.itba.ss.algorithm.GranularSystem;
import ar.edu.itba.ss.domain.Silo;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ar.edu.itba.ss.algorithm.ParticlesCreator.MAX_RADIUS;
import static ar.edu.itba.ss.algorithm.ParticlesCreator.MIN_RADIUS;
import static ar.edu.itba.ss.domain.Particle.G;

public class LastTask {

    private static final double WIDTH = .25;
    private static final double dt = 1e-5;
    private static final long dt2 = (long)2e2;
    private static final int particleNumbers = 400;
    private static final double np = 124.94;


    public static void main(String[] args) {
        List<Double> cList = initCList(0,4,0.08);
        double d_1 = WIDTH/7;
        double d0 = WIDTH/6;
        double d1 = WIDTH/5;
        double d2 = WIDTH/4;
        double d3 = WIDTH/3;
        double d4 = WIDTH/2;
        List<Double> openingList = Arrays.asList(d1,d2,d3,d4);

        //opening x caudal
       /* List<Vector2D> Qs = openingList.stream()
                .map(d -> new Vector2D( d, simulatedCaudal(d)))
                .collect(Collectors.toList());*/
        /*double s_1 = simulatedCaudal(d_1);
        double s_1_1 = simulatedCaudal(d_1);
        double s_1_2 = simulatedCaudal(d_1);
        double s2 = simulatedCaudal(d2);
        double s2_1 = simulatedCaudal(d2);
        double s2_2 = simulatedCaudal(d2);*/
        List<Vector2D> Qs = Arrays.asList(
                new Vector2D(d_1,1.7349397590361446),
                new Vector2D(d0,1.9236947791164658),
                new Vector2D(d1,2.1967871485943773),
                new Vector2D(d2,2.3413654618473894)
               // new Vector2D(d3,2.4136546184738954)
                //,new Vector2D(d4, 2.646586345381526)
                );

        List<Vector2D> eList = cList.stream()
                .map( c -> new Vector2D(c, error(Qs,c)))
                .collect(Collectors.toList());

        String cStr = eList.stream().filter(e->!Double.isNaN(e.getY())).map(e->String.format("%.3f",e.getX()))
                .collect(Collectors.joining(","));
        String eStr = eList.stream().filter(e->!Double.isNaN(e.getY())).map(e->String.format("%.3f",e.getY()))
                .collect(Collectors.joining(","));

        String dSStr = Qs.stream().map(e->String.format("%.3f",e.getX()))
                .collect(Collectors.joining(","));
        String QSStr = Qs.stream().map(e->String.format("%.3f",e.getY()))
                .collect(Collectors.joining(","));

        List<Double> dList = initCList(d_1,d2,(d2-d_1)/30);
        String dStr = dList.stream().map(d->d.toString()).collect(Collectors.joining(","));
        Double cMin = 1.6;
        String qt = dList.stream().map( d -> theoricCaudal(d,cMin)+"").collect(Collectors.joining(","));

        System.out.println(eList);
    }

    private static double error(List<Vector2D> Qs, Double c) {
        return Qs.stream().mapToDouble( qs -> Math.pow(qs.getY() - theoricCaudal(qs.getX(), c),2)).average().getAsDouble();
    }

    private static double theoricCaudal(double d, Double c) {
        double r = (MAX_RADIUS+MIN_RADIUS)/2;
        return np*Math.sqrt(G)*Math.pow(d-(c*r), 1.5);
    }

    private static double simulatedCaudal(Double d) {
        Silo room = new Silo(WIDTH, 2, d, .25,0.25);
        GranularSystem granularSystem = new GranularSystem(dt, dt2, 1.4, room, particleNumbers);
        granularSystem.updateStatisticalValues(Arrays.asList( .9));
        granularSystem.simulate();
        List<Vector2D> caudales = granularSystem.getCaudal();
        double qs = caudales.stream().mapToDouble( c -> c.getY()).average().getAsDouble();
        System.out.println("d,qs->"+d+","+qs);
        return qs;
    }

    private static List<Double> initCList(double bFrom, double bTo, double bStep) {
        List<Double> cList = new ArrayList<>();
        for(double c = bFrom; c < bTo ; c+=bStep){
            cList.add(c);
        }
        cList.add(bTo);
        return cList;
    }
}
