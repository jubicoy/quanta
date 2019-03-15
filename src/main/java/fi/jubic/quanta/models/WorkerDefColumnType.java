package fi.jubic.quanta.models;

public enum WorkerDefColumnType {
    input,
    output;

    public static WorkerDefColumnType parse(String workerDefColumnType) {
        switch (workerDefColumnType.toLowerCase()) {
            case "input": return input;
            case "output": return output;

            default: return null;
        }
    }
}