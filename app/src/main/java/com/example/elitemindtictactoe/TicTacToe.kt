package com.example.elitemindtictactoe

import java.util.*

import kotlin.concurrent.schedule


class TicTacToe() {

    class TimeCounter(var update_time_externally: () -> Unit) {
        var total_consumed_nano_s: Long = 0
        var resumed_at_nano_s: Long = System.nanoTime()
        var is_started: Boolean = false

        fun resume() {
            this.resumed_at_nano_s = System.nanoTime()
            is_started = true
            time_external_updater()
        }

        fun time_external_updater() {
            Timer("SettingUp", false).schedule(1000) {
                if (is_started) {
                    update_time_externally()
                    time_external_updater()
                }
            }
        }

        fun stop() {
            this.total_consumed_nano_s = this.total_consumed_nano_s + (System.nanoTime() - this.resumed_at_nano_s)
            is_started = false
        }

        fun long_nano_s_to_float_s(nano_s: Long): Float {
            return (nano_s / 1_000_000).toFloat() / 1000.toFloat()
        }

        fun consumed_time_s(): Float {
            return if (is_started) long_nano_s_to_float_s(this.total_consumed_nano_s + (System.nanoTime() - this.resumed_at_nano_s)) else long_nano_s_to_float_s(this.total_consumed_nano_s)
        }
    }

    open abstract class Player(var player_interface: PlayerOnDeviceInterface, var number: Int) {

        class Errors {
            var errors_count: Int = 0
            fun add() {
                this.errors_count++
            }

            fun count(): Int {
                return this.errors_count
            }
        }

        var consumed_timer = TimeCounter(this::update_player_time_in_interface)
        var errors = Errors()

        open var title: String = "Player"
        open var abbreviation: String = ""

        fun initialize() {
            player_interface.set_player_name(long_signature())
            player_interface.disable_game_interface()
        }

        fun short_signature():String { return "%s%d".format(abbreviation, number) }

        fun long_signature():String { return "%s %d".format(title, number) }

        fun update_player_time_in_interface() {
            player_interface.set_elapsed_time("%.1f".format(this.consumed_timer.consumed_time_s()))
        }

        operator fun compareTo(other_player: Player): Int {
            if (this == other_player) return 0
            if (this.errors.count() < other_player.errors.count()) return 1
            if (this.errors.count() > other_player.errors.count()) return -1
            if (this.consumed_timer.consumed_time_s() < other_player.consumed_timer.consumed_time_s()) return 1
            return -1
        }

        override operator fun equals(other: Any?): Boolean {
            var other_player: Player = other as Player
            return (this.errors.count() == other_player.errors.count()) && (this.consumed_timer.consumed_time_s() == other_player.consumed_timer.consumed_time_s())
        }

        abstract fun make_move(game: Game)


    }

    class OnDeviceHumanPlayer(
            player_interface: PlayerHumanOnDeviceInterface, number: Int): Player(player_interface, number) {

        override var title: String = "Human"
        override var abbreviation: String = "H"

        override fun make_move(game: Game) {

        }
    }

    class OnDeviceIAPlayer(
            player_interface: PlayerAIOnDeviceInterface, number: Int): Player(player_interface, number) {

        override var title: String = "AI"
        override var abbreviation: String = "A"

        override fun make_move(game: Game) {
            var choosed_position: Array<Int> = game.board.empty_positions()[
                    (0..(game.board.empty_positions().size - 1)).random()
            ]
            game.current_player_set_row(choosed_position[0])
            game.current_player_set_column(choosed_position[1])
            game.current_player_made_mark()
        }
    }

