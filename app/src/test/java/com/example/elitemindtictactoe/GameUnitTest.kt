package com.example.elitemindtictactoe

import android.app.AlertDialog
import android.text.Html
import android.widget.Button
import android.widget.TextView
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GameUnitTest {

    fun clear_position_pick_in_interface() {
    }

    fun set_start_activated(is_activated: Boolean) {
    }

    fun enable_game_interface() {
    }

    fun disable_game_interface() {
    }

    fun show_popup_message(title_text: String, msg_text: String, on_confirm: () -> (Unit)) {
    }


    fun append_to_game_msg_box(text: String) {
    }

    fun my_setEnabled( set_enabled : Boolean) {}

    fun my_setText( set_enabled : CharSequence) {}


    @Test
    fun gameBoardEmptyPositiosAreCorrect_isCorrect() {
        My()
        My.app.init_and_connect_game_to_interface(TicTacToe.GameManager.GameManagerHumanOnDeviceInterface(
            this::append_to_game_msg_box,
            this::show_popup_message,
            this::set_start_activated,
            this::my_setEnabled),
            TicTacToe.PlayerHumanOnDeviceInterface(
                this::enable_game_interface,
                this::disable_game_interface,
                this::clear_position_pick_in_interface,
                this::my_setText,
                this::my_setText,
                this::my_setText),
            TicTacToe.PlayerHumanOnDeviceInterface(
                this::enable_game_interface,
                this::disable_game_interface,
                this::clear_position_pick_in_interface,
                this::my_setText,
                this::my_setText,
                this::my_setText),
            TicTacToe.PlayerAIOnDeviceInterface(
                this::enable_game_interface,
                this::disable_game_interface,
                this::clear_position_pick_in_interface,
                this::my_setText,
                this::my_setText,
                this::my_setText))

        My.app.game_manager.initialize()

        My.app.game_manager.change_contest_type_to(0)

        My.app.game_manager.start_game()

        assertEquals(9, My.app.game_manager.game.board.empty_positions().size)
    }
}