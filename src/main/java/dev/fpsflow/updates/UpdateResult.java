package dev.fpsflow.updates;

public record UpdateResult(
        boolean available,
        String currentVersion,
        String latestVersion,
        String downloadUrl
) {
    public static UpdateResult upToDate(String currentVersion) {
        return new UpdateResult(false, currentVersion, currentVersion, null);
    }

    public static UpdateResult newVersion(String current, String latest, String url) {
        return new UpdateResult(true, current, latest, url);
    }

    public static UpdateResult failed() {
        return new UpdateResult(false, "unknown", "unknown", null);
    }
}
