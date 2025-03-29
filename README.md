# Ocean Sweepers

![Game Engine](https://img.shields.io/badge/Game_Engine-LibGDX-orange)
![Architecture](https://img.shields.io/badge/Architecture-Component_Based-blue)
![Design Patterns](https://img.shields.io/badge/Design_Patterns-Strategy_Factory_Composite-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Overview

A 2D game developed with LibGDX where players navigate a boat through ocean waters to collect floating trash before the sea turtle does, while avoiding hazardous rocks. Players must strategically maneuver to collect debris while managing collisions with environmental obstacles. The sea turtle actively competes with the player for trash, using sophisticated AI movement strategies to seek out and collect debris, making it a race against time and clever navigation to clean the ocean first.

## Key Technologies and Architecture

- **LibGDX Framework**: Cross-platform Java game development framework
- **Box2D Physics**: For collision detection and realistic movement physics
- **Component-Based Architecture**: Clear separation between engine and game implementation
- **Design Patterns**: Strategy, Builder, Factory Method, Composite, Visitor, and more

## Project Structure

```plaintext
project/
├── game/
│   ├── Main.java                     # Application entry point
│   ├── application/                  # Game-specific implementations
│   │   ├── entity/                   # Game entities (Boat, Rock, Trash, etc.)
│   │   │   ├── api/                  # Entity-related interfaces
│   │   │   ├── factory/              # Entity factory implementations
│   │   │   ├── flyweight/            # Texture flyweight implementations
│   │   │   ├── item/                 # Collectible item implementations
│   │   │   ├── npc/                  # Non-player character implementations
│   │   │   ├── obstacle/             # Obstacle implementations
│   │   │   └── player/               # Player entity implementations
│   │   ├── movement/                 # Movement system implementations
│   │   │   ├── api/                  # Movement interfaces
│   │   │   ├── builder/              # Movement builder implementations
│   │   │   ├── composite/            # Composite movement strategies
│   │   │   ├── decorator/            # Strategy decorators
│   │   │   ├── factory/              # Movement factory implementations
│   │   │   └── strategy/             # Concrete movement strategies
│   │   └── scene/                    # Game scenes (Menu, Game, etc.)
│   │       ├── factory/              # Scene factory implementations
│   │       ├── main/                 # Main gameplay scenes
│   │       ├── overlay/              # Overlay and transition scenes
│   │       └── ui/                   # User interface components
│   ├── common/                       # Common utilities and exceptions
│   │   ├── config/                   # Configuration management
│   │   ├── exception/                # Custom exceptions
│   │   ├── logging/                  # Logging utilities
│   │   └── util/                     # General utilities
│   └── engine/                       # Core engine (abstract, reusable)
│       ├── asset/                    # Asset management
│       ├── audio/                    # Audio system
│       ├── constant/                 # Configuration system
│       ├── entitysystem/             # Entity component system
│       │   ├── entity/               # Base entity framework
│       │   ├── movement/             # Movement management
│       │   └── physics/              # Physics and collision system
│       ├── io/                       # Input/output handling
│       ├── logging/                  # Logging implementation
│       └── scene/                    # Scene management
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

### Application Layer (Game Implementation)

Game-specific implementations built on top of the engine:

- **Entities**: Boat, Sea Turtle, Rock, Trash with specific behaviors
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

### Design Pattern Implementations

#### Creational Patterns

- **Singleton (1)**:
  - `AudioManager`, `MusicManager`, and `SoundManager` use singleton pattern
  - `MovementStrategyFactory` implements singleton for centralized strategy creation
  - Provides global access while ensuring single instance management

- **Factory Method (2)**:
  - `MovementStrategyFactory` creates various movement strategies
  - `SceneFactory` handles scene creation and registration
  - `RockFactory` and `TrashFactory` create game objects

- **Abstract Factory (3)**:
  - `IMovementStrategyFactory` defines an interface for creating movement strategies
  - Different factory implementations can produce different families of related objects
  - Allows for swapping out entire families of strategies

- **Builder (4)**:
  - `AbstractMovementBuilder` provides a base for fluent builders
  - `PlayerMovementBuilder` and `NPCMovementBuilder` extend it with specific configurations
  - Method chaining with type-safe generics (e.g., `withObstacleAvoidance().withConstantMovement()`)

- **Prototype (5)**:
  - Entities can be cloned to create new instances with similar properties
  - `ObjectPool` implementation leverages prototype-like behavior
  - Efficient creation of multiple similar objects

#### Structural Patterns

- **Adapter (6)**:
  - Adapts external libraries (like Box2D) to the game's interfaces
  - Shields game code from third-party API changes
  - `CollidableEntityHandler` adapts collision interfaces

- **Composite (7)**:
  - `CompositeMovementStrategy` combines multiple strategies with weighted influences
  - Allows complex behaviors through composition of simple strategies
  - `InterceptorAvoidanceStrategy` and `OceanCurrentStrategy` demonstrate practical usage

- **Proxy (8)**:
  - Lazy loading of audio resources through proxy objects
  - Audio files are only loaded when needed
  - Reduces initial loading time and memory usage

- **Flyweight (9)**:
  - `TextureFlyweightFactory` shares texture resources
  - Shared assets minimize memory usage
  - Ensures assets are loaded only once with efficient reference management

- **Facade (10)**:
  - `AudioManager` provides a simplified interface to the complex audio subsystems
  - Clients interact with a single class rather than multiple audio components
  - Encapsulates the complexities of audio resource management

- **Bridge (11)**:
  - Separation between entity abstraction and implementation
  - Movement behaviors can be changed independently of entity types
  - Decouples abstraction hierarchies from implementation hierarchies

- **Decorator (12)**:
  - `MovementStrategyDecorator` allows for dynamic enhancement of strategies
  - Strategies can be combined and enhanced without changing their base implementation
  - Additional behaviors can be added at runtime

#### Behavioral Patterns

- **Template Method (13)**:
  - `AbstractLogger` defines the logging algorithm structure
  - `AbstractConfigurationLoader` provides template methods for configuration operations
  - `AbstractMovementStrategy` provides template for common movement behavior
  - Subclasses implement the specific behaviors

- **Mediator (14)**:
  - `CollisionManager` acts as a mediator between collidable objects
  - Centralizes collision logic instead of distributing it across entities
  - Reduces dependencies between individual entities

- **Chain of Responsibility (15)**:
  - Collision handlers form a chain for processing different collision types
  - Each handler decides whether to process the collision or pass it to the next handler
  - Allows for flexible handling of various collision scenarios

- **Observer (16)**:
  - Audio volume changes notify registered listeners
  - UI components observe and respond to audio state changes
  - Entity removal listeners observe when entities are destroyed
  - Decouples event sources from event handlers

- **Strategy (17)**:
  - Multiple movement strategies implement the `IMovementStrategy` interface
  - Strategies like `ObstacleAvoidanceStrategy`, `FollowMovementStrategy`, and `ZigZagMovementStrategy`
  - Movement strategies can be swapped at runtime

- **Command (18)**:
  - `ICollisionOperation` encapsulates collision responses
  - Collision callbacks execute appropriate commands based on collision type
  - Decouples collision detection from collision response logic

- **State (19)**:
  - Scene transitions represent different game states
  - Each scene encapsulates state-specific behavior
  - `SceneManager` handles state transitions

- **Visitor (20)**:
  - `ICollidableVisitor` interface for type-safe collision handling
  - Double dispatch with `visit` and `accept` methods to handle different entity types
  - Eliminates the need for type checking and casting

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

All strategies are composable through the Strategy and Composite patterns, enabling complex behaviors from simpler components.

## Installation

### Prerequisites

- JDK 8 or higher
- Gradle 7.x or higher

To set up the project:

```bash
git clone [repository-url]
cd Ocean-Sweepers
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
2. **Application Layer**: Implements game-specific logic on top of the engine
3. **API Layer**: Interfaces that define contracts between components
4. **Implementation Layer**: Concrete implementations of those interfaces

This separation allows:

- Clean dependency management (lower layers don't depend on higher layers)
- Easier testing through interface-based mock objects
- Flexibility to change implementations without affecting other parts of the system
- Potential reuse of the engine for different game projects
