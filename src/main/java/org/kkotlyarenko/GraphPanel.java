package org.kkotlyarenko;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.kkotlyarenko.methods.NewtonSystemSolver;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.function.DoubleUnaryOperator;

public class GraphPanel extends JPanel {
    private ChartPanel chartPanel;
    private final int RESOLUTION = 400;

    private static final Shape DOT = new Rectangle2D.Double(-0.5, -0.5, 1, 1);

    public GraphPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    public void setFunction(DoubleUnaryOperator f, double xmin, double xmax, Double root) {
        XYSeries series = new XYSeries("f(x)");
        if (xmax <= xmin) {
            xmax = xmin + 1.0;
            System.err.println("Warning: Invalid range xmax <= xmin in setFunction. Using default range.");
        }
        double step = (xmax - xmin) / (double)RESOLUTION;
        if (step <= 0) {
            step = 1.0 / RESOLUTION;
        }

        for (double x = xmin; x <= xmax; x += step) {
            try {
                double y = f.applyAsDouble(x);
                if (Double.isFinite(y)) {
                    if (series.getItemCount() > 0) {
                        Double prevYObj = series.getY(series.getItemCount() - 1).doubleValue();
                        if (prevYObj != null) {
                            double prevY = prevYObj;
                            double rangeY = Math.abs(y - prevY);
                            double dynamicThreshold = Math.max(1.0, Math.abs(xmax-xmin)) * 50;
                            if (rangeY > dynamicThreshold && Double.isFinite(rangeY) && dynamicThreshold > 1e-9) {
                                series.add(x, null);
                            }
                        }
                    }
                    series.add(x, y);
                } else {
                    if (series.getItemCount() > 0 && series.getY(series.getItemCount() - 1) != null) {
                        series.add(x, null);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error calculating f(" + x + "): " + e.getMessage());
                if (series.getItemCount() > 0 && series.getY(series.getItemCount() - 1) != null) {
                    series.add(x, null);
                }
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "График функции f(x)", "x", "f(x)", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setDrawSeriesLineAsPath(true);

        plot.clearAnnotations();
        int rootIndex = dataset.getSeriesIndex("Корень");
        if (rootIndex >= 0) {
            dataset.removeSeries(rootIndex);
        }


        if (root != null && Double.isFinite(root)) {
            try {
                double yAtRoot = f.applyAsDouble(root);
                if (Double.isFinite(yAtRoot)) {
                    XYPointerAnnotation annotation = new XYPointerAnnotation(
                            String.format("Корень ≈ %.4f", root),
                            root, yAtRoot, -Math.PI / 2.0);
                    annotation.setTipRadius(10.0);
                    annotation.setBaseRadius(25.0);
                    annotation.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    annotation.setPaint(Color.RED);
                    annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
                    plot.addAnnotation(annotation);

                    XYSeries rootSeries = new XYSeries("Корень");
                    rootSeries.add(root.doubleValue(), yAtRoot);
                    dataset.addSeries(rootSeries);
                    rootIndex = dataset.getSeriesIndex("Корень");
                    if (rootIndex >= 0) {
                        renderer.setSeriesPaint(rootIndex, Color.RED);
                        renderer.setSeriesLinesVisible(rootIndex, false);
                        renderer.setSeriesShapesVisible(rootIndex, true);
                        renderer.setSeriesShape(rootIndex, new Ellipse2D.Double(-4, -4, 8, 8));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error calculating f(root): " + e.getMessage());
            }
        }

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setDomainZeroBaselinePaint(Color.BLACK);
        plot.setRangeZeroBaselinePaint(Color.BLACK);


        if (chartPanel != null) remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void setSystemFunctions(NewtonSystemSolver.Function2Var f1, NewtonSystemSolver.Function2Var f2,
                                   double xmin, double xmax, double ymin, double ymax,
                                   double[] solution) {
        if (xmax <= xmin || ymax <= ymin) {
            System.err.println("Invalid plot range provided for system.");
            clear();
            removeAll();
            add(new JLabel("Неверный диапазон для графика"), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        XYSeries s1 = generateContourNoNulls(f1, xmin, xmax, ymin, ymax, "f1(x,y)=0");
        XYSeries s2 = generateContourNoNulls(f2, xmin, xmax, ymin, ymax, "f2(x,y)=0");

        XYSeriesCollection dataset = new XYSeriesCollection();
        if (s1 != null && s1.getItemCount() > 0) dataset.addSeries(s1);
        if (s2 != null && s2.getItemCount() > 0) dataset.addSeries(s2);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Графики системы f1(x,y)=0, f2(x,y)=0", "x", "y", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        int seriesCount = dataset.getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            boolean isSolutionSeries = "Решение".equals(dataset.getSeriesKey(i));

            renderer.setSeriesLinesVisible(i, false);
            renderer.setSeriesShapesVisible(i, true);

            if (isSolutionSeries) {
                renderer.setSeriesShape(i, new Ellipse2D.Double(-5, -5, 10, 10));
                renderer.setSeriesPaint(i, Color.GREEN.darker());
            } else {
                renderer.setSeriesShape(i, DOT);
                if (dataset.getSeriesKey(i).toString().startsWith("f1")) renderer.setSeriesPaint(i, Color.RED);
                else if (dataset.getSeriesKey(i).toString().startsWith("f2")) renderer.setSeriesPaint(i, Color.BLUE);
            }
        }

        plot.clearAnnotations();
        int solutionIndex = dataset.getSeriesIndex("Решение");
        if (solutionIndex >= 0) {
            dataset.removeSeries(solutionIndex);
        }

        if (solution != null && solution.length == 2 && Double.isFinite(solution[0]) && Double.isFinite(solution[1])) {
            XYSeries solutionSeries = new XYSeries("Решение");
            solutionSeries.add(solution[0], solution[1]);
            dataset.addSeries(solutionSeries);
            solutionIndex = dataset.getSeriesIndex("Решение");

            if (solutionIndex >= 0) {
                renderer.setSeriesLinesVisible(solutionIndex, false);
                renderer.setSeriesShapesVisible(solutionIndex, true);
                renderer.setSeriesShape(solutionIndex, new Ellipse2D.Double(-5, -5, 10, 10));
                renderer.setSeriesPaint(solutionIndex, Color.GREEN.darker());
            }

            XYPointerAnnotation annotation = new XYPointerAnnotation(
                    String.format("Решение ≈ (%.3f, %.3f)", solution[0], solution[1]),
                    solution[0], solution[1], -Math.PI / 4.0);
            annotation.setTipRadius(10.0);
            annotation.setBaseRadius(35.0);
            annotation.setFont(new Font("SansSerif", Font.PLAIN, 12));
            annotation.setPaint(Color.BLACK);
            annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
            plot.addAnnotation(annotation);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setDomainZeroBaselinePaint(Color.BLACK);
        plot.setRangeZeroBaselinePaint(Color.BLACK);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(xmin, xmax);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(ymin, ymax);

        if (chartPanel != null) remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }


    private XYSeries generateContourNoNulls(NewtonSystemSolver.Function2Var f, double xmin, double xmax, double ymin, double ymax, String seriesName) {
        XYSeries series = new XYSeries(seriesName);
        double dx = (xmax - xmin) / RESOLUTION;
        double dy = (ymax - ymin) / RESOLUTION;
        if (dx <= 0 || dy <= 0) {
            System.err.println("Error in generateContour: dx or dy is not positive.");
            return series;
        }

        double[][] values = new double[RESOLUTION + 1][RESOLUTION + 1];
        for (int i = 0; i <= RESOLUTION; i++) {
            for (int j = 0; j <= RESOLUTION; j++) {
                try {
                    values[i][j] = f.apply(xmin + i * dx, ymin + j * dy);
                    if (!Double.isFinite(values[i][j])) values[i][j] = Double.NaN;
                } catch (Exception e) { values[i][j] = Double.NaN; }
            }
        }

        for (int i = 0; i < RESOLUTION; i++) {
            for (int j = 0; j < RESOLUTION; j++) {
                double x = xmin + i * dx;
                double y = ymin + j * dy;
                double v00 = values[i][j];
                double v10 = values[i + 1][j];
                double v01 = values[i][j + 1];
                double v11 = values[i + 1][j + 1];

                if (Double.isNaN(v00) || Double.isNaN(v10) || Double.isNaN(v01) || Double.isNaN(v11)) continue;

                int squareIndex = 0;
                if (v00 > 0) squareIndex |= 1;
                if (v10 > 0) squareIndex |= 2;
                if (v11 > 0) squareIndex |= 4;
                if (v01 > 0) squareIndex |= 8;

                Point A = interpolate(x, y, x, y + dy, v00, v01);
                Point B = interpolate(x, y, x + dx, y, v00, v10);
                Point C = interpolate(x + dx, y, x + dx, y + dy, v10, v11);
                Point D = interpolate(x, y + dy, x + dx, y + dy, v01, v11);

                switch (squareIndex) {
                    case 1: case 14: if(isValid(A)&&isValid(B)){ series.add(A.x, A.y); series.add(B.x, B.y); } break;
                    case 2: case 13: if(isValid(B)&&isValid(C)){ series.add(B.x, B.y); series.add(C.x, C.y); } break;
                    case 3: case 12: if(isValid(A)&&isValid(C)){ series.add(A.x, A.y); series.add(C.x, C.y); } break;
                    case 4: case 11: if(isValid(C)&&isValid(D)){ series.add(C.x, C.y); series.add(D.x, D.y); } break;
                    case 5:
                        double centerX = x + dx / 2.0;
                        double centerY = y + dy / 2.0;
                        double centerVal = Double.NaN; try { centerVal = f.apply(centerX, centerY); } catch (Exception ignored) {}
                        if (Double.isNaN(centerVal)) continue;
                        if ((centerVal > 0) == (v00 > 0)) {
                            if(isValid(A)&&isValid(D)){ series.add(A.x, A.y); series.add(D.x, D.y); }
                            if(isValid(B)&&isValid(C)){ series.add(B.x, B.y); series.add(C.x, C.y); }
                        } else {
                            if(isValid(A)&&isValid(B)){ series.add(A.x, A.y); series.add(B.x, B.y); }
                            if(isValid(C)&&isValid(D)){ series.add(C.x, C.y); series.add(D.x, D.y); }
                        }
                        break;
                    case 6: case 9: if(isValid(B)&&isValid(D)){ series.add(B.x, B.y); series.add(D.x, D.y); } break;
                    case 7: case 8: if(isValid(A)&&isValid(D)){ series.add(A.x, A.y); series.add(D.x, D.y); } break;
                    case 10:
                        centerX = x + dx / 2.0;
                        centerY = y + dy / 2.0;
                        centerVal = Double.NaN; try { centerVal = f.apply(centerX, centerY); } catch (Exception ignored) {}
                        if (Double.isNaN(centerVal)) continue;
                        if ((centerVal > 0) == (v00 > 0)) {
                            if(isValid(A)&&isValid(B)){ series.add(A.x, A.y); series.add(B.x, B.y); }
                            if(isValid(C)&&isValid(D)){ series.add(C.x, C.y); series.add(D.x, D.y); }
                        } else {
                            if(isValid(A)&&isValid(D)){ series.add(A.x, A.y); series.add(D.x, D.y); }
                            if(isValid(B)&&isValid(C)){ series.add(B.x, B.y); series.add(C.x, C.y); }
                        }
                        break;
                }
            }
        }
        return series;
    }

    private boolean isValid(Point p) {
        return p != null && !Double.isNaN(p.x) && !Double.isNaN(p.y);
    }
    private Point interpolate(double x1, double y1, double x2, double y2, double val1, double val2) {
        if (Double.isNaN(val1) || Double.isNaN(val2) || Math.signum(val1) == Math.signum(val2)) {
            return new Point(Double.NaN, Double.NaN);
        }
        if (Math.abs(val1) < 1e-15) return new Point(x1, y1);
        if (Math.abs(val2) < 1e-15) return new Point(x2, y2);
        double t = Math.abs(val1) / (Math.abs(val1) + Math.abs(val2));
        if (Double.isNaN(t)) {
            return new Point(Double.NaN, Double.NaN);
        }
        double ix = x1 + t * (x2 - x1);
        double iy = y1 + t * (y2 - y1);
        return new Point(ix, iy);
    }
    private record Point(double x, double y) {}
    public void clear() {
        if (chartPanel != null) {
            remove(chartPanel);
            chartPanel = null;
            revalidate();
            repaint();
        }
    }
}
