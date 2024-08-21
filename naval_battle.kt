import java.io.File

const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val MAIN_MENU = 100
const val MENU_DEFINE_BOARD = 101
const val MENU_DEFINE_SHIPS = 102
const val MENU_PLAY = 103
const val MENU_LOAD_GAME = 104
const val MENU_SAVE_GAME = 105
const val EXIT = 106
var numRows = -1
var numColumns = -1
var playerBoard: Array<Array<Char?>> = emptyArray()
var computerBoard: Array<Array<Char?>> = emptyArray()
var playerGuesses: Array<Array<Char?>> = emptyArray()
var computerGuesses: Array<Array<Char?>> = emptyArray()

// Check if the board size is valid
fun isValidBoardSize(rows: Int, columns: Int): Boolean {
    return when {
        rows == 4 && columns == 4 -> true
        rows == 5 && columns == 5 -> true
        rows == 7 && columns == 7 -> true
        rows == 8 && columns == 8 -> true
        rows == 10 && columns == 10 -> true
        else -> false
    }
}

// Process the coordinates and convert them into row and column indices
fun processCoordinates(coordinates: String, rows: Int, columns: Int): Pair<Int, Int>? {
    if ((coordinates.length == 3 || coordinates.length == 4) && rows in 1..26 && columns in 1..26) {
        var rowNumber = coordinates[0].toString().toIntOrNull()
        var columnLetter = coordinates[2].toString()
        var index = 0
        if (rowNumber == null) {
            return null
        }
        if (coordinates.length == 4) {
            rowNumber = (coordinates[0].toString() + coordinates[1].toString()).toIntOrNull()
            if (rowNumber == null) {
                return null
            } else {
                columnLetter = coordinates[3].toString()
            }
        }
        if (rowNumber <= rows && rowNumber != 0) {
            while (index != columns) {
                if (columnLetter == ALPHABET[index].toString()) {
                    return Pair(rowNumber, index + 1)
                } else {
                    index++
                }
            }
        }
    }
    return null
}

// Create a horizontal legend for the board
fun createHorizontalLegend(columns: Int): String {
    var count = 0
    var sequence = ""
    while (count < columns) {
        sequence += ALPHABET[count].toString()
        if (count < columns - 1) {
            sequence += " | "
        }
        count++
    }
    return sequence
}

// Generate an empty board with water ('~') cells
fun generateEmptyBoard(rows: Int, columns: Int): String {
    var rowIndex = 0
    var board = "| ${createHorizontalLegend(columns)} |\n"
    while (rowIndex < rows) {
        var colIndex = 0
        var row = ""
        while (colIndex < columns) {
            if (colIndex == 0) {
                row += "| "
            }
            row += "~ | "
            colIndex++
        }
        row += "${rowIndex + 1}"
        board += "$row\n"
        rowIndex++
    }
    return board
}

// Calculate the number of ships based on the board size
fun calculateNumberOfShips(rows: Int, columns: Int): Array<Int> {
    return when {
        rows == 4 && columns == 4 -> arrayOf(2, 0, 0, 0)
        rows == 5 && columns == 5 -> arrayOf(1, 1, 1, 0)
        rows == 7 && columns == 7 -> arrayOf(2, 1, 1, 1)
        rows == 8 && columns == 8 -> arrayOf(2, 2, 1, 1)
        rows == 10 && columns == 10 -> arrayOf(3, 2, 1, 1)
        else -> emptyArray()
    }
}

// Create an empty board with null cells
fun createEmptyBoard(rows: Int, columns: Int): Array<Array<Char?>> {
    val emptyBoard = Array(rows) { arrayOfNulls<Char>(columns) }
    for (row in 0 until rows) {
        for (col in 0 until columns) {
            emptyBoard[row][col] = null
        }
    }
    return emptyBoard
}

// Check if the coordinates are within the board boundaries
fun isCoordinateWithinBoard(board: Array<Array<Char?>>, row: Int, column: Int): Boolean {
    return row in 1..board.size && column in 1..board.size
}

