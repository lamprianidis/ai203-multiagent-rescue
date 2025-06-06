# AI203 Multiagent Rescue - Evacuation Simulation

A grid-based evacuation and rescue simulation built with JavaFX and JADE.  
Agents include **Evacuees** (Calm, Panicked, Injured), **Firefighters**, **Rescuers**, **Fire Sensors**, and **Fires**. You can load or edit maps at runtime, set agent counts, and watch agents navigate obstacles to exits or fires.

---

## Overview

This project simulates an indoor evacuation with multiple agent types:

- **Calm Evacuees** use BFS-based distances to find the nearest exit.
- **Panicked Evacuees** move faster but less predictably toward exits.
- **Injured Evacuees** wait to be rescued; once rescued, they become Calm evacuees.
- **Firefighters** navigate around obstacles (walls/obstacles) to extinguish fires.
- **Rescuers** seek injured agents, help them (freeze for 2 s), and convert them to Calm evacuees.
- **Fire Sensors** spawn on wall cells to report fires.
- **Fires** spread over cells marked as obstacles and must be extinguished.

You can design custom floorplans (maps) via a built-in Map Editor, save/load them as JSON, and run the simulation on any map.

---

## Requirements

1. **Java 23+**
2. **Maven 3.x** (for building and managing dependencies)
3. **Git** (to clone this repository)
4. **Internet access** (first build to download dependencies)

---

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/lamprianidis/ai203-multiagent-rescue.git
   cd ai203-multiagent-rescue
   ```
2. **Open in your IDE**
   - Most IDEs will detect the `pom.xml`.
   - Click “Import as Maven Project” or “Load Maven Projects” when prompted.
   - Maven will download all dependencies.

3. **Run the Application**
   - Run the `main.Main` class


---

## Quick Start Instructions

### Select or Edit a Map

1. Use the **Map** dropdown to pick a saved map (JSON files under `maps/`).
2. Click **Edit Map** to launch the Map Editor (`gui.MapEditor`).
   - Draw cells as **FREE**, **WALL**, **OBSTACLE**, or **EXIT**.
   - Resize the grid if needed (default **60 × 45**).
   - Save under a unique name (e.g., `office.json`). That file will appear in the Map dropdown.

### Load a Map

1. After saving or at startup, click **Load**.
2. The chosen map appears on the grid (via `environment.MapIO`).

### Adjust Agent Counts

- In the left panel, set how many of each agent type to spawn:
   - Calm, Panicked, Injured evacuees
   - Firefighters, Rescuers, Fire Sensors, Fire severity
- You can change these values any time before hitting **Start Simulation**.

### Start / Pause / Resume

- **Start Simulation**: spawns all agents on the current map and begins the animation loop.
- **Pause**: freezes agent behaviors and canvas redraw.
- **Resume**: continues from the paused state.

### Monitor Statistics

- Evacuated and dead counts for Calm, Panicked, Injured appear in the **Statistics** panel.
- They update each frame as simulation runs.

---

## Project Structure

```
ai203-multiagent-rescue/
├─ src/main/java/
│  ├─ agents/               # All agent implementations
│  │  ├─ AnnouncerAgent.java
│  │  ├─ EvacueeAgent.java
│  │  ├─ FirefighterHelperAgent.java
│  │  ├─ FireplaceAgent.java
│  │  ├─ HelperAgent.java
│  │  ├─ RescuerHelperAgent.java
│  │  ├─ FireSensorAgent.java
│  │  ├─ CalmEvacueeAgent.java
│  │  ├─ PanickedEvacueeAgent.java
│  │  └─ InjuredEvacueeAgent.java
│  ├─ agents/manager/       # AgentManager & settings (spawn, kill, suspend, resume)
│  ├─ environment/          # GridEnvironment, Cell, EnvironmentFactory, EnvironmentHolder, MapIO
│  │  ├─ Cell.java
│  │  ├─ GridEnvironment.java
│  │  ├─ EnvironmentFactory.java
│  │  ├─ EnvironmentHolder.java
│  │  └─ MapIO.java          # in package `environment`
│  ├─ gui/                  # JavaFX UI
│  │  ├─ SimulationView.java
│  │  └─ MapEditor.java      # in package `gui`
│  └─ main/                 # Main
├─ maps/                    # JSON files defining saved maps
│  └─ office.json
├─ pom.xml                  # Maven configuration
└─ README.md                # This file
```

### environment/MapIO.java (in package `environment`)

- **Responsibilities**:
   - Lists all `maps/*.json` files.
   - Loads a 2D array of `Cell.CellType` into a `GridEnvironment`.
   - Saves a 2D `CellType` array back to JSON.

### gui/MapEditor.java (in package `gui`)

- A simple visual editor:
   - Paint cells as **FREE**, **WALL**, **OBSTACLE**, or **EXIT**.
   - Right-click erases (sets to **FREE**).
   - Resize the grid via **Cols/Rows** spinners + “Resize”.
   - Enter a Name (no spaces) and click **Save**.
   - Saves to `maps/<name>.json` and updates the Map dropdown.

### gui/SimulationView.java

- JavaFX application that shows:
   - **Left pane**: Map selector / Editor, agent count spinners, Start/Pause buttons, live statistics.
   - **Right pane**: Canvas drawing the grid + agents each animation frame.

---

## Agents & Behavior

### EvacueeAgent (Calm, Panicked, Injured subclasses)

- **Spawning**: Each evacuee spawns at a random exit cell.
- **Behavior**:
   - Compute a 2D distance map to exits.
   - Move one cell per tick toward the nearest exit (BFS-based).
   - **Panicked Evacuees**: Move faster and less predictably toward exits.
   - **Injured Evacuees**: Remain stationary until rescued. After rescue, they become Calm evacuees.
- **Evacuation**: On reaching an exit, the agent is removed and increments the evacuated counter.

### FirefighterHelperAgent

- **Spawning**: Random exit (entrance) cell.
- **Behavior: Tick (every 500 ms)**:
   1. If adjacent to a fire cell, extinguish the fire (turn it to **FREE** and remove the corresponding Fire agent.)
   2. Otherwise, compute a BFS distance to all remaining fires and move one step along the shortest path around obstacles.

### RescuerHelperAgent

- **Spawning**: Random exit (entrance) cell.
- **Behavior: Tick (every 500 ms)**:
   1. Compute a BFS distance map toward all Injured agents.
   2. Move one step along that distance map, avoiding blocked cells.
   3. If adjacent to an Injured agent, rescue them by either providing first-aid and converting them to Calm evacuees at that position, or dragging them to the nearest exit.

### FireSensorAgent

- **Spawning**: On wall cells (selected to cover the grid roughly evenly).
- **Tick**: Detects if any adjacent cell has a fire and can broadcast to the Announcer agent.

### Fireplace (Fire)

- Represented in the environment as an `OBSTACLE` cell with a JADE agent named `Fire<N>`.
- Periodically spreads to adjacent cells marked **FREE**, turning them into `OBSTACLE` until firefighters extinguish them.

### Announcer
- Handles agents communication by receiving and redirecting ACL messages.
---
