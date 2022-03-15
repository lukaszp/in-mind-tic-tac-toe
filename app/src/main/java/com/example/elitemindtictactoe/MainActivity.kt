package com.example.elitemindtictactoe

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    class RowSpinnerTriggers : Activity(), AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (position > 0) My.app.game_manager.game.current_player_set_row(position - 1)
            if (position == 0) My.app.game_manager.game.current_player_set_row(null)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            My.app.game_manager.game.current_player_set_row(null)
        }
    }

    class ColumnSpinnerTriggers : Activity(), AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (position > 0) My.app.game_manager.game.current_player_set_column(position - 1)
            if (position == 0) My.app.game_manager.game.current_player_set_column(null)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            My.app.game_manager.game.current_player_set_column(null)
        }
    }

    class ContestTypeSpinnerTriggers : Activity(), AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            My.app.game_manager.change_contest_type_to(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    val rows_strings = arrayOf<String>("None", "1", "2", "3")
    val column_strings = arrayOf<String>("None", "1", "2", "3")
    val contest_type_strings = arrayOf<String>(
            "Human/Human on 1 shared phone",
            "Human/AI"
    )

    lateinit var row_spinner: Spinner
    lateinit var column_spinner: Spinner
    lateinit var contest_type_spinner: Spinner

    fun clear_position_pick_in_interface() {
        this.row_spinner.setSelection(0)
        this.column_spinner.setSelection(0)
    }


    fun set_start_activated(is_activated: Boolean) {
        (findViewById(R.id.startGame) as Button).setEnabled(is_activated)
        contest_type_spinner.setEnabled(is_activated)
    }

    fun enable_game_interface() {
        row_spinner.onItemSelectedListener = RowSpinnerTriggers()
        column_spinner.onItemSelectedListener = ColumnSpinnerTriggers()
        (findViewById(R.id.setXO) as Button).setEnabled(true)
        row_spinner.setEnabled(true)
        column_spinner.setEnabled(true)
        if (!(findViewById(R.id.setXO) as Button).hasOnClickListeners()) (findViewById(R.id.setXO) as Button).setOnClickListener {
            My.app.game_manager.game.current_player_made_mark()
        }

    }

    fun disable_game_interface() {
        row_spinner.onItemSelectedListener = null
        column_spinner.onItemSelectedListener = null
        (findViewById(R.id.setXO) as Button).setEnabled(false)
        row_spinner.setEnabled(false)
        column_spinner.setEnabled(false)
    }

    fun show_popup_message_and(title_text: String, msg_text: String, after_read: () -> (Unit)) {
        var alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle(Html.fromHtml(title_text))
        alertDialog.setMessage(Html.fromHtml(msg_text))
        alertDialog.setPositiveButton(
                "Received"
        ) { _, _ -> after_read() }
        alertDialog.setOnCancelListener { after_read() }
        alertDialog.show()
    }


    fun append_to_game_msg_box(text: String) {
        (findViewById(R.id.gameMessages) as TextView).append(Html.fromHtml(text))
    }

    fun show_approve_popup_message(title_text: String, msg_text: String, on_confirm: () -> (Unit), on_cancel: () -> (Unit)) {
        var alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle(Html.fromHtml(title_text))
        alertDialog.setMessage(Html.fromHtml(msg_text))
        alertDialog.setPositiveButton("Confirm") { _, _ -> on_confirm() }
        alertDialog.setNegativeButton("Cancel") { _, _ -> on_cancel() }
        alertDialog.setOnCancelListener { on_cancel() }
        alertDialog.show()
    }

    override fun onBackPressed() {
        var alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle(Html.fromHtml("Finish Application?"))
        alertDialog.setMessage(Html.fromHtml("Do you want to exit game and finish application?"))
        alertDialog.setPositiveButton("Yes") { _, _ -> My.app.game_manager.i_request_finish(this::on_game_finish) }
        alertDialog.setNegativeButton("No") { _, _ -> }
        alertDialog.show()
    }

    fun on_game_finish() {
        finish()
    }

    fun temp() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (findViewById(R.id.gameMessages) as TextView).setMovementMethod(ScrollingMovementMethod())

        row_spinner = findViewById(R.id.spinnerRow)
        column_spinner = findViewById(R.id.spinnerColumn)

        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, rows_strings).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            row_spinner.adapter = adapter
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, column_strings).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            column_spinner.adapter = adapter
        }

        contest_type_spinner = findViewById(R.id.spinnerContestType)
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, contest_type_strings).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            contest_type_spinner.adapter = adapter
        }

        My()
        My.app.init_and_connect_game_to_interface(TicTacToe.GameManager.GameManagerHumanOnDeviceInterface(
                this::append_to_game_msg_box,
                this::show_popup_message_and,
                this::set_start_activated,
                (findViewById(R.id.resetGame) as Button)::setEnabled),
                TicTacToe.PlayerHumanOnDeviceInterface(
                        this::enable_game_interface,
                        this::disable_game_interface,
                        this::clear_position_pick_in_interface,
                        (findViewById(R.id.player_1_name) as TextView)::setText,
                        (findViewById(R.id.mistakesNumberP1) as TextView)::setText,
                        (findViewById(R.id.elapsedTimeSP1) as TextView)::setText),
                TicTacToe.PlayerHumanOnDeviceInterface(
                        this::enable_game_interface,
                        this::disable_game_interface,
                        this::clear_position_pick_in_interface,
                        (findViewById(R.id.player_2_name) as TextView)::setText,
                        (findViewById(R.id.mistakesNumberP2) as TextView)::setText,
                        (findViewById(R.id.elapsedTimeSP2) as TextView)::setText),
                TicTacToe.PlayerAIOnDeviceInterface(
                        this::enable_game_interface,
                        this::disable_game_interface,
                        this::clear_position_pick_in_interface,
                        (findViewById(R.id.player_2_name) as TextView)::setText,
                        (findViewById(R.id.mistakesNumberP2) as TextView)::setText,
                        (findViewById(R.id.elapsedTimeSP2) as TextView)::setText))

        contest_type_spinner.onItemSelectedListener = ContestTypeSpinnerTriggers()

        (findViewById(R.id.startGame) as Button).setOnClickListener {
            My.app.game_manager.start_game()
        }

        (findViewById(R.id.resetGame) as Button).setOnClickListener {
            show_approve_popup_message("Reset?", "Do you really want to reset game?", { My.app.game_manager.reset_game() }, {})
        }

        My.app.game_manager.initialize()

    }


}

