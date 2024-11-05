package org.example;

import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Параметри випадкової величини
        double mean = 5;  // математичне сподівання
        double stdDev = 4.5;  // стандартне відхилення
        int n = 350;  // кількість значень

        // 1. Генерація випадкових значень
        double[] randomValues = generateRandomValues(mean, stdDev, n);



        // 2. Визначення числових характеристик
        DescriptiveStatistics stats = new DescriptiveStatistics(randomValues);
        double calculatedMean = stats.getMean();
        double calculatedVariance = stats.getVariance();
        double calculatedStdDev = stats.getStandardDeviation(); // Точкова оцінка стандартного відхилення

        System.out.println("Математичне сподівання (теоретичне): " + mean);
        System.out.println("Дисперсія (теоретична): " + (stdDev * stdDev));
        System.out.println("Точкова оцінка математичного сподівання: " + calculatedMean);
        System.out.println("Точкова оцінка дисперсії: " + calculatedVariance);
        System.out.println("Точкова оцінка стандартного відхилення: " + calculatedStdDev);

        // 3 розігрування випадкової величини
        System.out.println("Згенеровані випадкові значення:");
        for (double value : randomValues) {
            System.out.println(value);
        }

        // 4. Побудова інтервальної таблиці частот і виведення її в консоль
        createFrequencyTable(randomValues, n);

        // 5. Побудова гістограми відносних частот
        createHistogram(randomValues, n);

        // 7. Апроксимація гістограми теоретичним нормальним розподілом
        createNormalDistributionPlot(mean, stdDev, randomValues);
    }

    // Генерація випадкових значень нормального розподілу
    private static double[] generateRandomValues(double mean, double stdDev, int n) {
        Random random = new Random();
        return random.doubles(n, mean - 3 * stdDev, mean + 3 * stdDev)
                .map(x -> mean + stdDev * random.nextGaussian())
                .toArray();
    }

    // Створення гістограми відносних частот
    private static void createHistogram(double[] values, int n) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Частоти", values, 10);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Гістограма відносних частот",
                "Значення",
                "Частота",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame("Гістограма");
        ChartPanel chartPanel = new ChartPanel(histogram);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // Виведення інтервальної таблиці частот у консоль з використанням формули Стургерса
    private static void createFrequencyTable(double[] values, int n) {
        double min = Arrays.stream(values).min().orElse(0);
        double max = Arrays.stream(values).max().orElse(1);

        // Формула Стургерса для кількості інтервалів
        int binCount = (int) Math.ceil(1 + 1.44 * Math.log(n)) + 1;
        System.out.println("\nКількість інтервалів (за формулою Стургерса): " + binCount);
        double binWidth = (max - min) / binCount;
        int[] frequencies = new int[binCount];

        // Підрахунок кількості значень у кожному інтервалі
        for (double value : values) {
            int binIndex = (int) ((value - min) / binWidth);
            if (binIndex >= binCount) binIndex = binCount - 1;
            frequencies[binIndex]++;
        }

        // Виведення таблиці частот
        System.out.println("\nІнтервальна таблиця частот:");
        for (int i = 0; i < binCount; i++) {
            double binStart = min + i * binWidth;
            double binEnd = binStart + binWidth;
            System.out.printf("Інтервал [%f - %f]: %d значень\n", binStart, binEnd, frequencies[i]);
        }
    }

    // Апроксимація гістограми нормальним законом розподілу
    private static void createNormalDistributionPlot(double mean, double stdDev, double[] values) {
        // Створення гістограми
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Частоти", values, 10);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Гістограма з нормальним розподілом",
                "Значення",
                "Частота",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Додавання прозорості до гістограми
        XYPlot plot = histogram.getXYPlot();
        XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
        barRenderer.setSeriesPaint(0, new Color(255, 0, 0, 100));  // Прозора гістограма

        // Створення нормального розподілу
        NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);
        XYSeries series = new XYSeries("Нормальний розподіл");

        double minValue = Arrays.stream(values).min().orElse(0);
        double maxValue = Arrays.stream(values).max().orElse(1);

        // Розрахунок ширини інтервалів (бінів)
        int binCount = 10;  // Кількість інтервалів для гістограми
        double binWidth = (maxValue - minValue) / binCount;

        double step = (maxValue - minValue) / 1000;  // Більше точок для плавності кривої

        // Масштабування нормальної кривої на основі висоти гістограми
        for (double x = minValue; x <= maxValue; x += step) {
            // Масштабуємо криву нормального розподілу на основі кількості спостережень і ширини інтервалу
            series.add(x, normalDistribution.density(x) * values.length * binWidth);
        }

        XYSeriesCollection datasetLine = new XYSeriesCollection(series);

        // Додавання лінії нормального розподілу
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setDataset(1, datasetLine);  // Лінія додана як другий графік
        plot.setRenderer(1, renderer);

        // Налаштування осей
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);

        JFrame frame = new JFrame("Гістограма з накладеним нормальним розподілом");
        ChartPanel chartPanel = new ChartPanel(histogram);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

}
