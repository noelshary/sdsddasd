package com.nova.setting;

public class NumSetting extends Setting {
    public final double min, max, step;
    private double value;

    public NumSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        set(value);
    }

    public double get() { return value; }
    public int getInt() { return (int) Math.round(value); }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        if (step > 0) v = Math.round(v / step) * step;
        value = Math.round(v * 1000.0) / 1000.0;
    }

    public void setRatio(double r) { set(min + Math.max(0, Math.min(1, r)) * (max - min)); }
    public double ratio() { return (value - min) / (max - min); }
}
