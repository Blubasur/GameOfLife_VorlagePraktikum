package de.hawhamburg.inf.gol;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Main application class.
 *
 * @author Christian Lins
 */
public class Application {

    /* Size of the playground in X dimension */
    public static final int DIM_X = 100;

    /* Size of the playground in Y dimension */
    public static final int DIM_Y = 100;

    /* Probability threshold that a cell is initially being created */
    public static final float ALIVE_PROBABILITY = 0.3125f;

    /* Sleep time between every generation in milliseconds */
    public static final int SLEEP = 200;

    /**
     * Creates an potentially unlimited stream of Cell objects. The stream uses
     * random numbers between [0, 1] and the probability threshold whether a
     * cell is created DEAD (random > p) or ALIVE (random <= p).
     *
     * @param p Cell alive probability threshold.
     * @return
     */
    private static Stream<Cell> createCellStream(float p) {
        Random random = new Random();
        Stream<Cell> cellStream = Stream.generate(() -> random.nextFloat())
                .map(randomNumber -> {
                    if (randomNumber > p) {
                        return new Cell(Cell.DEAD);
                    } else {
                        return new Cell(Cell.ALIVE);
                    }
                });
        return cellStream;
    }

    ;
    
    public static void main(String[] args) {
        Stream<Cell> cellStream = createCellStream(ALIVE_PROBABILITY);
        Playground playground = new Playground(DIM_X, DIM_Y, cellStream);

        // Create and show the application window
        ApplicationFrame window = new ApplicationFrame();
        window.setVisible(true);
        window.getContentPane().add(new PlaygroundComponent(playground));

        // Create and start a LifeThreadPool with 50 threads
        LifeThreadPool pool = new LifeThreadPool(50);
        pool.start();

        while (true) {
            Life life = new Life(playground);
            synchronized (pool) {
                for (int xi = 0; xi < DIM_X; xi++) {
                    for (int yi = 0; yi < DIM_Y; yi++) {

                        final int finalXi = xi;
                        final int finalYi = yi;
                        Cell cell = playground.getCell(finalXi, finalYi);

                        pool.submit(()
                                -> {
                            life.process(cell, finalXi, finalYi);
                        });

                    }
                }
                try {
                    //wait
                    pool.barrier();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Submit switch to next generation for each cell and force a
                // window repaint to update the graphics

                pool.submit(() -> {
                    playground.asList().forEach(cell -> cell.nextGen());
                    window.validate();
                    window.repaint();
                });
                // Wait SLEEP milliseconds until the next generation
                // TODO
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