    interface PlayerOnDeviceInterface {
        var enable_game_interface: () -> Unit
        var disable_game_interface: () -> Unit
        var clear_position_pick_in_interface: () -> Unit
        var set_player_name: (String) -> Unit
        var set_mistakes_number: (CharSequence) -> Unit
        var set_elapsed_time: (CharSequence) -> Unit
    }
    class PlayerHumanOnDeviceInterface(override var enable_game_interface: () -> Unit,
                                       override var disable_game_interface: () -> Unit,
                                       override var clear_position_pick_in_interface: () -> Unit,
                                       override var set_player_name: (String) -> Unit,
                                       override var set_mistakes_number: (CharSequence) -> Unit,
                                       override var set_elapsed_time: (CharSequence) -> Unit): PlayerOnDeviceInterface {}

    class PlayerAIOnDeviceInterface(override var enable_game_interface: () -> Unit,
                                    override var disable_game_interface: () -> Unit,
                                    override var clear_position_pick_in_interface: () -> Unit,
                                    override var set_player_name: (String) -> Unit,
                                    override var set_mistakes_number: (CharSequence) -> Unit,
                                    override var set_elapsed_time: (CharSequence) -> Unit): PlayerOnDeviceInterface {}

    class GameManager(var game_manager_human_on_device_interface: GameManagerHumanOnDeviceInterface, var player_human_on_device_interface_1: PlayerHumanOnDeviceInterface, var player_human_on_device_interface_2: PlayerHumanOnDeviceInterface, var player_ai_on_device_interface: PlayerAIOnDeviceInterface) {
        class GameManagerHumanOnDeviceInterface(var append_to_game_msg_box: (String) -> Unit,
                                                var show_popup_message: (String, String, () -> Unit) -> Unit,
                                                var set_start_activated: (Boolean) -> Unit,
                                                var set_restart_activated: (Boolean) -> Unit) {
        }

        class ContestType {
            companion object {
                val HUMAN_HUMAN_ON_1_PHONE = 0
                val HUMAN_AI = 1
            }
        }

        class Texts {

            open class Translation() {}

            class EN_Translation : Translation() {
                val start_info_msg = "<b>Play 2D 3x3 Tic Tac Toe</b> in Yours own brain memory with friend <b>!!!</b> and have a smartphone as a referee. You chan choose to play with other Human or with AI. You are Human 1 (H1) and Your friend is Human 2 (H2) or AI (A1). Choose Row and Column and place mark by pressing \"Set mark in Row and Column on board in mind\". Mistakes numbers and elapsed times for you and your friend are displayed below. In case no body gets 3 in row, the winner is the person with the lowest number of mistakes, and in case of another tight a shorter elapsed time is a winner. In this text box game messages are displayed."
                val press_start_msg = "Please press <b>Start Game</b> button to start the game. During the game <b>Reset Game</b> button can be used to end game and start it again by <b>Start Game</b> button."
                val game_was_reset_msg = "! ! ! <b>Game was terminated in the middle.</b> ! ! !"

            }
        }

        var texts = Texts.EN_Translation()
        var contest_type: Int = ContestType.HUMAN_HUMAN_ON_1_PHONE
        lateinit var game: Game


        fun append_all_line_to_game_msg_box(text: String) {
            game_manager_human_on_device_interface.append_to_game_msg_box("<tt><b>To All:</b></tt> " + text + "<br>")
        }

        fun reset_game_model() {
            var players: Array<Player> = arrayOf(OnDeviceHumanPlayer(player_human_on_device_interface_1, 1))
            players = when (contest_type) {
                ContestType.HUMAN_HUMAN_ON_1_PHONE -> players + arrayOf(OnDeviceHumanPlayer(player_human_on_device_interface_2, 2))
                ContestType.HUMAN_AI -> players + arrayOf(OnDeviceIAPlayer(player_ai_on_device_interface, 1))
                else -> players + arrayOf(OnDeviceHumanPlayer(player_human_on_device_interface_2, 2))
            }

            game = Game(players, 3, this)
        }

        public fun game_has_been_settled() {
            this.append_all_line_to_game_msg_box(texts.press_start_msg)

            game_manager_human_on_device_interface.set_restart_activated(false)
            game_manager_human_on_device_interface.set_start_activated(true)
        }

