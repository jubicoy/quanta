package fi.jubic.quanta.models;

public enum TaskType {
    process,
    sync;

    public static TaskType parse(String taskType) {
        switch (taskType.toLowerCase()) {
            case "process": return process;
            case "sync": return sync;
            default: return null;
        }
    }

}
