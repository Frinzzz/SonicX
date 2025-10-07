package game.camera;

public class Camera2D {
    public double x, y;
    public double viewportW, viewportH;
    public double zoom = 1.0;
    public double smooth = 12.0;

    private double deadW = 0.30;
    private double deadH = 0.22;

    public double worldMinX, worldMinY, worldMaxX = 1e9, worldMaxY = 1e9;

    public Camera2D(double viewportW, double viewportH) {
        this.viewportW = viewportW;
        this.viewportH = viewportH;
    }

    public void setDeadZone(double wPercent, double hPercent) {
        this.deadW = wPercent;
        this.deadH = hPercent;
    }

    public void setZoom(double z) { this.zoom = Math.max(0.5, Math.min(2.0, z)); }

    public void update(double dt, double targetX, double targetY) {
        double dzW = viewportW * deadW / zoom;
        double dzH = viewportH * deadH / zoom;

        double dzLeft   = x - dzW * 0.5;
        double dzRight  = x + dzW * 0.5;
        double dzTop    = y - dzH * 0.5;
        double dzBottom = y + dzH * 0.5;

        double desiredX = x;
        double desiredY = y;

        if (targetX < dzLeft)  desiredX = targetX + dzW * 0.5;
        if (targetX > dzRight) desiredX = targetX - dzW * 0.5;
        if (targetY < dzTop)   desiredY = targetY + dzH * 0.5;
        if (targetY > dzBottom)desiredY = targetY - dzH * 0.5;

        double t = 1.0 - Math.exp(-smooth * dt);
        x = x + (desiredX - x) * t;
        y = y + (desiredY - y) * t;

        double halfW = (viewportW * 0.5) / zoom;
        double halfH = (viewportH * 0.5) / zoom;
        x = clamp(x, worldMinX + halfW, worldMaxX - halfW);
        y = clamp(y, worldMinY + halfH, worldMaxY - halfH);
    }

    public double getRenderTranslateX() {
        return (viewportW * 0.5) - x * zoom;
    }
    public double getRenderTranslateY() {
        return (viewportH * 0.5) - y * zoom;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}