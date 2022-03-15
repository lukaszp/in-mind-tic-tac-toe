package com.example.elitemindtictactoe

class My {
    lateinit var game_manager: TicTacToe.GameManager
    constructor() {
        app = this
    }

    companion object {
        lateinit var app: My
            private set
    }

    fun init_and_connect_game_to_interface(game_manager_interface: TicTacToe.GameManager.GameManagerHumanOnDeviceInterface, player_human_on_device_interface_1: TicTacToe.PlayerHumanOnDeviceInterface, player_human_on_device_interface_2: TicTacToe.PlayerHumanOnDeviceInterface, player_ai_on_device_interface: TicTacToe.PlayerAIOnDeviceInterface) {
        game_manager = TicTacToe.GameManager(game_manager_interface, player_human_on_device_interface_1, player_human_on_device_interface_2, player_ai_on_device_interface)
    }


}