package ar.edu.itba.ss.plotter;

import ar.edu.itba.ss.domain.CommonScenario;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CurvaDeDescargaConSlidingWindow {

    private int numberOfParticles;
    private double drivenVelocity;

    private List<List<Vector2D>> manyEgresos = new ArrayList<>();

    public CurvaDeDescargaConSlidingWindow(int numberOfParticles, double drivenVelocity) {
        this.numberOfParticles = numberOfParticles;
        this.drivenVelocity = drivenVelocity;
    }

    public void process(int samples){
        manyEgresos = IntStream.range(0, samples).boxed()
                .map( i -> CommonScenario.simulate(numberOfParticles, drivenVelocity).getEgresos())
                .collect(toList());
    }

    /**
     * cantidad de particulas x tiempo(avg, min, max) : (n, (t,t,t))
     */
    public Map<Integer, ErrorPoint> plotAdaptingX(){
        Map<Integer, ErrorPoint> plot = new HashMap<>();
        for(int n = 1; n <= numberOfParticles; n++){
            int index = n-1;
            double avg = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).average().getAsDouble();
            double min = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).min().getAsDouble();
            double max = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).max().getAsDouble();

            ErrorPoint errorPoint = new ErrorPoint( avg, min, max);
            plot.put(n, errorPoint);
        }
        return plot;
    }

    public String printXValues(Map<Integer, ErrorPoint> plot){
        return plot.keySet().stream().sorted()
                .map(x -> x.toString())
                .collect(joining(", "));
    }

    private String printYValues(Map<Integer, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("%.3f",plot.get(x).getAverage()))
                .collect(joining(", "));
    }


    private String printMinMaxValues(Map<Integer, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("[%.3f, %.3f]", plot.get(x).getMin(), plot.get(x).getMax()))
                .collect(joining(", "));
    }

    private Long countEgresosBefore(List<Vector2D> l, Double xValue) {
        return l.stream().filter( v -> v.getX() <= xValue).count();
    }

    private List<Double> getXValues(double minValue, double maxValue,int buckets) {
        double bucketSize = (maxValue - minValue)/ buckets;

        List<Double> values = new ArrayList<>();
        for(double x = minValue; x < maxValue+bucketSize; x+= bucketSize){
            values.add(x);
        }
        return values;
    }

    private double findMaxTime(List<List<Vector2D>> manyEgresos) {
        return manyEgresos.stream()
                .mapToDouble( l ->
                                    l.stream().mapToDouble( v -> v.getX())
                                            .max().getAsDouble()
                                ).max().getAsDouble();
    }

    /**
     * @param curvaDeDescaga : (n, t)
     *
     *  @return caudal : (t, q)
     */
    public List<Vector2D> calculateCaudal(int windowSize, List<Vector2D> curvaDeDescaga){
        if(windowSize > curvaDeDescaga.size()){
            throw new RuntimeException("La ventana es demasiado grande");
        }
        return IntStream.range(0, curvaDeDescaga.size()-windowSize).boxed().map(
                i ->
                        new Vector2D(
                                (curvaDeDescaga.get(i).getY()+curvaDeDescaga.get(i+windowSize).getY())/2,
                                windowSize/(curvaDeDescaga.get(i+windowSize).getY()-curvaDeDescaga.get(i).getY())
                        )
        ).collect(toList());
    }


    public static void main(String[] args) {
        CurvaDeDescargaConSlidingWindow cd = new CurvaDeDescargaConSlidingWindow(100,3);
        cd.process(5);
        Map<Integer, ErrorPoint> plot = cd.plotAdaptingX();
        List<Vector2D> curvaDeDescarga = cd.getCurvaDeDescarga(plot);
        List<Vector2D> caudal = cd.calculateCaudal(3, curvaDeDescarga);

        String x = cd.printXValues(plot);
        String y = cd.printYValues(plot);
        String minMax = cd.printMinMaxValues(plot);

        String xCaudal = caudal.stream().map(q -> String.format("%.3f",q.getX())).collect(joining(", "));
        String yCaudal = caudal.stream().map(q -> String.format("%.3f",q.getY())).collect(joining(", "));
        System.out.println(x);
        System.out.println(y);
        System.out.println(minMax);
        System.out.println(xCaudal);
        System.out.println(yCaudal);

    }

    /**
     *
     * @param plottedCD : (n, (t,t,t))
     * @return (n,t)
     */
    private List<Vector2D> getCurvaDeDescarga(Map<Integer, ErrorPoint> plottedCD) {
        return plottedCD.keySet().stream().sorted()
                .map( n -> new Vector2D(n, plottedCD.get(n).getAverage()))
                .collect(toList());
    }

}