// Clean up empty coordinates from the array
fun cleanEmptyCoordinates(coordinates: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
    val nonEmptyCoordinates = coordinates.filter { it.first != 0 && it.second != 0 }
    return nonEmptyCoordinates.toTypedArray()
}

// Combine two arrays of coordinates
fun combineCoordinates(array1: Array<Pair<Int, Int>>, array2: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
    return (array1 + array2).toTypedArray()
}

// Generate the coordinates of a ship based on its position and orientation
fun generateShipCoordinates(board: Array<Array<Char?>>, row: Int, column: Int, orientation: String, size: Int): Array<Pair<Int, Int>> {
    val shipCoordinates = Array(size) { Pair(0, 0) }
    val empty = emptyArray<Pair<Int, Int>>()
    var index = 0
    var currentRow = row
    var currentColumn = column
    shipCoordinates[index] = Pair(currentRow, currentColumn)
    while (index < size - 1) {
        index++
        when (orientation) {
            "E" -> {
                currentColumn++
                if (currentColumn in 1..board.size) {
                    shipCoordinates[index] = Pair(currentRow, currentColumn)
                } else {
                    return empty
                }
            }
            "O" -> {
                currentColumn--
                if (currentColumn in 1..board.size) {
                    shipCoordinates[index] = Pair(currentRow, currentColumn)
                } else {
                    return empty
                }
            }
            "N" -> {
                currentRow--
                if (currentRow in 1..board.size) {
                    shipCoordinates[index] = Pair(currentRow, currentColumn)
                } else {
                    return empty
                }
            }
            "S" -> {
                currentRow++
                if (currentRow in 1..board.size) {
                    shipCoordinates[index] = Pair(currentRow, currentColumn)
                } else {
                    return empty
                }
            }
        }
    }
    return shipCoordinates
}

// Generate the boundary coordinates around a ship
fun generateBoundaryCoordinates(board: Array<Array<Char?>>, row: Int, column: Int, orientation: String, size: Int): Array<Pair<Int, Int>> {
    var boundary = emptyArray<Pair<Int, Int>>()
    if (size == 1) {
        // Submarine
        for (r in row - 1..row + 1) {
            for (c in column - 1..column + size) {
                if (!(r == row && c == column)) {
                    if (isCoordinateWithinBoard(board, r, c)) boundary += Pair(r, c)
                }
            }
        }
    } else {
        when (orientation) {
            "E" -> {
                for (r in row - 1..row + 1) {
                    for (c in column - 1..column + size) {
                        if (!(r == row && (c in column..column + size - 1))) {
                            if (isCoordinateWithinBoard(board, r, c)) boundary += Pair(r, c)
                        }
                    }
                }
            }
            "O" -> {
                for (r in row - 1..row + 1) {
                    for (c in column - size..column + 1) {
                        if (!(r == row && (c in column - size + 1..column))) {
                            if (isCoordinateWithinBoard(board, r, c)) boundary += Pair(r, c)
                        }
                    }
                }
            }
            "S" -> {
                for (r in row - 1..row + size) {
                    for (c in column - 1..column + 1) {
                        if (!(c == column && (r in row..row + size - 1))) {
                            if (isCoordinateWithinBoard(board, r, c)) boundary += Pair(r, c)
                        }
                    }
                }
            }
            "N" -> {
                for (r in row - size..row + 1) {
                    for (c in column - 1..column + 1) {
                        if (!(c == column && (r in row - size + 1..row))) {
                            if (isCoordinateWithinBoard(board, r, c)) boundary += Pair(r, c)
                        }
                    }
                }
            }
        }
    }
    cleanEmptyCoordinates(boundary)
    return boundary
}

// Check if all the coordinates are free (no ships or boundaries)
fun isAreaFree(board: Array<Array<Char?>>, coordinates: Array<Pair<Int, Int>>): Boolean {
    for (coord in coordinates) {
        if (!isCoordinateWithinBoard(board, coord.first, coord.second)) {
            return false
        }
        if (board[coord.first - 1][coord.second - 1] != null) {
            return false
        }
    }
    return true
}

