package com.example.snakeladder

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var boardImage: ImageView
    private lateinit var player2PositionText: TextView
    private lateinit var rollDiceButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitAnswerButton: Button
    private lateinit var player1Container: FrameLayout
    private lateinit var player2Container: FrameLayout
    private lateinit var player1Piece: ImageView
    private lateinit var player2Piece: ImageView
    private lateinit var gameLogTextView: TextView


    private var currentPlayer = 1
    private var player1Pos = 1
    private var player2Pos = 1
    private val ladders = mapOf(9 to 27, 18 to 37, 25 to 54, 28 to 51, 56 to 64, 68 to 88, 76 to 97, 79 to 100)
    private val snakes = mapOf(16 to 7, 59 to 17, 63 to 19, 87 to 24, 67 to 30, 93 to 69, 95 to 75, 99 to 77)
    private var currentQuestion: Pair<Int, Int>? = null
    private var questionActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        boardImage = findViewById(R.id.boardImage) // ImageView for the boardplayer1PositionText
        rollDiceButton = findViewById(R.id.rollDiceButton)
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitAnswerButton = findViewById(R.id.submitAnswerButton)
        player1Container = findViewById(R.id.player1Container)
        player2Container = findViewById(R.id.player2Container)
        player1Piece = findViewById(R.id.player1Piece)
        player2Piece = findViewById(R.id.player2Piece)
        gameLogTextView = findViewById(R.id.gameLogTextView)

        rollDiceButton.setOnClickListener { rollDice() }
        submitAnswerButton.setOnClickListener { checkAnswer() }


        val gameLogTextView: TextView = findViewById(R.id.gameLogTextView)
        gameLogTextView.movementMethod = android.text.method.ScrollingMovementMethod()


        // Update initial positions
        updatePlayerPosition(1, player1Pos)
        updatePlayerPosition(2, player2Pos)
    }

    private fun logGame(message: String) {
        gameLogTextView.append("$message\n")
        gameLogTextView.scrollTo(0, gameLogTextView.height) // Scroll to the bottom
    }


    private fun rollDice() {
        if (questionActive) return

        val diceRoll = Random().nextInt(6) + 1
        logGame("You rolled a $diceRoll")
        movePlayer(diceRoll)
    }

    private fun generateQuestion(): Pair<Int, Int> {
        val num1 = Random().nextInt(100) + 1
        val num2 = Random().nextInt(100) + 1
        return Pair(num1, num2)
    }

    private fun askQuestion() {
        currentQuestion = generateQuestion()
        questionTextView.text = "What is ${currentQuestion!!.first} + ${currentQuestion!!.second}?"
        questionTextView.visibility = View.VISIBLE
        answerEditText.visibility = View.VISIBLE
        submitAnswerButton.visibility = View.VISIBLE

        rollDiceButton.isEnabled = false
        questionActive = true
    }

    private fun checkAnswer() {
        val userAnswer = answerEditText.text.toString().toIntOrNull()
        val correctAnswer = currentQuestion!!.first + currentQuestion!!.second

        if (userAnswer != null && userAnswer == correctAnswer) {
            logGame("Correct! Climbing the ladder.")
            val currentPlayerPosition = if (currentPlayer == 1) player1Pos else player2Pos
            val newPosition = ladders[currentPlayerPosition] ?: currentPlayerPosition

            if (currentPlayer == 1) {
                player1Pos = newPosition
            } else {
                player2Pos = newPosition
            }

            updatePlayerPosition(currentPlayer, newPosition)
        } else {
            logGame("Incorrect. Staying in place.")
        }

        // Hide question and reset UI
        questionTextView.visibility = View.GONE
        answerEditText.visibility = View.GONE
        submitAnswerButton.visibility = View.GONE
        rollDiceButton.isEnabled = true
        questionActive = false

        // Check for a win after moving (if any)
        checkWin()

        // Switch to the other player for their turn
        switchPlayer()
    }


    private fun movePlayer(diceRoll: Int) {
        var newPosition = if (currentPlayer == 1) player1Pos + diceRoll else player2Pos + diceRoll
        if (newPosition > 100) newPosition = 200 - newPosition
        if (newPosition < 1) newPosition = 1

        if (currentPlayer == 1) {
            player1Pos = newPosition
        } else {
            player2Pos = newPosition
        }
        updatePlayerPosition(currentPlayer, newPosition)

        if (ladders.containsKey(newPosition)) {
            askQuestion()
        } else if (snakes.containsKey(newPosition)) {
            if (currentPlayer == 1) {
                player1Pos = snakes[newPosition]!!
                updatePlayerPosition(currentPlayer, player1Pos)
                logGame("player 1 bites")
            } else {
                player2Pos = snakes[newPosition]!!
                updatePlayerPosition(currentPlayer, player2Pos)
                logGame("player 2 bites")
            }
            switchPlayer()
        } else {
            switchPlayer()
        }

        checkWin()
    }

    private fun switchPlayer() {
        currentPlayer = 3 - currentPlayer
    }

    private fun checkWin() {
        if (player1Pos == 100) {
            logGame("Player 1 Wins!")
            resetGame()
        } else if (player2Pos == 100) {
            logGame("Player 2 Wins!")
            resetGame()
        }
    }

    private fun resetGame() {
        player1Pos = 1
        player2Pos = 1
        updatePlayerPosition(1, 1)
        updatePlayerPosition(2, 1)
        currentPlayer = 1
    }

    private fun updatePlayerPosition(player: Int, position: Int) {
        logGame("player $player goes to $position")
        val row = 9 - (position - 1) / 10
        val col = if ((row + 1) % 2 == 0) (position - 1) % 10 else 9 - (position - 1) % 10
        val container = if (player == 1) player1Container else player2Container
        val piece = if (player == 1) player1Piece else player2Piece

        boardImage.post {
            // Calculate the position on the board image
            val cellWidth = boardImage.width / 10
            val cellHeight = boardImage.height / 10

            val leftMargin = col * cellWidth + (cellWidth - piece.width) / 2
            val topMargin = row * cellHeight + (cellHeight - piece.height) / 2

            val params = container.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = leftMargin
            params.topMargin = topMargin
            container.layoutParams = params
        }
    }
}
