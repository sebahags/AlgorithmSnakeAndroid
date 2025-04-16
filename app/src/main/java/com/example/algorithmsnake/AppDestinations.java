package com.example.algorithmsnake;

public class AppDestinations {
    public static final String MAIN_MENU_ROUTE = "main_menu";
    public static final String GAME_SCREEN_ROUTE_PATTERN = "game_screen/{isPlayerMode}/{gameSpeed}";
    public static final String IS_PLAYER_MODE_ARG = "isPlayerMode";
    public static final String GAME_SPEED_ARG = "gameSpeed";

    public static String buildGameRoute(boolean isPlayerMode, int gameSpeed) {
        return "game_screen/" + isPlayerMode + "/" + gameSpeed;
    }

    private AppDestinations() {}
}