        public fun initialize() {
            this.append_all_line_to_game_msg_box(texts.start_info_msg)
            this.append_all_line_to_game_msg_box(texts.press_start_msg)


            player_human_on_device_interface_1.disable_game_interface()
            player_human_on_device_interface_2.disable_game_interface()
            player_ai_on_device_interface.disable_game_interface()

            player_human_on_device_interface_1.clear_position_pick_in_interface()
            player_human_on_device_interface_2.clear_position_pick_in_interface()
            player_ai_on_device_interface.clear_position_pick_in_interface()

            game_manager_human_on_device_interface.set_restart_activated(false)
            game_manager_human_on_device_interface.set_start_activated(true)
        }

        public fun start_game() {
            reset_game_model()
            game_manager_human_on_device_interface.set_restart_activated(true)
            game_manager_human_on_device_interface.set_start_activated(false)
            game.start()
        }

        public fun reset_game() {
            game.terminate_without_settelment()
            this.append_all_line_to_game_msg_box(texts.game_was_reset_msg)
            this.append_all_line_to_game_msg_box(texts.press_start_msg)

            player_human_on_device_interface_1.clear_position_pick_in_interface()
            player_human_on_device_interface_2.clear_position_pick_in_interface()
            player_ai_on_device_interface.clear_position_pick_in_interface()

            game_manager_human_on_device_interface.set_restart_activated(false)
            game_manager_human_on_device_interface.set_start_activated(true)
        }

        public fun change_contest_type_to(contest_type: Int) {
            this.contest_type = contest_type
        }

        public fun i_request_finish(on_finished: () -> (Unit)) {
            if (this::game.isInitialized) game.i_request_finish(on_finished) else {
                on_finished()
            }
        }
    }

    class Game(var players: Array<Player>, var board_size: Int, var game_manager: GameManager) {

        class Turns(val players: Array<Player>) {

            var current: Int = 0

            var current_position = Board.Position()

            fun move_to_next() {
                current++
                current_position = Board.Position()
            }

            fun current_turn_player_order_index(): Int {
                return if (current.rem(this.players.count()) > 0) current.rem(this.players.count()) - 1 else (this.players.count() - 1)
            }

            fun current_turn_player(): Player {
                return players[current_turn_player_order_index()]
            }

            fun winner_by_score(): Player? {
                if (players[0] == players[1]) return null
                return if (players[0] > players[1]) players[0] else players[1]
            }

            fun stop() {
                players.forEach { player ->
                    player.consumed_timer.stop()
                    player.player_interface.set_mistakes_number(0.toString())
                    player.player_interface.set_elapsed_time(0.toString())
                }
            }
        }

        class Board(var size: Int) {
            class Position() {
                var row: Int? = null
                var column: Int? = null
                fun is_invalid(): Boolean {
                    return (this.row == null || this.column == null)
                }
            }

            val empty: Player? = null
            var grid = arrayOf(arrayOf(empty, empty, empty), arrayOf(empty, empty, empty), arrayOf(empty, empty, empty))

            fun is_move_possible(position: Position): Boolean {
                return (this.grid[position.row!!][position.column!!] === empty)
            }

            fun record_move(player: Player, position: Position) {
                this.grid[position.row!!][position.column!!] = player
            }

            fun is_won_by_pattern_for(player: Player): Boolean {
                grid.forEach row_point@{ row ->
                    row.forEach { column_player ->
                        if (column_player !== player) {
                            return@row_point
                        }
                    }
                    return true
                }

                grid[0].forEachIndexed column_point@{ column_index, column_player ->
                    grid.forEachIndexed { row_index, row ->
                        if (grid[row_index][column_index] !== player) {
                            return@column_point
                        }
                    }
                    return true
                }

                grid[0].forEachIndexed { column_index_1, column_player_1 ->
                    if (grid[column_index_1][column_index_1] !== player) {
                        grid[0].forEachIndexed { column_index_2, column_player_2 ->
                            if (grid[this.size - (column_index_2 + 1)][column_index_2] !== player) {
                                return false
                            }
                        }
                        return true
                    }
                }
                return true

            }

