# Multiagent Rescue Project

## Getting Started
1. **Clone the repo**
   ```bash
   git clone https://github.com/lamprianidis/ai203-multiagent-rescue.git
   cd ai203-multiagent-rescue
   ```
2. **Branch for new features**
   - Work off `develop`:
     ```bash
     git checkout develop
     git pull
     git checkout -b feature/<issue-number>-<short>
     ```
    You can use tools like [Sourcetree](https://www.sourcetreeapp.com/) for a visual Git interface.

3. **Commit messages**
   - Should be simple and describe the change made 
   - Example: `Create initial drone agent class`

## Issues & Kanban Board
- **Issues** track tasks and decisions.
- **Kanban columns**:
  - `To do` (new tasks)
  - `In progress` (active work)
  - `Done` (closed tasks)
- Move cards manually or set up column automations.

## Branching Model (gitflow)
- **Long-living**:
  - `main`: production-ready (tagged releases)
  - `develop`: integration of new work
- **Short-lived**:
  - `feature/*`: new features from `develop`
  - `release/*`: prep for release from `develop`
  - `hotfix/*`: urgent fixes from `main`

## Pull Requests
- PRs go from feature branches into `develop`.
- Include the Issue number in the title or description (e.g., `#12`).

