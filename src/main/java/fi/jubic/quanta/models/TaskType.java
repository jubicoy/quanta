package fi.jubic.quanta.models;

public enum TaskType {
    process,
    sync,
    IMPORT,
    IMPORT_SAMPLE;

    public static TaskType parse(String taskType) {
        switch (taskType.toLowerCase()) {
            case "process": return process;
            case "sync": return sync;
            case "import": return IMPORT;
            case "import_sample": return IMPORT_SAMPLE;
            default: return null;
        }
    }

}