            fun has_no_empty_positions(): Boolean {
                grid.forEach { row ->
                    row.forEach { column_player ->
                        if (column_player === empty) {
                            return false
                        }
                    }
                }
                return true
            }

            fun empty_positions(): Array<Array<Int>> {
                var accumulated_empty_positions: Array<Array<Int>> = arrayOf()
                grid.forEachIndexed { row_index, row ->
                    row.forEachIndexed { column_index, column_player ->
                        if (column_player === empty) {
                            accumulated_empty_positions += arrayOf(row_index, column_index)
                        }
                    }
                }
                return accumulated_empty_positions
            }

            fun grid_to_html(): String {
                var html_grid = "<tt>"
                this.grid.forEach { row ->
                    html_grid += "<br><br>"
                    row.forEach { column_player ->
                        if (column_player === null) html_grid += "&nbsp;&nbsp;--" else html_grid += "&nbsp;&nbsp;" + column_player?.short_signature()
                    }
                }
                html_grid += "<br></tt>"
                return html_grid
            }
        }

        class Texts {

            open class Translation() {}

            class EN_Translation : Translation() {
                val move_action_manual = "choose Row and column to mark your sign in and press \"Set mark in Row and Column on board in mind\". "
                val player_turn_msg = "<b>!!!</b> Turn change occurred. This is <b>%s %d</b> turn now. <b>!!!</b>"
                val missing_data_msg = "did you forget to set something? "
                val occupated_position_msg = "this position is already taken. Mistake point has been added to your score. Please try again. "
                val move_accepted_msg = "your move has been accepted. "
                val won_with_marks_msg = "you won the game with marks in row. "
                val won_by_non_marks_msg = "<b>%s %d</b> won the game by score <b>(Errors: %d, Time: %f)</b> and no marks in row in full board. "
                val ended_in_tight_msg = "! ! ! <b>Game ended in tight</b>, nobody won. ! ! !"
                val final_board_state = "Final board state is below: "

            }
        }

        var texts = Texts.EN_Translation()

        var turns = Turns(players)
        var board = Board(this.board_size)



        fun append_all_line_to_game_msg_box(text: String) {
            game_manager.game_manager_human_on_device_interface.append_to_game_msg_box("<tt><b>To All:</b></tt> " + text + "<br>")
        }

        fun append_player_line_to_game_msg_box(text: String) {
            game_manager.game_manager_human_on_device_interface.append_to_game_msg_box("<tt><b>To %s:</b></tt> ".format(turns.current_turn_player().long_signature()) + text + "<br>")
        }

        fun start() {
            players.forEach { player -> player.initialize() }
            move_to_next_turn()
        }

        fun move_to_next_turn() {
            if (board.has_no_empty_positions()) {
                if (turns.winner_by_score() === null) {
                    this.game_has_ended_in_tight()
                } else {
                    this.game_was_won_by_non_marks()
                }
                return
            }
            turns.move_to_next()
            turns.current_turn_player().player_interface.clear_position_pick_in_interface()
            this.append_all_line_to_game_msg_box(texts.player_turn_msg.format(turns.current_turn_player().title, turns.current_turn_player().number))
            this.append_player_line_to_game_msg_box(texts.move_action_manual)
            turns.current_turn_player().consumed_timer.resume()
            turns.current_turn_player().player_interface.enable_game_interface()
            turns.current_turn_player().make_move(this)
        }