// Insert a simple ship horizontally if possible
fun insertSimpleShip(board: Array<Array<Char?>>, row: Int, column: Int, size: Int): Boolean {
    val ship = generateShipCoordinates(board, row, column, "E", size)
    val boundary = generateBoundaryCoordinates(board, row, column, "E", size)
    val coordinates = combineCoordinates(ship, boundary)
    val shipType = size.toString()[0]
    if (isAreaFree(board, coordinates) && ship.isNotEmpty()) {
        for (i in ship.indices) {
            board[ship[i].first - 1][ship[i].second - 1] = shipType
        }
        return true
    }
    return false
}

// Insert a ship in any orientation if possible
fun insertShip(board: Array<Array<Char?>>, row: Int, column: String, orientation: String, size: Int): Boolean {
    val ship = generateShipCoordinates(board, row, column.toInt(), orientation, size)
    val boundary = generateBoundaryCoordinates(board, row, column.toInt(), orientation, size)
    val coordinates = combineCoordinates(ship, boundary)
    val shipType = size.toString()[0]
    if (isAreaFree(board, coordinates) && ship.isNotEmpty()) {
        for (i in ship.indices) {
            board[ship[i].first - 1][ship[i].second - 1] = shipType
        }
        return true
    }
    return false
}

// Randomly populate the computer's board with ships
fun populateComputerBoard(board: Array<Array<Char?>>, shipSizes: Array<Int>) {
    var row: Int
    var col: Int
    val orientations = "NSOE"
    var orientationIndex: Int
    for (size in 1..4) {
        var quantity = shipSizes[size - 1]
        while (quantity > 0) {
            do {
                do {
                    row = (0 until numRows).random()
                    col = (0 until numColumns).random()
                } while (board[row][col] != null)
                orientationIndex = (0..3).random()
            } while (!insertShip(board, row + 1, col + 1, orientations[orientationIndex].toString(), size))
            quantity--
        }
    }
}

// Check if a ship is fully placed on the board
fun isShipComplete(board: Array<Array<Char?>>, row: Int, column: Int): Boolean {
    val shipType = board[row - 1][column - 1].toString().toIntOrNull()
    var count = 0
    if (shipType == null || board[row - 1][column - 1].toString() != shipType.toString()) {
        return false
    }
    if (board[row - 1][column - 1] == '1') {
        return true
    }
    for (size in 1..4) {
        if (size == shipType) {
            for (r in row - (size - 1)..row + (size - 1)) {
                if (isCoordinateWithinBoard(board, r, column) &&
                    board[r - 1][column - 1].toString() == shipType.toString()) {
                    count++
                }
                if (count == shipType) {
                    return true
                }
            }
            count = 0
            for (c in column - (size - 1)..column + (size - 1)) {
                if (isCoordinateWithinBoard(board, row, c) &&
                    board[row - 1][c - 1].toString() == shipType.toString()) {
                    count++
                }
                if (count == shipType) {
                    return true
                }
            }
        }
    }
    return false
}

// Generate the board view for either real or guesses board
fun getBoardView(board: Array<Array<Char?>>, isRealBoard: Boolean): Array<String> {
    val view = Array(board.size + 1) { "" }
    if (!isRealBoard) {
        for (row in 0..board.size) {
            if (row == 0) {
                view[row] = "| ${createHorizontalLegend(board[0].size)} |"
            } else {
                var rowText = ""
                for (col in 0 until board.size) {
                    rowText += when (board[row - 1][col]) {
                        null -> "| ? "
                        'X' -> "| X "
                        '2' -> if (isShipComplete(board, row, col + 1)) "| 2 " else "| \u2082 "
                        '3' -> if (isShipComplete(board, row, col + 1)) "| 3 " else "| \u2083 "
                        '4' -> if (isShipComplete(board, row, col + 1)) "| 4 " else "| \u2084 "
                        '1' -> "| 1 "
                        else -> "| ? "
                    }
                }
                rowText += "| $row"
                view[row] = rowText
            }
        }
    } else {
        for (row in 0..board.size) {
            if (row == 0) {
                view[0] = "| ${createHorizontalLegend(board[0].size)} |"
            } else {
                var rowText = ""
                for (col in 0 until board[row - 1].size) {
                    rowText += when (board[row - 1][col]) {
                        null -> "| ~ "
                        'X' -> "| X "
                        '1' -> "| 1 "
                        '2' -> "| 2 "
                        '3' -> "| 3 "
                        '4' -> "| 4 "
                        else -> "| ? "
                    }
                }
                rowText += "| $row"
                view[row] = rowText
            }
        }
    }
    return view
}

