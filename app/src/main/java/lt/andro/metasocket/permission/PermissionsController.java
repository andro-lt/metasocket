package lt.andro.metasocket.permission;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-09
 */
public interface PermissionsController {
    boolean hasPermissionGranted(String permission);

    void requestPermission(String permission);
}