        fun finalize_turn() {
            if (turns.current_position.is_invalid()) {
                this.append_player_line_to_game_msg_box(texts.missing_data_msg + texts.move_action_manual)
                return
            }
            if (board.is_move_possible(turns.current_position)) {
                turns.current_turn_player().consumed_timer.stop()
                board.record_move(turns.current_turn_player(), turns.current_position)
                this.append_player_line_to_game_msg_box(texts.move_accepted_msg)
                if (board.is_won_by_pattern_for(turns.current_turn_player())) {
                    this.game_was_won_by_pattern()
                    return
                }
                turns.current_turn_player().player_interface.disable_game_interface()
                game_manager.game_manager_human_on_device_interface.show_popup_message("Message from <b>%s</b>:".format(turns.current_turn_player().long_signature()),
                        "I marked: <b>Row %d, Column %d</b>".format(turns.current_position.row?.plus(1), turns.current_position.column?.plus(1)), this::move_to_next_turn)
                return
            } else {
                this.turns.current_turn_player().errors.add()
                turns.current_turn_player().consumed_timer.stop()
                this.turns.current_turn_player().player_interface.set_mistakes_number.invoke(this.turns.current_turn_player().errors.count().toString())
                this.append_player_line_to_game_msg_box(texts.occupated_position_msg)
                turns.current_turn_player().player_interface.disable_game_interface()
                game_manager.game_manager_human_on_device_interface.show_popup_message("Message from <b>%s</b>:".format(turns.current_turn_player().long_signature()),
                    "I made mark on occupied position: <b>Row %d, Column %d</b>. I skip this turn. It is your turn now.".format(turns.current_position.row?.plus(1), turns.current_position.column?.plus(1)), this::move_to_next_turn)
                return
            }
        }

        fun game_was_won_by_non_marks() {
            this.append_all_line_to_game_msg_box(texts.won_by_non_marks_msg.format(turns.winner_by_score()?.title, turns.winner_by_score()?.number, turns.winner_by_score()?.errors?.count(), turns.winner_by_score()?.consumed_timer?.consumed_time_s()))
            game_manager.game_manager_human_on_device_interface.show_popup_message("Message from <b>%s</b>:".format(turns.current_turn_player().long_signature()),
                    "I won by score <b>(Errors: %d, Time: %f)</b> and no marks in row in full board.".format(turns.winner_by_score()?.errors?.count(), turns.winner_by_score()?.consumed_timer?.consumed_time_s())) {}
            game_has_been_settled()
        }

        fun game_has_ended_in_tight() {
            this.append_all_line_to_game_msg_box(texts.ended_in_tight_msg)
            game_manager.game_manager_human_on_device_interface.show_popup_message("Message to ALL:",
                    "! ! ! <b>Game ended in tight</b>, nobody won. ! ! !") {}
            game_has_been_settled()
        }

        fun game_was_won_by_pattern() {
            this.append_player_line_to_game_msg_box(texts.won_with_marks_msg)
            game_manager.game_manager_human_on_device_interface.show_popup_message("Message from <b>%s</b>:".format(turns.current_turn_player().long_signature()),
                    "I won the game with marks in row. ") {}
            game_has_been_settled()
        }

        fun game_has_been_settled() {
            turns.current_turn_player().player_interface.disable_game_interface()
            this.append_all_line_to_game_msg_box(texts.final_board_state + this.board.grid_to_html())
            game_manager.game_has_been_settled()
        }

        fun terminate_without_settelment() {
            turns.current_turn_player().player_interface.disable_game_interface()
            this.append_all_line_to_game_msg_box(texts.final_board_state + this.board.grid_to_html())
            turns.stop()
        }

        public fun current_player_made_mark() {
             this.finalize_turn()
        }

        public fun current_player_set_row(row: Int?) {
            turns.current_position.row = row
        }

        public fun current_player_set_column(column: Int?) {
            turns.current_position.column = column
        }

        public fun i_request_finish(onFinished: () -> Unit) {
            terminate_without_settelment()
            onFinished()
        }
    }
}