// Launch a shot at the opponent's board and update the guesses board
fun launchShot(realBoard: Array<Array<Char?>>, guessesBoard: Array<Array<Char?>>, shotCoordinates: Pair<Int, Int>): String {
    val responses = arrayOf("Hit a submarine.", "Hit a destroyer.", "Hit a tank.", "Hit an aircraft carrier.", "Missed.")
    var responseIndex = 0
    when (realBoard[shotCoordinates.first - 1][shotCoordinates.second - 1]) {
        '1' -> {
            guessesBoard[shotCoordinates.first - 1][shotCoordinates.second - 1] = '1'
            responseIndex = 0
        }
        '2' -> {
            guessesBoard[shotCoordinates.first - 1][shotCoordinates.second - 1] = '2'
            responseIndex = 1
        }
        '3' -> {
            guessesBoard[shotCoordinates.first - 1][shotCoordinates.second - 1] = '3'
            responseIndex = 2
        }
        '4' -> {
            guessesBoard[shotCoordinates.first - 1][shotCoordinates.second - 1] = '4'
            responseIndex = 3
        }
        null -> {
            guessesBoard[shotCoordinates.first - 1][shotCoordinates.second - 1] = 'X'
            responseIndex = 4
        }
    }
    return responses[responseIndex]
}

// Generate a random shot for the computer
fun generateComputerShot(guessesBoard: Array<Array<Char?>>): Pair<Int, Int> {
    var shot = Pair(0, 0)
    do {
        val row = (1..guessesBoard.size).random()
        val col = (1..guessesBoard.size).random()
        shot = Pair(row, col)
    } while (guessesBoard[row - 1][col - 1] != null)
    return shot
}

// Count the number of complete ships of a given size on the board
fun countCompleteShips(board: Array<Array<Char?>>, size: Int): Int {
    val shipType = size.toString()
    var count = 0
    val totalShips = calculateNumberOfShips(board.size, board.size)
    for (row in 0 until board.size) {
        for (col in 0 until board.size) {
            if (board[row][col].toString() == shipType && isShipComplete(board, row + 1, col + 1)) {
                count++
                when (size) {
                    2 -> if (count == 2 * totalShips[0]) count /= 2
                    3 -> if (count == 3 * totalShips[1]) count /= 3
                    4 -> if (count == 4 * totalShips[2]) count /= 4
                }
            }
        }
    }
    return count
}

// Check if the player has won the game
fun hasWon(guessesBoard: Array<Array<Char?>>): Boolean {
    val ships = calculateNumberOfShips(guessesBoard.size, guessesBoard.size)
    val totalShips = ships.sum()
    var count = 0
    for (size in 1..4) {
        if (countCompleteShips(guessesBoard, size) == ships[size - 1]) {
            count++
        }
    }
    return count == 4
}

