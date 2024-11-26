package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final static String MINIMUM_STRING = "minimum";
    private final static String MAXIMUM_STRING = "maximum";
    private final static String ATTEMPTS_STRING = "attempts";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final String nameFile, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        final Configuration.Builder builder = new Configuration.Builder();
        try (var application = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(nameFile)))) {
            for (var line = application.readLine(); line != ""; line = application.readLine()){
                String lineValue = application.readLine().toLowerCase(Locale.ROOT).trim();
                while (lineValue != null) {
                    final StringTokenizer st = new StringTokenizer(lineValue, ":");
                    final String element = st.nextToken();
                    if (MINIMUM_STRING.equals(element)) {
                        builder.setMin(Integer.parseInt(st.nextToken(":").trim()));
                    } else if (MAXIMUM_STRING.equals(element)) {
                        builder.setMax(Integer.parseInt(st.nextToken(":").trim()));
                    } else if (ATTEMPTS_STRING.equals(element)) {
                        builder.setAttempts(Integer.parseInt(st.nextToken(":").trim()));
                    }
                }
                lineValue = application.readLine().toLowerCase(Locale.ROOT).trim();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        };
        var x = builder.build();
        model = new DrawNumberImpl(x.getMin(), x.getMax(), x.getAttempts());

    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp("config.yml", new DrawNumberViewImpl(), new PrintStreamView(System.out), 
        new DrawNumberViewImpl(), new PrintStreamView("output.log"));
    }

}
