package com.example.algorithmsnake;

public class AppDestinations {
    public static final String MAIN_MENU_ROUTE = "main_menu";
    public static final String GAME_SCREEN_ROUTE_PATTERN = "game_screen/{isPlayerMode}/{gameSpeed}"; // Pattern for route definition

    // Argument names
    public static final String IS_PLAYER_MODE_ARG = "isPlayerMode";
    public static final String GAME_SPEED_ARG = "gameSpeed";

    // Helper method to build the actual route string for navigation
    public static String buildGameRoute(boolean isPlayerMode, int gameSpeed) {
        return "game_screen/" + isPlayerMode + "/" + gameSpeed;
    }

    // Private constructor to prevent instantiation
    private AppDestinations() {}
}
