To run the code:
----------------
- Run the compiled code with ‘gradle build' and 'gradle run'

Implemented Features:
----------------------
- Difficulty Level (Singleton Pattern)
- Time and Score (Observer Pattern)
- Undo and Cheat (Memento Pattern)

Design Patterns and Classnames:
-------------------------------
- Singleton: DifficultyLevel.java (Singleton)
- Observer: GameEventPublisher.java (Subject), ScoreEvent.java (ConcreteEvent), GameObserver.java (Observer)
- Memento: GameMemento.java (Memento), GameEngine.java (Caretaker)

Game Operations:
----------------
- At game start, select difficulty by clicking the corresponding button for easy, medium, or hard.
- The time and score are displayed at the top left corner during the game.
- Press 'S' to save the state for the undo feature.
- Press 'L' to load the saved state and revert to that point.
(Please pay attention!!! Currently the system can only perform one Undo operation with the L key after pressing the S key. This means that the Undo function can only be performed once at the same time! So please press the S and L keys in turn each time you use the Undo function, they work together.)

- Cheat keys: 'Q', 'W', 'E', 'R' correspond to removing fast projectiles, slow projectiles, enemies with fast projectiles, and enemies with slow projectiles, respectively.

Additional Information:
-----------------------
- The undo functionality restores the state of the game objects and score as it was at the last saved state.
- Currently the system can only perform one Undo operation with the L key after pressing the S key. This means that the Undo function can only be performed once at the same time! So please press the S and L keys in turn each time you use the Undo function, they work together!