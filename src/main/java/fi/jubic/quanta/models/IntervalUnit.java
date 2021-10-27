package fi.jubic.quanta.models;

public enum IntervalUnit {
    s,
    m,
    h,
    d,
    w,
    M,
    y;

    public static IntervalUnit parse(String intervalUnit) {
        switch (intervalUnit.toLowerCase()) {
            case "m": return m;
            case "h": return h;
            case "d": return d;
            case "w": return w;
            case "M": return M;
            case "y": return y;
            default: return s;
        }
    }
}