// Load a game from a file
fun loadGame(filename: String, boardType: Int): Array<Array<Char?>> {
    val lines = File(filename).readLines().toTypedArray()
    val size = if (lines[0][0] == '1') (lines[0][0].toString() + lines[0][1]).toInt() else lines[0][0].toString().toInt()
    val board = Array(size) { Array(size) { null as Char? } }
    when (boardType) {
        1 -> {
            for (row in 4 until 3 + size + 1) {
                val parts = lines[row].split(",")
                for (col in parts.indices) {
                    if (parts[col].isNotEmpty()) {
                        board[row - 4][col] = parts[col][0]
                    }
                }
            }
        }
        2 -> {
            for (row in 7 + size until 6 + (size * 2) + 1) {
                val parts = lines[row].split(",")
                for (col in parts.indices) {
                    if (parts[col].isNotEmpty()) {
                        board[row - (7 + size)][col] = parts[col][0]
                    }
                }
            }
        }
        3 -> {
            for (row in 10 + (size * 2) until 9 + (size * 3) + 1) {
                val parts = lines[row].split(",")
                for (col in parts.indices) {
                    if (parts[col].isNotEmpty()) {
                        board[row - (10 + (size * 2))][col] = parts[col][0]
                    }
                }
            }
        }
        4 -> {
            for (row in 13 + (size * 3) until 12 + (size * 4) + 1) {
                val parts = lines[row].split(",")
                for (col in parts.indices) {
                    if (parts[col].isNotEmpty()) {
                        board[row - (13 + (size * 3))][col] = parts[col][0]
                    }
                }
            }
        }
    }
    return board
}

// Save the current game state to a file
fun saveGame(filename: String, playerBoard: Array<Array<Char?>>, playerGuesses: Array<Array<Char?>>, computerBoard: Array<Array<Char?>>, computerGuesses: Array<Array<Char?>>) {
    val writer = File(filename).printWriter()
    writer.println("${playerBoard.size},${playerBoard.size}\n")
    writer.println("Player\nBoard")
    for (row in playerBoard.indices) {
        for (col in playerBoard[row].indices) {
            val char = playerBoard[row][col]?.toString() ?: ""
            writer.print(char)
            if (col < playerBoard[row].size - 1) writer.print(",")
        }
        writer.println()
    }
    writer.println()
    writer.println("Player\nGuesses")
    for (row in playerGuesses.indices) {
        for (col in playerGuesses[row].indices) {
            val char = playerGuesses[row][col]?.toString() ?: ""
            writer.print(char)
            if (col < playerGuesses[row].size - 1) writer.print(",")
        }
        writer.println()
    }
    writer.println()
    writer.println("Computer\nBoard")
    for (row in computerBoard.indices) {
        for (col in computerBoard[row].indices) {
            val char = computerBoard[row][col]?.toString() ?: ""
            writer.print(char)
            if (col < computerBoard[row].size - 1) writer.print(",")
        }
        writer.println()
    }
    writer.println()
    writer.println("Computer\nGuesses")
    for (row in computerGuesses.indices) {
        for (col in computerGuesses[row].indices) {
            val char = computerGuesses[row][col]?.toString() ?: ""
            writer.print(char)
            if (col < computerGuesses[row].size - 1) writer.print(",")
        }
        writer.println()
    }
    writer.close()
}

// Main game loop and menu handling
fun mainMenu(): Int {
    println("\n> > Battleship < <\n")
    println("1 - Define Board and Ships")
    println("2 - Play")
    println("3 - Save Game")
    println("4 - Load Game")
    println("0 - Exit\n")
    var selectedMenu = readln().toIntOrNull()
    if (selectedMenu !in 0..4) {
        do {
            println("!!! Invalid option, please try again")
            selectedMenu = readln().toIntOrNull()
        } while (selectedMenu !in 0..4)
    }
    return when (selectedMenu) {
        1 -> MENU_DEFINE_BOARD
        2 -> MENU_PLAY
        3 -> MENU_SAVE_GAME
        4 -> MENU_LOAD_GAME
        0 -> EXIT
        else -> EXIT
    }
}

