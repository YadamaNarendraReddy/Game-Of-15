# Game of 15

Game of 15 is a classic puzzle game that is isomorphic to Tic Tac Toe. In this project, you can play the Game of 15 against an AI through a web interface. The game allows you to choose the order (who goes first) and the digit set (odd or even). The objective is to place unique digits (1–9) on a 3×3 board so that a row, column, or diagonal sums to 15.

## Features

- **Order Selection**: Choose who goes first—Human or AI.
- **Parity Selection**: Select whether you play with Odd digits (1, 3, 5, 7, 9) or Even digits (2, 4, 6, 8). The AI automatically plays the opposite set.
- **Interactive Game Board**: A responsive 3×3 grid where you click a cell to place your digit.
- **Status Display**: Real-time updates on the current game state (e.g., “Your move”, “AI is thinking”, “You win!”, etc.).
- **Customizable AI Logic**: The current implementation uses random moves for the AI; you can replace it with a minimax algorithm for unbeatable play.

## How to Play

1. **Open the Game:**  
   Open `index.html` in your web browser.

2. **Select Order and Parity:**  
   - Choose who goes first by selecting either "I want to go first" or "AI goes first".  
   - Choose your digit set (Odd or Even). The AI will automatically take the other set.

3. **Start Game:**  
   Click the **Start Game** button.  
   - If you go first, click on an empty cell and follow the prompt to enter your digit.  
   - If the AI goes first, it will make its move automatically.

4. **Objective:**  
   Fill the board by taking turns placing digits. The first to complete a row, column, or diagonal that sums to 15 wins. If the board fills up without any winning line, the game is a tie.

## Installation

1. **Clone the Repository:**

   ```bash
   (http://127.0.0.1:5500/src/)
