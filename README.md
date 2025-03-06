# Ocean Cleanup Game

![Game Engine](https://img.shields.io/badge/Game_Engine-LibGDX-orange)
![Architecture](https://img.shields.io/badge/Architecture-Component_Based-blue)
![Design Patterns](https://img.shields.io/badge/Design_Patterns-Strategy_Builder_Factory-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Overview

A 2D game developed with LibGDX where players control a boat to clean up trash in the ocean while avoiding obstacles and monsters. The project demonstrates advanced software engineering principles with a clean separation between engine architecture and game-specific implementations.

## Key Technologies and Architecture

- **LibGDX Framework**: Cross-platform Java game development framework
- **Box2D Physics**: For collision detection and realistic movement physics
- **Component-Based Architecture**: Clear separation between engine and game implementation
- **Design Patterns**: Strategy, Builder, Factory Method, Decorator, Singleton, and more

## Project Structure

```plaintext
project/
├── game/
│   ├── Main.java                 # Application entry point
│   ├── common/                   # Common utilities and exceptions
│   │   ├── exception/            # Custom exceptions
│   │   ├── logging/              # Logging utilities
│   │   └── util/                 # General utilities like path handling
│   ├── engine/                   # Core engine (abstract, reusable)
│   │   ├── api/                  # Interfaces defining core contracts
│   │   │   ├── collision/        # Collision detection interfaces
│   │   │   ├── constant/         # Configuration interfaces
│   │   │   ├── logging/          # Logging interfaces
│   │   │   ├── movement/         # Movement strategy interfaces
│   │   │   ├── render/           # Rendering interfaces
│   │   │   └── scene/            # Scene management interfaces
│   │   ├── asset/                # Asset management
│   │   ├── audio/                # Audio system
│   │   ├── constant/             # Abstract configuration system
│   │   ├── entitysystem/         # Entity component system
│   │   │   ├── collision/        # Collision handling
│   │   │   ├── entity/           # Base entity classes
│   │   │   └── movement/         # Movement management
│   │   ├── io/                   # Input/output handling
│   │   ├── logging/              # Logging implementation
│   │   └── scene/                # Scene management
│   └── context/                  # Game implementation (concrete)
│       ├── api/                  # Game-specific interfaces
│       ├── builder/              # Movement builder implementations
│       ├── constant/             # Game constants
│       ├── decorator/            # Strategy decorators
│       ├── entity/               # Game entities (Boat, Monster, etc.)
│       ├── factory/              # Factory implementations
│       ├── movement/             # Concrete movement strategies
│       ├── scene/                # Game scenes (Menu, Game, etc.)
│       └── ui/                   # User interface components
```

### Engine Layer (Abstract, Reusable)

The engine provides a framework of abstract components that can be reused across different games:

- **API Interfaces**: Define contracts for core game components
- **Scene Management**: Abstract scene handling, transitions, rendering pipeline
- **Entity System**: Generic entity framework with collision, movement, and rendering
- **Audio System**: Music and sound effect management
- **Input/Output**: Input handling and IO operations
- **Logging**: Extensible logging system
- **Configuration**: Profile-based configurable constants system

### Context Layer (Game Implementation)

Game-specific implementations built on top of the engine:

- **Entities**: Boat, Monster, Rock, Trash with specific behaviors
- **Movement Strategies**: Different AI patterns (interceptor, obstacle avoidance, etc.)
- **Scene Implementation**: Menu, Game, Options, GameOver screens
- **Game Constants**: Game-specific configurations and parameters
- **Factories**: Creates game objects with appropriate configurations

## Features

- **Advanced Movement AI**: Various enemy movement patterns using the Strategy Pattern
- **Configurable Controls**: Rebindable key controls
- **Audio Management**: Background music and sound effects with volume controls
- **Physics-Based Collisions**: Using Box2D for realistic interactions
- **Scene Management**: Smooth transitions between game states
- **Configuration Management**: JSON-based configuration with profiles

## OOP Principles & Design Patterns

The codebase demonstrates exemplary use of OOP principles and design patterns across various systems:

### SOLID Principles Implementation

- **Single Responsibility Principle**:
  - Each class has a well-defined purpose (e.g., `MovementManager` manages movement, `SceneManager` manages scenes)
  - Abstract base classes like `AbstractMovementStrategy` handle common functionality while concrete implementations focus on specific behaviors

- **Open/Closed Principle**:
  - The engine is designed to be extended without modification
  - Movement system can be extended with new strategies without modifying existing code
  - New scenes can be added without changing the scene management system

- **Liskov Substitution Principle**:
  - Extensive use of interfaces ensures subtypes can substitute base types
  - For example, any `IMovementStrategy` implementation can be used with any `MovementManager`

- **Interface Segregation Principle**:
  - Specialized interfaces like `IMovable`, `IPositionable`, and `IMovementStrategy`
  - Each interface focuses on specific behaviors rather than being overly broad

- **Dependency Inversion Principle**:
  - Heavy use of dependency injection through constructors
  - Components depend on abstractions (interfaces) rather than concrete implementations
  - Factory patterns further decouple component creation from usage

### Design Pattern Highlights

#### Movement System Patterns

- **Strategy Pattern**:
  - Multiple movement strategies implement the `IMovementStrategy` interface
  - Strategies like `ObstacleAvoidanceStrategy`, `FollowMovementStrategy`, and `ZigZagMovementStrategy`
  - Movement strategies can be swapped at runtime

- **Builder Pattern**:
  - `AbstractMovementBuilder` provides a base for fluent builders
  - `PlayerMovementBuilder` and `NPCMovementBuilder` extend it with specific configurations
  - Method chaining with type-safe generics (e.g., `withObstacleAvoidance().withConstantMovement()`)

- **Composite Pattern**:
  - `CompositeMovementStrategy` combines multiple strategies with weighted influences
  - Allows complex behaviors through composition of simple strategies

- **Decorator Pattern**:
  - `MovementStrategyDecorator` allows for dynamic enhancement of strategies
  - Strategies can be combined and enhanced without changing their base implementation

#### Entity System Patterns

- **Object Pool Pattern**:
  - `ObjectPool` interface with implementation for game object recycling
  - `RockFactory` and `TrashFactory` work with object pools for efficient memory usage
  - Reduces garbage collection overhead during gameplay

#### Collision System Patterns

- **Visitor Pattern**:
  - `ICollidableVisitor` interface for type-safe collision handling
  - Double dispatch with `visit` and `accept` methods to handle collisions between different entity types
  - Eliminates the need for type checking and casting

- **Command Pattern**:
  - `ICollisionOperation` encapsulates collision responses
  - Collision callbacks execute appropriate commands based on collision type
  - Decouples collision detection from collision response logic

#### Audio System Patterns

- **Singleton Pattern**:
  - `AudioManager`, `MusicManager`, and `SoundManager` use singleton pattern
  - Provides global access to audio functionality
  - Ensures only one instance manages audio resources

- **Facade Pattern**:
  - `AudioManager` provides a simplified interface to the complex audio subsystems
  - Clients interact with a single class rather than multiple audio components
  - Encapsulates the complexities of audio resource management

- **Observer Pattern**:
  - Audio volume changes notify registered listeners
  - UI components observe and respond to audio state changes
  - Decouples audio system from UI components

#### Factory Patterns

- **Factory Method Pattern**:
  - `MovementStrategyFactory` creates various movement strategies
  - `SceneFactory` handles scene creation and registration
  - `RockFactory` and `TrashFactory` create game objects

- **Abstract Factory Pattern**:
  - `IMovementStrategyFactory` defines an interface for creating movement strategies
  - Different factory implementations can produce different families of related objects
  - Allows for swapping out entire families of strategies

#### Additional Patterns

- **Template Method Pattern**:
  - `AbstractLogger` defines the logging algorithm structure
  - Subclasses implement the specific logging behavior

- **State Pattern**:
  - Scene transitions represent different game states
  - Each scene encapsulates state-specific behavior
  - `SceneManager` handles state transitions

- **Service Locator Pattern**:
  - Used for accessing common services like logging and configuration
  - Components can locate required services without direct dependencies
  - Enhances testability by allowing service mocking

## Movement Strategies

The game includes multiple AI movement strategies including:

- **Obstacle Avoidance**: NPCs navigate around obstacles with dynamic path adjustment
- **Interceptor Movement**: Entities predict and intercept moving targets
- **Spring-Follow**: Physics-based following with spring-like behavior
- **Orbital Movement**: Entities orbit around a target
- **Zigzag Movement**: Creates unpredictable zigzag patterns
- **Spiral Approach**: Entities approach a target in a spiral pattern
- **Randomized Movement**: Randomly selects from a pool of strategies
- **Composite Movement**: Combines multiple strategies with weighted influence

All strategies are composable through the Strategy and Decorator patterns, enabling complex behaviors from simpler components.

## Installation

### Prerequisites

- JDK 8 or higher
- Gradle 7.x or higher

To set up the project:

```bash
git clone [repository-url]
cd OOPProject
```

## Usage

Run the game using Gradle:

```bash
./gradlew lwjgl3:run
```

Build a distributable JAR:

```bash
./gradlew lwjgl3:jar
```

## Gradle Tasks

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Project Architecture Highlights

The architecture follows a layered approach:

1. **Engine Layer**: Provides abstractions and reusable components
2. **Context Layer**: Implements game-specific logic on top of the engine
3. **API Layer**: Interfaces that define contracts between components
4. **Implementation Layer**: Concrete implementations of those interfaces

This separation allows:

- Clean dependency management (lower layers don't depend on higher layers)
- Easier testing through interface-based mock objects
- Flexibility to change implementations without affecting other parts of the system
- Potential reuse of the engine for different game projects