// Handle the board definition menu
fun defineBoardMenu(): Int {
    println("\n> > Battleship < <\n")
    println("Define the board size:")
    var numRowsInput: Int? = 0
    var numColumnsInput: Int? = 0
    do {
        println("How many rows?")
        numRowsInput = readln().toIntOrNull()
        if (numRowsInput == -1) return MAIN_MENU
        if (numRowsInput == 0) return EXIT
        if (numRowsInput == null) {
            println("!!! Invalid number of rows, please try again")
        }
    } while (numRowsInput == null)
    do {
        println("How many columns?")
        numColumnsInput = readln().toIntOrNull()
        if (numColumnsInput == -1) return MAIN_MENU
        if (numColumnsInput == 0) return EXIT
        if (numColumnsInput == null) {
            println("!!! Invalid number of columns, please try again")
        }
    } while (numColumnsInput == null)
    if (isValidBoardSize(numRowsInput, numColumnsInput)) {
        numRows = numRowsInput
        numColumns = numColumnsInput
        playerBoard = Array(numRows) { arrayOfNulls(numColumns) }
        playerGuesses = Array(numRows) { arrayOfNulls(numColumns) }
        computerBoard = Array(numRows) { arrayOfNulls(numColumns) }
        computerGuesses = Array(numRows) { arrayOfNulls(numColumns) }
        return MENU_DEFINE_SHIPS
    }
    return MENU_DEFINE_BOARD
}

// Handle the ship definition menu
fun defineShipsMenu(): Int {
    val boardView = getBoardView(playerBoard, true)
    for (row in boardView.indices) {
        println(boardView[row])
    }
    val messages = arrayOf(
        "Enter the coordinates for a submarine:", "Enter the coordinates for a destroyer:",
        "Enter the coordinates for a tank:", "Enter the coordinates for an aircraft carrier:")
    val ships = calculateNumberOfShips(numRows, numColumns)
    for (shipIndex in ships.indices) {
        while (ships[shipIndex] != 0) {
            println("${messages[shipIndex]}\nCoordinates? (e.g., 6,G)")
            val enteredCoordinates = readln()
            when (enteredCoordinates) {
                "-1" -> return MAIN_MENU
                "0" -> return EXIT
                else -> {
                    when (val realCoordinates = processCoordinates(enteredCoordinates, numRows, numColumns)) {
                        null -> println("!!! Invalid coordinates, please try again")
                        else -> {
                            when (shipIndex) {
                                0 -> {
                                    if (insertSimpleShip(playerBoard, realCoordinates.first, realCoordinates.second, 1)) {
                                        val updatedBoard = getBoardView(playerBoard, true)
                                        for (row in updatedBoard.indices) {
                                            println(updatedBoard[row])
                                        }
                                        ships[shipIndex] -= 1
                                    }
                                }
                                else -> {
                                    var orientation = ""
                                    do {
                                        println("Enter the ship's orientation:\nOrientation? (N, S, E, W)")
                                        orientation = readln()
                                        when (orientation) {
                                            "-1" -> return MAIN_MENU
                                            "0" -> return EXIT
                                            else -> if (orientation !in listOf("N", "S", "E", "O")) {
                                                println("!!! Invalid orientation, please try again")
                                            }
                                        }
                                    } while (orientation !in listOf("N", "S", "E", "O"))
                                    if (insertShip(playerBoard, realCoordinates.first, realCoordinates.second.toString(), orientation, shipIndex + 1)) {
                                        val updatedBoard = getBoardView(playerBoard, true)
                                        for (row in updatedBoard.indices) {
                                            println(updatedBoard[row])
                                        }
                                        ships[shipIndex] -= 1
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    populateComputerBoard(createEmptyBoard(numRows, numColumns), Array(1) { 1 })
    println("Would you like to see the computer's board? (Y/N)")
    val response = readln()
    return when (response) {
        "-1" -> MAIN_MENU
        "0" -> EXIT
        "Y" -> {
            val computerBoardView = getBoardView(computerBoard, true)
            for (row in computerBoardView.indices) {
                println(computerBoardView[row])
            }
            MAIN_MENU
        }
        else -> MAIN_MENU
    }
}

// Handle the play menu where the game is played
fun playMenu(): Int {
    if (playerBoard.isEmpty()) {
        println("!!! You must define the board first, please try again")
        return MAIN_MENU
    } else {
        while (!hasWon(playerGuesses) && !hasWon(computerGuesses)) {
            val playerBoardView = getBoardView(playerGuesses, false)
            for (row in playerBoardView.indices) {
                println(playerBoardView[row])
            }
            println("Indicate the position you want to hit")
            println("Coordinates? (e.g., 6,G)")
            val enteredShot = readln()
            when (enteredShot) {
                "-1" -> return MAIN_MENU
                "0" -> return EXIT
                else -> {
                    val realShot = processCoordinates(enteredShot, numRows, numColumns)
                    when (realShot) {
                        null -> println("!!! Invalid shot, please try again")
                        else -> {
                            val hitMessages = arrayOf("Miss.", "Ship sunk!")
                            launchShot(computerBoard, playerGuesses, realShot)
                            if (isShipComplete(computerBoard, realShot.first, realShot.second)) {
                                println(">>> PLAYER >>> ${hitMessages[1]}")
                            } else {
                                println(">>> PLAYER >>> ${launchShot(computerBoard, playerGuesses, realShot)}")
                            }
                            if (!hasWon(playerGuesses)) {
                                val computerShot = generateComputerShot(computerGuesses)
                                launchShot(playerBoard, computerGuesses, computerShot)
                                if (isShipComplete(playerBoard, computerShot.first, computerShot.second)) {
                                    println("Computer fired at (${computerShot.first},${computerShot.second})\n>>> COMPUTER >>> ${hitMessages[1]}")
                                } else {
                                    println("Computer fired at (${computerShot.first},${computerShot.second})\n>>> COMPUTER >>> ${launchShot(playerBoard, computerGuesses, computerShot)}")
                                }
                            }
                        }
                    }
                }
            }
            if (!hasWon(playerGuesses) && !hasWon(computerGuesses)) {
                println("Press enter to continue")
                readln()
            }
        }
        if (hasWon(playerGuesses)) {
            println("CONGRATULATIONS! You won the game!")
        } else {
            println("Oops! The computer won the game!")
        }
    }
    println("Press enter to return to the main menu")
    readln()
    return MAIN_MENU
}

// Handle the load game menu
fun loadGameMenu(): Int {
    println("Enter the filename (e.g., game.txt)")
    val filename = readln()
    return when (filename) {
        "-1" -> MAIN_MENU
        "0" -> EXIT
        else -> {
            playerBoard = loadGame(filename, 1)
            playerGuesses = loadGame(filename, 2)
            computerBoard = loadGame(filename, 3)
            computerGuesses = loadGame(filename, 4)
            println("Successfully loaded a ${playerBoard.size}x${playerBoard.size} board")
            val boardView = getBoardView(playerBoard, true)
            for (row in boardView.indices) {
                println(boardView[row])
            }
            numRows = playerBoard.size
            numColumns = playerBoard.size
            MAIN_MENU
        }
    }
}

// Handle the save game menu
fun saveGameMenu(): Int {
    if (playerBoard.isEmpty()) {
        println("!!! You must define the board first, please try again")
        return MAIN_MENU
    }
    println("Enter the filename (e.g., game.txt)")
    val filename = readln()
    return when (filename) {
        "-1" -> MAIN_MENU
        "0" -> EXIT
        else -> {
            saveGame(filename, playerBoard, playerGuesses, computerBoard, computerGuesses)
            println("Successfully saved the ${numRows}x${numColumns} board")
            MAIN_MENU
        }
    }
}

fun main() {
    var currentMenu = MAIN_MENU
    while (true) {
        currentMenu = when (currentMenu) {
            MAIN_MENU -> mainMenu()
            MENU_DEFINE_BOARD -> defineBoardMenu()
            MENU_DEFINE_SHIPS -> defineShipsMenu()
            MENU_PLAY -> playMenu()
            MENU_LOAD_GAME -> loadGameMenu()
            MENU_SAVE_GAME -> saveGameMenu()
            EXIT -> return
            else -> return
        }
    }
}
